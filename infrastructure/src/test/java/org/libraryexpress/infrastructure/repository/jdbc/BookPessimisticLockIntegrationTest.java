package org.libraryexpress.infrastructure.repository.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.infrastructure.IntegrationTest;
import org.libraryexpress.infrastructure.config.database.PostgresTestContainerConfig;
import org.libraryexpress.infrastructure.config.database.transaction.TransactionAwareDataSource;
import org.libraryexpress.infrastructure.config.database.transaction.TransactionContext;
import org.libraryexpress.infrastructure.exception.LockAcquisitionException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

@IntegrationTest
@DisplayName("BookDbRepository - Pessimistic Lock Integration Test")
class BookPessimisticLockIntegrationTest extends PostgresTestContainerConfig {

    private static final String ISBN = "978-0-321-35668-0";

    private BookDbRepository bookRepository;
    private String bookId;

    @BeforeEach
    void setUp() {
        bookRepository = new BookDbRepository(new TransactionAwareDataSource(dataSource));

        Book book = Book.create(
                ISBN,
                "Effective Java",
                "Joshua Bloch",
                2018,
                BookStatus.AVAILABLE.name(),
                "",
                ""
        );
        bookRepository.create(book);
        bookId = book.getId().toString();
    }

    @Test
    @DisplayName("Should append FOR UPDATE and return the book when a transaction is active")
    void shouldLockBookRow_whenTransactionIsActive() throws Exception {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        TransactionContext.bind(connection);

        try {
            Book locked = bookRepository.getById(bookId).orElseThrow();

            assertEquals(bookId, locked.getId().toString());
            assertEquals(BookStatus.AVAILABLE, locked.getStatus());
            assertTrue(TransactionContext.isTransactionActive());
        } finally {
            TransactionContext.clear();
            connection.rollback();
            connection.close();
        }
    }

    @Test
    @DisplayName("Should throw LockAcquisitionException when another transaction already holds the book row lock")
    void shouldThrowLockAcquisitionException_whenBookRowIsAlreadyLocked() throws Exception {
        Connection holder = dataSource.getConnection();
        holder.setAutoCommit(false);
        holder.createStatement().execute("SET LOCAL lock_timeout = '500ms'");

        try (PreparedStatement lockStatement = holder.prepareStatement(
                "SELECT id FROM book WHERE id::text = ? FOR UPDATE"
        )) {
            lockStatement.setString(1, bookId);

            try (ResultSet ignored = lockStatement.executeQuery()) {
                Connection contender = dataSource.getConnection();
                contender.setAutoCommit(false);
                contender.createStatement().execute("SET LOCAL lock_timeout = '500ms'");
                TransactionContext.bind(contender);

                try {
                    LockAcquisitionException exception = assertThrows(
                            LockAcquisitionException.class,
                            () -> bookRepository.getById(bookId)
                    );

                    assertTrue(
                            exception.getMessage().contains(bookId),
                            "Lock failure message should identify the contested book"
                    );
                } finally {
                    TransactionContext.clear();
                    contender.rollback();
                    contender.close();
                }
            }
        } finally {
            holder.rollback();
            holder.close();
        }
    }

    @Test
    @DisplayName("Should not use FOR UPDATE when no transaction is active")
    void shouldNotRequireLock_whenNoTransactionIsActive() {
        assertFalse(TransactionContext.isTransactionActive());

        Book book = bookRepository.getById(bookId).orElseThrow();

        assertEquals(BookStatus.AVAILABLE, book.getStatus());
    }
}

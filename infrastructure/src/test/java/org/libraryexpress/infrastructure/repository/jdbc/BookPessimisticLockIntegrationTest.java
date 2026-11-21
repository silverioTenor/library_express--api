package org.libraryexpress.infrastructure.repository.jdbc;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

@IntegrationTest
@DisplayName("BookDbRepository - Pessimistic Lock Integration Test")
class BookPessimisticLockIntegrationTest extends PostgresTestContainerConfig {

    private static final String ISBN = "9780321356680";

    private BookDbRepository bookRepository;
    private String bookId;

    @BeforeEach
    void setUp() {
        bookRepository = new BookDbRepository(new TransactionAwareDataSource(dataSource));

        Book book = Book.create(ISBN, "Effective Java", "Joshua Bloch", 2018, BookStatus.AVAILABLE.name(), "", "");
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

        try (PreparedStatement lockStatement =
                holder.prepareStatement("SELECT id FROM book WHERE id::text = ? FOR UPDATE")) {
            lockStatement.setString(1, bookId);

            try (ResultSet ignored = lockStatement.executeQuery()) {
                Connection contender = dataSource.getConnection();
                contender.setAutoCommit(false);
                contender.createStatement().execute("SET LOCAL lock_timeout = '500ms'");
                TransactionContext.bind(contender);

                try {
                    LockAcquisitionException exception =
                            assertThrows(LockAcquisitionException.class, () -> bookRepository.getById(bookId));

                    assertTrue(
                            exception.getMessage().contains(bookId),
                            "Lock failure message should identify the contested book");
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
    @DisplayName("Should lock a book by ISBN when a transaction is active")
    void shouldThrowLockAcquisitionException_whenBookIsbnRowIsAlreadyLocked() throws Exception {
        try (Connection holder = dataSource.getConnection()) {
            holder.setAutoCommit(false);
            setLockTimeout(holder);

            try (PreparedStatement lockStatement =
                            holder.prepareStatement("SELECT id FROM book WHERE isbn = ? FOR UPDATE");
                    ResultSet ignored = lockBookByIsbn(lockStatement)) {
                assertTrue(ignored.next());
                assertBookIsbnLockTimesOut();
            } finally {
                holder.rollback();
            }
        }
    }

    @Test
    @DisplayName("Should not use FOR UPDATE when no transaction is active")
    void shouldNotRequireLock_whenNoTransactionIsActive() {
        assertFalse(TransactionContext.isTransactionActive());

        Book book = bookRepository.getById(bookId).orElseThrow();

        assertEquals(BookStatus.AVAILABLE, book.getStatus());
    }

    private ResultSet lockBookByIsbn(PreparedStatement statement) throws Exception {
        statement.setString(1, ISBN);
        return statement.executeQuery();
    }

    private void assertBookIsbnLockTimesOut() throws Exception {
        try (Connection contender = dataSource.getConnection()) {
            contender.setAutoCommit(false);
            setLockTimeout(contender);
            TransactionContext.bind(contender);

            try {
                LockAcquisitionException exception =
                        assertThrows(LockAcquisitionException.class, () -> bookRepository.getByIsbn(ISBN));

                assertTrue(exception.getMessage().contains(ISBN));
            } finally {
                TransactionContext.clear();
                contender.rollback();
            }
        }
    }

    private static void setLockTimeout(Connection connection) throws Exception {
        try (var statement = connection.createStatement()) {
            statement.execute("SET LOCAL lock_timeout = '500ms'");
        }
    }
}

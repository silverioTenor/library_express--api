package org.libraryexpress.infrastructure.repository.jdbc;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.core.repository.TransactionManager;
import org.libraryexpress.domain.customer.entity.Customer;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.infrastructure.IntegrationTest;
import org.libraryexpress.infrastructure.config.database.PostgresTestContainerConfig;
import org.libraryexpress.infrastructure.config.database.transaction.TransactionAwareDataSource;
import org.libraryexpress.infrastructure.config.database.transaction.TransactionContext;

@IntegrationTest
@DisplayName("TransactionDb - Atomicity and Connection Sharing Integration Test")
class TransactionDbIntegrationTest extends PostgresTestContainerConfig {

    private static final String CUSTOMER_ID = "20000000-0000-4000-8000-000000000010";
    private static final String LOAN_ID = "30000000-0000-4000-8000-000000000010";
    private static final String ISBN = "978-1-491-95599-0";

    private TransactionManager transactionManager;
    private BookDbRepository bookRepository;
    private LoanDbRepository loanRepository;
    private String bookId;

    @BeforeEach
    void setUp() {
        DataSource transactionAwareDataSource = new TransactionAwareDataSource(dataSource);

        transactionManager = new TransactionDb(dataSource);
        bookRepository = new BookDbRepository(transactionAwareDataSource);
        loanRepository = new LoanDbRepository(transactionAwareDataSource);

        CustomerDbRepository customerRepository = new CustomerDbRepository(transactionAwareDataSource);
        customerRepository.create(Customer.restore(CUSTOMER_ID, "Txn Tester", "txn.tester@libraryexpress.com"));

        Book book = Book.create(
                ISBN, "Designing Data-Intensive Applications", "Martin Kleppmann", 2017, "AVAILABLE", "", "");
        bookRepository.create(book);
        bookId = book.getId().toString();
    }

    @Test
    @DisplayName("Should commit loan and book changes together when the transactional block succeeds")
    void shouldCommitLoanAndBookChanges_whenTransactionalBlockSucceeds() {
        transactionManager.execute(() -> {
            Book book = bookRepository.getById(bookId).orElseThrow();
            book.changeStatus(BookStatus.BORROWED);

            Loan loan = Loan.restore(
                    LOAN_ID,
                    bookId,
                    CUSTOMER_ID,
                    LoanStatus.ACTIVE,
                    LocalDate.now(),
                    LocalDate.now().plusDays(14),
                    null);

            loanRepository.create(loan);
            bookRepository.update(book);
            return null;
        });

        Optional<Loan> persistedLoan = loanRepository.findById(LOAN_ID);
        Optional<Book> persistedBook = bookRepository.getById(bookId);

        assertTrue(persistedLoan.isPresent());
        assertEquals(LoanStatus.ACTIVE, persistedLoan.get().getStatus());
        assertTrue(persistedBook.isPresent());
        assertEquals(BookStatus.BORROWED, persistedBook.get().getStatus());
    }

    @Test
    @DisplayName("Should rollback loan and book changes when the transactional block throws")
    void shouldRollbackLoanAndBookChanges_whenTransactionalBlockThrows() {
        RuntimeException boom = new RuntimeException("Simulated failure after writes");

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> transactionManager.execute(() -> {
                    Book book = bookRepository.getById(bookId).orElseThrow();
                    book.changeStatus(BookStatus.BORROWED);

                    Loan loan = Loan.restore(
                            LOAN_ID,
                            bookId,
                            CUSTOMER_ID,
                            LoanStatus.ACTIVE,
                            LocalDate.now(),
                            LocalDate.now().plusDays(14),
                            null);

                    loanRepository.create(loan);
                    bookRepository.update(book);
                    throw boom;
                }));

        assertSame(boom, thrown);
        assertTrue(loanRepository.findById(LOAN_ID).isEmpty(), "Loan insert must be rolled back");
        assertEquals(
                BookStatus.AVAILABLE,
                bookRepository.getById(bookId).orElseThrow().getStatus(),
                "Book status update must be rolled back");
    }

    @Test
    @DisplayName("Should reuse the bound connection for nested execute calls on the same thread")
    void shouldReuseBoundConnection_whenNestedExecuteIsCalled() throws Exception {
        AtomicReference<Connection> outerConnection = new AtomicReference<>();
        AtomicReference<Connection> nestedConnection = new AtomicReference<>();

        transactionManager.execute(() -> {
            outerConnection.set(TransactionContext.getCurrentConnection());

            transactionManager.execute(() -> {
                nestedConnection.set(TransactionContext.getCurrentConnection());
                return null;
            });

            return null;
        });

        assertNotNull(outerConnection.get());
        assertSame(
                outerConnection.get(),
                nestedConnection.get(),
                "Nested execute must join the active transaction instead of opening another connection");
    }

    @Test
    @DisplayName("Should clear TransactionContext after the transactional block finishes")
    void shouldClearTransactionContext_whenBlockFinishes() {
        transactionManager.execute(() -> {
            assertTrue(TransactionContext.isTransactionActive());
            return null;
        });

        assertFalse(TransactionContext.isTransactionActive());
        assertNull(TransactionContext.getCurrentConnection());
    }
}

package org.libraryexpress.infrastructure.repository.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.customer.entity.Customer;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.infrastructure.IntegrationTest;
import org.libraryexpress.infrastructure.config.database.PostgresTestContainerConfig;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@IntegrationTest
@DisplayName("Loan Repository - Core Operations Integration Test")
class LoanDbRepositoryIntegrationTest extends PostgresTestContainerConfig {

    private LoanDbRepository loanRepository;

    // Standard business entities required to fulfill foreign key constraints
    private static final String VALID_ISBN = "978-16-14841-20-0"; // Task Requirement: Matching your custom formatting rule
    private static final String VALID_CUSTOMER_ID = "cust-loan-owner-123";

    @BeforeEach
    void setUp() {
        this.loanRepository = new LoanDbRepository(dataSource);
        BookDbRepository bookRepository = new BookDbRepository(dataSource);
        CustomerDbRepository customerRepository = new CustomerDbRepository(dataSource);

        // Task Solution: Seed structural parent records into the isolated database to prevent FK violations
        Customer parentCustomer = new Customer.Builder()
                .setId(VALID_CUSTOMER_ID)
                .setName("Loan Tester Customer")
                .setEmail("loan.tester@libraryexpress.com")
                .build();
        customerRepository.create(parentCustomer);

        Book parentBook = new Book.Builder()
                .setISBN(VALID_ISBN)
                .setTitle("Continuous Delivery Baseline")
                .setAuthor("Jez Humble")
                .setYear(2010)
                .setStatus(BookStatus.AVAILABLE)
                .build();
        bookRepository.create(parentBook);
    }

    @Test
    @DisplayName("Should successfully persist a new transactional loan and retrieve it back using its unique ID identity")
    void shouldPersistAndRetrieveLoan_whenPayloadIsValidAndFkConstraintsAreMet() {
        // Arrange (Testing create and findById)
        String loanId = "loan-uuid-active-789";
        Loan newLoan = new Loan.Builder()
                .setId(loanId)
                .setISBN(VALID_ISBN)
                .setCustomerId(VALID_CUSTOMER_ID)
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(LocalDate.now())
                .setEndDate(LocalDate.now().plusDays(14))
                .build();

        // Act
        loanRepository.create(newLoan);
        Optional<Loan> retrieved = loanRepository.findById(loanId);

        // Assert
        assertTrue(retrieved.isPresent(), "Persisted loan transaction metadata must be located inside the database");
        assertEquals(VALID_ISBN, retrieved.get().getISBN().value());
        assertEquals(VALID_CUSTOMER_ID, retrieved.get().getCustomerId());
        assertEquals(LoanStatus.ACTIVE, retrieved.get().getStatus());
        assertEquals(LocalDate.now(), retrieved.get().getStartDate(), "Temporal state mappings must preserve date alignments");
    }

    @Test
    @DisplayName("Should successfully overwrite database transaction states when executing loan record modifications")
    void shouldUpdateLoanState_whenModifyingExistingRecord() {
        // Arrange (Testing update)
        String loanId = "loan-uuid-updt-555";
        Loan loan = new Loan.Builder()
                .setId(loanId)
                .setISBN(VALID_ISBN)
                .setCustomerId(VALID_CUSTOMER_ID)
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(LocalDate.now().minusDays(5))
                .setEndDate(LocalDate.now().plusDays(9))
                .build();
        loanRepository.create(loan);

        // Reconstructing entity representing a standard business return execution lifecycle
        Loan returnedLoan = new Loan.Builder()
                .setId(loanId)
                .setISBN(VALID_ISBN)
                .setCustomerId(VALID_CUSTOMER_ID)
                .setStatus(LoanStatus.FINISHED) // Mutating business state
                .setStartDate(LocalDate.now().minusDays(5))
                .setEndDate(LocalDate.now()) // Modifying temporal fields
                .build();

        // Act
        loanRepository.update(returnedLoan);
        Optional<Loan> retrieved = loanRepository.findById(loanId);

        // Assert
        assertTrue(retrieved.isPresent());
        assertEquals(LoanStatus.FINISHED, retrieved.get().getStatus());
        assertEquals(LocalDate.now(), retrieved.get().getEndDate());
    }

    @Test
    @DisplayName("Should fetch isolated multi-criteria results when utilizing high-performance array clauses")
    void shouldReturnFilteredSet_whenSearchingByJointRelationalKeysAndNativeStatusArrays() {
        // Arrange
        String loanId1 = "loan-search-1";
        String loanId2 = "loan-search-2";

        Loan activeLoan = new Loan.Builder()
                .setId(loanId1)
                .setISBN(VALID_ISBN)
                .setCustomerId(VALID_CUSTOMER_ID)
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(LocalDate.now())
                .setEndDate(LocalDate.now().plusDays(14))
                .build();

        Loan overdueLoan = new Loan.Builder()
                .setId(loanId2)
                .setISBN(VALID_ISBN)
                .setCustomerId(VALID_CUSTOMER_ID)
                .setStatus(LoanStatus.OVERDUE)
                .setStartDate(LocalDate.now().minusDays(20))
                .setEndDate(LocalDate.now().minusDays(6))
                .build();

        loanRepository.create(activeLoan);
        loanRepository.create(overdueLoan);

        // Act - Triggering search using your optimized ANY clause database algorithm
        Set<Loan> matchedLoans = loanRepository.search(
                VALID_CUSTOMER_ID,
                null,
                Set.of(LoanStatus.OVERDUE), null
        ).items();

        // Assert
        assertNotNull(matchedLoans);
        assertEquals(1, matchedLoans.size(), "Filter must pinpoint exactly the OVERDUE entry based on native array unpacking");
        assertTrue(matchedLoans.stream().anyMatch(l -> l.getId().equals(loanId2)), "The correctly mapped target id must match records data");
    }

}

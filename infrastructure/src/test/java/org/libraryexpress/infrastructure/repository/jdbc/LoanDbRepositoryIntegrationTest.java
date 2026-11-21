package org.libraryexpress.infrastructure.repository.jdbc;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.customer.entity.Customer;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.domain.loan.repository.LoanFilter;
import org.libraryexpress.infrastructure.IntegrationTest;
import org.libraryexpress.infrastructure.config.database.PostgresTestContainerConfig;

@IntegrationTest
@DisplayName("Loan Repository - Core Operations Integration Test")
class LoanDbRepositoryIntegrationTest extends PostgresTestContainerConfig {

    private LoanDbRepository loanRepository;
    private String validBookId;

    // Standard business entities required to fulfill foreign key constraints
    private static final String VALID_ISBN = "978-16-14841-20-0";
    private static final String VALID_CUSTOMER_ID = "20000000-0000-4000-8000-000000000001";

    @BeforeEach
    void setUp() {
        this.loanRepository = new LoanDbRepository(dataSource);
        BookDbRepository bookRepository = new BookDbRepository(dataSource);
        CustomerDbRepository customerRepository = new CustomerDbRepository(dataSource);

        // Task Solution: Seed structural parent records into the isolated database to prevent FK violations
        Customer parentCustomer =
                Customer.restore(VALID_CUSTOMER_ID, "Loan Tester Customer", "loan.tester@libraryexpress.com");
        customerRepository.create(parentCustomer);

        Book parentBook =
                Book.create(VALID_ISBN, "Continuous Delivery Baseline", "Jez Humble", 2010, "AVAILABLE", "", "");
        bookRepository.create(parentBook);
        validBookId = parentBook.getId().toString();
    }

    @Test
    @DisplayName(
            "Should successfully persist a new transactional loan and retrieve it back using its unique ID identity")
    void shouldPersistAndRetrieveLoan_whenPayloadIsValidAndFkConstraintsAreMet() {
        // Arrange (Testing create and findById)
        String loanId = "30000000-0000-4000-8000-000000000001";
        Loan newLoan = Loan.restore(
                loanId,
                validBookId,
                VALID_CUSTOMER_ID,
                LoanStatus.ACTIVE,
                LocalDate.now(),
                LocalDate.now().plusDays(14),
                null);

        // Act
        loanRepository.create(newLoan);
        Optional<Loan> retrieved = loanRepository.findById(loanId);

        // Assert
        assertTrue(retrieved.isPresent(), "Persisted loan transaction metadata must be located inside the database");
        assertEquals(validBookId, retrieved.get().getBookId());
        assertEquals(VALID_CUSTOMER_ID, retrieved.get().getCustomerId());
        assertEquals(LoanStatus.ACTIVE, retrieved.get().getStatus());
        assertEquals(
                LocalDate.now(),
                retrieved.get().getStartDate(),
                "Temporal state mappings must preserve date alignments");
    }

    @Test
    @DisplayName("Should successfully overwrite database transaction states when executing loan record modifications")
    void shouldUpdateLoanState_whenModifyingExistingRecord() {
        // Arrange (Testing update)
        String loanId = "30000000-0000-4000-8000-000000000002";
        Loan loan = Loan.restore(
                loanId,
                validBookId,
                VALID_CUSTOMER_ID,
                LoanStatus.ACTIVE,
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(9),
                null);
        loanRepository.create(loan);

        // Reconstructing entity representing a standard business return execution lifecycle
        Loan returnedLoan = Loan.restore(
                loanId,
                validBookId,
                VALID_CUSTOMER_ID,
                LoanStatus.FINISHED,
                // Mutating business state
                LocalDate.now().minusDays(5),
                LocalDate.now(),
                // Modifying temporal fields
                null);

        // Act
        loanRepository.update(returnedLoan);
        Optional<Loan> retrieved = loanRepository.findById(loanId);

        // Assert
        assertTrue(retrieved.isPresent());
        assertEquals(LoanStatus.FINISHED, retrieved.get().getStatus());
        assertEquals(LocalDate.now(), retrieved.get().getEndDate());
    }

    @Test
    @DisplayName("Should update loan when persisted status still matches the expected status")
    void shouldUpdateLoan_whenCurrentStatusMatchesExpected() {
        String loanId = "30000000-0000-4000-8000-000000000005";
        Loan loan = Loan.restore(
                loanId,
                validBookId,
                VALID_CUSTOMER_ID,
                LoanStatus.ACTIVE,
                LocalDate.now().minusDays(3),
                LocalDate.now().plusDays(12),
                null);
        loanRepository.create(loan);

        Loan finishedLoan = Loan.restore(
                loanId,
                validBookId,
                VALID_CUSTOMER_ID,
                LoanStatus.FINISHED,
                loan.getStartDate(),
                loan.getEndDate(),
                null);

        boolean applied = loanRepository.updateIfCurrentStatus(finishedLoan, LoanStatus.ACTIVE);

        assertTrue(applied);
        assertEquals(
                LoanStatus.FINISHED,
                loanRepository.findById(loanId).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Should skip update when persisted status no longer matches the expected status")
    void shouldSkipUpdate_whenCurrentStatusDoesNotMatchExpected() {
        String loanId = "30000000-0000-4000-8000-000000000006";
        Loan loan = Loan.restore(
                loanId,
                validBookId,
                VALID_CUSTOMER_ID,
                LoanStatus.FINISHED,
                LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(1),
                null);
        loanRepository.create(loan);

        Loan conflictingUpdate = Loan.restore(
                loanId,
                validBookId,
                VALID_CUSTOMER_ID,
                LoanStatus.OVERDUE,
                loan.getStartDate(),
                loan.getEndDate(),
                null);

        boolean applied = loanRepository.updateIfCurrentStatus(conflictingUpdate, LoanStatus.ACTIVE);

        assertFalse(applied, "Conditional update must refuse to overwrite a loan that left ACTIVE");
        assertEquals(
                LoanStatus.FINISHED,
                loanRepository.findById(loanId).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Should fetch isolated multi-criteria results when utilizing high-performance array clauses")
    void shouldReturnFilteredSet_whenSearchingByJointRelationalKeysAndNativeStatusArrays() {
        // Arrange
        String loanId1 = "30000000-0000-4000-8000-000000000003";
        String loanId2 = "30000000-0000-4000-8000-000000000004";

        Loan activeLoan = Loan.restore(
                loanId1,
                validBookId,
                VALID_CUSTOMER_ID,
                LoanStatus.ACTIVE,
                LocalDate.now(),
                LocalDate.now().plusDays(14),
                null);

        Loan overdueLoan = Loan.restore(
                loanId2,
                validBookId,
                VALID_CUSTOMER_ID,
                LoanStatus.OVERDUE,
                LocalDate.now().minusDays(20),
                LocalDate.now().minusDays(6),
                null);

        loanRepository.create(activeLoan);
        loanRepository.create(overdueLoan);

        // Act - Triggering search using your optimized ANY clause database algorithm
        var filter = new LoanFilter(null, VALID_CUSTOMER_ID, null, Set.of(LoanStatus.OVERDUE), null);
        List<Loan> matchedLoans = loanRepository.search(filter).items();

        // Assert
        assertNotNull(matchedLoans);
        assertEquals(
                1,
                matchedLoans.size(),
                "Filter must pinpoint exactly the OVERDUE entry based on native array unpacking");
        assertTrue(
                matchedLoans.stream().anyMatch(l -> l.getId().equals(loanId2)),
                "The correctly mapped target id must match records data");

        var bookIdFilter = new LoanFilter(null, null, validBookId, Set.of(LoanStatus.OVERDUE), null);
        assertEquals(1, loanRepository.search(bookIdFilter).items().size());
    }
}

package org.libraryexpress.domain.loan;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.libraryexpress.domain.UnitTest;
import org.libraryexpress.domain.core.repository.QueryResult;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.domain.loan.exception.LoanLimitReachedException;
import org.libraryexpress.domain.loan.exception.OverdueLoanException;
import org.libraryexpress.domain.loan.repository.LoanRepository;
import org.libraryexpress.domain.loan.validator.LoanEligibilityValidator;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

@UnitTest
@ExtendWith(MockitoExtension.class)
@DisplayName("Loan Eligibility Validator - Unit Test")
class LoanEligibilityValidatorTest {

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private LoanEligibilityValidator validator;

    private static final String CUSTOMER_ID = "customer-uuid-123";
    private static final String VALID_ISBN = "978-43-12345-67-8";

    @Test
    @DisplayName("Should validate successfully when the customer has no active or overdue loans")
    void shouldValidateSuccessfully_whenCustomerHasNoLoans() {
        // Arrange
        when(loanRepository.search(CUSTOMER_ID, null, Set.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE), null))
                .thenReturn(new QueryResult<>(Collections.emptySet(), 0));

        // Act & Assert
        // assertDoesNotThrow ensures the flow runs cleanly without triggering business exceptions
        assertDoesNotThrow(() -> validator.validate(CUSTOMER_ID));
        verify(loanRepository, times(1))
                .search(CUSTOMER_ID, null, Set.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE), null);
    }

    @Test
    @DisplayName("Should throw OverdueLoanException when the customer has at least one overdue loan")
    void shouldThrowOverdueLoanException_whenCustomerHasOverdueLoan() {
        // Arrange
        Loan overdueLoan = new Loan.Builder()
                .setId("loan-1")
                .setISBN(VALID_ISBN)
                .setCustomerId(CUSTOMER_ID)
                .setStatus(LoanStatus.OVERDUE) // Status that triggers the blocking mechanism
                .setStartDate(LocalDate.now().minusDays(20))
                .build();

        when(loanRepository.search(CUSTOMER_ID, null, Set.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE), null))
                .thenReturn(new QueryResult<>(Set.of(overdueLoan), 1));

        // Act & Assert
        OverdueLoanException exception = assertThrows(OverdueLoanException.class, () ->
                validator.validate(CUSTOMER_ID)
        );

        assertEquals("Customer has a pending overdue return.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw LoanLimitReachedException when active loans count reaches maximum allowed limit")
    void shouldThrowLoanLimitReachedException_whenActiveLoansCountReachesMaxLimit() {
        // Arrange
        Loan activeLoan1 = new Loan.Builder()
                .setId("loan-1")
                .setISBN(VALID_ISBN)
                .setCustomerId(CUSTOMER_ID)
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(LocalDate.now())
                .build();

        Loan activeLoan2 = new Loan.Builder()
                .setId("loan-2")
                .setISBN("111-22-33333-44-5")
                .setCustomerId(CUSTOMER_ID)
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(LocalDate.now())
                .build();

        when(loanRepository.search(CUSTOMER_ID, null, Set.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE), null))
                .thenReturn(new QueryResult<>(Set.of(activeLoan1, activeLoan2), 1));

        // Act & Assert
        LoanLimitReachedException exception = assertThrows(LoanLimitReachedException.class, () ->
                validator.validate(CUSTOMER_ID)
        );

        assertEquals("Customer has reached the maximum limit of active loans.", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate successfully when active loans count is strictly below the limit")
    void shouldValidateSuccessfully_whenActiveLoansCountIsBelowLimit() {
        // Arrange
        Loan activeLoan = new Loan.Builder()
                .setId("loan-1")
                .setISBN(VALID_ISBN)
                .setCustomerId(CUSTOMER_ID)
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(LocalDate.now())
                .build();

        when(loanRepository.search(CUSTOMER_ID, null, Set.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE), null))
                .thenReturn(new QueryResult<>(Set.of(activeLoan), 1));

        // Act & Assert
        assertDoesNotThrow(() -> validator.validate(CUSTOMER_ID));
    }
}

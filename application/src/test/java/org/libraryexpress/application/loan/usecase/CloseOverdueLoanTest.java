package org.libraryexpress.application.loan.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.libraryexpress.application.UnitTest;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.domain.loan.exception.LoanNotFoundException;
import org.libraryexpress.domain.loan.exception.OverdueLoanException;
import org.libraryexpress.domain.loan.repository.LoanRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@UnitTest
@ExtendWith(MockitoExtension.class)
@DisplayName("CloseOverdueLoan UseCase - Unit Test")
class CloseOverdueLoanTest {

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private CloseOverdueLoan closeOverdueLoan;

    private static final String LOAN_ID = "loan-uuid-123";
    private static final String ISBN_STR = "978-43-12345-67-8";
    private static final String CUSTOMER_ID = "customer-uuid-123";

    @BeforeEach
    void setUp() {}

    @Test
    @DisplayName("Should successfully transition status to FINISHED when loan is OVERDUE")
    void shouldCloseLoan_whenLoanStatusIsOverdue() {
        // Arrange
        Loan overdueLoan = Loan.restore(
                LOAN_ID,
                ISBN_STR,
                CUSTOMER_ID,
                LoanStatus.OVERDUE,
                // Valid state for this palliative operational flow
                LocalDate.now().minusDays(20),
                LocalDate.now().minusDays(5),
                null
        );

        Clock clock = Clock.fixed(Instant.now().minus(20, ChronoUnit.DAYS), ZoneId.of("UTC"));
        CloseOverdueLoan closeOverdueLoan1 = new CloseOverdueLoan(loanRepository, clock);

        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(overdueLoan));

        // Act
        closeOverdueLoan1.execute(LOAN_ID);

        // Assert
        assertEquals(LoanStatus.FINISHED, overdueLoan.getStatus(), "Loan status should transition from OVERDUE to FINISHED");

        // Verifying orchestration: core data mutation must be pushed to database
        verify(loanRepository).update(overdueLoan);
    }

    @Test
    @DisplayName("Should throw LoanNotFoundException and abort process when loan ID does not exist")
    void shouldThrowLoanNotFoundException_whenLoanIsNotFound() {
        // Arrange
        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(LoanNotFoundException.class, () -> closeOverdueLoan.execute(LOAN_ID));

        // Ensuring no database modifications take place on invalid queries
        verify(loanRepository, never()).update(any(Loan.class));
    }

    @Test
    @DisplayName("Should throw OverdueLoanException and never update database when loan status is not OVERDUE")
    void shouldThrowOverdueLoanException_whenLoanStatusIsActive() {
        // Arrange
        Loan activeLoan = Loan.restore(
                LOAN_ID,
                ISBN_STR,
                CUSTOMER_ID,
                LoanStatus.ACTIVE,
                // Invalid state: this usecase is exclusively restricted to OVERDUE
                LocalDate.now(),
                LocalDate.now().plusDays(15),
                null
        );

        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(activeLoan));

        // Act & Assert
        assertThrows(OverdueLoanException.class, () -> closeOverdueLoan.execute(LOAN_ID));

        // Compliance with User Story style guide: critical business protection assertion
        verify(loanRepository, never()).update(any(Loan.class));
    }
}

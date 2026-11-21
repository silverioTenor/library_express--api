package org.libraryexpress.application.loan.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.libraryexpress.application.UnitTest;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.domain.loan.exception.LoanNotFoundException;
import org.libraryexpress.domain.loan.exception.OverdueLoanException;
import org.libraryexpress.domain.loan.repository.LoanRepository;
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

    private static final String LOAN_ID = "loan-uuid-123";
    private static final String BOOK_ID = "00000000-0000-4000-8000-000000000001";
    private static final String CUSTOMER_ID = "customer-uuid-123";

    @Test
    @DisplayName("Should successfully transition status to FINISHED when loan is OVERDUE")
    void shouldCloseLoan_whenLoanStatusIsOverdue() {
        Loan overdueLoan = overdueLoan();
        Clock clock = Clock.fixed(Instant.now().minus(20, ChronoUnit.DAYS), ZoneId.of("UTC"));
        CloseOverdueLoan useCase = new CloseOverdueLoan(loanRepository, clock);

        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(overdueLoan));
        when(loanRepository.updateIfCurrentStatus(overdueLoan, LoanStatus.OVERDUE)).thenReturn(true);

        useCase.execute(LOAN_ID);

        assertEquals(LoanStatus.FINISHED, overdueLoan.getStatus());
        verify(loanRepository).updateIfCurrentStatus(overdueLoan, LoanStatus.OVERDUE);
        verify(loanRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should complete idempotently when the overdue loan is already finished")
    void shouldCompleteIdempotently_whenLoanIsAlreadyFinished() {
        Loan finishedLoan = Loan.restore(
                LOAN_ID,
                BOOK_ID,
                CUSTOMER_ID,
                LoanStatus.FINISHED,
                LocalDate.now().minusDays(20),
                LocalDate.now().minusDays(5),
                Instant.now()
        );

        CloseOverdueLoan useCase = new CloseOverdueLoan(loanRepository, Clock.systemUTC());
        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(finishedLoan));

        assertDoesNotThrow(() -> useCase.execute(LOAN_ID));

        verify(loanRepository, never()).updateIfCurrentStatus(any(), any());
        verify(loanRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should complete idempotently when concurrent close already changed the status")
    void shouldCompleteIdempotently_whenConditionalUpdateDoesNotApply() {
        Loan overdueLoan = overdueLoan();
        Clock clock = Clock.fixed(Instant.now().minus(20, ChronoUnit.DAYS), ZoneId.of("UTC"));
        CloseOverdueLoan useCase = new CloseOverdueLoan(loanRepository, clock);

        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(overdueLoan));
        when(loanRepository.updateIfCurrentStatus(overdueLoan, LoanStatus.OVERDUE)).thenReturn(false);

        assertDoesNotThrow(() -> useCase.execute(LOAN_ID));

        verify(loanRepository).updateIfCurrentStatus(overdueLoan, LoanStatus.OVERDUE);
        verify(loanRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should throw LoanNotFoundException and abort process when loan ID does not exist")
    void shouldThrowLoanNotFoundException_whenLoanIsNotFound() {
        CloseOverdueLoan useCase = new CloseOverdueLoan(loanRepository, Clock.systemUTC());
        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.empty());

        assertThrows(LoanNotFoundException.class, () -> useCase.execute(LOAN_ID));

        verify(loanRepository, never()).updateIfCurrentStatus(any(), any());
        verify(loanRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should throw OverdueLoanException and never update database when loan status is not OVERDUE")
    void shouldThrowOverdueLoanException_whenLoanStatusIsActive() {
        Loan activeLoan = Loan.restore(
                LOAN_ID,
                BOOK_ID,
                CUSTOMER_ID,
                LoanStatus.ACTIVE,
                LocalDate.now(),
                LocalDate.now().plusDays(15),
                null
        );

        CloseOverdueLoan useCase = new CloseOverdueLoan(loanRepository, Clock.systemUTC());
        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(activeLoan));

        assertThrows(OverdueLoanException.class, () -> useCase.execute(LOAN_ID));

        verify(loanRepository, never()).updateIfCurrentStatus(any(), any());
        verify(loanRepository, never()).update(any());
    }

    private static Loan overdueLoan() {
        return Loan.restore(
                LOAN_ID,
                BOOK_ID,
                CUSTOMER_ID,
                LoanStatus.OVERDUE,
                LocalDate.now().minusDays(20),
                LocalDate.now().minusDays(5),
                null
        );
    }
}

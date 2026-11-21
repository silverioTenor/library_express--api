package org.libraryexpress.application.loan.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.libraryexpress.application.UnitTest;
import org.libraryexpress.application.loan.dto.response.LoanReturnResponseDTO;
import org.libraryexpress.application.support.PassthroughTransactionManager;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.exception.BookNotFoundException;
import org.libraryexpress.domain.book.repository.BookRepository;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.domain.loan.exception.LoanNotFoundException;
import org.libraryexpress.domain.loan.repository.LoanRepository;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
@DisplayName("LoanReturn UseCase - Unit Test")
class LoanReturnTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookRepository bookRepository;

    private static final String LOAN_ID = "loan-uuid-123";
    private static final String BOOK_ID = "00000000-0000-4000-8000-000000000001";
    private static final String BOOK_ISBN = "9788535902778";
    private static final String CUSTOMER_ID = "customer-uuid-123";

    @Test
    @DisplayName("Should finish the loan and make the book available when the loan is not overdue")
    void shouldFinishLoan_whenLoanIsActiveAndWithinDeadline() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        Loan activeLoan = activeLoan(startDate, startDate.plusDays(15));
        Book borrowedBook = borrowedBook();

        Clock fixedClock = Clock.fixed(Instant.parse("2026-08-10T12:00:00Z"), ZoneId.of("UTC"));
        LoanReturn useCase = newLoanReturn(fixedClock);

        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(activeLoan));
        when(bookRepository.getById(BOOK_ID)).thenReturn(Optional.of(borrowedBook));
        when(loanRepository.updateIfCurrentStatus(activeLoan, LoanStatus.ACTIVE))
                .thenReturn(true);

        LoanReturnResponseDTO response = useCase.execute(LOAN_ID);

        assertEquals("success", response.status());
        assertEquals("Book returned.", response.message());
        assertEquals(LoanStatus.FINISHED, activeLoan.getStatus());
        assertEquals(BookStatus.AVAILABLE, borrowedBook.getStatus());

        verify(loanRepository).updateIfCurrentStatus(activeLoan, LoanStatus.ACTIVE);
        verify(bookRepository).update(borrowedBook);
        verify(loanRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should mark the loan as overdue when execution happens past the deadline")
    void shouldMarkAsOverdue_whenCurrentDateExceedsDeadline() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        Loan activeLoan = activeLoan(startDate, startDate.plusDays(15));
        Book borrowedBook = borrowedBook();

        Clock fixedClock = Clock.fixed(Instant.parse("2026-08-20T12:00:00Z"), ZoneId.of("UTC"));
        LoanReturn useCase = newLoanReturn(fixedClock);

        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(activeLoan));
        when(bookRepository.getById(BOOK_ID)).thenReturn(Optional.of(borrowedBook));
        when(loanRepository.updateIfCurrentStatus(activeLoan, LoanStatus.ACTIVE))
                .thenReturn(true);

        LoanReturnResponseDTO response = useCase.execute(LOAN_ID);

        assertEquals("warning", response.status());
        assertEquals("Book returned, but a late fee is pending.", response.message());
        assertEquals(LoanStatus.OVERDUE, activeLoan.getStatus());
        assertEquals(BookStatus.AVAILABLE, borrowedBook.getStatus());

        verify(loanRepository).updateIfCurrentStatus(activeLoan, LoanStatus.ACTIVE);
        verify(bookRepository).update(borrowedBook);
    }

    @Test
    @DisplayName("Should return success idempotently when the loan is already finished")
    void shouldReturnSuccessIdempotently_whenLoanIsAlreadyFinished() {
        Loan finishedLoan = Loan.restore(
                LOAN_ID,
                BOOK_ID,
                CUSTOMER_ID,
                LoanStatus.FINISHED,
                LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(1),
                Instant.now());

        LoanReturn useCase = newLoanReturn(Clock.systemUTC());
        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(finishedLoan));

        LoanReturnResponseDTO response = useCase.execute(LOAN_ID);

        assertEquals("success", response.status());
        assertEquals("Book returned.", response.message());
        verifyNoInteractions(bookRepository);
        verify(loanRepository, never()).updateIfCurrentStatus(any(), any());
        verify(loanRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should return warning idempotently when the loan is already overdue after a previous return")
    void shouldReturnWarningIdempotently_whenLoanIsAlreadyOverdue() {
        Loan overdueLoan = Loan.restore(
                LOAN_ID,
                BOOK_ID,
                CUSTOMER_ID,
                LoanStatus.OVERDUE,
                LocalDate.now().minusDays(20),
                LocalDate.now().minusDays(5),
                null);

        LoanReturn useCase = newLoanReturn(Clock.systemUTC());
        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(overdueLoan));

        LoanReturnResponseDTO response = useCase.execute(LOAN_ID);

        assertEquals("warning", response.status());
        assertEquals("Book returned, but a late fee is pending.", response.message());
        verifyNoInteractions(bookRepository);
        verify(loanRepository, never()).updateIfCurrentStatus(any(), any());
        verify(loanRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should treat concurrent return as idempotent when conditional update finds no ACTIVE row")
    void shouldTreatConcurrentReturnAsIdempotent_whenConditionalUpdateDoesNotApply() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        Loan activeLoan = activeLoan(startDate, startDate.plusDays(15));
        Book borrowedBook = borrowedBook();

        Loan alreadyFinished = Loan.restore(
                LOAN_ID,
                BOOK_ID,
                CUSTOMER_ID,
                LoanStatus.FINISHED,
                startDate,
                startDate.plusDays(15),
                Instant.parse("2026-08-10T12:00:00Z"));

        Clock fixedClock = Clock.fixed(Instant.parse("2026-08-10T12:00:00Z"), ZoneId.of("UTC"));
        LoanReturn useCase = newLoanReturn(fixedClock);

        when(loanRepository.findById(LOAN_ID))
                .thenReturn(Optional.of(activeLoan))
                .thenReturn(Optional.of(alreadyFinished));
        when(bookRepository.getById(BOOK_ID)).thenReturn(Optional.of(borrowedBook));
        when(loanRepository.updateIfCurrentStatus(activeLoan, LoanStatus.ACTIVE))
                .thenReturn(false);

        LoanReturnResponseDTO response = useCase.execute(LOAN_ID);

        assertEquals("success", response.status());
        assertEquals("Book returned.", response.message());
        verify(bookRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should throw LoanNotFoundException and never touch the book repository when loan ID does not exist")
    void shouldThrowLoanNotFoundException_whenLoanIsNotFound() {
        LoanReturn useCase = newLoanReturn(Clock.systemUTC());
        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.empty());

        assertThrows(LoanNotFoundException.class, () -> useCase.execute(LOAN_ID));

        verifyNoInteractions(bookRepository);
        verify(loanRepository, never()).updateIfCurrentStatus(any(), any());
    }

    @Test
    @DisplayName("Should throw BookNotFoundException and abort persistence when the registered book is missing")
    void shouldThrowBookNotFoundException_whenBookIsMissing() {
        Loan activeLoan = activeLoan(LocalDate.now(), LocalDate.now().plusDays(15));
        LoanReturn useCase = newLoanReturn(Clock.systemUTC());

        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(activeLoan));
        when(bookRepository.getById(BOOK_ID)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> useCase.execute(LOAN_ID));

        verify(loanRepository, never()).updateIfCurrentStatus(any(), any());
        verify(bookRepository, never()).update(any());
    }

    private LoanReturn newLoanReturn(Clock clock) {
        return new LoanReturn(new PassthroughTransactionManager(), loanRepository, bookRepository, clock);
    }

    private static Loan activeLoan(LocalDate startDate, LocalDate endDate) {
        return Loan.restore(LOAN_ID, BOOK_ID, CUSTOMER_ID, LoanStatus.ACTIVE, startDate, endDate, null);
    }

    private static Book borrowedBook() {
        return Book.create(BOOK_ISBN, "Clean Architecture", "Robert C. Martin", 0, "BORROWED", "", "");
    }
}

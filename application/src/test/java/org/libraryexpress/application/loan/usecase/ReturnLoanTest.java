package org.libraryexpress.application.loan.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.libraryexpress.application.UnitTest;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.exception.BookNotFoundException;
import org.libraryexpress.domain.book.repository.BookRepository;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.domain.loan.exception.LoanNotFoundException;
import org.libraryexpress.domain.loan.repository.LoanRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@UnitTest
@ExtendWith(MockitoExtension.class)
@DisplayName("ReturnLoan UseCase - Unit Test (Mockito)")
class ReturnLoanTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private ReturnLoan returnLoan;

    private static final String LOAN_ID = "loan-uuid-123";
    private static final String ISBN_STR = "9788535902778";
    private static final String CUSTOMER_ID = "customer-uuid-123";

    @Test
    @DisplayName("Should finish the loan and make the book available when the loan is not overdue")
    void shouldFinishLoan_whenLoanIsActiveAndWithinDeadline() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        Loan activeLoan = new Loan.Builder()
                .setId(LOAN_ID)
                .setISBN(ISBN_STR)
                .setCustomerId(CUSTOMER_ID)
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(startDate)
                .setEndDate(startDate.plusDays(15))
                .build();

        Book borrowedBook = new Book.Builder()
                .setISBN(ISBN_STR)
                .setTitle("Clean Architecture")
                .setStatus(BookStatus.BORROWED)
                .build();

        // Clock paused within the 15-day period (Day 10)
        Clock fixedClock = Clock.fixed(Instant.parse("2026-08-10T12:00:00Z"), ZoneId.of("UTC"));
        ReturnLoan useCaseOnTime = new ReturnLoan(loanRepository, bookRepository, fixedClock);

        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(activeLoan));
        when(bookRepository.getByIsbn(ISBN_STR)).thenReturn(Optional.of(borrowedBook));

        // Act
        useCaseOnTime.execute(LOAN_ID);

        // Assert
        assertEquals(LoanStatus.FINISHED, activeLoan.getStatus());
        assertEquals(BookStatus.AVAILABLE, borrowedBook.getStatus());

        // Style rule: Only relevant persistence assertions
        verify(loanRepository).update(activeLoan);
        verify(bookRepository).update(borrowedBook);
    }

    @Test
    @DisplayName("Should mark the loan as overdue when execution happens past the deadline")
    void shouldMarkAsOverdue_whenCurrentDateExceedsDeadline() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        Loan activeLoan = new Loan.Builder()
                .setId(LOAN_ID)
                .setISBN(ISBN_STR)
                .setCustomerId(CUSTOMER_ID)
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(startDate)
                .setEndDate(startDate.plusDays(15))
                .build();

        Book borrowedBook = new Book.Builder()
                .setISBN(ISBN_STR)
                .setTitle("Clean Architecture")
                .setStatus(BookStatus.BORROWED)
                .build();

        // Clock frozen past the deadline (Day 20 -> 19 days later)
        Clock fixedClock = Clock.fixed(Instant.parse("2026-08-20T12:00:00Z"), ZoneId.of("UTC"));
        ReturnLoan useCaseOverdue = new ReturnLoan(loanRepository, bookRepository, fixedClock);

        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(activeLoan));
        when(bookRepository.getByIsbn(ISBN_STR)).thenReturn(Optional.of(borrowedBook));

        // Act
        useCaseOverdue.execute(LOAN_ID);

        // Assert
        assertEquals(LoanStatus.OVERDUE, activeLoan.getStatus());
        assertEquals(BookStatus.AVAILABLE, borrowedBook.getStatus());

        verify(loanRepository).update(activeLoan);
        verify(bookRepository).update(borrowedBook);
    }

    @Test
    @DisplayName("Should throw LoanNotFoundException and never activate BookRepository when loan ID does not exist")
    void shouldThrowLoanNotFoundException_whenLoanIsNotFound() {
        // Arrange
        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(LoanNotFoundException.class, () -> returnLoan.execute(LOAN_ID));

        // Style rule: Ensuring secure orchestration behavior
        verifyNoInteractions(bookRepository);
        verify(loanRepository, never()).update(any());
    }

    // --- ADDITIONAL TECHNICAL PROTECTION SCENARIO 1 ---
    @Test
    @DisplayName("Should throw InvalidArgumentException when trying to return a loan that is already finished")
    void shouldThrowInvalidLoanStatusException_whenLoanStatusIsNotAllowed() {
        // Arrange
        Loan finishedLoan = new Loan.Builder()
                .setId(LOAN_ID)
                .setISBN(ISBN_STR)
                .setCustomerId(CUSTOMER_ID)
                .setStatus(LoanStatus.FINISHED) // Forbidden status in the Use Case's Set.of
                .setStartDate(LocalDate.now())
                .setEndDate(LocalDate.now().plusDays(15))
                .build();

        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(finishedLoan));

        // Act & Assert
        assertThrows(InvalidArgumentException.class, () -> returnLoan.execute(LOAN_ID));

        verifyNoInteractions(bookRepository);
        verify(loanRepository, never()).update(any());
    }

    // --- ADDITIONAL TECHNICAL PROTECTION SCENARIO 2 ---
    @Test
    @DisplayName("Should throw BookNotFoundException and abort persistence when the registered book is missing from database")
    void shouldThrowBookNotFoundException_whenBookIsMissing() {
        // Arrange
        Loan activeLoan = new Loan.Builder()
                .setId(LOAN_ID)
                .setISBN(ISBN_STR)
                .setCustomerId(CUSTOMER_ID)
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(LocalDate.now())
                .setEndDate(LocalDate.now().plusDays(15))
                .build();

        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(activeLoan));
        when(bookRepository.getByIsbn(ISBN_STR)).thenReturn(Optional.empty()); // Livro sumiu da base externa

        // Act & Assert
        assertThrows(BookNotFoundException.class, () -> returnLoan.execute(LOAN_ID));

        // Garante a atomicidade do fluxo de aplicação: se falhar no livro, nada é persistido parcial/indevidamente
        verify(loanRepository, never()).update(any());
        verify(bookRepository, never()).update(any());
    }
}
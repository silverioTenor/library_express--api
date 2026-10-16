package org.libraryexpress.application.loan.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.libraryexpress.application.UnitTest;
import org.libraryexpress.application.loan.dto.request.CreateLoanDTO;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.exception.BookUnavailableException;
import org.libraryexpress.domain.book.repository.BookRepository;
import org.libraryexpress.domain.book.validator.BookAvailabilityValidator;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.loan.exception.LoanLimitReachedException;
import org.libraryexpress.domain.loan.exception.OverdueLoanException;
import org.libraryexpress.domain.loan.repository.LoanRepository;
import org.libraryexpress.domain.loan.validator.LoanEligibilityValidator;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@UnitTest
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateLoan UseCase - Unit Test")
class CreateLoanTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LoanEligibilityValidator loanEligibilityValidator;

    @Mock
    private BookAvailabilityValidator bookAvailabilityValidator;

    @InjectMocks
    private CreateLoan createLoan;

    private static final String CUSTOMER_ID = "customer-uuid-123";
    private static final String ISBN_STR = "978-43-12345-67-8";

    @Test
    @DisplayName("Should create loan successfully when customer is eligible and book is available")
    void shouldCreateLoan_whenCustomerIsEligibleAndBookIsAvailable() {
        // Arrange
        CreateLoanDTO requestDto = new CreateLoanDTO(CUSTOMER_ID, ISBN_STR);

        Book availableBook = new Book.Builder()
                .setISBN(ISBN_STR)
                .setTitle("Test-Driven Development")
                .setStatus(BookStatus.AVAILABLE)
                .build();

        // Stubbing validators behavior for the happy path (void methods do nothing by default)
        doNothing().when(loanEligibilityValidator).validate(CUSTOMER_ID);
        when(bookAvailabilityValidator.validate(ISBN_STR)).thenReturn(availableBook);

        // Act
        createLoan.execute(requestDto);

        // Assert
        assertEquals(BookStatus.BORROWED, availableBook.getStatus(), "Book status must transition to BORROWED");

        // Business orchestration verification (skipping trivial times(1) per style guide)
        verify(bookRepository).update(availableBook);
        verify(loanRepository).create(any(Loan.class));
    }

    @Test
    @DisplayName("Should throw LoanLimitReachedException and abort execution when customer reaches active loan limit")
    void shouldThrowLoanLimitReachedException_whenCustomerReachesActiveLoanLimit() {
        // Arrange
        CreateLoanDTO requestDto = new CreateLoanDTO(CUSTOMER_ID, ISBN_STR);

        // Simulating domain validator blocking the customer
        doThrow(new LoanLimitReachedException("Customer has reached the maximum limit of active loans."))
                .when(loanEligibilityValidator).validate(CUSTOMER_ID);

        // Act & Assert
        assertThrows(LoanLimitReachedException.class, () -> createLoan.execute(requestDto));

        // Orchestration style guide compliance: ensure subsequent validations and persistence are entirely bypassed
        verifyNoInteractions(bookAvailabilityValidator);
        verifyNoInteractions(bookRepository);
        verifyNoInteractions(loanRepository);
    }

    @Test
    @DisplayName("Should throw OverdueLoanException and abort execution when customer has a pending overdue loan")
    void shouldThrowOverdueLoanException_whenCustomerHasOverdueLoan() {
        // Arrange
        CreateLoanDTO requestDto = new CreateLoanDTO(CUSTOMER_ID, ISBN_STR);

        doThrow(new OverdueLoanException("Customer has a pending overdue return."))
                .when(loanEligibilityValidator).validate(CUSTOMER_ID);

        // Act & Assert
        assertThrows(OverdueLoanException.class, () -> createLoan.execute(requestDto));

        verifyNoInteractions(bookAvailabilityValidator);
        verifyNoInteractions(bookRepository);
        verifyNoInteractions(loanRepository);
    }

    @Test
    @DisplayName("Should throw BookUnavailableException and prevent loan creation when book is not available")
    void shouldThrowBookUnavailableException_whenBookIsUnavailable() {
        // Arrange
        CreateLoanDTO requestDto = new CreateLoanDTO(CUSTOMER_ID, ISBN_STR);

        doNothing().when(loanEligibilityValidator).validate(CUSTOMER_ID);

        // Simulating book validator blocking the process
        when(bookAvailabilityValidator.validate(ISBN_STR)).thenThrow(new BookUnavailableException());

        // Act & Assert
        assertThrows(BookUnavailableException.class, () -> createLoan.execute(requestDto));

        // Ensure the domain entities are never persisted or updated when validation fails halfway
        verifyNoInteractions(bookRepository);
        verifyNoInteractions(loanRepository);
    }
}


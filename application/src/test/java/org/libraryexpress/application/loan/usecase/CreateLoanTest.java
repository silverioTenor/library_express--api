package org.libraryexpress.application.loan.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.libraryexpress.application.UnitTest;
import org.libraryexpress.application.loan.dto.request.CreateLoanDTO;
import org.libraryexpress.domain.loan.validator.LoanPeriodValidator;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.exception.BookUnavailableException;
import org.libraryexpress.domain.book.repository.BookRepository;
import org.libraryexpress.domain.book.validator.BookAvailabilityValidator;
import org.libraryexpress.domain.customer.entity.Customer;
import org.libraryexpress.domain.customer.exception.CustomerNotFoundException;
import org.libraryexpress.domain.customer.repository.CustomerRepository;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.loan.exception.DateOutOfBoundException;
import org.libraryexpress.domain.loan.exception.LoanLimitReachedException;
import org.libraryexpress.domain.loan.exception.OverdueLoanException;
import org.libraryexpress.domain.loan.repository.LoanRepository;
import org.libraryexpress.domain.loan.validator.LoanEligibilityValidator;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

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
    private CustomerRepository customerRepository;

    @Mock
    private LoanEligibilityValidator loanEligibilityValidator;

    @Mock
    private BookAvailabilityValidator bookAvailabilityValidator;

    private CreateLoan createLoan;

    private static final String CUSTOMER_ID = "customer-uuid-123";
    private static final String ISBN_STR = "9788535902778";

    @BeforeEach
    void setUp() {
        createLoan = new CreateLoan(
                loanRepository,
                bookRepository,
                customerRepository,
                loanEligibilityValidator,
                bookAvailabilityValidator,
                new LoanPeriodValidator()
        );
    }

    @Test
    @DisplayName("Should create loan successfully when customer is eligible and book is available")
    void shouldCreateLoan_whenCustomerIsEligibleAndBookIsAvailable() {
        CreateLoanDTO requestDto = new CreateLoanDTO(CUSTOMER_ID, ISBN_STR);
        Book availableBook = availableBook();
        stubHappyPath(availableBook);

        createLoan.execute(requestDto);

        assertEquals(BookStatus.BORROWED, availableBook.getStatus(), "Book status must transition to BORROWED");
        verify(bookRepository).update(availableBook);
        verify(loanRepository).create(any(Loan.class));
    }

    @Test
    @DisplayName("Should default due date to start date plus 15 days when due date is missing")
    void shouldDefaultDueDate_whenDueDateIsMissing() {
        CreateLoanDTO requestDto = new CreateLoanDTO(CUSTOMER_ID, ISBN_STR);
        stubHappyPath(availableBook());

        createLoan.execute(requestDto);

        ArgumentCaptor<Loan> loanCaptor = ArgumentCaptor.forClass(Loan.class);
        verify(loanRepository).create(loanCaptor.capture());
        Loan created = loanCaptor.getValue();
        assertEquals(LocalDate.now(), created.getStartDate());
        assertEquals(LocalDate.now().plusDays(LoanPeriodValidator.MAX_LOAN_DAYS), created.getEndDate());
    }

    @Test
    @DisplayName("Should reject loan creation when due date exceeds the 15-day limit")
    void shouldRejectLoan_whenDueDateExceeds15DayLimit() {
        LocalDate startDate = LocalDate.now();
        CreateLoanDTO requestDto = new CreateLoanDTO(
                CUSTOMER_ID,
                ISBN_STR,
                startDate.plusDays(LoanPeriodValidator.MAX_LOAN_DAYS + 1)
        );
        stubHappyPath(availableBook());

        DateOutOfBoundException exception = assertThrows(
                DateOutOfBoundException.class,
                () -> createLoan.execute(requestDto)
        );

        assertEquals("End date cannot exceed the 15-day limit from start date", exception.getMessage());
        verifyNoInteractions(loanRepository);
        verifyNoInteractions(bookRepository);
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException when the customer does not exist")
    void shouldThrowCustomerNotFoundException_whenCustomerIsUnknown() {
        CreateLoanDTO requestDto = new CreateLoanDTO(CUSTOMER_ID, ISBN_STR);
        doNothing().when(loanEligibilityValidator).validate(CUSTOMER_ID);
        when(bookAvailabilityValidator.validate(ISBN_STR)).thenReturn(availableBook());
        when(customerRepository.getById(CUSTOMER_ID)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> createLoan.execute(requestDto));

        verifyNoInteractions(loanRepository);
        verifyNoInteractions(bookRepository);
    }

    @Test
    @DisplayName("Should throw LoanLimitReachedException and abort execution when customer reaches active loan limit")
    void shouldThrowLoanLimitReachedException_whenCustomerReachesActiveLoanLimit() {
        CreateLoanDTO requestDto = new CreateLoanDTO(CUSTOMER_ID, ISBN_STR);

        doThrow(new LoanLimitReachedException("Customer has reached the maximum limit of active loans."))
                .when(loanEligibilityValidator).validate(CUSTOMER_ID);

        assertThrows(LoanLimitReachedException.class, () -> createLoan.execute(requestDto));

        verifyNoInteractions(bookAvailabilityValidator);
        verifyNoInteractions(bookRepository);
        verifyNoInteractions(loanRepository);
    }

    @Test
    @DisplayName("Should throw OverdueLoanException and abort execution when customer has a pending overdue loan")
    void shouldThrowOverdueLoanException_whenCustomerHasOverdueLoan() {
        CreateLoanDTO requestDto = new CreateLoanDTO(CUSTOMER_ID, ISBN_STR);

        doThrow(new OverdueLoanException("Customer has a pending overdue return."))
                .when(loanEligibilityValidator).validate(CUSTOMER_ID);

        assertThrows(OverdueLoanException.class, () -> createLoan.execute(requestDto));

        verifyNoInteractions(bookAvailabilityValidator);
        verifyNoInteractions(bookRepository);
        verifyNoInteractions(loanRepository);
    }

    @Test
    @DisplayName("Should throw BookUnavailableException and prevent loan creation when book is not available")
    void shouldThrowBookUnavailableException_whenBookIsUnavailable() {
        CreateLoanDTO requestDto = new CreateLoanDTO(CUSTOMER_ID, ISBN_STR);

        doNothing().when(loanEligibilityValidator).validate(CUSTOMER_ID);
        when(bookAvailabilityValidator.validate(ISBN_STR)).thenThrow(new BookUnavailableException());

        assertThrows(BookUnavailableException.class, () -> createLoan.execute(requestDto));

        verifyNoInteractions(bookRepository);
        verifyNoInteractions(loanRepository);
    }

    private void stubHappyPath(Book availableBook) {
        Customer customer = new Customer.Builder()
                .setName("John Doe")
                .setEmail("test@test.com")
                .setId(CUSTOMER_ID)
                .build();

        doNothing().when(loanEligibilityValidator).validate(CUSTOMER_ID);
        when(bookAvailabilityValidator.validate(ISBN_STR)).thenReturn(availableBook);
        when(customerRepository.getById(CUSTOMER_ID)).thenReturn(Optional.ofNullable(customer));
    }

    private static Book availableBook() {
        return new Book.Builder()
                .setISBN(ISBN_STR)
                .setTitle("Test-Driven Development")
                .setStatus(BookStatus.AVAILABLE)
                .build();
    }
}

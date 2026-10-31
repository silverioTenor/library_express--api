package org.libraryexpress.application.loan.usecase;

import org.libraryexpress.application.loan.dto.response.CreateLoanResponseDTO;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.validator.BookAvailabilityValidator;
import org.libraryexpress.application.loan.dto.request.CreateLoanDTO;
import org.libraryexpress.domain.loan.validator.LoanPeriodValidator;
import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;
import org.libraryexpress.domain.customer.exception.CustomerNotFoundException;
import org.libraryexpress.domain.customer.repository.CustomerRepository;
import org.libraryexpress.domain.loan.validator.LoanEligibilityValidator;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.domain.core.util.RandomGenerator;
import org.libraryexpress.domain.book.repository.BookRepository;
import org.libraryexpress.domain.loan.repository.LoanRepository;

import java.time.LocalDate;

public class CreateLoan {

    private static final CustomLogger logger =  CustomLoggerFactory.getLogger(CreateLoan.class);

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final CustomerRepository customerRepository;
    private final LoanEligibilityValidator loanEligibilityValidator;
    private final BookAvailabilityValidator bookAvailabilityValidator;
    private final LoanPeriodValidator loanPeriodValidator;

    public CreateLoan(
            LoanRepository loanRepository,
            BookRepository bookRepository,
            CustomerRepository customerRepository,
            LoanEligibilityValidator loanEligibilityValidator,
            BookAvailabilityValidator bookAvailabilityValidator,
            LoanPeriodValidator loanPeriodValidator
    ) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.customerRepository = customerRepository;
        this.loanEligibilityValidator = loanEligibilityValidator;
        this.bookAvailabilityValidator = bookAvailabilityValidator;
        this.loanPeriodValidator = loanPeriodValidator;
    }

    public CreateLoanResponseDTO execute(CreateLoanDTO createLoanDto) {
        logger.info("Initiating loan creation. loan DTO: [{}]", createLoanDto);

        this.loanEligibilityValidator.validate(createLoanDto.customerId());
        Book book = this.bookAvailabilityValidator.validate(createLoanDto.ISBN().value());

        this.customerRepository
                .getById(createLoanDto.customerId())
                .orElseThrow(CustomerNotFoundException::new);

        LocalDate startDate = LocalDate.now();
        LocalDate dueDate = createLoanDto.dueDate() != null
                ? createLoanDto.dueDate()
                : startDate.plusDays(LoanPeriodValidator.MAX_LOAN_DAYS);
        this.loanPeriodValidator.validate(startDate, dueDate);

        String id = RandomGenerator.UUID();

        Loan loan = new Loan.Builder()
                .setId(id)
                .setISBN(createLoanDto.ISBN().value())
                .setCustomerId(createLoanDto.customerId())
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(startDate)
                .setEndDate(dueDate)
                .build();

        book.changeStatus(BookStatus.BORROWED);

        this.loanRepository.create(loan);
        this.bookRepository.update(book);

        logger.info("Created loan flow finished successfully! Loan ID: [{}]", id);

        return new CreateLoanResponseDTO(id);
    }
}

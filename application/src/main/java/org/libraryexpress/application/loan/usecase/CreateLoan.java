package org.libraryexpress.application.loan.usecase;

import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.exception.BookNotFoundException;
import org.libraryexpress.domain.book.exception.BookUnavailableException;
import org.libraryexpress.domain.book.validator.BookAvailabilityValidator;
import org.libraryexpress.application.loan.dto.request.CreateLoanDto;
import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;
import org.libraryexpress.domain.loan.exception.LoanLimitReachedException;
import org.libraryexpress.domain.loan.exception.OverdueLoanException;
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
    private final LoanEligibilityValidator loanEligibilityValidator;
    private final BookAvailabilityValidator bookAvailabilityValidator;

    public CreateLoan(
            LoanRepository loanRepository,
            BookRepository bookRepository,
            LoanEligibilityValidator loanEligibilityValidator,
            BookAvailabilityValidator bookAvailabilityValidator
    ) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.loanEligibilityValidator = loanEligibilityValidator;
        this.bookAvailabilityValidator = bookAvailabilityValidator;
    }

    public void execute(CreateLoanDto createLoanDto) {
        logger.info("Initiating loan creation. loan DTO: [{}]", createLoanDto);

        this.loanEligibilityValidator.validate(createLoanDto.customerId());
        Book book = this.bookAvailabilityValidator.validate(createLoanDto.ISBN());

        String id = RandomGenerator.UUID();

        Loan loan = new Loan.Builder()
                .setId(id)
                .setISBN(createLoanDto.ISBN())
                .setCustomerId(createLoanDto.customerId())
                .setStatus(LoanStatus.ACTIVE)
                .setStartDate(LocalDate.now())
                .build();

        book.changeStatus(BookStatus.BORROWED);

        this.bookRepository.update(book);
        this.loanRepository.create(loan);

        logger.info("Created loan flow finished successfully!");
    }
}

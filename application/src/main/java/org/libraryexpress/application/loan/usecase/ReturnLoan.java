package org.libraryexpress.application.loan.usecase;

import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.exception.BookNotFoundException;
import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;
import org.libraryexpress.domain.loan.exception.InvalidLoanStatusException;
import org.libraryexpress.domain.loan.exception.LoanNotFoundException;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.domain.book.repository.BookRepository;
import org.libraryexpress.domain.loan.repository.LoanRepository;

import java.time.Clock;
import java.util.Set;

public class ReturnLoan {

    private static final Set<LoanStatus> ALLOWED_STATUSES = Set.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE);

    private static final CustomLogger logger = CustomLoggerFactory.getLogger(ReturnLoan.class);

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final Clock clock;

    public ReturnLoan(LoanRepository loanRepository, BookRepository bookRepository, Clock clock) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.clock = clock;
    }

    public void execute(String loanId) {
        logger.info("Starting loan return. Loan ID: [{}]", loanId);

        Loan loan = this.loanRepository.findById(loanId)
                .orElseThrow(() -> {
                    logger.warn("ABORTED: No loan found matching the provided parameters. Loan ID: [{}]", loanId);
                    return new LoanNotFoundException("No loan found matching the provided parameters.");
                });

        if (!ALLOWED_STATUSES.contains(loan.getStatus())) {
            logger.warn("ABORTED: Loan status not allowed. Loan ID: [{}]", loan.getStatus());
            throw new InvalidLoanStatusException("The Loan cannot be completed with the current status.");
        }

        Book book = this.bookRepository.getByIsbn(loan.getISBN().value())
                .orElseThrow(() -> {
                    logger.error(
                            "CRITICAL: Book record missing for an active loan. ISBN: [{}], Loan ID: [{}]",
                            loan.getISBN().value(), loanId);
                    return new BookNotFoundException();
                });

        // TODO - v2 - use JOB to change LOAN status and then send e-mail/notification
        LoanStatus updatedStatus = loan.isOverdue(this.clock)
                ? LoanStatus.OVERDUE
                : LoanStatus.FINISHED;

        loan.changeStatus(updatedStatus);
        book.changeStatus(BookStatus.AVAILABLE);

        this.loanRepository.update(loan);
        this.bookRepository.update(book);

        logger.info("Loan successfully completed!. Loan ID: [{}], final status: [{}]", loanId, updatedStatus);
    }
}

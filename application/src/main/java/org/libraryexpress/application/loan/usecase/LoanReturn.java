package org.libraryexpress.application.loan.usecase;

import org.libraryexpress.application.loan.dto.response.LoanReturnResponseDTO;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.exception.BookNotFoundException;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;
import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;
import org.libraryexpress.domain.loan.exception.LoanNotFoundException;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.domain.book.repository.BookRepository;
import org.libraryexpress.domain.loan.repository.LoanRepository;

import java.time.Clock;
import java.util.Set;

public class LoanReturn {

    private static final CustomLogger logger = CustomLoggerFactory.getLogger(LoanReturn.class);

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final Clock clock;

    public LoanReturn(LoanRepository loanRepository, BookRepository bookRepository, Clock clock) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.clock = clock;
    }

    public LoanReturnResponseDTO execute(String loanId) {
        logger.info("Starting loan return. Loan ID: [{}]", loanId);

        Loan loan = this.loanRepository.findById(loanId)
                .orElseThrow(() -> {
                    logger.warn("ABORTED: No loan found matching the provided parameters. Loan ID: [{}]", loanId);
                    return new LoanNotFoundException("No loan found matching the provided parameters.");
                });

        if (!loan.getStatus().equals(LoanStatus.ACTIVE)) {
            logger.warn("ABORTED: Loan status not allowed. Loan ID: [{}]", loan.getStatus());
            throw new InvalidArgumentException("The Loan cannot be completed with the current status.");
        }

        Book book = this.bookRepository.getByIsbn(loan.getISBN().value())
                .orElseThrow(() -> {
                    logger.error(
                            "CRITICAL: Book record missing for an active loan. isbn: [{}], Loan ID: [{}]",
                            loan.getISBN().value(), loanId);
                    return new BookNotFoundException();
                });

        // TODO - v2 - use JOB to change LOAN status and then send e-mail/notification
        loan.processReturn(this.clock, false);
        book.changeStatus(BookStatus.AVAILABLE);

        this.loanRepository.update(loan);
        this.bookRepository.update(book);

        if (loan.getStatus() == LoanStatus.OVERDUE) {
            logger.info("Book returned with outstanding late fees. Loan ID: [{}]", loanId);
            return new LoanReturnResponseDTO("warning", "Book returned, but a late fee is pending.");
        }

        logger.info("Loan successfully completed!. Loan ID: [{}]", loanId);
        return new LoanReturnResponseDTO("success", "Book returned.");
    }
}

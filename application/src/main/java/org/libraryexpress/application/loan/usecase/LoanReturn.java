package org.libraryexpress.application.loan.usecase;

import java.time.Clock;
import org.libraryexpress.application.loan.dto.response.LoanReturnResponseDTO;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.exception.BookNotFoundException;
import org.libraryexpress.domain.book.repository.BookRepository;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;
import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;
import org.libraryexpress.domain.core.repository.TransactionManager;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.domain.loan.exception.LoanNotFoundException;
import org.libraryexpress.domain.loan.repository.LoanRepository;

public class LoanReturn {

    private static final CustomLogger LOG = CustomLoggerFactory.getLogger(LoanReturn.class);

    private static final String SUCCESS_MESSAGE = "Book returned.";
    private static final String WARNING_MESSAGE = "Book returned, but a late fee is pending.";

    private final TransactionManager transaction;
    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final Clock clock;

    public LoanReturn(
            TransactionManager transaction, LoanRepository loanRepository, BookRepository bookRepository, Clock clock) {
        this.transaction = transaction;
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.clock = clock;
    }

    public LoanReturnResponseDTO execute(String loanId) {
        LOG.info("Starting loan return. Loan ID: [{}]", loanId);

        return this.transaction.execute(() -> {
            Loan loan = this.loanRepository.findById(loanId).orElseThrow(() -> {
                LOG.warn("ABORTED: No loan found matching the provided parameters. Loan ID: [{}]", loanId);
                return new LoanNotFoundException("No loan found matching the provided parameters.");
            });

            // Repeated return requests for an already-processed loan are treated as idempotent.
            if (loan.getStatus() == LoanStatus.FINISHED || loan.getStatus() == LoanStatus.OVERDUE) {
                LOG.info(
                        "Idempotent loan return. Loan already processed. Loan ID: [{}], status: [{}]",
                        loanId,
                        loan.getStatus());
                return responseFor(loan);
            }

            if (loan.getStatus() != LoanStatus.ACTIVE) {
                LOG.warn("ABORTED: Loan status not allowed. Loan ID: [{}], status: [{}]", loanId, loan.getStatus());
                throw new InvalidArgumentException("The Loan cannot be completed with the current status.");
            }

            Book book = this.bookRepository.getById(loan.getBookId()).orElseThrow(() -> {
                LOG.error(
                        "CRITICAL: Book record missing for an active loan. bookId: [{}], Loan ID: [{}]",
                        loan.getBookId(),
                        loanId);
                return new BookNotFoundException();
            });

            // TODO - v2 - use JOB to change LOAN status and then send e-mail/notification
            loan.processReturn(this.clock, false);
            book.changeStatus(BookStatus.AVAILABLE);

            boolean applied = this.loanRepository.updateIfCurrentStatus(loan, LoanStatus.ACTIVE);
            if (!applied) {
                // Another concurrent request already completed the return.
                Loan current = this.loanRepository.findById(loanId).orElseThrow(LoanNotFoundException::new);
                LOG.info(
                        "Idempotent loan return after concurrent update. Loan ID: [{}], status: [{}]",
                        loanId,
                        current.getStatus());
                return responseFor(current);
            }

            this.bookRepository.update(book);

            if (loan.getStatus() == LoanStatus.OVERDUE) {
                LOG.info("Book returned with outstanding late fees. Loan ID: [{}]", loanId);
                return responseFor(loan);
            }

            LOG.info("Loan successfully completed!. Loan ID: [{}]", loanId);
            return responseFor(loan);
        });
    }

    private static LoanReturnResponseDTO responseFor(Loan loan) {
        if (loan.getStatus() == LoanStatus.OVERDUE) {
            return new LoanReturnResponseDTO("warning", WARNING_MESSAGE);
        }
        return new LoanReturnResponseDTO("success", SUCCESS_MESSAGE);
    }
}

package org.libraryexpress.application.loan.usecase;

import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;
import org.libraryexpress.domain.loan.exception.LoanNotFoundException;
import org.libraryexpress.domain.loan.exception.OverdueLoanException;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.domain.loan.repository.LoanRepository;

import java.time.Clock;

/**
 * TODO - Temporary/palliative use case.
 * Once fine calculation and score adjustment are implemented directly in LoanReturn,
 * this use case should become obsolete — OVERDUE loans should transition to FINISHED
 * automatically at return time, not via manual intervention.
 */
public class CloseOverdueLoan {

    private static final CustomLogger LOG =  CustomLoggerFactory.getLogger(CloseOverdueLoan.class);

    private final LoanRepository loanRepository;

    private final Clock clock;

    public CloseOverdueLoan(LoanRepository loanRepository, Clock clock) {
        this.loanRepository = loanRepository;
        this.clock = clock;
    }

    public void execute(String loanId) {
        LOG.info("Initiating overdue loan settlement...");

        Loan loan = this.loanRepository.findById(loanId)
                .orElseThrow(() -> {
                    LOG.error("CRITICAL: No loan was found for the ID: [{}]", loanId);
                    return new LoanNotFoundException();
                });

        // Repeated close requests for an already-finished loan are treated as idempotent.
        if (loan.getStatus() == LoanStatus.FINISHED) {
            LOG.info("Idempotent overdue close. Loan already finished. Loan ID: [{}]", loanId);
            return;
        }

        if (loan.getStatus() != LoanStatus.OVERDUE) {
            LOG.warn("ABORTED: Loan status is not OVERDUE. Loan ID: [{}], status: [{}]", loanId, loan.getStatus());
            throw new OverdueLoanException();
        }

        loan.processReturn(clock, true);

        boolean applied = this.loanRepository.updateIfCurrentStatus(loan, LoanStatus.OVERDUE);
        if (!applied) {
            LOG.info("Idempotent overdue close after concurrent update. Loan ID: [{}]", loanId);
            return;
        }

        LOG.info("Loan successfully completed! Loan ID: [{}]", loanId);
    }
}

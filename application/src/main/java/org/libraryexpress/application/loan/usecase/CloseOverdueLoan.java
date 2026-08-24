package org.libraryexpress.application.loan.usecase;

import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;
import org.libraryexpress.domain.loan.exception.LoanNotFoundException;
import org.libraryexpress.domain.loan.exception.OverdueLoanException;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.domain.loan.repository.LoanRepository;

/**
 * TODO - Temporary/palliative use case.
 * Once fine calculation and score adjustment are implemented directly in ReturnLoan,
 * this use case should become obsolete — OVERDUE loans should transition to FINISHED
 * automatically at return time, not via manual intervention.
 */
public class CloseOverdueLoan {

    private static final CustomLogger logger =  CustomLoggerFactory.getLogger(CloseOverdueLoan.class);

    private final LoanRepository loanRepository;

    public CloseOverdueLoan(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public void execute(String loanId) {
        logger.info("Initiating overdue loan settlement...");

        Loan loan = this.loanRepository.findById(loanId)
                .orElseThrow(() -> {
                    logger.error("CRITICAL: No loan was found for the ID: [{}]", loanId);
                    return new LoanNotFoundException();
                });

        if (!loan.getStatus().equals(LoanStatus.OVERDUE)) {
            logger.warn("ABORTED: The loan was already overdue");
            throw new OverdueLoanException();
        }

        loan.changeStatus(LoanStatus.FINISHED);
        this.loanRepository.update(loan);

        logger.info("Loan successfully completed! Loan ID: [{}]", loanId);
    }
}

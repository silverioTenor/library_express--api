package org.libraryexpress.domain.loan.repository;

import org.libraryexpress.domain.core.repository.QueryResult;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.loan.enums.LoanStatus;

import java.util.Optional;

public interface LoanRepository {
    void create(Loan loan);
    void update(Loan loanToUpdate);

    /**
     * Updates the loan only when its persisted status still matches {@code expectedStatus}.
     * Returns {@code true} when one row was updated; {@code false} when another operation
     * already changed the status (safe for concurrent/idempotent flows).
     */
    boolean updateIfCurrentStatus(Loan loan, LoanStatus expectedStatus);

    Optional<Loan> findById(String id);
    QueryResult<Loan> search(LoanFilter filter);
}

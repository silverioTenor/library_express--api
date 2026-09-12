package org.libraryexpress.domain.loan.repository;

import org.libraryexpress.domain.core.dto.InputPaginationDto;
import org.libraryexpress.domain.core.repository.QueryResult;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.loan.enums.LoanStatus;

import java.util.Optional;
import java.util.Set;

public interface LoanRepository {
    void create(Loan loan);
    void update(Loan loanToUpdate);
    Optional<Loan> findById(String loanId);
    QueryResult<Loan> search(String customerId, String ISBN, Set<LoanStatus> statuses, InputPaginationDto paginationDto);
}

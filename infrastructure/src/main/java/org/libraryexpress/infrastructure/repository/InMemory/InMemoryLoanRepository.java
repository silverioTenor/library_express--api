package org.libraryexpress.infrastructure.repository.InMemory;

import org.libraryexpress.domain.core.dto.InputPaginationDto;
import org.libraryexpress.domain.core.repository.QueryResult;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.domain.loan.repository.LoanRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InMemoryLoanRepository implements LoanRepository {

    private final Map<String, Loan> group = new ConcurrentHashMap<>();

    @Override
    public void create(Loan loan) {
        group.put(loan.getId(), loan);
    }

    @Override
    public void update(Loan loanToUpdate) {
        Optional.ofNullable(group.get(loanToUpdate.getISBN().value()))
                .ifPresent(loan -> loan.changeStatus(loanToUpdate.getStatus()));
    }

    @Override
    public Optional<Loan> findById(String loanId) {
        return Optional.ofNullable(group.get(loanId));
    }

    @Override
    public QueryResult<Loan> search(String customerId, String ISBN, Set<LoanStatus> statuses, InputPaginationDto paginationDto) {

        Predicate<Loan> criteria = loan -> true;

        if (Objects.nonNull(customerId) && !customerId.isBlank()) {
            criteria = criteria.and(loan -> loan.getCustomerId().equals(customerId));
        }

        if (Objects.nonNull(ISBN) && !ISBN.isBlank()) {
            criteria = criteria.and(loan -> loan.getISBN().value().equals(ISBN));
        }

        if (Objects.nonNull(statuses)) {
            criteria = criteria.and(loan -> statuses.contains(loan.getStatus()));
        }

        Set<Loan> loans = group.values().stream()
                .filter(criteria)
                .collect(Collectors.toUnmodifiableSet());

        return new QueryResult<>(loans, loans.size());
    }
}

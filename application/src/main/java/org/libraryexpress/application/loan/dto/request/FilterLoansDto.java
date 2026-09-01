package org.libraryexpress.application.loan.dto.request;

import org.libraryexpress.domain.core.dto.InputPaginationDto;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.domain.loan.exception.InvalidLoanStatusException;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public record FilterLoansDto(
        String customerId,
        String ISBN,
        Set<LoanStatus> statuses,
        InputPaginationDto paginationDto
) {
    public FilterLoansDto(String customerId, String ISBN, String statusesStr, int page, int size) {
        this(
                customerId,
                ISBN,
                parseStatuses(statusesStr),
                new InputPaginationDto(page, size)
        );
    }

    private static Set<LoanStatus> parseStatuses(String statusesStr) {
        if (statusesStr == null || statusesStr.isBlank()) {
            return Set.of();
        }
        try {
            return Arrays.stream(statusesStr.split(","))
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .map(LoanStatus::valueOf)
                    .collect(Collectors.toSet());
        } catch (IllegalArgumentException e) {
            throw new InvalidLoanStatusException("Invalid status values provided in query params.");
        }
    }
}

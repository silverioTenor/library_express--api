package org.libraryexpress.application.loan.dto.request;

import org.libraryexpress.domain.book.valueobject.Isbn;
import org.libraryexpress.domain.core.dto.InputPaginationDto;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.domain.loan.repository.LoanFilter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public record LoanFilterDTO(
        String id,
        String customerId,
        Isbn ISBN,
        Set<LoanStatus> statuses,
        InputPaginationDto paginationDto
) {
    public LoanFilterDTO(String id, String customerId, String ISBN, String statusesStr, int page, int size) {
        this(
                id,
                customerId,
                parseIsbn(ISBN),
                parseStatuses(statusesStr),
                new InputPaginationDto(page, size)
        );
    }

    public LoanFilter toLoanFilter() {
        return new LoanFilter(
                id,
                customerId,
                ISBN,
                statuses,
                paginationDto
        );
    }

    private static Isbn parseIsbn(String ISBN) {
        if (ISBN == null || ISBN.isBlank()) return null;

        return new Isbn(ISBN);
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
            throw new InvalidArgumentException("Invalid status values provided in query params.");
        }
    }
}

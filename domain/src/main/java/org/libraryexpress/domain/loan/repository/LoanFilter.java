package org.libraryexpress.domain.loan.repository;

import org.libraryexpress.domain.core.dto.InputPaginationDto;
import org.libraryexpress.domain.loan.enums.LoanStatus;

import java.util.Set;

public record LoanFilter(
        String id,
        String customerId,
        String bookId,
        Set<LoanStatus> statuses,
        InputPaginationDto paginationDto
) {
}

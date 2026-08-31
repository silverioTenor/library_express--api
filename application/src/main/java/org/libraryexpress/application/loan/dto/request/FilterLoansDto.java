package org.libraryexpress.application.loan.dto.request;

import org.libraryexpress.domain.core.dto.InputPaginationDto;
import org.libraryexpress.domain.loan.enums.LoanStatus;

import java.util.Set;

public record FilterLoansDto(
        String customerId,
        String ISBN,
        Set<LoanStatus> statuses,
        InputPaginationDto paginationDto
) {
}

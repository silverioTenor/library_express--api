package org.libraryexpress.domain.loan.repository;

import java.util.Set;
import org.libraryexpress.domain.core.dto.InputPaginationDto;
import org.libraryexpress.domain.loan.enums.LoanStatus;

public record LoanFilter(
        String id, String customerId, String bookId, Set<LoanStatus> statuses, InputPaginationDto paginationDto) {}

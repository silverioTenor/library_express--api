package org.libraryexpress.application.loan.dto.response;

import org.libraryexpress.domain.loan.enums.LoanStatus;

import java.time.Instant;
import java.time.LocalDate;

public record LoanDTO(
        String id,
        String customerId,
        String bookId,
        LoanStatus status,
        LocalDate startDate,
        LocalDate endDate,
        Instant closedAt
) {
}

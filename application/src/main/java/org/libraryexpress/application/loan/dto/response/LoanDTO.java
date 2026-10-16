package org.libraryexpress.application.loan.dto.response;

import org.libraryexpress.domain.loan.enums.LoanStatus;

import java.time.LocalDate;

public record LoanDTO(
        String id,
        String customerId,
        String ISBN,
        LoanStatus status,
        LocalDate startDate,
        LocalDate endDate
) {
}

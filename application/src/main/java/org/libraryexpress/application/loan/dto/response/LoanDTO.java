package org.libraryexpress.application.loan.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import org.libraryexpress.domain.loan.enums.LoanStatus;

public record LoanDTO(
        String id,
        String customerId,
        String bookId,
        LoanStatus status,
        LocalDate startDate,
        LocalDate endDate,
        Instant closedAt) {}

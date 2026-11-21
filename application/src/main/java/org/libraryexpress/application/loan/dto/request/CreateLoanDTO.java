package org.libraryexpress.application.loan.dto.request;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;
import org.libraryexpress.domain.loan.entity.Loan;

public record CreateLoanDTO(String customerId, String bookId, LocalDate dueDate) {

    public CreateLoanDTO {
        List<String> errors = new ArrayList<>(2);

        if (customerId == null || customerId.isBlank()) {
            errors.add("The 'customerId' field cannot be null or empty");
        }

        if (bookId == null || bookId.isBlank()) {
            errors.add("The 'bookId' field cannot be null or empty");
        }

        if (!errors.isEmpty()) {
            throw new InvalidArgumentException(errors.toString());
        }
    }

    public Loan toEntity(String id, LocalDate startDate, LocalDate endDate) {
        return Loan.create(id, bookId, customerId, startDate, endDate);
    }
}

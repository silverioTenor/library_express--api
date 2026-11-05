package org.libraryexpress.application.loan.dto.request;

import org.libraryexpress.domain.core.exception.InvalidArgumentException;
import org.libraryexpress.domain.loan.entity.Loan;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record CreateLoanDTO(String customerId, String isbn, LocalDate dueDate) {

    public CreateLoanDTO {
        List<String> errors = new ArrayList<>(2);

        if (customerId == null || customerId.isBlank()) {
            errors.add("The 'customerId' field cannot be null or empty");
        }

        if (isbn == null || isbn.isBlank()) {
            errors.add("The 'isbn' field cannot be null or empty");
        }

        if (!errors.isEmpty()) {
            throw new InvalidArgumentException(errors.toString());
        }
    }

    public Loan toEntity(String id, LocalDate startDate, LocalDate endDate) {
        return Loan.create(id, isbn, customerId, startDate, endDate);
    }
}

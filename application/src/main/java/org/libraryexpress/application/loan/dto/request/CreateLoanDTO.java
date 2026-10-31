package org.libraryexpress.application.loan.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.libraryexpress.domain.book.valueobject.Isbn;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;

import java.time.LocalDate;

public record CreateLoanDTO(String customerId, Isbn ISBN, LocalDate dueDate) {

    public CreateLoanDTO {
        if (customerId == null || customerId.isBlank())
            throw new InvalidArgumentException("Customer ID cannot be null or blank");
    }

    public CreateLoanDTO(String customerId, String ISBN) {
        this(customerId, ISBN, null);
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public CreateLoanDTO(
            @JsonProperty("customerId") String customerId,
            @JsonProperty("ISBN") String ISBN,
            @JsonProperty("dueDate") LocalDate dueDate
    ) {
        this(customerId, new Isbn(ISBN), dueDate);
    }
}

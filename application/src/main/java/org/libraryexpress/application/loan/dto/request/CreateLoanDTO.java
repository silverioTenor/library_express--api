package org.libraryexpress.application.loan.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.libraryexpress.domain.book.valueobject.Isbn;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;

public record CreateLoanDTO(String customerId, Isbn ISBN) {

    public CreateLoanDTO {
        if (customerId == null || customerId.isBlank())
            throw new InvalidArgumentException("Customer ID cannot be null or blank");
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public CreateLoanDTO(@JsonProperty("customerId") String customerId, @JsonProperty("ISBN") String ISBN) {
        this(customerId, new Isbn(ISBN));
    }
}

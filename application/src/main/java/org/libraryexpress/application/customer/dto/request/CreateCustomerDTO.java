package org.libraryexpress.application.customer.dto.request;

import org.libraryexpress.domain.core.exception.InvalidArgumentException;

import java.util.ArrayList;
import java.util.List;

public record CreateCustomerDTO(String name, String email) {

    public CreateCustomerDTO {
        List<String> errors = new ArrayList<>(2);

        if (name == null || name.isBlank()) {
            errors.add("The 'name' field cannot be null or empty");
        }

        if (email == null || email.isBlank()) {
            errors.add("The 'email' field cannot be null or empty");
        }

        if (!errors.isEmpty()) {
            throw new InvalidArgumentException(errors.toString());
        }
    }
}
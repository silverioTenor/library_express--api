package org.libraryexpress.application.customer.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.libraryexpress.domain.customer.valueobject.Email;

public record CreateCustomerDTO(String name, Email email) {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public CreateCustomerDTO(@JsonProperty("name") String name, @JsonProperty("email") String email) {
        this(name, new Email(email));
    }
}
package org.libraryexpress.domain.customer.exception;

import org.libraryexpress.domain.core.exception.DomainErrorType;
import org.libraryexpress.domain.core.exception.DomainException;

public class CustomerNotFoundException extends DomainException {

    public CustomerNotFoundException() {
        super(DomainErrorType.RESOURCE_NOT_FOUND, "Customer not found!");
    }

    public CustomerNotFoundException(String message) {
        super(DomainErrorType.RESOURCE_NOT_FOUND, message);
    }
}

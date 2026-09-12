package org.libraryexpress.domain.customer.exception;

import org.libraryexpress.domain.core.exception.DomainErrorType;
import org.libraryexpress.domain.core.exception.DomainException;

public class InvalidEmailException extends DomainException {

    public InvalidEmailException() {
        super(DomainErrorType.INVALID_DATA, "The email cannot be null or empty.");
    }

    public InvalidEmailException(String message) {
        super(DomainErrorType.INVALID_DATA, message);
    }
}

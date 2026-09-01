package org.libraryexpress.domain.book.exception;

import org.libraryexpress.domain.core.exception.DomainErrorType;
import org.libraryexpress.domain.core.exception.DomainException;

public class InvalidIsbnException extends DomainException {

    public InvalidIsbnException() {
        super(DomainErrorType.INVALID_DATA, "The ISBN cannot be null or empty.");
    }

    public InvalidIsbnException(String message) {
        super(DomainErrorType.INVALID_DATA, message);
    }
}

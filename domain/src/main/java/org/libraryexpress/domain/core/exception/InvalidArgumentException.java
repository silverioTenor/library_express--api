package org.libraryexpress.domain.core.exception;

public class InvalidArgumentException extends DomainException {

    public InvalidArgumentException(String message) {
        super(DomainErrorType.INVALID_DATA, message);
    }
}

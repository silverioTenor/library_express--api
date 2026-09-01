package org.libraryexpress.domain.core.exception;

public class DomainException extends RuntimeException {

    private final DomainErrorType errorType;

    public DomainException(DomainErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public DomainErrorType getErrorType() {
        return errorType;
    }
}

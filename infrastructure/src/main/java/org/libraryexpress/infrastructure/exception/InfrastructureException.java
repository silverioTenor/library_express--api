package org.libraryexpress.infrastructure.exception;

public class InfrastructureException extends RuntimeException {

    private final InfrastructureErrorType errorType;

    public InfrastructureException(InfrastructureErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public InfrastructureErrorType getErrorType() {
        return errorType;
    }
}

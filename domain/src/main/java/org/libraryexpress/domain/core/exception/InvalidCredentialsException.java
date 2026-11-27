package org.libraryexpress.domain.core.exception;

import static org.libraryexpress.domain.core.exception.DomainErrorType.INVALID_CREDENTIAL;

public final class InvalidCredentialsException extends DomainException {

    private static final String MESSAGE = "Invalid credentials.";

    public InvalidCredentialsException() {
        super(INVALID_CREDENTIAL, MESSAGE);
    }
}

package org.libraryexpress.domain.customer.exception;

import org.libraryexpress.domain.core.exception.DomainErrorType;
import org.libraryexpress.domain.core.exception.DomainException;

public class UniqueEmailViolationException extends DomainException {

    public UniqueEmailViolationException() {
        super(DomainErrorType.DATA_CONFLICT, "E-mail must be unique.");
    }

    public UniqueEmailViolationException(String message) {
        super(DomainErrorType.DATA_CONFLICT, message);
    }
}

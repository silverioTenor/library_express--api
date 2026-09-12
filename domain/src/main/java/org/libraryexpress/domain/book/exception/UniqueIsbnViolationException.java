package org.libraryexpress.domain.book.exception;

import org.libraryexpress.domain.core.exception.DomainErrorType;
import org.libraryexpress.domain.core.exception.DomainException;

public class UniqueIsbnViolationException extends DomainException {

    public UniqueIsbnViolationException() {
        super(DomainErrorType.DATA_CONFLICT, "It is not permitted to register a book with an ISBN that is already in use.");
    }

    public UniqueIsbnViolationException(String message) {
        super(DomainErrorType.DATA_CONFLICT, message);
    }
}

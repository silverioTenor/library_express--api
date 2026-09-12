package org.libraryexpress.domain.book.exception;

import org.libraryexpress.domain.core.exception.DomainErrorType;
import org.libraryexpress.domain.core.exception.DomainException;

public class BookUnavailableException extends DomainException {

    public BookUnavailableException() {
        super(DomainErrorType.BUSINESS_VIOLATION, "Book is not available.");
    }

    public BookUnavailableException(String message) {
        super(DomainErrorType.BUSINESS_VIOLATION, message);
    }
}

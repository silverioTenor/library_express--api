package org.libraryexpress.domain.book.exception;

import org.libraryexpress.domain.core.exception.DomainErrorType;
import org.libraryexpress.domain.core.exception.DomainException;

public class BookNotFoundException extends DomainException {

    public BookNotFoundException() {
        super(DomainErrorType.RESOURCE_NOT_FOUND, "Book not Found!");
    }

    public BookNotFoundException(String message) {
        super(DomainErrorType.RESOURCE_NOT_FOUND, message);
    }
}

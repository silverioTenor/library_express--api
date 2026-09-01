package org.libraryexpress.domain.loan.exception;

import org.libraryexpress.domain.core.exception.DomainErrorType;
import org.libraryexpress.domain.core.exception.DomainException;

public class DateOutOfBoundException extends DomainException {

    public DateOutOfBoundException(String message) {
        super(DomainErrorType.BUSINESS_VIOLATION, message);
    }
}

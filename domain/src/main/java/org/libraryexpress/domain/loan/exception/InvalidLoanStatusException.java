package org.libraryexpress.domain.loan.exception;

import org.libraryexpress.domain.core.exception.DomainErrorType;
import org.libraryexpress.domain.core.exception.DomainException;

public class InvalidLoanStatusException extends DomainException {

    public InvalidLoanStatusException(String message) {
        super(DomainErrorType.INVALID_DATA, message);
    }
}

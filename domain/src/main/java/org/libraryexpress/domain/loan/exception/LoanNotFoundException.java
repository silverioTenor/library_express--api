package org.libraryexpress.domain.loan.exception;

import org.libraryexpress.domain.core.exception.DomainErrorType;
import org.libraryexpress.domain.core.exception.DomainException;

public class LoanNotFoundException extends DomainException {

    public LoanNotFoundException() {
        super(DomainErrorType.RESOURCE_NOT_FOUND, "Loan not found!");
    }

    public LoanNotFoundException(String message) {
        super(DomainErrorType.RESOURCE_NOT_FOUND, message);
    }
}

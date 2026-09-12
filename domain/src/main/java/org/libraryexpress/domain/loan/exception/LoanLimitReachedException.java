package org.libraryexpress.domain.loan.exception;

import org.libraryexpress.domain.core.exception.DomainErrorType;
import org.libraryexpress.domain.core.exception.DomainException;

public class LoanLimitReachedException extends DomainException {

    public LoanLimitReachedException(String message) {
        super(DomainErrorType.BUSINESS_VIOLATION, message);
    }
}

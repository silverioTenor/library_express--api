package org.libraryexpress.domain.loan.exception;

import org.libraryexpress.domain.core.exception.DomainErrorType;
import org.libraryexpress.domain.core.exception.DomainException;

public class OverdueLoanException extends DomainException {

    public OverdueLoanException() {
        super(DomainErrorType.BUSINESS_VIOLATION, "Only overdue loans can be closed through this flow.");
    }

    public OverdueLoanException(String message) {
        super(DomainErrorType.BUSINESS_VIOLATION, message);
    }
}

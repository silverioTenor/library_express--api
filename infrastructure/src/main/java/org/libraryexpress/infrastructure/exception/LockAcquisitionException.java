package org.libraryexpress.infrastructure.exception;

import static org.libraryexpress.infrastructure.exception.InfrastructureErrorType.SQL_TRANSACTION_LOCK;

public class LockAcquisitionException extends InfrastructureException {
    public LockAcquisitionException(String message) {
        super(SQL_TRANSACTION_LOCK, message);
    }
}

package org.libraryexpress.infrastructure.exception;

import static org.libraryexpress.infrastructure.exception.InfrastructureErrorType.SQL_TRANSACTION;

public class TransactionException extends InfrastructureException {
    public TransactionException(String message) {
        super(SQL_TRANSACTION, message);
    }
}

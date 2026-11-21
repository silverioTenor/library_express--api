package org.libraryexpress.infrastructure.exception;

public enum InfrastructureErrorType {
    INVALID_DATA,
    RESOURCE_NOT_FOUND,
    BAD_GATEWAY,
    GATEWAY_TIMEOUT,
    SERVICE_UNAVAILABLE,
    SQL_TRANSACTION,
    SQL_TRANSACTION_LOCK
}

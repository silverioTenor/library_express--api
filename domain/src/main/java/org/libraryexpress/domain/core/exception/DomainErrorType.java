package org.libraryexpress.domain.core.exception;

public enum DomainErrorType {
    INVALID_DATA,      // Malformed data (e.g., invalid ISBN, invalid email)
    RESOURCE_NOT_FOUND,// Resource not found (e.g., book, customer)
    DATA_CONFLICT,     // Uniqueness violation (e.g., CPF/ISBN already registered)
    BUSINESS_VIOLATION // Business rule violation (e.g., book unavailable, overdue loan)
}

package org.libraryexpress.domain.book.enums;

import org.libraryexpress.domain.core.exception.InvalidArgumentException;

public enum BookStatus {
    AVAILABLE,
    BORROWED,
    UNAVAILABLE;

    public static BookStatus from(String strStatus) {
        if (strStatus == null || strStatus.isBlank()) {
            return AVAILABLE;
        }
        try {
            return BookStatus.valueOf(strStatus.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidArgumentException("Invalid book status: " + strStatus);
        }
    }
}

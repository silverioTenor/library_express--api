package org.libraryexpress.application.book.dto.response;

import org.libraryexpress.domain.book.enums.BookStatus;

public record BookDTO(
        String ISBN,
        String title,
        String author,
        int year,
        BookStatus status
) {
}

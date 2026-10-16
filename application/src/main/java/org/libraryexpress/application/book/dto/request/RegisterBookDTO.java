package org.libraryexpress.application.book.dto.request;

import org.libraryexpress.domain.book.enums.BookStatus;

public record RegisterBookDTO(
        String ISBN,
        String title,
        String author,
        int year,
        BookStatus status
) {
}

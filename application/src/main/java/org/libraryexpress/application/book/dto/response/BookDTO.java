package org.libraryexpress.application.book.dto.response;

import org.libraryexpress.domain.book.enums.BookStatus;

public record BookDTO(
        String isbn,
        String title,
        String author,
        int publicationYear,
        BookStatus status
) {
}

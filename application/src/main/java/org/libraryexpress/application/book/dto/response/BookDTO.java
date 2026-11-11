package org.libraryexpress.application.book.dto.response;

import org.libraryexpress.domain.book.enums.BookStatus;

import java.util.List;

public record BookDTO(
        String id,
        String isbn,
        String title,
        List<String> authors,
        int publicationYear,
        BookStatus status,
        List<String> categories,
        String description
) {
}

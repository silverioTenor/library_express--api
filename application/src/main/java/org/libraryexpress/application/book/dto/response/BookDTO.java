package org.libraryexpress.application.book.dto.response;

import java.util.List;
import org.libraryexpress.domain.book.enums.BookStatus;

public record BookDTO(
        String id,
        String isbn,
        String title,
        List<String> authors,
        int publicationYear,
        BookStatus status,
        List<String> categories,
        String description) {}

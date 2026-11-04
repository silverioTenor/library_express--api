package org.libraryexpress.application.book.dto.request;

import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;

import java.security.InvalidParameterException;
import java.util.Arrays;

public record RegisterBookDTO(
        String isbn,
        String title,
        String author,
        int publicationYear,
        String status
) {
    public RegisterBookDTO {
        validateField("isbn", isbn);
        validateField("title", title);
        validateField("author", author);
        parseStatus(status);
    }

    public Book toEntity() {
        return Book.create(
                this.isbn(),
                this.title(),
                this.author(),
                this.publicationYear(),
                this.status()
        );
    }

    private static String validateField(String key, String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidParameterException("The field " + key + " is required");
        }
        return value;
    }

    private static String parseStatus(String strStatus) {
        if (strStatus == null || strStatus.isBlank()) {
            return BookStatus.AVAILABLE.name();
        }

        String srtStatusCleared = strStatus.trim().toUpperCase();
        var result = Arrays.stream(BookStatus.values()).filter(status -> status.name().equals(srtStatusCleared));

        if (result.findFirst().isEmpty()) {
            throw new InvalidArgumentException("Invalid status value provided in query params.");
        }

        return srtStatusCleared;
    }
}

package org.libraryexpress.application.book.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.libraryexpress.domain.book.entity.Book;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.valueobject.Isbn;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;

import java.security.InvalidParameterException;

public record RegisterBookDTO(
        Isbn isbn,
        String title,
        String author,
        int publicationYear,
        BookStatus status
) {
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public RegisterBookDTO(
            @JsonProperty("isbn") String isbn,
            @JsonProperty("title") String title,
            @JsonProperty("author") String author,
            @JsonProperty("publicationYear") int publicationYear,
            @JsonProperty("status") String status
    ) {
        this(
            new Isbn(isbn),
            validateField("title", title),
            validateField("author", author),
            publicationYear,
            parseStatus(status)
        );
    }

    public Book toEntity() {
        return Book.create(
                this.isbn().value(),
                this.title(),
                this.author(),
                this.publicationYear(),
                this.status().name()
        );
    }

    private static String validateField(String key, String value) {
        if (value == null || value.isBlank()) {
            return switch (key) {
                case "title" -> throw new InvalidArgumentException("Invalid " + key + " provided.");
                case "author" -> "Unnamed";
                default -> throw new InvalidParameterException("The provided field is unknown: " + key);
            };
        }
        return value;
    }

    private static BookStatus parseStatus(String strStatus) {
        if (strStatus == null || strStatus.isBlank()) {
            return BookStatus.AVAILABLE;
        }

        try {
            String srtStatusCleared = strStatus.trim().toUpperCase();
            return BookStatus.valueOf(srtStatusCleared);
        } catch (IllegalArgumentException e) {
            throw new InvalidArgumentException("Invalid status value provided in query params.");
        }
    }
}

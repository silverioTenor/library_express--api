package org.libraryexpress.application.book.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.valueobject.Isbn;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;

import java.security.InvalidParameterException;

public record RegisterBookDTO(
        Isbn ISBN,
        String title,
        String author,
        int year,
        BookStatus status
) {
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public RegisterBookDTO(
            @JsonProperty("ISBN") String ISBN,
            @JsonProperty("title") String title,
            @JsonProperty("author") String author,
            @JsonProperty("year") int year,
            @JsonProperty("status") String status
    ) {
        this(
            new Isbn(ISBN),
            validateField("title", title),
            validateField("author", author),
            year,
            parseStatus(status)
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

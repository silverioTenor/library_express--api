package org.libraryexpress.domain.book.valueobject;

import org.libraryexpress.domain.core.exception.InvalidArgumentException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.function.Predicate.not;

public record Author(String name) {

    public Author(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidArgumentException("Author name cannot be null or empty.");
        }
        this.name = name.trim();
    }

    public static List<Author> parseMany(String strAuthor) {
        if (strAuthor == null || strAuthor.isBlank()) {
            throw new InvalidArgumentException("Author is missing.");
        }
        return parseMany(Arrays.stream(strAuthor.split(","))
                .map(String::trim)
                .filter(not(String::isBlank))
                .toList());
    }

    public static List<Author> parseMany(String[] authorsArray) {
        if (authorsArray == null || authorsArray.length == 0) {
            throw new InvalidArgumentException("Author is missing.");
        }
        return parseMany(Arrays.stream(authorsArray)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(not(String::isBlank))
                .toList());
    }

    private static List<Author> parseMany(List<String> names) {
        if (names.isEmpty()) {
            throw new InvalidArgumentException("Author is missing.");
        }
        return names.stream().map(Author::new).toList();
    }
}

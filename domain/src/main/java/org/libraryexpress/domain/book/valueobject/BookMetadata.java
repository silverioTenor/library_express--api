package org.libraryexpress.domain.book.valueobject;

public record BookMetadata(
        String isbn,
        String title,
        String author,
        int publicationYear,
        String category,
        String description
) {
}

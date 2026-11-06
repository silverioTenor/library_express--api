package org.libraryexpress.infrastructure.util;

import org.libraryexpress.domain.book.valueobject.BookMetadata;

import java.util.Optional;

public final class BookMetadataParser {

    private BookMetadataParser() {}

    public static Optional<BookMetadata> parse(String jsonBody) {
        if (jsonBody == null || jsonBody.isBlank()) return Optional.empty();

        try {
            //
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

package org.libraryexpress.infrastructure.gateway;

import org.libraryexpress.domain.book.gateway.BookMetadataGateway;
import org.libraryexpress.domain.book.valueobject.BookMetadata;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;

public final class FakeBookMetadataGateway implements BookMetadataGateway {

    @Override
    public Optional<BookMetadata> fetch(String title, String author) {
        if (title == null || title.isBlank() || author == null || author.isBlank()) {
            throw new IllegalArgumentException("Title and author parameters cannot be null or empty");
        }

        return Optional.of(new BookMetadata(
                createIsbn(title, author),
                title.trim(),
                author.trim(),
                LocalDate.now().getYear(),
                "Test",
                "Metadata returned by the test gateway."
        ));
    }

    private String createIsbn(String title, String author) {
        int suffix = Math.floorMod((title + author).toLowerCase(Locale.ROOT).hashCode(), 1_000_000_000);
        String isbnBase = "978" + "%09d".formatted(suffix);

        int checksum = 0;
        for (int i = 0; i < isbnBase.length(); i++) {
            int digit = Character.digit(isbnBase.charAt(i), 10);
            checksum += digit * (i % 2 == 0 ? 1 : 3);
        }

        return isbnBase + (10 - checksum % 10) % 10;
    }
}

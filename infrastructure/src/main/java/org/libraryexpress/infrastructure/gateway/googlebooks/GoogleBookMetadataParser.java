package org.libraryexpress.infrastructure.gateway.googlebooks;

import com.fasterxml.jackson.databind.JsonNode;
import org.libraryexpress.domain.book.valueobject.BookMetadata;
import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;
import org.libraryexpress.infrastructure.config.JacksonConfig;

import java.util.Optional;
import java.util.StringJoiner;

final class GoogleBookMetadataParser {

    private static final CustomLogger log = CustomLoggerFactory.getLogger(GoogleBookMetadataParser.class);

    private GoogleBookMetadataParser() {}

    static Optional<BookMetadata> parse(String jsonBody) {
        if (jsonBody == null || jsonBody.isBlank()) return Optional.empty();

        try {
            log.info("Starting BookMetadata parsing...");

            JsonNode root = JacksonConfig.mapper().readTree(jsonBody);
            JsonNode items = root.path("items");

            if (!items.isArray() || items.isEmpty()) return Optional.empty();

            JsonNode firstMatch = items.get(0);
            return Optional.of(mapToMetadata(firstMatch));

        } catch (Exception e) {
            log.warn("Book metadata parser failed: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    private static BookMetadata mapToMetadata(JsonNode item) {
        String isbn = extractIsbn(item);
        String title = extractTitle(item);
        String author = extractAuthor(item);
        int publicationYear = extractYear(item);
        String description = extractDescription(item);

        log.info("BookMetadata parse completed.");

        return new BookMetadata(isbn, title, author, publicationYear, description);
    }

    private static String extractIsbn(JsonNode item) {
        JsonNode industryIdentifiers = item.path("volumeInfo")
                .path("industryIdentifiers");
        String isbn10 = null;

        for (JsonNode identifier : industryIdentifiers) {
            String type = identifier.path("type").asText();

            if ("ISBN_13".equals(type)) {
               return identifier.path("identifier").asText();
            }

            if ("ISBN_10".equals(type)) {
                isbn10 = identifier.path("identifier").asText();
            }
        }

        return isbn10 != null ? isbn10 : "";
    }

    private static String extractTitle(JsonNode item) {
        return item.path("volumeInfo")
                .path("title").asText();
    }

    private static String extractAuthor(JsonNode item) {
        JsonNode authors = item.path("volumeInfo")
                .path("authors");

        if (!authors.isArray() || authors.isEmpty()) return "";

        StringJoiner joiner = new StringJoiner(", ");

        for (JsonNode author : authors) {
            joiner.add(author.asText());
        }

        return joiner.toString();
    }

    private static int extractYear(JsonNode item) {
        String publishedDate = item.path("volumeInfo")
                .path("publishedDate").asText();

        if (publishedDate.isBlank() || publishedDate.length() < 4) return 0;

        try {
            return Integer.parseInt(publishedDate.substring(0, 4));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String extractDescription(JsonNode item) {
        String description = item.path("volumeInfo")
                .path("description").asText();

        if (description.length() <= 500) return description;

        return description.substring(0, 497) + "...";
    }
}

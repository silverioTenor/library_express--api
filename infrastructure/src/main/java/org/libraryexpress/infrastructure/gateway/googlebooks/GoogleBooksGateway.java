package org.libraryexpress.infrastructure.gateway.googlebooks;

import org.libraryexpress.domain.book.valueobject.BookMetadata;
import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;
import org.libraryexpress.infrastructure.api.adapter.CustomHttpClient;
import org.libraryexpress.infrastructure.api.contract.ClientResponse;
import org.libraryexpress.infrastructure.exception.InfrastructureException;
import org.libraryexpress.infrastructure.util.JsonReader;

import java.io.IOException;
import java.util.Optional;

import static org.libraryexpress.infrastructure.exception.InfrastructureErrorType.*;

final class GoogleBooksGateway {

    private static final CustomLogger log = CustomLoggerFactory.getLogger(GoogleBooksGateway.class);

    private final CustomHttpClient customHttpClient;

    public GoogleBooksGateway() {
        customHttpClient = new CustomHttpClient();
    }

    public Optional<BookMetadata> fetch(String titleParam, String authorParam) {
        log.info("Searching in Google Books registry...");

        if ((titleParam == null || titleParam.isBlank()) || (authorParam == null || authorParam.isBlank())) {
            log.error("Search failed. missing required parameters: Title: [{}], Author: [{}]", titleParam, authorParam);
            throw new IllegalArgumentException("Title and author parameters cannot be null or empty");
        }

        String title = titleParam.replace(" ", "+");
        String author = authorParam.replace(" ", "+");

        String url = "https://openlibrary.org/search.json?title="
                + title + "&author=" + author + "&fields=title,author_name,isbn,first_publish_year&limit=1";

        try {
            ClientResponse response = this.customHttpClient.get(url);

            return switch (response.statusCode()) {
                case 200 -> {
                    log.info("External search completed successfully");
                    BookMetadata bookMetadata = JsonReader.read(response.body(), BookMetadata.class);
                    yield Optional.of(bookMetadata);
                }
                case 404 -> {
                    log.warn("The provided information does not match any book in our external registry. Please review the search data or fill in all required fields manually. HTTP response [{}]", response.body());
                    throw new InfrastructureException(RESOURCE_NOT_FOUND, response.body());
                }
                case 500, 502, 504, 503 -> {
                    log.error("External service error during book search. HTTP response: [{}]", response.body());
                    throw new InfrastructureException(SERVICE_UNAVAILABLE, response.body());
                }
                default -> {
                    log.error("Unexpected HTTP response received from external API: [{}]", response.body());
                    throw new IllegalStateException("Unexpected value: " + response.body());
                }
            };

        } catch (IOException | InterruptedException e) {
            log.error("Fatal error during external API communication: [{}]" + e.getMessage(), e);

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            throw new RuntimeException("Failed to fetch book data due to an internal communication error.", e);
        }
    }
}

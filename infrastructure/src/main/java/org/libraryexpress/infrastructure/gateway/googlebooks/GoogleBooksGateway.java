package org.libraryexpress.infrastructure.gateway.googlebooks;

import org.libraryexpress.domain.book.gateway.BookMetadataGateway;
import org.libraryexpress.domain.book.valueobject.BookMetadata;
import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;
import org.libraryexpress.infrastructure.gateway.adapter.CustomHttpClient;
import org.libraryexpress.infrastructure.gateway.contract.ClientResponse;
import org.libraryexpress.infrastructure.config.ConfigRegistry;
import org.libraryexpress.infrastructure.exception.InfrastructureException;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.libraryexpress.infrastructure.exception.InfrastructureErrorType.*;

public final class GoogleBooksGateway implements BookMetadataGateway {

    private static final CustomLogger log = CustomLoggerFactory.getLogger(GoogleBooksGateway.class);

    private final CustomHttpClient customHttpClient;

    public GoogleBooksGateway(CustomHttpClient customHttpClient) {
        this.customHttpClient = customHttpClient;
    }

    public Optional<BookMetadata> fetch(String titleParam, String authorParam) {
        log.info("Searching in External Registry...");

        if ((titleParam == null || titleParam.isBlank()) || (authorParam == null || authorParam.isBlank())) {
            log.error("Search failed. missing required parameters: Title: {}, Author: {}", titleParam, authorParam);
            throw new IllegalArgumentException("Title and author parameters cannot be null or empty");
        }

        String encodedTitle = URLEncoder.encode(titleParam, StandardCharsets.UTF_8);
        String encodedAuthor = URLEncoder.encode(authorParam, StandardCharsets.UTF_8);

        String apiKey = ConfigRegistry.getGoogleBooksApi();

        String secureUrl = "https://www.googleapis.com/books/v1/volumes?q=" +
                encodedTitle + "&inauthor=" + encodedAuthor + "&key=" + apiKey;

        log.error("Google API URL: {}", secureUrl.replace(apiKey, "*****"));

        try {
            ClientResponse response = this.customHttpClient.get(secureUrl);

            return switch (response.statusCode()) {
                case 200 -> {
                    log.info("Search completed.");
                    yield GoogleBookMetadataParser.parse(response.body());
                }
                case 404 -> {
                    log.warn("The provided information does not match any book in our external registry. "
                            + "Please review the search data or fill in all required fields manually. HTTP response [{}]", response.body());
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

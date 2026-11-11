package org.libraryexpress.infrastructure.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.book.valueobject.BookMetadata;
import org.libraryexpress.infrastructure.UnitTest;
import org.libraryexpress.infrastructure.gateway.adapter.CustomHttpClient;
import org.libraryexpress.infrastructure.gateway.googlebooks.GoogleBooksGateway;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@UnitTest
@DisplayName("Book metadata gateway factory")
class BookMetadataGatewayFactoryTest {

    @Test
    @DisplayName("Uses the fake gateway in the test environment without calling the HTTP client")
    void shouldUseFakeGateway_whenAppEnvironmentIsTest() throws Exception {
        CustomHttpClient httpClient = mock(CustomHttpClient.class);

        var gateway = BookMetadataGatewayFactory.create("test", httpClient);
        Optional<BookMetadata> result = gateway.fetch("A test title", "A test author");

        assertInstanceOf(FakeBookMetadataGateway.class, gateway);
        assertTrue(result.orElseThrow().isbn().matches("\\d{13}"));
        assertEquals("A test title", result.orElseThrow().title());
        assertEquals("A test author", result.orElseThrow().author());
        verifyNoInteractions(httpClient);
    }

    @Test
    @DisplayName("Uses the Google Books gateway outside the test environment")
    void shouldUseGoogleBooksGateway_whenAppEnvironmentIsNotTest() {
        var gateway = BookMetadataGatewayFactory.create("production", mock(CustomHttpClient.class));

        assertInstanceOf(GoogleBooksGateway.class, gateway);
    }

    @Test
    @DisplayName("Rejects missing title or author in the fake gateway")
    void shouldRejectMissingSearchParameters_whenUsingFakeGateway() {
        FakeBookMetadataGateway gateway = new FakeBookMetadataGateway();

        assertThrows(IllegalArgumentException.class, () -> gateway.fetch(null, "Author"));
        assertThrows(IllegalArgumentException.class, () -> gateway.fetch("Title", " "));
    }
}

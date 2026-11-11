package org.libraryexpress.infrastructure.gateway.googlebooks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.libraryexpress.domain.book.valueobject.BookMetadata;
import org.libraryexpress.infrastructure.UnitTest;
import org.libraryexpress.infrastructure.exception.InfrastructureErrorType;
import org.libraryexpress.infrastructure.exception.InfrastructureException;
import org.libraryexpress.infrastructure.gateway.adapter.CustomHttpClient;
import org.libraryexpress.infrastructure.gateway.contract.ClientResponse;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@UnitTest
@DisplayName("Google Books gateway")
class GoogleBooksGatewayTest {

    private final CustomHttpClient httpClient = mock(CustomHttpClient.class);
    private final GoogleBooksGateway gateway = new GoogleBooksGateway(httpClient);

    @Test
    @DisplayName("Fetches and parses a matching result")
    void shouldReturnBookMetadata_whenGoogleBooksRespondsSuccessfully() throws Exception {
        when(httpClient.get(anyString())).thenReturn(new ClientResponse(200, """
                {"items":[{"volumeInfo":{
                  "industryIdentifiers":[{"type":"ISBN_13","identifier":"9780132350884"}],
                  "title":"Clean Code",
                  "authors":["Robert C. Martin"],
                  "publishedDate":"2008"
                }}]}
                """));

        Optional<BookMetadata> result = gateway.fetch("Clean Code", "Robert C. Martin");

        assertEquals(Optional.of(new BookMetadata(
                "9780132350884", "Clean Code", "Robert C. Martin", 2008, "", ""
        )), result);
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(httpClient).get(urlCaptor.capture());
        assertTrue(urlCaptor.getValue().contains("q=Clean+Code&inauthor=Robert+C.+Martin&key="));
    }

    @Test
    @DisplayName("Returns an empty result when the successful response contains no matching books")
    void shouldReturnEmpty_whenGoogleBooksHasNoItems() throws Exception {
        when(httpClient.get(anyString())).thenReturn(new ClientResponse(200, "{\"items\":[]}"));

        assertEquals(Optional.empty(), gateway.fetch("Unknown", "Unknown"));
    }

    @Test
    @DisplayName("Rejects missing search parameters without making an HTTP request")
    void shouldRejectMissingParameters_whenFetching() {
        assertThrows(IllegalArgumentException.class, () -> gateway.fetch(null, "Author"));
        assertThrows(IllegalArgumentException.class, () -> gateway.fetch("Title", " "));

        verifyNoInteractions(httpClient);
    }

    @Test
    @DisplayName("Maps a not-found response to a resource-not-found infrastructure error")
    void shouldThrowResourceNotFound_whenGoogleBooksResponds404() throws Exception {
        when(httpClient.get(anyString())).thenReturn(new ClientResponse(404, "not found"));

        InfrastructureException exception = assertThrows(
                InfrastructureException.class,
                () -> gateway.fetch("Missing", "Author")
        );

        assertEquals(InfrastructureErrorType.RESOURCE_NOT_FOUND, exception.getErrorType());
        assertEquals("not found", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {500, 502, 503, 504})
    @DisplayName("Maps upstream server failures to service unavailable")
    void shouldThrowServiceUnavailable_whenGoogleBooksHasServerError(int statusCode) throws Exception {
        when(httpClient.get(anyString())).thenReturn(new ClientResponse(statusCode, "upstream failure"));

        InfrastructureException exception = assertThrows(
                InfrastructureException.class,
                () -> gateway.fetch("Title", "Author")
        );

        assertEquals(InfrastructureErrorType.SERVICE_UNAVAILABLE, exception.getErrorType());
    }

    @Test
    @DisplayName("Wraps communication failures")
    void shouldWrapIOException_whenHttpRequestFails() throws Exception {
        when(httpClient.get(anyString())).thenThrow(new IOException("network unavailable"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> gateway.fetch("Title", "Author")
        );

        assertTrue(exception.getMessage().contains("internal communication error"));
        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    @DisplayName("Restores the interrupted flag when the HTTP request is interrupted")
    void shouldRestoreInterruptFlag_whenHttpRequestIsInterrupted() throws Exception {
        when(httpClient.get(anyString())).thenThrow(new InterruptedException("interrupted"));

        try {
            assertThrows(RuntimeException.class, () -> gateway.fetch("Title", "Author"));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    @DisplayName("Rejects unexpected HTTP responses")
    void shouldThrowIllegalStateException_whenHttpResponseIsUnexpected() throws Exception {
        when(httpClient.get(anyString())).thenReturn(new ClientResponse(401, "unauthorized"));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> gateway.fetch("Title", "Author")
        );

        assertTrue(exception.getMessage().contains("unauthorized"));
    }
}

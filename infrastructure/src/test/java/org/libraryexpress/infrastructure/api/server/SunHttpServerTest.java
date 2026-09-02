package org.libraryexpress.infrastructure.api.server;

import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.infrastructure.UnitTest;
import org.libraryexpress.infrastructure.api.contract.Pagination;
import org.libraryexpress.infrastructure.api.routing.HttpContextRequest;
import org.libraryexpress.infrastructure.api.server.SunHttpServer;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@UnitTest
@DisplayName("SunHttpServer - Unit Test")
class SunHttpServerTest {

    @Test
    @DisplayName("Should extract and decode URL query parameters safely applying UTF-8 standards")
    void shouldExtractAndDecodeQueryParams_whenRequestContainsQueryString() throws Exception {
        // Arrange
        HttpExchange exchange = mock(HttpExchange.class);
        // Simulating a native URI stream containing url-encoded special characters and spaces
        URI simulatedUri = new URI("http://localhost:8080/books?title=Dev+S%C3%AAnior&author=John");
        when(exchange.getRequestURI()).thenReturn(simulatedUri);

        // Act
        HttpContextRequest request = SunHttpServer.adaptRequest(exchange, Map.of());

        // Assert
        assertEquals("Dev Sênior", request.getQueryParam("title"), "The adapter must cleanly decode url parameters in UTF-8 standard");
        assertEquals("John", request.getQueryParam("author"));
    }

    @Test
    @DisplayName("Should enforce safe baseline page request defaults when query parameters are completely absent")
    void shouldEnforceSafePageDefaults_whenParametersAreAbsent() throws Exception {
        // Arrange
        HttpExchange exchange = mock(HttpExchange.class);
        URI simulatedUri = new URI("http://localhost:8080/books");
        when(exchange.getRequestURI()).thenReturn(simulatedUri);

        // Act
        HttpContextRequest request = SunHttpServer.adaptRequest(exchange, Map.of());
        Pagination.PageRequest pageRequest = request.getPageRequest();

        // Assert
        assertNotNull(pageRequest);
        assertEquals(0, pageRequest.page(), "Missing page token must fallback smoothly to page 0");
        assertEquals(10, pageRequest.size(), "Missing size token must fallback smoothly to default size 10");
    }

    @Test
    @DisplayName("Should expose pre-extracted dynamic route parameters maps seamlessly to handlers")
    void shouldExposeRouteParameters_whenInjectedByTheRouterEngine() throws Exception {
        // Arrange
        HttpExchange exchange = mock(HttpExchange.class);
        URI simulatedUri = new URI("http://localhost:8080/books/978-85-333-0227-3");
        when(exchange.getRequestURI()).thenReturn(simulatedUri);

        // Simulating the exact parameter dictionary map injected by the Router's Regex matching edge
        Map<String, String> simulatedRouteParams = Map.of("isbn", "978-85-333-0227-3");

        // Act
        HttpContextRequest request = SunHttpServer.adaptRequest(exchange, simulatedRouteParams);

        // Assert
        assertEquals("978-85-333-0227-3", request.getRouteParam("isbn"), "The handler must fetch path parameters cleanly from the request context");
        assertNull(request.getRouteParam("invalid_key"), "Absent route variables must return null safely");
    }
}

package org.libraryexpress.infrastructure.api.routing;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.infrastructure.UnitTest;
import org.libraryexpress.infrastructure.api.enums.HttpStatusCode;
import org.libraryexpress.infrastructure.api.enums.HttpVerb;
import org.libraryexpress.infrastructure.api.routing.Router;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@UnitTest
@DisplayName("Router - Unit Test")
class RouterTest {

    @Test
    @DisplayName("Should successfully dispatch a request to the correct static route handler")
    void shouldInvokeCorrectHandler_whenStaticRouteMatches() throws Exception {
        // Arrange
        Router router = new Router();
        AtomicBoolean handlerWasInvoked = new AtomicBoolean(false);

        // Registering a clean lambda static test route
        router.register(HttpVerb.GET, "/books", (req, res) -> handlerWasInvoked.set(true));

        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("http://localhost:8080/books"));

        // Mocking native bodies and streaming hooks to avoid side effects crashes on SunHttpServer conversion boundaries
        when(exchange.getRequestBody()).thenReturn(mock(java.io.InputStream.class));
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());
        when(exchange.getResponseHeaders()).thenReturn(new Headers());

        // Act
        router.handle(exchange);

        // Assert
        assertTrue(handlerWasInvoked.get(), "The static resource handler must be executed when path matches exactly");
    }

    @Test
    @DisplayName("Should process parametric routes and forward parsed parameters to the handler context")
    void shouldExtractRouteParams_whenParametricPathMatchesPattern() throws Exception {
        // Arrange
        Router router = new Router();
        AtomicBoolean handlerWasInvoked = new AtomicBoolean(false);

        // Registering a dynamic path matching pattern syntax framework
        router.register(HttpVerb.GET, "/books/{isbn}", (req, res) -> {
            handlerWasInvoked.set(true);
            // Verification hook: Check if the inner path extraction matching behaves precisely
            assertEquals("978-85-333-0227-3", req.getRouteParam("isbn"), "Path param values must match the incoming path segments");
        });

        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("http://localhost:8080/books/978-85-333-0227-3"));

        when(exchange.getRequestBody()).thenReturn(mock(java.io.InputStream.class));
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());
        when(exchange.getResponseHeaders()).thenReturn(new Headers());

        // Act
        router.handle(exchange);

        // Assert
        assertTrue(handlerWasInvoked.get(), "The parametric handler must be triggered by custom dynamic path entries matches");
    }

    @Test
    @DisplayName("Should return a structured 404 response when path pattern does not match any entry")
    void shouldReturn404_whenNoRouteMatchesIncomingContext() throws Exception {
        // Arrange
        Router router = new Router();
        router.register(HttpVerb.POST, "/books", (req, res) -> {});

        HttpExchange exchange = mock(HttpExchange.class);
        // Simulating an unmatched path routing scenario (GET request to a route that only expects POST)
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("http://localhost:8080/books"));

        Headers headers = new Headers();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(exchange.getResponseHeaders()).thenReturn(headers);
        when(exchange.getResponseBody()).thenReturn(outputStream);

        // Act
        router.handle(exchange);

        // Assert
        // Verify if the router executed the native socket headers mapping contracts safely
        verify(exchange, times(1)).sendResponseHeaders(eq(HttpStatusCode.NOT_FOUND.getCode()), anyLong());
        assertEquals("application/json", headers.getFirst("Content-Type"));

        String responseBody = outputStream.toString();
        assertTrue(responseBody.contains("Resource path or method execution context not found."), "Response must contain structural details");
    }
}

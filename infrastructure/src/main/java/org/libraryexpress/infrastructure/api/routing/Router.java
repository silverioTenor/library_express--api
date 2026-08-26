package org.libraryexpress.infrastructure.api.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.libraryexpress.infrastructure.api.adapter.SunHttpServer;
import org.libraryexpress.infrastructure.api.contract.ErrorResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public final class Router implements HttpHandler {

    private final Map<RouteKey, CustomHttpHandler> routes = new HashMap<>();

    private final ObjectMapper mapper;

    public Router() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    public void register(String method, String path, CustomHttpHandler handler) {
        routes.put(new RouteKey(method.toUpperCase(), path), handler);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        String path = exchange.getRequestURI().getPath();

        RouteKey targetKey = new RouteKey(method, path);
        CustomHttpHandler handler = routes.get(targetKey);

        if (handler == null) {
            sendError(exchange, 404, "Resource path or method execution context not found.");
            return;
        }

        try {
            HttpContextRequest request = SunHttpServer.adaptRequest(exchange, mapper);
            HttpContextResponse response = SunHttpServer.adaptResponse(exchange, mapper);

            handler.handle(request, response);
        } catch (Exception e) {
            sendError(exchange, 500, "Internal Server execution failure: " + e.getMessage());
        }
    }

    private void sendError(HttpExchange exchange, int status, String message) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");

        byte[] payload = mapper.writeValueAsBytes(ErrorResponse.of(status, message));

        exchange.sendResponseHeaders(status, payload.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(payload);
        }
    }

    public record RouteKey(String method, String path) {}
}

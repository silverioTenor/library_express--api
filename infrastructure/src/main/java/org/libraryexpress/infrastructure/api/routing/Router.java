package org.libraryexpress.infrastructure.api.routing;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.libraryexpress.infrastructure.api.adapter.SunHttpServer;
import org.libraryexpress.infrastructure.api.contract.ErrorResponse;
import org.libraryexpress.infrastructure.api.enums.HttpStatusCode;
import org.libraryexpress.infrastructure.api.enums.HttpVerb;
import org.libraryexpress.infrastructure.util.JsonPrinter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Router implements HttpHandler {

    private final List<RouteEntry> routes = new ArrayList<>();

    public Router() {}

    public void register(HttpVerb method, String path, CustomHttpHandler handler) {
        List<String> parameterNames = new ArrayList<>();
        Matcher parameterMatcher = Pattern.compile("\\{([^}]+)\\}").matcher(path);

        while (parameterMatcher.find()) {
            parameterNames.add(parameterMatcher.group(1));
        }

        String regexPattern = path.replaceAll("\\{[^}Setup]+\\}", "([^/]+)");

        if (!regexPattern.startsWith("^")) regexPattern = "^" + regexPattern;
        if (!regexPattern.endsWith("$")) regexPattern += "$";

        Pattern pattern = Pattern.compile(regexPattern);

        CustomHttpHandler customHttpHandler = new GlobalExceptionHandler(handler);

        routes.add(new RouteEntry(method.getVerb(), pattern, parameterNames, customHttpHandler));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        String path = exchange.getRequestURI().getPath();

        RouteMatch activeMatch = findMatchedRoute(method, path);

        // Java 21 Feature: Advanced Pattern Matching Switch evaluating custom records and destructuring them in-line
        switch (activeMatch) {
            case RouteMatch.Found(RouteEntry entry, Map<String, String> routeParams) -> {

                HttpContextRequest request = SunHttpServer.adaptRequest(exchange, routeParams);
                HttpContextResponse response = SunHttpServer.adaptResponse(exchange);

                try {
                    entry.handler().handle(request, response);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            case RouteMatch.NotFound e ->
                    sendError(exchange, HttpStatusCode.NOT_FOUND, "Resource path or method execution context not found.");
        }
    }

    private RouteMatch findMatchedRoute(String method, String path) {
        for (RouteEntry entry : routes) {
            if (entry.method().equals(method)) {
                Matcher matcher = entry.pattern().matcher(path);

                if (matcher.matches()) {
                    Map<String, String> extractedParams = new HashMap<>();
                    for (int i = 0; i < entry.parameterNames().size(); i++) {
                        extractedParams.put(entry.parameterNames().get(i), matcher.group(i + 1));
                    }
                    return new RouteMatch.Found(entry, Map.copyOf(extractedParams));
                }
            }
        }
        return new RouteMatch.NotFound();
    }

    private void sendError(HttpExchange exchange, HttpStatusCode status, String message) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");

        byte[] payload = JsonPrinter.printBytes(ErrorResponse.of(status.getCode(), message));

        exchange.sendResponseHeaders(status.getCode(), payload.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(payload);
        }
    }

    private record RouteEntry(
            String method,
            Pattern pattern,
            List<String> parameterNames,
            CustomHttpHandler handler
    ) {}

    /**
     * Java 21 Sealed Interface governing strict polymorphic state match tokens.
     */
    private sealed interface RouteMatch permits RouteMatch.Found, RouteMatch.NotFound {
        record Found(RouteEntry entry, Map<String, String> params) implements RouteMatch {}
        record NotFound() implements RouteMatch {}
    }
}

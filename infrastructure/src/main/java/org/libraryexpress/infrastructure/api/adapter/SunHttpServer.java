package org.libraryexpress.infrastructure.api.adapter;

import com.sun.net.httpserver.HttpExchange;
import org.libraryexpress.infrastructure.api.enums.HttpStatusCode;
import org.libraryexpress.infrastructure.api.routing.HttpContextRequest;
import org.libraryexpress.infrastructure.api.routing.HttpContextResponse;
import org.libraryexpress.infrastructure.util.JsonPrinter;
import org.libraryexpress.infrastructure.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.libraryexpress.infrastructure.api.contract.Pagination.*;

public final class SunHttpServer {

    private SunHttpServer() {}

    public static HttpContextRequest adaptRequest(HttpExchange exchange, Map<String, String> routParams) {
        return new HttpContextRequest() {

            private Map<String, String> queryParams;

            @Override
            public <T> T parseBody(Class<T> targetClass) throws IOException {
                try (InputStream is = exchange.getRequestBody()) {
                    return  JsonReader.read(is, targetClass);
                }
            }

            @Override
            public String getQueryParam(String key) {
                return getQueryParams().get(key);
            }

            @Override
            public String getRouteParam(String key) {
                return routParams != null ? routParams.get(key) : null;
            }

            @Override
            public PageRequest getPageRequest() {
                Map<String, String> params = getQueryParams();

                int page = params.containsKey("page") ? Integer.parseInt(params.get("page")) : 0;
                int size = params.containsKey("limit") ? Integer.parseInt(params.get("limit")) : 10;

                return PageRequest.of(page, size);
            }

            @Override
            public String getPath() {
                return exchange.getRequestURI().getPath();
            }

            private Map<String, String> getQueryParams() {
                if (queryParams != null) return queryParams;

                queryParams = new HashMap<>();
                String query = exchange.getRequestURI().getRawQuery();

                if (query == null || query.isEmpty()) return queryParams;

                for (String pair : query.split("&")) {
                    String[] entries = pair.split("=");

                    if (entries.length > 0) {
                        String  key = URLDecoder.decode(entries[0], StandardCharsets.UTF_8);
                        String  value = entries.length > 1 ? URLDecoder.decode(entries[1], StandardCharsets.UTF_8) : "";

                        queryParams.put(key, value);
                    }
                }
                return queryParams;
            }

        };
    }

    public static HttpContextResponse adaptResponse(HttpExchange exchange) {

        return new HttpContextResponse() {

            private int currentStatus = 200;

            @Override
            public HttpContextResponse status(HttpStatusCode statusCode) {
                this.currentStatus = statusCode.getCode();
                return this;
            }

            @Override
            public void json(Object body) throws IOException {
                exchange.getResponseHeaders().set("Content-Type", "application/json");

                byte[] bytes = JsonPrinter.printBytes(body);

                exchange.sendResponseHeaders(currentStatus, bytes.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }

            @Override
            public void sendEmpty() throws IOException {
                exchange.sendResponseHeaders(currentStatus, -1);
            }

        };

    }
}

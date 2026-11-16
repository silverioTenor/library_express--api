package org.libraryexpress.infrastructure.gateway.adapter;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.infrastructure.UnitTest;
import org.libraryexpress.infrastructure.gateway.contract.ClientResponse;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@DisplayName("Native HTTP client")
class NativeHttpClientTest {

    @Test
    @DisplayName("Returns the HTTP status and body from a GET response")
    void shouldReturnResponse_whenServerResponds() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        byte[] responseBody = "book metadata".getBytes(StandardCharsets.UTF_8);
        server.createContext("/books", exchange -> {
            exchange.sendResponseHeaders(200, responseBody.length);
            exchange.getResponseBody().write(responseBody);
            exchange.close();
        });
        server.start();

        try {
            ClientResponse response = new NativeHttpClient().get(
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/books"
            );

            assertEquals(200, response.statusCode());
            assertEquals("book metadata", response.body());
        } finally {
            server.stop(0);
        }
    }
}

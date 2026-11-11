package org.libraryexpress.infrastructure.gateway.adapter;

import org.libraryexpress.infrastructure.gateway.contract.ClientResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;

public class NativeHttpClient implements CustomHttpClient {
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3)) // To fail-fast use 2s
            .build();

    public ClientResponse get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5)) // To fail-fast use 3s
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return new ClientResponse(response.statusCode(), response.body());
    }
}

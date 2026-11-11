package org.libraryexpress.infrastructure.gateway;

import org.libraryexpress.domain.book.gateway.BookMetadataGateway;
import org.libraryexpress.infrastructure.gateway.adapter.CustomHttpClient;
import org.libraryexpress.infrastructure.gateway.googlebooks.GoogleBooksGateway;

public final class BookMetadataGatewayFactory {

    private BookMetadataGatewayFactory() {}

    public static BookMetadataGateway create(String appEnv, CustomHttpClient httpClient) {
        if ("test".equalsIgnoreCase(appEnv == null ? "" : appEnv.trim())) {
            return new FakeBookMetadataGateway();
        }
        return new GoogleBooksGateway(httpClient);
    }
}

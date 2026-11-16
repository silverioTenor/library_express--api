package org.libraryexpress.infrastructure.gateway.contract;

public record ClientResponse(int statusCode, String body) {
}

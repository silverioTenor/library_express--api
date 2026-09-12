package org.libraryexpress.infrastructure.api.routing;

@FunctionalInterface
public interface CustomHttpHandler {
    void handle(HttpContextRequest request, HttpContextResponse response) throws Exception;
}

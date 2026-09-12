package org.libraryexpress.infrastructure.api.routing;

@FunctionalInterface
public interface HttpFilter {
    void doFilter(HttpContextRequest  request, HttpContextResponse response, CustomHttpHandler next) throws Exception;
}

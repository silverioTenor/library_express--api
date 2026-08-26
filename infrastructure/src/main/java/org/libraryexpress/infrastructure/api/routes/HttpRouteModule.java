package org.libraryexpress.infrastructure.api.routes;

import org.libraryexpress.infrastructure.api.routing.Router;

/**
 * Architectural contract for isolated resource route registration blocks.
 */
public interface HttpRouteModule {
    void register(Router router);
}

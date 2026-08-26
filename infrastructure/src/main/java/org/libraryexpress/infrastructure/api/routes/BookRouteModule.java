package org.libraryexpress.infrastructure.api.routes;

import org.libraryexpress.infrastructure.api.routing.Router;
import org.libraryexpress.infrastructure.config.AppContext;

import java.util.Map;

public final class BookRouteModule implements HttpRouteModule {

    private final AppContext context;

    public BookRouteModule(AppContext context) {
        this.context = context;
    }

    @Override
    public void register(Router router) {
        router.register("GET", "/books", (request, response) -> {
            response.status(200).json(Map.of("message", "Book List"));
        });
    }
}

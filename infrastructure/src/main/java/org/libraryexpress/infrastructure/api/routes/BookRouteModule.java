package org.libraryexpress.infrastructure.api.routes;

import org.libraryexpress.infrastructure.api.controller.BookController;
import org.libraryexpress.infrastructure.api.routing.Router;
import org.libraryexpress.infrastructure.config.AppContext;

import static org.libraryexpress.infrastructure.api.enums.HttpVerb.*;

public final class BookRouteModule implements HttpRouteModule {

    private final BookController controller;

    public BookRouteModule(AppContext context) {
        this.controller = context.getBookController();
    }

    @Override
    public void register(Router router) {
        router.register(POST, "/books", controller::register);
        router.register(GET, "/books/{isbn}", controller::get);
        router.register(GET, "/books", controller::list);
    }
}

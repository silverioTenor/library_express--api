package org.libraryexpress.infrastructure.api.routes;

import org.libraryexpress.infrastructure.api.controller.CustomerController;
import org.libraryexpress.infrastructure.api.routing.Router;
import org.libraryexpress.infrastructure.config.AppContext;

import static org.libraryexpress.infrastructure.api.enums.HttpVerb.*;

public class CustomerRouteModule implements HttpRouteModule {

    private final CustomerController controller;

    public CustomerRouteModule(AppContext context) {
        this.controller = context.getCustomerController();
    }

    @Override
    public void register(Router router) {
        router.register(POST, "/customers", controller::create);
        router.register(GET, "/customers/search", controller::get);
        router.register(PATCH, "/customers/{id}/update-email", controller::updateEmail);
        router.register(GET, "/customers", controller::list);
    }
}

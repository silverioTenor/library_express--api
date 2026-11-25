package org.libraryexpress.infrastructure.api.routes;

import static org.libraryexpress.infrastructure.api.enums.HttpVerb.*;

import org.libraryexpress.infrastructure.api.controller.CustomerController;
import org.libraryexpress.infrastructure.api.routing.Router;

public class CustomerRouteModule implements HttpRouteModule {

    private final CustomerController controller;

    public CustomerRouteModule(CustomerController controller) {
        this.controller = controller;
    }

    @Override
    public void register(Router router) {
        router.register(POST, "/customers", controller::create);
        router.register(PATCH, "/customers/{id}/update-email", controller::updateEmail);
        router.register(GET, "/customers", controller::search);
    }
}

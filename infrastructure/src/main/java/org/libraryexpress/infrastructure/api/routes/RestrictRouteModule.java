package org.libraryexpress.infrastructure.api.routes;

import static org.libraryexpress.infrastructure.api.enums.HttpVerb.POST;

import org.libraryexpress.infrastructure.api.controller.RestrictController;
import org.libraryexpress.infrastructure.api.guard.CreateSuperAdminGuard;
import org.libraryexpress.infrastructure.api.routing.Router;

public class RestrictRouteModule implements HttpRouteModule {

    private final RestrictController controller;
    private final CreateSuperAdminGuard createSuperAdminGuard;

    public RestrictRouteModule(RestrictController controller, CreateSuperAdminGuard createSuperAdminGuard) {
        this.controller = controller;
        this.createSuperAdminGuard = createSuperAdminGuard;
    }

    @Override
    public void register(Router router) {
        router.register(POST, "/setup/super-admin", controller::createSuperAdmin, createSuperAdminGuard);
    }
}

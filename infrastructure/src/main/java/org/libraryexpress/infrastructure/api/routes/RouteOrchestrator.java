package org.libraryexpress.infrastructure.api.routes;

import java.util.List;
import org.libraryexpress.infrastructure.api.routing.Router;
import org.libraryexpress.infrastructure.config.AppContext;

/** Infrastructure composite pattern orchestrator collecting and firing search modular resource routes. */
public final class RouteOrchestrator {

    private final List<HttpRouteModule> modules;

    public RouteOrchestrator(AppContext context) {
        this.modules = List.of(
                new BookRouteModule(context),
                new CustomerRouteModule(context),
                new LoanRouteModule(context),
                new DocsRouteModule(context));
    }

    public void bindAll(Router router) {
        modules.forEach(module -> module.register(router));
    }
}

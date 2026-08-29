package org.libraryexpress.infrastructure.api.routes;

import org.libraryexpress.infrastructure.api.routing.Router;
import org.libraryexpress.infrastructure.config.AppContext;

import java.util.List;

/**
 * Infrastructure composite pattern orchestrator collecting and firing findAll modular resource routes.
 */
public final class RouteOrchestrator {

    private final List<HttpRouteModule> modules;

    public RouteOrchestrator(AppContext context) {
        this.modules = List.of(
                new BookRouteModule(context),
                new CustomerRouteModule(context)
        );
    }

    public void bindAll(Router router) {
        modules.forEach(module -> module.register(router));
    }
}

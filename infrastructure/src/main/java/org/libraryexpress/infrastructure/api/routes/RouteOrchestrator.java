package org.libraryexpress.infrastructure.api.routes;

import java.util.List;
import org.libraryexpress.infrastructure.api.routing.Router;

/** Infrastructure composite pattern orchestrator collecting and firing search modular resource routes. */
public final class RouteOrchestrator {

    private final List<HttpRouteModule> modules;

    public RouteOrchestrator(
            BookRouteModule bookRouter,
            CustomerRouteModule customerRouter,
            LoanRouteModule loanRouter,
            DocsRouteModule docsRouter) {
        this.modules = List.of(bookRouter, customerRouter, loanRouter, docsRouter);
    }

    public void bindAll(Router router) {
        modules.forEach(module -> module.register(router));
    }
}

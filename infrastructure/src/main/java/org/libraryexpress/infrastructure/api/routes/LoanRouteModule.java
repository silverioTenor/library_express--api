package org.libraryexpress.infrastructure.api.routes;

import org.libraryexpress.infrastructure.api.controller.LoanController;
import org.libraryexpress.infrastructure.api.routing.Router;
import org.libraryexpress.infrastructure.config.AppContext;

import static org.libraryexpress.infrastructure.api.enums.HttpVerb.*;

public class LoanRouteModule implements HttpRouteModule {

    private final LoanController controller;

    public LoanRouteModule(AppContext context) {
        this.controller = context.getLoanController();
    }

    @Override
    public void register(Router router) {
        router.register(POST, "/loans", controller::create);
        router.register(GET, "/loans", controller::search);
        router.register(POST, "/loans/{loanId}/returns", controller::returnLoan);
        router.register(PATCH, "/loans/{loanId}/close-overdue", controller::closeOverdueLoan);
    }
}

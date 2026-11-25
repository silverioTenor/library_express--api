package org.libraryexpress.infrastructure.api.routes;

import static org.libraryexpress.infrastructure.api.enums.HttpVerb.GET;

import org.libraryexpress.infrastructure.api.routing.Router;
import org.libraryexpress.infrastructure.api.routing.openApi.OpenApiContractHandler;
import org.libraryexpress.infrastructure.api.routing.openApi.SwaggerUiHandler;

public final class DocsRouteModule implements HttpRouteModule {

    private final OpenApiContractHandler contractHandler;
    private final SwaggerUiHandler swaggerUiHandler;

    public DocsRouteModule(OpenApiContractHandler contractHandler, SwaggerUiHandler swaggerUiHandler) {
        this.contractHandler = contractHandler;
        this.swaggerUiHandler = swaggerUiHandler;
    }

    @Override
    public void register(Router router) {
        router.register(GET, "/openapi.json", contractHandler);
        router.register(GET, "/docs", swaggerUiHandler);
        router.register(GET, "/docs/{file}", swaggerUiHandler);
    }
}

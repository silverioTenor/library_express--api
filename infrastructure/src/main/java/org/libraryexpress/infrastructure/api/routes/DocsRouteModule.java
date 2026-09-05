package org.libraryexpress.infrastructure.api.routes;

import org.libraryexpress.infrastructure.api.routing.Router;
import org.libraryexpress.infrastructure.api.routing.openApi.OpenApiContractHandler;
import org.libraryexpress.infrastructure.api.routing.openApi.SwaggerUiHandler;
import org.libraryexpress.infrastructure.config.AppContext;

import static org.libraryexpress.infrastructure.api.enums.HttpVerb.GET;

public final class DocsRouteModule implements HttpRouteModule {

    private final OpenApiContractHandler contractHandler;
    private final SwaggerUiHandler swaggerUiHandler;

    public DocsRouteModule(AppContext context) {
        contractHandler = context.getOpenApiContractHandler();
        swaggerUiHandler = context.getSwaggerUiHandler();
    }

    @Override
    public void register(Router router) {
        router.register(GET, "/openapi.json", contractHandler);
        router.register(GET, "/docs", swaggerUiHandler);
        router.register(GET, "/docs/{file}", swaggerUiHandler);
    }
}

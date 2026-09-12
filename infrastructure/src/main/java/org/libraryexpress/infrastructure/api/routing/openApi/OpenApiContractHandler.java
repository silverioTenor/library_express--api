package org.libraryexpress.infrastructure.api.routing.openApi;

import org.libraryexpress.infrastructure.api.routing.CustomHttpHandler;
import org.libraryexpress.infrastructure.api.routing.HttpContextRequest;
import org.libraryexpress.infrastructure.api.routing.HttpContextResponse;

import java.io.InputStream;

import static org.libraryexpress.infrastructure.api.enums.HttpStatusCode.*;

public final class OpenApiContractHandler implements CustomHttpHandler {

    private static final String CONTRACT_PATH = "/generated-openapi/openapi.json";

    @Override
    public void handle(HttpContextRequest request, HttpContextResponse response) throws Exception {
        try (InputStream is = getClass().getResourceAsStream(CONTRACT_PATH)) {
            if (is == null) {
                response.status(NOT_FOUND).sendEmpty();
                return;
            }
            response.setHeader("Content-Type", "application/json");
            response.status(SUCCESS).raw(is.readAllBytes());
        }
    }
}

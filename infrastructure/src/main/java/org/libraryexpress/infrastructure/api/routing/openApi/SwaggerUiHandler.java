package org.libraryexpress.infrastructure.api.routing.openApi;

import org.libraryexpress.infrastructure.api.routing.CustomHttpHandler;
import org.libraryexpress.infrastructure.api.routing.HttpContextRequest;
import org.libraryexpress.infrastructure.api.routing.HttpContextResponse;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.libraryexpress.infrastructure.api.enums.HttpStatusCode.*;

public final class SwaggerUiHandler implements CustomHttpHandler {

    private static final String WEBJAR_VERSION = "5.17.14";
    private static final String WEBJAR_BASE = "/META-INF/resources/webjars/swagger-ui/" + WEBJAR_VERSION + "/";

    // Força toda resolução de caminho relativo (./swagger-ui.css, ./swagger-ui-bundle.js etc.)
    // a ser feita a partir de /docs/, independente de o navegador estar em /docs ou /docs/.
    private static final String BASE_TAG = "<base href=\"/docs/\">";

    // O webjar traz um swagger-initializer.js apontando pro petstore de exemplo.
    // Interceptamos esse arquivo específico e servimos nossa própria versão,
    // apontando para o contrato real gerado no passo anterior.
    private static final String CUSTOM_INITIALIZER = """
        window.onload = function() {
          window.ui = SwaggerUIBundle({
            url: "/openapi.json",
            dom_id: "#swagger-ui",
            presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],
            layout: "StandaloneLayout"
          });
        };
        """;

    @Override
    public void handle(HttpContextRequest request, HttpContextResponse response) throws Exception {
        String file = request.getRouteParam("file");
        if (file == null || file.isBlank()) file = "index.html";

        if (file.equals("swagger-initializer.js")) {
            response.setHeader("Content-Type", "application/javascript");
            response.status(SUCCESS).raw(CUSTOM_INITIALIZER.getBytes(StandardCharsets.UTF_8));
            return;
        }

        try (InputStream is = getClass().getResourceAsStream(WEBJAR_BASE + file)) {
            if (is == null) {
                response.status(NOT_FOUND).sendEmpty();
                return;
            }

            byte[] bytes = is.readAllBytes();

            if (file.equals("index.html")) {
                String html = new String(bytes, StandardCharsets.UTF_8)
                        .replaceFirst("<head>", "<head>" + BASE_TAG);
                bytes = html.getBytes(StandardCharsets.UTF_8);
            }

            response.setHeader("Content-Type", contentTypeFor(file));
            response.status(SUCCESS).raw(bytes);
        }
    }

    private String contentTypeFor(String file) {
        if (file.endsWith(".html")) return "text/html";
        if (file.endsWith(".css")) return "text/css";
        if (file.endsWith(".js")) return "application/javascript";
        if (file.endsWith(".png")) return "image/png";
        return "application/octet-stream";
    }
}

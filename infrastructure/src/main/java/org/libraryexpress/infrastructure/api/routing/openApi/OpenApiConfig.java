package org.libraryexpress.infrastructure.api.routing.openApi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * Anchor class for global OpenAPI metadata (info, servers).
 * Holds no endpoints — exists only so the swagger-jaxrs2 reader has a place
 * to read top-level document metadata during the build-time scan.
 */
@OpenAPIDefinition(
        info = @Info(
                title = "Library Express API",
                version = "0.0.0-PLACEHOLDER",
                description = "REST API for the Library Express book/customer/loan management system.",
                contact = @Contact(name = "GitHub", url = "https://github.com/silverioTenor/library_express--api"),
                license = @License(name = "MIT", url = "https://opensource.org/licenses/MIT")
        ),
        servers = {
                @Server(url = "http://localhost:4000", description = "Local development server")
        }
)
public final class OpenApiConfig {

    private OpenApiConfig() {}
}

package org.libraryexpress.infrastructure.api.routing;

import org.libraryexpress.infrastructure.api.enums.HttpStatusCode;

import java.io.IOException;

public interface HttpContextResponse {
    HttpContextResponse status(HttpStatusCode statusCode);
    void json(Object body) throws IOException;
    void sendEmpty() throws IOException;
}

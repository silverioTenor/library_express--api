package org.libraryexpress.infrastructure.api.routing;

import java.io.IOException;

public interface HttpContextResponse {
    HttpContextResponse status(int statusCode);
    void json(Object body) throws IOException;
    void sendEmpty(Object body) throws IOException;
}

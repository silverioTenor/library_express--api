package org.libraryexpress.infrastructure.api.routing;

import static org.libraryexpress.infrastructure.api.contract.Pagination.*;

import java.io.IOException;

public interface HttpContextRequest {
    <T> T parseBody(Class<T> targetClass) throws IOException;

    String getQueryParam(String key);

    String getRouteParam(String key);

    PageRequest getPageRequest();

    String getPath();

    String getHeader(String key);

    String getMethod();
}

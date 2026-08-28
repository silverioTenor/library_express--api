package org.libraryexpress.infrastructure.api.routing;

import java.io.IOException;

import static org.libraryexpress.infrastructure.api.contract.Pagination.*;

public interface HttpContextRequest {
    <T> T parseBody(Class<T> targetClass) throws IOException;
    String getQueryParam(String key);
    String getRouteParam(String key);
    PageRequest getPageRequest();
    String getPath();
}

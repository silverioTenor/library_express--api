package org.libraryexpress.infrastructure.api.guard;

import org.libraryexpress.infrastructure.api.routing.HttpContextRequest;

@FunctionalInterface
public interface Guard {
    void check(HttpContextRequest request);
}

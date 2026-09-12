package org.libraryexpress.domain.core.repository;

import java.util.Set;

public record QueryResult<T>(Set<T> items, long total) {
}

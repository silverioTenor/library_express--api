package org.libraryexpress.domain.core.repository;

import java.util.List;

public record QueryResult<T>(List<T> items, long total) {
}

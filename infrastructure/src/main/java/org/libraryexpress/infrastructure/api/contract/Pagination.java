package org.libraryexpress.infrastructure.api.contract;

import java.util.List;

public class Pagination {

    private Pagination() {}

    public record PageRequest(int page, int size) {

        public static PageRequest of(int page, int size) {
            int resolvedPage = Math.max(page, 0);
            int resolvedSize = size <= 0 ? 20 : size;

            return new PageRequest(resolvedPage, resolvedSize);
        }

    }

    public record PageResponse<T>(
            List<T> items,
            int page,
            int size,
            long total
    ) {}
}

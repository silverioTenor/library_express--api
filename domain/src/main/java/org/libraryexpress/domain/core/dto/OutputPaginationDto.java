package org.libraryexpress.domain.core.dto;

import java.util.List;

public record OutputPaginationDto<T>(
        List<T> items,
        int page,
        int limit,
        int totalPages,
        long total
) {
    public static <T> OutputPaginationDto<T> unpaginated(List<T> items) {
        return new OutputPaginationDto<T>(items, 0, items.size(), 1, items.size());
    }
}

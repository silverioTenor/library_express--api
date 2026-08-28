package org.libraryexpress.application.core.dto;

import java.util.Set;

public record OutputPaginationDto<T>(
        Set<T> items,
        int page,
        int size,
        long total
) {
    public static <T> OutputPaginationDto<T> unpaginated(Set<T> items) {
        return new OutputPaginationDto<T>(items, 0, items.size(), items.size());
    }
}

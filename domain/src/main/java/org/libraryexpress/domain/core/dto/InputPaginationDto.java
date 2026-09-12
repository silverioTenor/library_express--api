package org.libraryexpress.domain.core.dto;

public record InputPaginationDto(Integer page, Integer limit) {

    public static InputPaginationDto of(Integer page, Integer size) {
        return new InputPaginationDto(page, size);
    }

    public boolean isPaginated() {
        return (page != null && page > 0) && limit != null && limit > 0;
    }

    public int offset() {
        return isPaginated() ? (page - 1) * limit : 0;
    }
}

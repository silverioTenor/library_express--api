package org.libraryexpress.application.core.dto;

public record InputPaginationDto(Integer page, Integer size) {

    public static InputPaginationDto of(Integer page, Integer size) {
        return new InputPaginationDto(page, size);
    }

    public boolean isPaginated() {
        return page != null && size != null;
    }

    public int offset() {
        return isPaginated() ? page * size : 0;
    }
}

package org.libraryexpress.domain.customer.repository;

import org.libraryexpress.domain.core.dto.InputPaginationDto;

public record CustomerFilter(String id, String name, String email, InputPaginationDto paginationDto) {
}

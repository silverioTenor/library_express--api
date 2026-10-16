package org.libraryexpress.application.customer.dto.request;

import org.libraryexpress.domain.core.dto.InputPaginationDto;
import org.libraryexpress.domain.customer.repository.CustomerFilter;

public record CustomerFilterDTO(String id, String name, String email, InputPaginationDto paginationDto) {
    public CustomerFilterDTO(String id, String name, String email, int page, int size) {
        this(id, name, email, new InputPaginationDto(page, size));
    }

    public CustomerFilter toCustomerFilter() {
        return new CustomerFilter(id, name, email, paginationDto);
    }
}

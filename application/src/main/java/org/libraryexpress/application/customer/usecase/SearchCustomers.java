package org.libraryexpress.application.customer.usecase;

import org.libraryexpress.application.customer.dto.response.CustomerDto;
import org.libraryexpress.application.customer.mapper.CustomerMapper;
import org.libraryexpress.domain.core.dto.InputPaginationDto;
import org.libraryexpress.domain.core.dto.OutputPaginationDto;
import org.libraryexpress.domain.customer.repository.CustomerRepository;

import java.util.List;

public class SearchCustomers {

    private final CustomerRepository customerRepository;
    private final CustomerMapper mapper;

    public SearchCustomers(CustomerRepository customerRepository, CustomerMapper mapper) {
        this.customerRepository = customerRepository;
        this.mapper = mapper;
    }

    public OutputPaginationDto<CustomerDto> execute(String name, String email, InputPaginationDto paginationDto) {
        var result = this.customerRepository.search(name, email, paginationDto);

        List<CustomerDto> customersDto = mapper.toResponseListDto(result.items());

        if  (paginationDto == null || !paginationDto.isPaginated()) {
            return OutputPaginationDto.unpaginated(customersDto);
        }

        int totalPages = Math.toIntExact(result.total() / paginationDto.limit());

        return new OutputPaginationDto<>(
                customersDto,
                paginationDto.page(),
                paginationDto.limit(),
                totalPages,
                result.total()
        );
    }
}

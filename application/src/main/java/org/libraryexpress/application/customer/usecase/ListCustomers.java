package org.libraryexpress.application.customer.usecase;

import org.libraryexpress.application.customer.dto.response.CustomerDto;
import org.libraryexpress.application.customer.mapper.CustomerMapper;
import org.libraryexpress.domain.core.dto.InputPaginationDto;
import org.libraryexpress.domain.core.dto.OutputPaginationDto;
import org.libraryexpress.domain.customer.repository.CustomerRepository;

import java.util.Set;

public class ListCustomers {

    private final CustomerRepository customerRepository;
    private final CustomerMapper mapper;

    public ListCustomers(CustomerRepository customerRepository, CustomerMapper mapper) {
        this.customerRepository = customerRepository;
        this.mapper = mapper;
    }

    public OutputPaginationDto<CustomerDto> execute(InputPaginationDto paginationDto) {
        var result = this.customerRepository.findAll(paginationDto);

        Set<CustomerDto> customersDto = mapper.toResponseListDto(result.items());

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

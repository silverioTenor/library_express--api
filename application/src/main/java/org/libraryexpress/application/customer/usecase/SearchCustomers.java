package org.libraryexpress.application.customer.usecase;

import org.libraryexpress.application.customer.dto.request.CustomerFilterDTO;
import org.libraryexpress.application.customer.dto.response.CustomerDTO;
import org.libraryexpress.application.customer.mapper.CustomerMapper;
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

    public OutputPaginationDto<CustomerDTO> execute(CustomerFilterDTO filterDto) {
        var result = this.customerRepository.search(filterDto.toCustomerFilter());

        List<CustomerDTO> customersDto = mapper.toResponseListDto(result.items());

        if  (filterDto.paginationDto() == null || !filterDto.paginationDto().isPaginated()) {
            return OutputPaginationDto.unpaginated(customersDto);
        }

        int totalPages = Math.toIntExact(result.total() / filterDto.paginationDto().limit());

        return new OutputPaginationDto<>(
                customersDto,
                filterDto.paginationDto().page(),
                filterDto.paginationDto().limit(),
                totalPages,
                result.total()
        );
    }
}

package org.libraryexpress.application.customer.mapper;

import org.libraryexpress.application.customer.dto.request.CreateCustomerDTO;
import org.libraryexpress.application.customer.dto.response.CustomerDTO;
import org.libraryexpress.domain.customer.entity.Customer;
import org.libraryexpress.domain.customer.valueobject.Email;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.WARN)
public interface CustomerMapper {

    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);

    CustomerDTO toResponseDto(Customer customer);

    List<CustomerDTO> toResponseListDto(List<Customer> customers);

    default Customer toEntity(CreateCustomerDTO dto) {
        return Customer.create(dto.name(), dto.email());
    }

    default String mapEmailToString(Email email) {
        return email != null ? email.value() : null;
    }
}

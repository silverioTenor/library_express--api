package org.libraryexpress.application.customer.mapper;

import org.libraryexpress.application.customer.dto.request.CreateCustomerDto;
import org.libraryexpress.application.customer.dto.response.CustomerDto;
import org.libraryexpress.domain.customer.entity.Customer;
import org.libraryexpress.domain.customer.valueobject.Email;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.WARN)
public interface CustomerMapper {

    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);

    CustomerDto toResponseDto(Customer customer);

    List<CustomerDto> toResponseListDto(List<Customer> customers);

    @Mapping(target = "email", source = "email", qualifiedByName = "stringToEmail")
    Customer.Builder toEntity(CreateCustomerDto dto);

    @Named("stringToEmail")
    default Email mapStringToEmail(String value) {
        return value != null ? new Email(value) : null;
    }

    default String mapEmailToString(Email email) {
        return email != null ? email.value() : null;
    }
}

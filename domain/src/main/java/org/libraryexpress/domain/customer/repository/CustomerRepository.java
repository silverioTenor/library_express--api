package org.libraryexpress.domain.customer.repository;

import org.libraryexpress.domain.core.dto.InputPaginationDto;
import org.libraryexpress.domain.core.repository.QueryResult;
import org.libraryexpress.domain.customer.entity.Customer;

import java.util.Optional;

public interface CustomerRepository {
    void create(Customer customer);
    void update(Customer customerToUpdate);
    Optional<Customer> getById(String id);
    Optional<Customer> getByEmail(String email);
    QueryResult<Customer> findAll(InputPaginationDto paginationDto);
}

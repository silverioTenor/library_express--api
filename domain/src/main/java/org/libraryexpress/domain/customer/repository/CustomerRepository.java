package org.libraryexpress.domain.customer.repository;

import java.util.Optional;
import org.libraryexpress.domain.core.repository.QueryResult;
import org.libraryexpress.domain.customer.entity.Customer;

public interface CustomerRepository {
    void create(Customer customer);

    void update(Customer customerToUpdate);

    Optional<Customer> getById(String id);

    Optional<Customer> getByEmail(String email);

    QueryResult<Customer> search(CustomerFilter filter);
}

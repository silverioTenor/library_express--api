package org.libraryexpress.infrastructure.repository.InMemory;

import org.libraryexpress.domain.core.dto.InputPaginationDto;
import org.libraryexpress.domain.core.dto.OutputPaginationDto;
import org.libraryexpress.domain.core.repository.QueryResult;
import org.libraryexpress.domain.customer.entity.Customer;
import org.libraryexpress.domain.customer.repository.CustomerRepository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCustomerRepository implements CustomerRepository {

    private final Map<String, Customer> group = new ConcurrentHashMap<>();

    @Override
    public void create(Customer customer) {
        group.put(customer.getId(), customer);
    }

    @Override
    public void update(Customer customerToUpdate) {
        Optional.ofNullable(group.get(customerToUpdate.getId()))
                .ifPresent(customer -> customer.changeEmail(customerToUpdate.getEmail().value()));
    }

    @Override
    public Optional<Customer> getById(String id) {
        return Optional.ofNullable(group.get(id));
    }

    @Override
    public Optional<Customer> getByEmail(String email) {
        return group.values().stream()
                .filter(client -> client.getEmail().value().equals(email))
                .findFirst();
    }

    @Override
    public QueryResult<Customer> findAll(InputPaginationDto paginationDto) {
        return new QueryResult<>(Set.copyOf(group.values()), group.size());
    }
}

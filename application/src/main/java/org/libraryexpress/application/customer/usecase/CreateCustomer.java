package org.libraryexpress.application.customer.usecase;

import org.libraryexpress.application.customer.dto.request.CreateCustomerDto;
import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;
import org.libraryexpress.domain.customer.exception.UniqueEmailViolationException;
import org.libraryexpress.application.customer.mapper.CustomerMapper;
import org.libraryexpress.domain.customer.entity.Customer;
import org.libraryexpress.domain.customer.repository.CustomerRepository;

import java.util.Optional;

public class CreateCustomer {

    private static final CustomLogger logger = CustomLoggerFactory.getLogger(CreateCustomer.class);

    private final CustomerRepository customerRepository;
    private final CustomerMapper mapper;

    public CreateCustomer(CustomerRepository customerRepository, CustomerMapper mapper) {
        this.customerRepository = customerRepository;
        this.mapper = mapper;
    }

    public void execute(CreateCustomerDto createCustomerDto) {
        logger.info("Starting customer registration...");

        Optional<Customer> hasClient = this.customerRepository.getByEmail(createCustomerDto.email());

        if (hasClient.isPresent()) {
            logger.warn("ABORTED: Customer with email [{}] already exists", createCustomerDto.email());
            throw new UniqueEmailViolationException();
        }

        Customer customer = mapper.toEntity(createCustomerDto).build();

        this.customerRepository.create(customer);

        logger.info("Customer successfully registered! Customer ID: [{}]", customer.getId());
    }
}

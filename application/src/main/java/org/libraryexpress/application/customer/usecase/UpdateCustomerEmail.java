package org.libraryexpress.application.customer.usecase;

import org.libraryexpress.application.customer.dto.request.UpdateCustomerEmailDto;
import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;
import org.libraryexpress.domain.customer.exception.CustomerNotFoundException;
import org.libraryexpress.application.customer.mapper.CustomerMapper;
import org.libraryexpress.domain.customer.entity.Customer;
import org.libraryexpress.domain.customer.repository.CustomerRepository;

import java.util.Optional;

public class UpdateCustomerEmail {

    private static final CustomLogger logger  = CustomLoggerFactory.getLogger(UpdateCustomerEmail.class);

    private final CustomerRepository customerRepository;
    private final CustomerMapper mapper;

    public UpdateCustomerEmail(CustomerRepository customerRepository, CustomerMapper mapper) {
        this.customerRepository = customerRepository;
        this.mapper = mapper;
    }

    public void execute(UpdateCustomerEmailDto updateCustomerEmailDto) {
        logger.info("Initiating Update Customer Email Flow");

        Customer customer = this.customerRepository.getById(updateCustomerEmailDto.id())
                .orElseThrow(() -> {
                    logger.error("CRITICAL: No customer was found for the ID: [{}] ", updateCustomerEmailDto.id());
                    return new CustomerNotFoundException();
                });

        customer.changeEmail(updateCustomerEmailDto.email());

        this.customerRepository.update(customer);

        logger.info("Customer email successfully updated! Customer ID [{}]", customer.getId());
    }
}

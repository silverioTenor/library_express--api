package org.libraryexpress.application.customer.usecase;

import org.libraryexpress.application.customer.dto.request.UpdateCustomerEmailDTO;
import org.libraryexpress.application.customer.mapper.CustomerMapper;
import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;
import org.libraryexpress.domain.customer.entity.Customer;
import org.libraryexpress.domain.customer.exception.CustomerNotFoundException;
import org.libraryexpress.domain.customer.exception.UniqueEmailViolationException;
import org.libraryexpress.domain.customer.repository.CustomerRepository;

public class UpdateCustomerEmail {

    private static final CustomLogger LOG = CustomLoggerFactory.getLogger(UpdateCustomerEmail.class);

    private final CustomerRepository customerRepository;
    private final CustomerMapper mapper;

    public UpdateCustomerEmail(CustomerRepository customerRepository, CustomerMapper mapper) {
        this.customerRepository = customerRepository;
        this.mapper = mapper;
    }

    public void execute(UpdateCustomerEmailDTO updateCustomerEmailDto) {
        LOG.info("Initiating Update Customer Email Flow");

        Customer customer = this.customerRepository
                .getById(updateCustomerEmailDto.id())
                .orElseThrow(() -> {
                    LOG.error("CRITICAL: No customer was found for the ID: [{}] ", updateCustomerEmailDto.id());
                    return new CustomerNotFoundException();
                });

        Customer isEmailUsed = this.customerRepository
                .getByEmail(updateCustomerEmailDto.email())
                .orElse(null);

        if (isEmailUsed != null) {
            LOG.warn(
                    "ALERT: The email address has already been used. Customer ID: [{}] - email provided: [{}]",
                    updateCustomerEmailDto.id(),
                    updateCustomerEmailDto.email());
            throw new UniqueEmailViolationException("The email address is not permitted");
        }

        customer.changeEmail(updateCustomerEmailDto.email());

        this.customerRepository.update(customer);

        LOG.info("Customer email successfully updated! Customer ID [{}]", customer.getId());
    }
}

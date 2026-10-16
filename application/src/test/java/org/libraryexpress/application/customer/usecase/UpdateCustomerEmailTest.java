package org.libraryexpress.application.customer.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.libraryexpress.application.UnitTest;
import org.libraryexpress.application.customer.dto.request.UpdateCustomerEmailDTO;
import org.libraryexpress.domain.customer.entity.Customer;
import org.libraryexpress.domain.customer.exception.CustomerNotFoundException;
import org.libraryexpress.domain.customer.repository.CustomerRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@UnitTest
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateCustomerEmail UseCase - Unit Test")
class UpdateCustomerEmailTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private UpdateCustomerEmail updateCustomerEmail;

    private static final String VALID_ID = "customer-uuid-123";
    private static final String OLD_EMAIL = "old.email@libraryexpress.com";
    private static final String NEW_EMAIL = "new.email@libraryexpress.com";
    private static final String CUSTOMER_NAME = "John Doe";

    @Test
    @DisplayName("Should successfully modify customer email and persist updates when customer ID exists")
    void shouldUpdateCustomerEmail_whenCustomerExistsAndNewEmailIsValid() {
        // Arrange
        UpdateCustomerEmailDTO requestDto = new UpdateCustomerEmailDTO(VALID_ID, NEW_EMAIL);

        Customer targetCustomer = new Customer.Builder()
                .setId(VALID_ID)
                .setName(CUSTOMER_NAME)
                .setEmail(OLD_EMAIL)
                .build();

        // Stubbing repository to return our editable domain entity instance
        when(customerRepository.getById(VALID_ID)).thenReturn(Optional.of(targetCustomer));

        // Act
        updateCustomerEmail.execute(requestDto);

        // Assert
        assertEquals(NEW_EMAIL, targetCustomer.getEmail().value(), "Domain model state must reflect the new email structure");

        // Business orchestration verification (style guide compliant)
        verify(customerRepository).update(targetCustomer);
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException and abort update cycle when customer ID does not exist")
    void shouldThrowCustomerNotFoundException_whenCustomerIdDoesNotExist() {
        // Arrange
        UpdateCustomerEmailDTO requestDto = new UpdateCustomerEmailDTO(VALID_ID, NEW_EMAIL);

        when(customerRepository.getById(VALID_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CustomerNotFoundException.class, () -> updateCustomerEmail.execute(requestDto));

        // Security check: ensure database modifications are completely bypassed if query fails
        verify(customerRepository, never()).update(any(Customer.class));
    }
}

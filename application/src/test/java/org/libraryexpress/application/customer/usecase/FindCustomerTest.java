package org.libraryexpress.application.customer.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.libraryexpress.application.UnitTest;
import org.libraryexpress.application.customer.dto.response.CustomerDTO;
import org.libraryexpress.application.customer.mapper.CustomerMapper;
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
@DisplayName("FindCustomer UseCase - Unit Test")
class FindCustomerTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper mapper;

    @InjectMocks
    private FindCustomer findCustomer;

    private static final String VALID_EMAIL = "john.doe@libraryexpress.com";
    private static final String VALID_ID = "customer-uuid-123";
    private static final String CUSTOMER_NAME = "John Doe";

    @Test
    @DisplayName("Should successfully return mapped DTO when customer is queried by a valid email string")
    void shouldReturnCustomerDto_whenCustomerExistsAndQueriedByEmail() {
        // Arrange
        Customer foundCustomer = new Customer.Builder()
                .setId(VALID_ID)
                .setName(CUSTOMER_NAME)
                .setEmail(VALID_EMAIL)
                .build();

        CustomerDTO expectedDto = new CustomerDTO(VALID_ID, CUSTOMER_NAME, VALID_EMAIL);

        // Stubbing email query pathway and mapping translation
        when(customerRepository.getByEmail(VALID_EMAIL)).thenReturn(Optional.of(foundCustomer));
        when(mapper.toResponseDto(foundCustomer)).thenReturn(expectedDto);

        // Act
        CustomerDTO result = findCustomer.execute(VALID_EMAIL);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDto, result);

        // Orchestration compliance: verify that only getByEmail was triggered, avoiding getById
        verify(customerRepository).getByEmail(VALID_EMAIL);
        verify(customerRepository, never()).getById(anyString());
    }

    @Test
    @DisplayName("Should successfully return mapped DTO when customer is queried by a valid ID string")
    void shouldReturnCustomerDto_whenCustomerExistsAndQueriedById() {
        // Arrange
        Customer foundCustomer = new Customer.Builder()
                .setId(VALID_ID)
                .setName(CUSTOMER_NAME)
                .setEmail(VALID_EMAIL)
                .build();

        CustomerDTO expectedDto = new CustomerDTO(VALID_ID, CUSTOMER_NAME, VALID_EMAIL);

        // Stubbing ID query pathway and mapping translation
        when(customerRepository.getById(VALID_ID)).thenReturn(Optional.of(foundCustomer));
        when(mapper.toResponseDto(foundCustomer)).thenReturn(expectedDto);

        // Act
        CustomerDTO result = findCustomer.execute(VALID_ID);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDto, result);

        // Orchestration compliance: verify that only getById was triggered, avoiding getByEmail
        verify(customerRepository).getById(VALID_ID);
        verify(customerRepository, never()).getByEmail(anyString());
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException and bypass mapping when queried email does not exist")
    void shouldThrowCustomerNotFoundException_whenEmailDoesNotExist() {
        // Arrange
        when(customerRepository.getByEmail(VALID_EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CustomerNotFoundException.class, () -> findCustomer.execute(VALID_EMAIL));

        // Validation guard: ensure no object conversions take place if entity search fails
        verifyNoInteractions(mapper);
        verify(customerRepository, never()).getById(anyString());
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException and bypass mapping when queried ID does not exist")
    void shouldThrowCustomerNotFoundException_whenIdDoesNotExist() {
        // Arrange
        when(customerRepository.getById(VALID_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CustomerNotFoundException.class, () -> findCustomer.execute(VALID_ID));

        // Validation guard: ensure no object conversions take place if entity search fails
        verifyNoInteractions(mapper);
        verify(customerRepository, never()).getByEmail(anyString());
    }
}

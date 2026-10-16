package org.libraryexpress.application.customer.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.libraryexpress.application.UnitTest;
import org.libraryexpress.application.customer.dto.request.CreateCustomerDTO;
import org.libraryexpress.application.customer.mapper.CustomerMapper;
import org.libraryexpress.domain.customer.entity.Customer;
import org.libraryexpress.domain.customer.exception.UniqueEmailViolationException;
import org.libraryexpress.domain.customer.repository.CustomerRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@UnitTest
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateCustomer UseCase - Unit Test")
class CreateCustomerTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper mapper;

    @InjectMocks
    private CreateCustomer createCustomer;

    private static final String EMAIL_STR = "john.doe@libraryexpress.com";
    private static final String CUSTOMER_NAME = "John Doe";

    @Test
    @DisplayName("Should successfully create a customer when the email is unique in database")
    void shouldCreateCustomer_whenEmailIsUnique() {
        // Arrange
        CreateCustomerDTO requestDto = new CreateCustomerDTO(CUSTOMER_NAME, EMAIL_STR);

        // Mocking the Customer.Builder fluid chain returned by the mapper layer
        Customer.Builder mockBuilder = mock(Customer.Builder.class);
        Customer targetCustomer = new Customer.Builder()
                .setName(CUSTOMER_NAME)
                .setEmail(EMAIL_STR)
                .build();

        // Stubbing repository query to simulate an empty database slot
        when(customerRepository.getByEmail(EMAIL_STR)).thenReturn(Optional.empty());
        when(mapper.toEntity(requestDto)).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(targetCustomer);

        // Act
        createCustomer.execute(requestDto);

        // Assert & Orchestration verification (style guide compliant)
        verify(customerRepository).create(targetCustomer);
    }

    @Test
    @DisplayName("Should throw UniqueEmailViolationException and abort persistence when email already exists")
    void shouldThrowUniqueEmailViolationException_whenEmailAlreadyExists() {
        // Arrange
        CreateCustomerDTO requestDto = new CreateCustomerDTO(CUSTOMER_NAME, EMAIL_STR);

        Customer existingCustomer = new Customer.Builder()
                .setId("existing-id")
                .setName("Another Customer Name")
                .setEmail(EMAIL_STR)
                .build();

        // Stubbing repository query to simulate an existing email collision
        when(customerRepository.getByEmail(EMAIL_STR)).thenReturn(Optional.of(existingCustomer));

        // Act & Assert
        assertThrows(UniqueEmailViolationException.class, () -> createCustomer.execute(requestDto));

        // Orchestration guard: ensure no data persistence takes place on email duplication
        verify(customerRepository, never()).create(any(Customer.class));
        verifyNoInteractions(mapper);
    }
}

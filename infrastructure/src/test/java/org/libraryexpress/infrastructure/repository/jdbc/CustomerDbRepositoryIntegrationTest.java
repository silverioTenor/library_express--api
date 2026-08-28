package org.libraryexpress.infrastructure.repository.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.customer.entity.Customer;
import org.libraryexpress.infrastructure.IntegrationTest;
import org.libraryexpress.infrastructure.config.database.PostgresTestContainerConfig;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@IntegrationTest
@DisplayName("Customer Repository - Core Operations Integration Test")
class CustomerDbRepositoryIntegrationTest extends PostgresTestContainerConfig {

    private CustomerDbRepository customerRepository;

    @BeforeEach
    void setUp() {
        this.customerRepository = new CustomerDbRepository(dataSource);
    }

    @Test
    @DisplayName("Should successfully persist a new customer and retrieve it back by its unique ID descriptor")
    void shouldPersistAndRetrieveCustomer_whenPayloadIsValidAndQueriedById() {
        // Arrange (Testing create and getById)
        String customerId = "custom-1111-2222-3333";
        Customer newCustomer = createSampleCustomer(customerId, "John Doe", "john.doe@libraryexpress.com");

        // Act
        customerRepository.create(newCustomer);
        Optional<Customer> retrieved = customerRepository.getById(customerId);

        // Assert
        assertTrue(retrieved.isPresent(), "Persisted customer must be found by ID inside the container");
        assertEquals("John Doe", retrieved.get().getName());
        assertEquals("john.doe@libraryexpress.com", retrieved.get().getEmail().value());
    }

    @Test
    @DisplayName("Should successfully query and locate a customer record when searching by email index")
    void shouldFindCustomer_whenQueryingByValidEmailString() {
        // Arrange (Testing getByEmail)
        String targetEmail = "alice.smith@libraryexpress.com";
        Customer customer = createSampleCustomer("custom-0000-aaaa-bbbb", "Alice Smith", targetEmail);
        customerRepository.create(customer);

        // Act
        Optional<Customer> retrieved = customerRepository.getByEmail(targetEmail);

        // Assert
        assertTrue(retrieved.isPresent(), "Customer must be retrievable via unique email lookup index");
        assertEquals("Alice Smith", retrieved.get().getName());
    }

    @Test
    @DisplayName("Should throw RuntimeException triggered by a SQL integrity violation when inserting a duplicated email")
    void shouldThrowRuntimeException_whenEmailConstraintIsViolatedAtDatabaseLevel() {
        // Arrange (Testing structural database constraint protection without leaking domain exceptions)
        String sharedEmail = "duplicate@libraryexpress.com";
        Customer client1 = createSampleCustomer("cust-first-id-123", "First Client", sharedEmail);
        customerRepository.create(client1);

        Customer client2 = createSampleCustomer("cust-second-id-456", "Second Client Boot Attempt", sharedEmail);

        // Act & Assert - DDD Principle: Expecting technical wrapper exception, not business rules mappings
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                        customerRepository.create(client2),
                "Repository layer must encapsulate raw relational engine unique constraint errors into technical runtime exceptions"
        );

        assertNotNull(exception.getCause(), "The technical execution failure wrapper must contain the original SQLException");
    }

    @Test
    @DisplayName("Should successfully modify customer personal attributes and persist changes during record updates")
    void shouldUpdateCustomerState_whenModifyingExistingRecord() {
        // Arrange (Testing update)
        String customerId = "cust-updt-id-999";
        Customer baseCustomer = createSampleCustomer(customerId, "Old Name", "old.email@libraryexpress.com");
        customerRepository.create(baseCustomer);

        Customer updatedCustomer = new Customer.Builder()
                .setId(customerId)
                .setName("New Brand Identity Name")
                .setEmail("new.email@libraryexpress.com")
                .build();

        // Act
        customerRepository.update(updatedCustomer);
        Optional<Customer> retrieved = customerRepository.getById(customerId);

        // Assert
        assertTrue(retrieved.isPresent());
        assertEquals("New Brand Identity Name", retrieved.get().getName());
        assertEquals("new.email@libraryexpress.com", retrieved.get().getEmail().value());
    }

    @Test
    @DisplayName("Should successfully capture and retrieve the entire customers collection dataset during unrestricted scans")
    void shouldReturnAllPersistedCustomers_whenTriggeringUnrestrictedCatalogScan() {
        // Arrange (Testing findAll)
        Customer customer1 = createSampleCustomer("id-multi-1", "User Alpha", "alpha@libraryexpress.com");
        Customer customer2 = createSampleCustomer("id-multi-2", "User Beta", "beta@libraryexpress.com");

        customerRepository.create(customer1);
        customerRepository.create(customer2);

        // Act
        Set<Customer> allCustomers = customerRepository.all();

        // Assert
        assertNotNull(allCustomers);
        assertTrue(allCustomers.size() >= 2, "Customers list lookup must yield findAll entries matching current database isolation scopes");
    }

    /**
     * Clean helper method to encapsulate recurring customer builder mappings inside the test suite matrix.
     */
    private Customer createSampleCustomer(String id, String name, String email) {
        return new Customer.Builder()
                .setId(id)
                .setName(name)
                .setEmail(email)
                .build();
    }
}

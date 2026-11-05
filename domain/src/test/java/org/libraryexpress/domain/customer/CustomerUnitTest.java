package org.libraryexpress.domain.customer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.UnitTest;
import org.libraryexpress.domain.customer.entity.Customer;
import org.libraryexpress.domain.customer.valueobject.Email;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@DisplayName("Customer Entity - Unit Test")
public class CustomerUnitTest {

    @Test
    @DisplayName("Should register a customer when no ID is provided.")
    void shouldRegisterCustomer_whenNoIdIsProvided() {

        Customer customer = Customer.create("John Doe", "j.doe@test.com");

        assertNotNull(customer.getId());
        assertFalse(customer.getId().isBlank());
        assertEquals("John Doe", customer.getName());
        assertEquals(new Email("j.doe@test.com"), customer.getEmail());
    }

    @Test
    @DisplayName("Should register a customer when ID is provided")
    void shouldRegisterCustomer_whenIdIsProvided() {

        String customId = "custom-uuid-123";

        Customer customer = Customer.restore(customId, "John Doe", "j.doe@test.com");

        assertNotNull(customer.getId());
        assertFalse(customer.getId().isBlank());
        assertEquals(customId, customer.getId());
    }

    @Test
    @DisplayName("Should consider two customers equal when they have the same ID")
    void shouldConsiderTwoCustomersEqual_whenTheyHaveTheSameId() {

        String customId = "custom-uuid-123";

        Customer firstCustomer = Customer.restore(customId, "John Doe", "j.doe@test.com");

        Customer secondCustomer = Customer.restore(customId, "Jane Doe", "jane.28@test.com");

        assertEquals(firstCustomer, secondCustomer);
        assertEquals(firstCustomer.hashCode(), secondCustomer.hashCode());
    }

    @Test
    @DisplayName("Should sort customers by name when the order is alphabetical.")
    void shouldSortCustomersByName_WhenOrderIsAlphabetical() {

        Customer firstCustomer = Customer.create("Samantha Smith", "s.s@test.com");

        Customer secondCustomer = Customer.create("David Parker", "dp.25@test.com");

        assertFalse(firstCustomer.compareTo(secondCustomer) < 0); // Samantha comes after David (D...S)
        assertFalse(secondCustomer.compareTo(firstCustomer) > 0); // Samantha comes after David (D...S)
    }
}

package org.libraryexpress.domain.customer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.libraryexpress.domain.UnitTest;
import org.libraryexpress.domain.customer.exception.InvalidEmailException;
import org.libraryexpress.domain.customer.valueobject.Email;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@DisplayName("Email Value Object - Unit Test")
public class EmailUnitTest {

    @Test
    @DisplayName("Should create an Email instance when a valid format is provided")
    void shouldCreateEmail_whenFormatIsValid() {

        String validEmailStr = "john.doe@test.com";

        Email email = new Email(validEmailStr);

        assertEquals(validEmailStr, email.value());
    }

    @Test
    @DisplayName("Should throw an exception when email provided is null or blank")
    void shouldThrowException_whenEmailProvidedIsNullOrBlank() {

        InvalidEmailException exception1 = assertThrows(InvalidEmailException.class, () -> new Email(null));
        InvalidEmailException exception2 = assertThrows(InvalidEmailException.class, () -> new Email(""));
        InvalidEmailException exception3 = assertThrows(InvalidEmailException.class, () -> new Email("  "));

        assertEquals("Email cannot be null or empty", exception1.getMessage());
        assertEquals("Email cannot be null or empty", exception2.getMessage());
        assertEquals("Email cannot be null or empty", exception3.getMessage());
    }

    @Test
    @DisplayName("Should throw an exception when email size is greater than 254 characters")
    void shouldThrowException_whenEmailSizeIsGreaterThan254Characters() {
        String valueGreaterThan254 =  "test".repeat(255);
        InvalidEmailException exception1 = assertThrows(InvalidEmailException.class, () -> new Email(valueGreaterThan254));

        assertEquals("Email exceeds maximum allowed length", exception1.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalidFormat",
            "invalid.@test.com",
            "t12child@.er",
            "@t12child"
    })
    @DisplayName("Should throw an exception when email format provided is invalid")
    void shouldThrowException_whenEmailFormatProvidedIsInvalid(String invalidEmail) {

        InvalidEmailException exception1 = assertThrows(InvalidEmailException.class, () -> new Email(invalidEmail));

        assertEquals("Invalid email format", exception1.getMessage());
    }

    @Test
    @DisplayName("Should be equal when e-mails have same value")
    void shouldBeEqual_whenEmailsHaveSameValue() {

        Email email1 = new Email("test@test.com");
        Email email2 = new Email("test@test.com");
        Email email3 = new Email("another@test.com");

        assertEquals(email1, email2);
        assertEquals(email1.hashCode(), email2.hashCode());
        assertNotEquals(email1, email3);
    }
}

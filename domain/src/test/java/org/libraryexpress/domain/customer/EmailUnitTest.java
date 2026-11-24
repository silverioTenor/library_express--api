package org.libraryexpress.domain.customer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.libraryexpress.domain.UnitTest;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;
import org.libraryexpress.domain.core.valueobject.Email;

@UnitTest
@DisplayName("Email Value Object - Unit Test")
public class EmailUnitTest {

    @Test
    @DisplayName("Should create an Email instance when a valid format is provided")
    void shouldCreateEmail_whenFormatIsValid() {

        String validEmailStr = "john.doe@test.com";

        Email email = Email.of(validEmailStr);

        assertEquals(validEmailStr, email.value());
    }

    @Test
    @DisplayName("Should throw an exception when email provided is null or blank")
    void shouldThrowException_whenEmailProvidedIsNullOrBlank() {

        InvalidArgumentException exception1 = assertThrows(InvalidArgumentException.class, () -> Email.of(null));
        InvalidArgumentException exception2 = assertThrows(InvalidArgumentException.class, () -> Email.of(""));
        InvalidArgumentException exception3 = assertThrows(InvalidArgumentException.class, () -> Email.of("  "));

        assertEquals("Email cannot be null or empty", exception1.getMessage());
        assertEquals("Email cannot be null or empty", exception2.getMessage());
        assertEquals("Email cannot be null or empty", exception3.getMessage());
    }

    @Test
    @DisplayName("Should throw an exception when email size is greater than 254 characters")
    void shouldThrowException_whenEmailSizeIsGreaterThan254Characters() {
        String valueGreaterThan254 = "test".repeat(255);
        InvalidArgumentException exception1 =
                assertThrows(InvalidArgumentException.class, () -> Email.of(valueGreaterThan254));

        assertEquals("Email exceeds maximum allowed length", exception1.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalidFormat", "invalid.@test.com", "t12child@.er", "@t12child"})
    @DisplayName("Should throw an exception when email format provided is invalid")
    void shouldThrowException_whenEmailFormatProvidedIsInvalid(String invalidEmail) {

        InvalidArgumentException exception1 =
                assertThrows(InvalidArgumentException.class, () -> Email.of(invalidEmail));

        assertEquals("Invalid email format", exception1.getMessage());
    }

    @Test
    @DisplayName("Should be equal when e-mails have same value")
    void shouldBeEqual_whenEmailsHaveSameValue() {

        Email email1 = Email.of("test@test.com");
        Email email2 = Email.of("test@test.com");
        Email email3 = Email.of("another@test.com");

        assertEquals(email1, email2);
        assertEquals(email1.hashCode(), email2.hashCode());
        assertNotEquals(email1, email3);
    }
}

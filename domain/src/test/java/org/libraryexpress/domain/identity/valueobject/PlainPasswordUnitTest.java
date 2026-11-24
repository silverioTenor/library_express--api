package org.libraryexpress.domain.identity.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.UnitTest;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;

@UnitTest
@DisplayName("Plain password value object")
class PlainPasswordUnitTest {

    @Test
    @DisplayName("Should accept a password at the minimum length")
    void shouldAcceptPassword_whenLengthIsAtMinimum() {
        String passwordValue = "x".repeat(12);

        PlainPassword password = new PlainPassword(passwordValue);

        assertEquals(passwordValue, password.value());
    }

    @Test
    @DisplayName("Should normalize a password using NFKC when it contains compatibility characters")
    void shouldNormalizePassword_whenCompatibilityCharactersArePresent() {
        String passwordValue = "Ｘ".repeat(12);

        PlainPassword password = new PlainPassword(passwordValue);

        assertEquals("X".repeat(12), password.value());
    }

    @Test
    @DisplayName("Should count Unicode code points when validating password length")
    void shouldAcceptPassword_whenTwelveUnicodeCodePointsAreProvided() {
        String passwordValue = "😀".repeat(12);

        PlainPassword password = new PlainPassword(passwordValue);

        assertEquals(passwordValue, password.value());
    }

    @Test
    @DisplayName("Should reject a null password")
    void shouldThrowException_whenPasswordIsNull() {
        InvalidArgumentException exception =
                assertThrows(InvalidArgumentException.class, () -> new PlainPassword(null));

        assertEquals("Password cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject a password shorter than the minimum length")
    void shouldThrowException_whenPasswordIsTooShort() {
        InvalidArgumentException exception =
                assertThrows(InvalidArgumentException.class, () -> new PlainPassword("a".repeat(11)));

        assertEquals("Password must have at least 12 characters", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject a password longer than the maximum length")
    void shouldThrowException_whenPasswordIsTooLong() {
        InvalidArgumentException exception =
                assertThrows(InvalidArgumentException.class, () -> new PlainPassword("a".repeat(129)));

        assertEquals("Password must have at most 128 characters", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject a common password regardless of letter case")
    void shouldThrowException_whenPasswordIsOnTheDenylist() {
        InvalidArgumentException exception =
                assertThrows(InvalidArgumentException.class, () -> new PlainPassword("PASSWORD1234"));

        assertEquals("Password is too common", exception.getMessage());
    }

    @Test
    @DisplayName("Should hide the password value when converted to a string")
    void shouldHidePassword_whenConvertedToString() {
        PlainPassword password = new PlainPassword("unique-passphrase");

        String representation = password.toString();

        assertEquals("PlainPassword[****]", representation);
    }
}

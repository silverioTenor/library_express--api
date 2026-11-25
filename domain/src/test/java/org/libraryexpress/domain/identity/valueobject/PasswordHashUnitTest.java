package org.libraryexpress.domain.identity.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.UnitTest;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;

@UnitTest
@DisplayName("Password hash value object")
class PasswordHashUnitTest {

    private static final String VALID_HASH = "$argon2id$v=19$m=65536,t=3,p=1$c2FsdA$aGFzaA";

    @Test
    @DisplayName("Should accept an Argon2id PHC string")
    void shouldCreatePasswordHash_whenValueIsArgon2idPhcString() {
        PasswordHash passwordHash = PasswordHash.of(VALID_HASH);

        assertEquals(VALID_HASH, passwordHash.value());
    }

    @Test
    @DisplayName("Should reject a null hash")
    void shouldThrowException_whenHashIsNull() {
        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class, () -> PasswordHash.of(null));

        assertEquals("Password hash must be an Argon2id PHC string", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject a hash that does not use Argon2id")
    void shouldThrowException_whenHashHasInvalidAlgorithm() {
        InvalidArgumentException exception =
                assertThrows(InvalidArgumentException.class, () -> PasswordHash.of("$bcrypt$hash"));

        assertEquals("Password hash must be an Argon2id PHC string", exception.getMessage());
    }

    @Test
    @DisplayName("Should accept a hash at the maximum storage length")
    void shouldCreatePasswordHash_whenLengthIsAtMaximum() {
        String hashValue = "$argon2id$" + "a".repeat(245);

        PasswordHash passwordHash = PasswordHash.of(hashValue);

        assertEquals(255, passwordHash.value().length());
    }

    @Test
    @DisplayName("Should reject a hash longer than the maximum storage length")
    void shouldThrowException_whenHashExceedsMaximumLength() {
        String hashValue = "$argon2id$" + "a".repeat(247);

        InvalidArgumentException exception =
                assertThrows(InvalidArgumentException.class, () -> PasswordHash.of(hashValue));

        assertEquals("Password hash exceeds maximum length", exception.getMessage());
    }

    @Test
    @DisplayName("Should hide the hash value when converted to a string")
    void shouldHideHash_whenConvertedToString() {
        PasswordHash passwordHash = PasswordHash.of(VALID_HASH);

        String representation = passwordHash.toString();

        assertEquals("PasswordHash[****]", representation);
    }
}

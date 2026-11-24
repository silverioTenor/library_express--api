package org.libraryexpress.domain.identity.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.UnitTest;

@UnitTest
@DisplayName("Password policy")
class PasswordPolicyUnitTest {

    @Test
    @DisplayName("Should identify a denied password when it is on the denylist")
    void shouldReturnTrue_whenPasswordIsOnDenylist() {
        boolean denied = PasswordPolicy.isDenied("library123456");

        assertTrue(denied);
    }

    @Test
    @DisplayName("Should compare denied passwords without regard to letter case")
    void shouldReturnTrue_whenDeniedPasswordUsesDifferentLetterCase() {
        boolean denied = PasswordPolicy.isDenied("LIBRARY123456");

        assertTrue(denied);
    }

    @Test
    @DisplayName("Should allow a password when it is not on the denylist")
    void shouldReturnFalse_whenPasswordIsNotOnDenylist() {
        boolean denied = PasswordPolicy.isDenied("a-private-passphrase");

        assertFalse(denied);
    }

    @Test
    @DisplayName("Should expose the configured minimum password length")
    void shouldReturnMinimumLength_whenPolicyIsQueried() {
        assertEquals(12, PasswordPolicy.MIN_LENGTH);
    }

    @Test
    @DisplayName("Should expose the configured maximum password length")
    void shouldReturnMaximumLength_whenPolicyIsQueried() {
        assertEquals(128, PasswordPolicy.MAX_LENGTH);
    }
}

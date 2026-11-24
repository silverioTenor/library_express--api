package org.libraryexpress.domain.identity.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.UnitTest;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;
import org.libraryexpress.domain.core.valueobject.Email;
import org.libraryexpress.domain.identity.enums.IdentityStatus;
import org.libraryexpress.domain.identity.valueobject.PasswordHash;

@UnitTest
@DisplayName("Identity entity")
class IdentityUnitTest {

    private static final String VALID_HASH = "$argon2id$v=19$m=65536,t=3,p=1$c2FsdA$aGFzaA";

    @Test
    @DisplayName("Should create a pending identity without a password hash")
    void shouldCreatePendingIdentity_whenEmailIsValid() {
        Identity identity = Identity.createPending("pending@example.com");

        assertNotNull(identity.getId());
        assertEquals(Email.of("pending@example.com"), identity.getEmail());
        assertEquals(IdentityStatus.PENDING_ACTIVATION, identity.getStatus());
        assertTrue(identity.getPasswordHash().isEmpty());
        assertEquals(0, identity.getFailedAttempts());
        assertTrue(identity.getLockedUntil().isEmpty());
    }

    @Test
    @DisplayName("Should create an active identity with its password hash")
    void shouldCreateActiveIdentity_whenEmailAndPasswordHashAreValid() {
        PasswordHash passwordHash = new PasswordHash(VALID_HASH);

        Identity identity = Identity.createActive("active@example.com", passwordHash);

        assertEquals(IdentityStatus.ACTIVE, identity.getStatus());
        assertEquals(passwordHash, identity.getPasswordHash().orElseThrow());
    }

    @Test
    @DisplayName("Should restore an identity with its persisted security state")
    void shouldRestoreIdentity_whenPersistedValuesAreValid() {
        UUID id = UUID.randomUUID();
        Instant lockedUntil = Instant.parse("2026-11-24T12:00:00Z");

        Identity identity =
                Identity.restore(id, "locked@example.com", VALID_HASH, IdentityStatus.BLOCKED, 3, lockedUntil);

        assertEquals(id, identity.getId());
        assertEquals(IdentityStatus.BLOCKED, identity.getStatus());
        assertEquals(new PasswordHash(VALID_HASH), identity.getPasswordHash().orElseThrow());
        assertEquals(3, identity.getFailedAttempts());
        assertEquals(lockedUntil, identity.getLockedUntil().orElseThrow());
    }

    @Test
    @DisplayName("Should restore a pending identity without a password hash")
    void shouldRestorePendingIdentity_whenPersistedHashIsNull() {
        Identity identity = Identity.restore(
                UUID.randomUUID(), "pending@example.com", null, IdentityStatus.PENDING_ACTIVATION, 0, null);

        assertTrue(identity.getPasswordHash().isEmpty());
    }

    @Test
    @DisplayName("Should reject a negative failed-attempt count")
    void shouldThrowException_whenFailedAttemptsAreNegative() {
        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> Identity.restore(
                        UUID.randomUUID(), "user@example.com", VALID_HASH, IdentityStatus.ACTIVE, -1, null));

        assertTrue(exception.getMessage().contains("Failed attempts cannot be negative"));
    }

    @Test
    @DisplayName("Should reject a pending identity that has a password hash")
    void shouldThrowException_whenPendingIdentityHasPasswordHash() {
        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> Identity.restore(
                        UUID.randomUUID(),
                        "pending@example.com",
                        VALID_HASH,
                        IdentityStatus.PENDING_ACTIVATION,
                        0,
                        null));

        assertTrue(exception.getMessage().contains("A pending identity cannot have a password hash"));
    }

    @Test
    @DisplayName("Should reject an active identity without a password hash")
    void shouldThrowException_whenActiveIdentityHasNoPasswordHash() {
        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> Identity.restore(UUID.randomUUID(), "active@example.com", null, IdentityStatus.ACTIVE, 0, null));

        assertTrue(exception.getMessage().contains("An identity that is not pending must have a password hash"));
    }

    @Test
    @DisplayName("Should compare identities by identifier")
    void shouldBeEqual_whenIdentityIdsAreEqual() {
        UUID id = UUID.randomUUID();
        Identity firstIdentity = Identity.restore(id, "first@example.com", VALID_HASH, IdentityStatus.ACTIVE, 0, null);
        Identity secondIdentity =
                Identity.restore(id, "second@example.com", VALID_HASH, IdentityStatus.ACTIVE, 0, null);

        assertEquals(firstIdentity, secondIdentity);
        assertEquals(firstIdentity.hashCode(), secondIdentity.hashCode());
    }

    @Test
    @DisplayName("Should not compare identities as equal when identifiers differ")
    void shouldNotBeEqual_whenIdentityIdsDiffer() {
        Identity firstIdentity =
                Identity.restore(UUID.randomUUID(), "first@example.com", VALID_HASH, IdentityStatus.ACTIVE, 0, null);
        Identity secondIdentity =
                Identity.restore(UUID.randomUUID(), "second@example.com", VALID_HASH, IdentityStatus.ACTIVE, 0, null);

        assertNotEquals(firstIdentity, secondIdentity);
        assertFalse(firstIdentity.equals(null));
    }
}

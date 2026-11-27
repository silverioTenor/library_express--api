package org.libraryexpress.domain.user.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.UnitTest;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;
import org.libraryexpress.domain.identity.entity.Identity;
import org.libraryexpress.domain.identity.valueobject.PasswordHash;
import org.libraryexpress.domain.user.enums.Role;

@UnitTest
@DisplayName("User entity")
class UserUnitTest {

    private static final String PASSWORD_HASH = "$argon2id$v=19$m=65536,t=3,p=1$c2FsdA$aGFzaA";

    @Test
    @DisplayName("Should create a super admin without a creator")
    void shouldCreateSuperAdmin_whenCreatorIsNotProvided() {
        Identity identity = Identity.createActive("admin@example.com", PasswordHash.of(PASSWORD_HASH));

        User user = User.create("Library Admin", Role.SA, identity, null);

        assertNotNull(user.getId());
        assertEquals("Library Admin", user.getName());
        assertEquals(Role.SA, user.getRole());
        assertEquals(identity, user.getIdentity());
        assertTrue(user.getCreatedBy().isEmpty());
    }

    @Test
    @DisplayName("Should create a non-super-admin when a creator is provided")
    void shouldCreateUser_whenCreatorIsProvided() {
        Identity identity = createIdentity();
        UUID userId = UUID.randomUUID();

        User user = User.create("Operator", Role.OPERATOR, identity, userId);

        assertEquals(userId, user.getCreatedBy().orElseThrow());
    }

    @Test
    @DisplayName("Should reject a super admin with a creator")
    void shouldThrowException_whenSuperAdminHasCreator() {
        UUID userId = UUID.randomUUID();

        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class, () -> User.create("Admin", Role.SA, createIdentity(), userId));

        assertEquals("The super admin cannot have a creator", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject a non-super-admin without a creator")
    void shouldThrowException_whenNonSuperAdminHasNoCreator() {
        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class, () -> User.create("Operator", Role.OPERATOR, createIdentity(), null));

        assertEquals("A user other than the super admin must have a creator", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject a user with a null name")
    void shouldThrowException_whenNameIsNull() {
        UUID userId = UUID.randomUUID();

        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class, () -> User.restore(userId, null, Role.ADMIN, createIdentity(), userId));

        assertTrue(exception.getMessage().contains("User name cannot be null or empty"));
    }

    @Test
    @DisplayName("Should reject a user name longer than the maximum length")
    void shouldThrowException_whenNameExceedsMaximumLength() {
        UUID userId = UUID.randomUUID();

        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> User.restore(userId, "a".repeat(151), Role.ADMIN, createIdentity(), userId));

        assertTrue(exception.getMessage().contains("User name exceeds 150 characters"));
    }

    @Test
    @DisplayName("Should reject a user with a missing identifier")
    void shouldThrowException_whenIdIsBlank() {
        UUID userId = UUID.randomUUID();

        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> User.restore(null, "Operator", Role.ADMIN, createIdentity(), userId));

        assertTrue(exception.getMessage().contains("User id cannot be null or empty"));
    }

    @Test
    @DisplayName("Should reject a user with a missing identity")
    void shouldThrowException_whenIdentityIsNull() {
        UUID userId = UUID.randomUUID();

        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class, () -> User.restore(userId, "Operator", Role.ADMIN, null, userId));

        assertTrue(exception.getMessage().contains("User identity cannot be null"));
    }

    @Test
    @DisplayName("Should restore a user using its persisted identifier and creator")
    void shouldRestoreUser_whenPersistedValuesAreValid() {
        UUID userId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        Identity identity = createIdentity();

        User user = User.restore(userId, "Supervisor", Role.SUPERVISOR, identity, creatorId);

        assertEquals(userId, user.getId());
        assertEquals(Role.SUPERVISOR, user.getRole());
        assertEquals(identity, user.getIdentity());
        assertEquals(creatorId, user.getCreatedBy().orElseThrow());
    }

    @Test
    @DisplayName("Should compare users by identifier")
    void shouldBeEqual_whenUserIdsAreEqual() {
        UUID userId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID creatorId2 = UUID.randomUUID();

        User firstUser = User.restore(userId, "First", Role.ADMIN, createIdentity(), creatorId);
        User secondUser = User.restore(userId, "Second", Role.OPERATOR, createIdentity(), creatorId2);

        assertEquals(firstUser, secondUser);
        assertEquals(firstUser.hashCode(), secondUser.hashCode());
    }

    @Test
    @DisplayName("Should not compare users as equal when identifiers differ")
    void shouldNotBeEqual_whenUserIdsDiffer() {
        UUID userId = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID creatorId2 = UUID.randomUUID();

        User firstUser = User.restore(userId, "First", Role.ADMIN, createIdentity(), creatorId);
        User secondUser = User.restore(userId2, "Second", Role.ADMIN, createIdentity(), creatorId2);

        assertNotEquals(firstUser, secondUser);
        assertNotNull(firstUser);
    }

    private Identity createIdentity() {
        PasswordHash passwordHash = PasswordHash.of(PASSWORD_HASH);
        return Identity.createActive("user@example.com", passwordHash);
    }
}

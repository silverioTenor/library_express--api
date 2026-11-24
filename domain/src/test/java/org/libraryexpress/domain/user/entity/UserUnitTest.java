package org.libraryexpress.domain.user.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        Identity identity = Identity.createActive("admin@example.com", new PasswordHash(PASSWORD_HASH));

        User user = User.create("Library Admin", Role.SUPER_ADMIN, identity, null);

        assertNotNull(user.getId());
        assertEquals("Library Admin", user.getName());
        assertEquals(Role.SUPER_ADMIN, user.getRole());
        assertEquals(identity, user.getIdentity());
        assertTrue(user.getCreatedBy().isEmpty());
    }

    @Test
    @DisplayName("Should create a non-super-admin when a creator is provided")
    void shouldCreateUser_whenCreatorIsProvided() {
        Identity identity = createIdentity();

        User user = User.create("Operator", Role.OPERATOR, identity, "creator-123");

        assertEquals("creator-123", user.getCreatedBy().orElseThrow());
    }

    @Test
    @DisplayName("Should reject a super admin with a creator")
    void shouldThrowException_whenSuperAdminHasCreator() {
        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> User.create("Admin", Role.SUPER_ADMIN, createIdentity(), "creator"));

        assertEquals("The super admin cannot have a creator", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject a non-super-admin without a creator")
    void shouldThrowException_whenNonSuperAdminHasNoCreator() {
        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class, () -> User.create("Operator", Role.OPERATOR, createIdentity(), "  "));

        assertEquals("A user other than the super admin must have a creator", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject a user with a null name")
    void shouldThrowException_whenNameIsNull() {
        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> User.restore("user-123", null, Role.ADMIN, createIdentity(), "creator-123"));

        assertTrue(exception.getMessage().contains("User name cannot be null or empty"));
    }

    @Test
    @DisplayName("Should reject a user name longer than the maximum length")
    void shouldThrowException_whenNameExceedsMaximumLength() {
        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> User.restore("user-123", "a".repeat(151), Role.ADMIN, createIdentity(), "creator-123"));

        assertTrue(exception.getMessage().contains("User name exceeds 150 characters"));
    }

    @Test
    @DisplayName("Should reject a user with a missing identifier")
    void shouldThrowException_whenIdIsBlank() {
        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> User.restore(" ", "Operator", Role.ADMIN, createIdentity(), "creator-123"));

        assertTrue(exception.getMessage().contains("User id cannot be null or empty"));
    }

    @Test
    @DisplayName("Should reject a user with a missing identity")
    void shouldThrowException_whenIdentityIsNull() {
        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> User.restore("user-123", "Operator", Role.ADMIN, null, "creator-123"));

        assertTrue(exception.getMessage().contains("User identity cannot be null"));
    }

    @Test
    @DisplayName("Should restore a user using its persisted identifier and creator")
    void shouldRestoreUser_whenPersistedValuesAreValid() {
        String userId = UUID.randomUUID().toString();
        Identity identity = createIdentity();

        User user = User.restore(userId, "Supervisor", Role.SUPERVISOR, identity, "creator-123");

        assertEquals(userId, user.getId());
        assertEquals(Role.SUPERVISOR, user.getRole());
        assertEquals(identity, user.getIdentity());
        assertEquals("creator-123", user.getCreatedBy().orElseThrow());
    }

    @Test
    @DisplayName("Should compare users by identifier")
    void shouldBeEqual_whenUserIdsAreEqual() {
        String userId = "user-123";
        User firstUser = User.restore(userId, "First", Role.ADMIN, createIdentity(), "creator-1");
        User secondUser = User.restore(userId, "Second", Role.OPERATOR, createIdentity(), "creator-2");

        assertEquals(firstUser, secondUser);
        assertEquals(firstUser.hashCode(), secondUser.hashCode());
    }

    @Test
    @DisplayName("Should not compare users as equal when identifiers differ")
    void shouldNotBeEqual_whenUserIdsDiffer() {
        User firstUser = User.restore("user-1", "First", Role.ADMIN, createIdentity(), "creator-1");
        User secondUser = User.restore("user-2", "Second", Role.ADMIN, createIdentity(), "creator-2");

        assertNotEquals(firstUser, secondUser);
        assertNotNull(firstUser);
    }

    private Identity createIdentity() {
        PasswordHash passwordHash = new PasswordHash(PASSWORD_HASH);
        return Identity.createActive("user@example.com", passwordHash);
    }
}

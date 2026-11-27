package org.libraryexpress.infrastructure.repository.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.core.valueobject.Email;
import org.libraryexpress.domain.identity.entity.Identity;
import org.libraryexpress.domain.identity.enums.IdentityStatus;
import org.libraryexpress.domain.identity.valueobject.PasswordHash;
import org.libraryexpress.domain.identity.valueobject.PlainPassword;
import org.libraryexpress.domain.user.entity.User;
import org.libraryexpress.domain.user.enums.Role;
import org.libraryexpress.infrastructure.IntegrationTest;
import org.libraryexpress.infrastructure.config.database.PostgresTestContainerConfig;
import org.libraryexpress.infrastructure.config.database.transaction.TransactionAwareDataSource;
import org.libraryexpress.infrastructure.exception.TransactionException;
import org.libraryexpress.infrastructure.security.Argon2Settings;
import org.libraryexpress.infrastructure.security.Argon2idPasswordHasher;

@IntegrationTest
@DisplayName("User database repository integration tests")
class UserDbRepositoryIntegrationTest extends PostgresTestContainerConfig {

    private TransactionManagerDb transactionManager;
    private UserDbRepository userRepository;

    @BeforeEach
    void setUp() {
        transactionManager = new TransactionManagerDb(dataSource);
        userRepository = new UserDbRepository(new TransactionAwareDataSource(dataSource));
    }

    @Test
    @DisplayName("Should report role existence accurately on an empty table and after super admin creation")
    void shouldReportRoleExistence_whenSuperAdminIsCreated() {
        assertFalse(userRepository.existsByRole(Role.SA));
        assertFalse(userRepository.existsByRole(Role.ADMIN));

        Identity identity =
                Identity.createActive(uniqueEmail(), PasswordHash.of("$argon2id$v=19$m=65536,t=3,p=1$c2FsdA$aGFzaA"));
        User superAdmin = User.create("Super admin", Role.SA, identity, null);

        persist(superAdmin);

        assertTrue(userRepository.existsByRole(Role.SA));
        assertFalse(userRepository.existsByRole(Role.ADMIN));
    }

    @Test
    @DisplayName("Should round-trip a pending user and preserve its creator")
    void shouldRoundTripPendingUser_whenCreatedWithinTransaction() {
        UUID creatorId = insertCreator();
        User user = User.create("Pending operator", Role.OPERATOR, Identity.createPending(uniqueEmail()), creatorId);

        transactionManager.execute(() -> {
            userRepository.create(user);
            return null;
        });

        User persistedUser = userRepository.getById(user.getId()).orElseThrow();

        assertEquals(
                IdentityStatus.PENDING_ACTIVATION, persistedUser.getIdentity().getStatus());
        assertTrue(persistedUser.getIdentity().getPasswordHash().isEmpty());
        assertEquals(0, persistedUser.getIdentity().getFailedAttempts());
        assertTrue(persistedUser.getIdentity().getLockedUntil().isEmpty());
        assertEquals(Role.OPERATOR, persistedUser.getRole());
        assertEquals(creatorId, persistedUser.getCreatedBy().orElseThrow());
    }

    @Test
    @DisplayName("Should round-trip a SUPER_ADMIN and verify its Argon2id password hash")
    void shouldRoundTripSuperAdminWithVerifiablePasswordHash_whenCreatedWithinTransaction() throws SQLException {
        PlainPassword password = PlainPassword.of("N0tCommon-LongPassword!2026");
        Argon2idPasswordHasher hasher = new Argon2idPasswordHasher(Argon2Settings.owaspDefaults());
        Identity identity = Identity.createActive(uniqueEmail(), hasher.hash(password));
        User user = User.create("Super administrator", Role.SA, identity, null);

        transactionManager.execute(() -> {
            userRepository.create(user);
            return null;
        });

        User persistedUser =
                userRepository.getByEmail(identity.getEmail().value()).orElseThrow();
        String storedHash = readPasswordHash(identity.getId());

        assertEquals(Role.SA, persistedUser.getRole());
        assertTrue(persistedUser.getCreatedBy().isEmpty());
        assertTrue(hasher.verify(
                password, persistedUser.getIdentity().getPasswordHash().orElseThrow()));
        assertTrue(storedHash.startsWith("$argon2id$"));
        assertFalse(storedHash.contains(password.value()));
    }

    @Test
    @DisplayName("Should find a user by email without regard to letter case")
    void shouldFindUserByEmailIgnoringCase_whenEmailHasDifferentLetterCase() {
        UUID creatorId = insertCreator();
        User user =
                User.create("Case-insensitive user", Role.ADMIN, Identity.createPending("ana@example.com"), creatorId);
        persist(user);

        User persistedUser = userRepository.getByEmail("ANA@Example.com").orElseThrow();

        assertEquals(user.getId(), persistedUser.getId());
        assertEquals(Email.of("ana@example.com"), persistedUser.getIdentity().getEmail());
    }

    @Test
    @DisplayName("Should return empty results when the requested user does not exist")
    void shouldReturnEmpty_whenUserIdAndEmailDoNotExist() {
        assertTrue(userRepository.getById(UUID.randomUUID()).isEmpty());
        assertTrue(userRepository.getByEmail(uniqueEmail()).isEmpty());
    }

    @Test
    @DisplayName("Should preserve a populated locked-until instant when loading a user")
    void shouldPreserveLockedUntil_whenUserIsRoundTripped() {
        UUID creatorId = insertCreator();
        Instant lockedUntil = Instant.parse("2026-11-24T20:12:34.123456Z");
        Identity identity = Identity.restore(
                UUID.randomUUID(), uniqueEmail(), null, IdentityStatus.PENDING_ACTIVATION, 3, lockedUntil);
        User user = User.create("Locked user", Role.ADMIN, identity, creatorId);
        persist(user);

        Instant persistedLockedUntil = userRepository
                .getById(user.getId())
                .orElseThrow()
                .getIdentity()
                .getLockedUntil()
                .orElseThrow();

        assertEquals(lockedUntil, persistedLockedUntil);
    }

    @Test
    @DisplayName("Should require an active transaction before inserting a user")
    void shouldThrowTransactionException_whenCreateRunsOutsideTransaction() throws SQLException {
        User user = User.create(
                "Outside transaction", Role.OPERATOR, Identity.createPending(uniqueEmail()), UUID.randomUUID());

        assertThrows(TransactionException.class, () -> userRepository.create(user));

        assertFalse(identityExists(user.getIdentity().getId()));
        assertFalse(userExists(user.getId()));
    }

    @Test
    @DisplayName("Should roll back the identity when inserting its user fails a creator foreign key")
    void shouldRollbackIdentity_whenUserInsertViolatesCreatorForeignKey() throws SQLException {
        User user =
                User.create("Missing creator", Role.ADMIN, Identity.createPending(uniqueEmail()), UUID.randomUUID());

        assertThrows(RuntimeException.class, () -> persist(user));

        assertFalse(identityExists(user.getIdentity().getId()));
        assertFalse(userExists(user.getId()));
    }

    @Test
    @DisplayName("Should preserve the first user when a second create uses a duplicate email")
    void shouldPreserveFirstUser_whenSecondCreateUsesDuplicateEmail() {
        UUID creatorId = insertCreator();
        String email = uniqueEmail();
        User firstUser = User.create("First user", Role.ADMIN, Identity.createPending(email), creatorId);
        User secondUser = User.create("Second user", Role.OPERATOR, Identity.createPending(email), creatorId);

        persist(firstUser);

        assertThrows(RuntimeException.class, () -> persist(secondUser));

        assertTrue(userRepository.getById(firstUser.getId()).isPresent());
        assertTrue(userRepository.getById(secondUser.getId()).isEmpty());
    }

    @Test
    @DisplayName("Should reject a second super admin at the database and leave no orphan identity")
    void shouldRejectSecondSuperAdmin_whenUniqueIndexIsViolated() throws SQLException {
        PasswordHash hash = PasswordHash.of("$argon2id$v=19$m=65536,t=3,p=1$c2FsdA$aGFzaA");
        Identity firstIdentity = Identity.createActive(uniqueEmail(), hash);
        Identity secondIdentity = Identity.createActive(uniqueEmail(), hash);
        User first = User.create("First super admin", Role.SA, firstIdentity, null);
        User second = User.create("Second super admin", Role.SA, secondIdentity, null);

        persist(first);

        assertThrows(RuntimeException.class, () -> persist(second));

        assertFalse(identityExists(secondIdentity.getId()));
        assertFalse(userExists(second.getId()));
    }

    private void persist(User user) {
        transactionManager.execute(() -> {
            userRepository.create(user);
            return null;
        });
    }

    private UUID insertCreator() {
        UUID identityId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();

        executeSql("""
                INSERT INTO identity (id, email, status_id)
                VALUES (?, ?, 1)
                """, identityId, uniqueEmail());
        executeSql("""
                INSERT INTO admin_user (id, name, identity_id, role_id)
                VALUES (?, ?, ?, 2)
                """, creatorId, "Integration test creator", identityId);

        return creatorId;
    }

    private boolean identityExists(UUID identityId) throws SQLException {
        return rowExists("SELECT 1 FROM identity WHERE id = ?", identityId);
    }

    private boolean userExists(UUID userId) throws SQLException {
        return rowExists("SELECT 1 FROM admin_user WHERE id = ?", userId);
    }

    private boolean rowExists(String query, UUID id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setObject(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private String readPasswordHash(UUID identityId) throws SQLException {
        String query = "SELECT password_hash FROM identity WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setObject(1, identityId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Identity was not found while reading its password hash");
                }

                return resultSet.getString("password_hash");
            }
        }
    }

    private void executeSql(String sql, Object... parameters) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < parameters.length; index++) {
                statement.setObject(index + 1, parameters[index]);
            }

            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to prepare integration test data", exception);
        }
    }

    private String uniqueEmail() {
        return UUID.randomUUID() + "@example.com";
    }
}

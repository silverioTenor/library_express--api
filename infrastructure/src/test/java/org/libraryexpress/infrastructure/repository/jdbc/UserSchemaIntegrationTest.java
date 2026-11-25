package org.libraryexpress.infrastructure.repository.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.identity.enums.IdentityStatus;
import org.libraryexpress.domain.user.enums.Role;
import org.libraryexpress.infrastructure.IntegrationTest;
import org.libraryexpress.infrastructure.config.database.PostgresTestContainerConfig;

@IntegrationTest
@DisplayName("User schema constraints integration tests")
class UserSchemaIntegrationTest extends PostgresTestContainerConfig {
    @Test
    @DisplayName("Should accept the first super admin and reject a second")
    void shouldRejectSecondSuperAdmin_whenRoleIdIsOne() throws SQLException {
        UUID firstIdentity = UUID.randomUUID();
        insertIdentity(firstIdentity, uniqueEmail(), "$argon2id$direct-test-hash", 2);
        insertAdminUserWithRole(UUID.randomUUID(), firstIdentity, 1);

        UUID secondIdentity = UUID.randomUUID();
        insertIdentity(secondIdentity, uniqueEmail(), "$argon2id$direct-test-hash", 2);

        assertThrows(SQLException.class, () -> insertAdminUserWithRole(UUID.randomUUID(), secondIdentity, 1));
    }

    @Test
    @DisplayName("Should accept several users in non-super-admin roles")
    void shouldAcceptOtherRoles_whenSeveralUsersExist() throws SQLException {
        insertRoleUser(2);
        insertRoleUser(3);
        insertRoleUser(4);
    }

    private void insertRoleUser(int roleId) throws SQLException {
        UUID identityId = UUID.randomUUID();
        insertIdentity(identityId, uniqueEmail());
        insertAdminUserWithRole(UUID.randomUUID(), identityId, roleId);
    }

    private void insertAdminUserWithRole(UUID userId, UUID identityId, int roleId) throws SQLException {
        executeSql("""
                INSERT INTO admin_user (id, name, identity_id, role_id)
                VALUES (?, ?, ?, ?)
                """, userId, "Schema test user", identityId, roleId);
    }

    @Test
    @DisplayName("Should seed the configured role and identity status lookup rows")
    void shouldSeedConfiguredLookupRows_whenSchemaIsMigratedFromZero() throws SQLException {
        Map<Integer, String> roles = readLookupRows("role");
        Map<Integer, String> statuses = readLookupRows("identity_status");

        assertEquals(Map.of(1, "SA", 2, "ADMIN", 3, "SUPERVISOR", 4, "OPERATOR"), roles);
        assertEquals(Map.of(1, "PENDING_ACTIVATION", 2, "ACTIVE", 3, "BLOCKED"), statuses);
    }

    @Test
    @DisplayName("Should keep identity status enum names in parity with the seeded table")
    void shouldMatchIdentityStatusNames_whenSchemaIsMigratedFromZero() throws SQLException {
        Set<String> enumNames =
                Arrays.stream(IdentityStatus.values()).map(Enum::name).collect(Collectors.toSet());
        Set<String> tableNames = new HashSet<>(readLookupRows("identity_status").values());

        assertEquals(enumNames, tableNames);
    }

    @Test
    @DisplayName("Should keep role enum names in parity with the seeded table")
    void shouldMatchRoleNames_whenSchemaIsMigratedFromZero() throws SQLException {
        Set<String> enumNames = Arrays.stream(Role.values()).map(Enum::name).collect(Collectors.toSet());
        Set<String> tableNames = new HashSet<>(readLookupRows("role").values());

        assertEquals(enumNames, tableNames);
    }

    @Test
    @DisplayName("Should reject identities whose email differs only by letter case")
    void shouldRejectCaseInsensitiveDuplicateEmail_whenIdentityIsInsertedDirectly() throws SQLException {
        UUID firstIdentityId = UUID.randomUUID();
        UUID secondIdentityId = UUID.randomUUID();
        String firstEmail = "Ana" + UUID.randomUUID() + "@example.com";
        String secondEmail = firstEmail.toLowerCase(java.util.Locale.ROOT);
        insertIdentity(firstIdentityId, firstEmail);

        assertThrows(SQLException.class, () -> insertIdentity(secondIdentityId, secondEmail));
    }

    @Test
    @DisplayName("Should reject a second admin user with the same identity id")
    void shouldRejectDuplicateIdentityReference_whenAdminUsersAreInsertedDirectly() throws SQLException {
        UUID identityId = UUID.randomUUID();
        insertIdentity(identityId, uniqueEmail());
        insertAdminUser(UUID.randomUUID(), identityId);

        assertThrows(SQLException.class, () -> insertAdminUser(UUID.randomUUID(), identityId));
    }

    @Test
    @DisplayName("Should reject nonexistent identity status and role ids through foreign keys")
    void shouldRejectUnknownLookupIds_whenIdentityOrUserIsInsertedDirectly() throws SQLException {
        UUID invalidStatusIdentityId = UUID.randomUUID();

        assertThrows(
                SQLException.class,
                () -> executeSql("""
                        INSERT INTO identity (id, email, password_hash, status_id)
                        VALUES (?, ?, ?, ?)
                        """, invalidStatusIdentityId, uniqueEmail(), "$argon2id$direct-test-hash", 99));

        UUID identityId = UUID.randomUUID();
        insertIdentity(identityId, uniqueEmail());

        assertThrows(SQLException.class, () -> executeSql("""
                        INSERT INTO admin_user (id, name, identity_id, role_id)
                        VALUES (?, ?, ?, ?)
                        """, UUID.randomUUID(), "Invalid role", identityId, 99));
    }

    @Test
    @DisplayName("Should reject pending identities with a hash and active identities without a hash")
    void shouldEnforcePasswordHashCheck_whenIdentityIsInsertedDirectly() {
        assertThrows(
                SQLException.class,
                () -> insertIdentity(UUID.randomUUID(), uniqueEmail(), "$argon2id$direct-test-hash", 1));

        assertThrows(SQLException.class, () -> insertIdentity(UUID.randomUUID(), uniqueEmail(), null, 2));
    }

    private Map<Integer, String> readLookupRows(String tableName) throws SQLException {
        Map<Integer, String> lookupRows = new HashMap<>();
        String query = "SELECT id, name FROM " + tableName;

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                lookupRows.put(resultSet.getInt("id"), resultSet.getString("name"));
            }
        }

        return lookupRows;
    }

    private void insertIdentity(UUID identityId, String email) throws SQLException {
        insertIdentity(identityId, email, null, 1);
    }

    private void insertIdentity(UUID identityId, String email, String passwordHash, int statusId) throws SQLException {
        executeSql("""
                INSERT INTO identity (id, email, password_hash, status_id)
                VALUES (?, ?, ?, ?)
                """, identityId, email, passwordHash, statusId);
    }

    private void insertAdminUser(UUID userId, UUID identityId) throws SQLException {
        executeSql("""
                INSERT INTO admin_user (id, name, identity_id, role_id)
                VALUES (?, ?, ?, ?)
                """, userId, "Schema test user", identityId, 2);
    }

    private void executeSql(String sql, Object... parameters) throws SQLException {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < parameters.length; index++) {
                statement.setObject(index + 1, parameters[index]);
            }

            statement.executeUpdate();
        }
    }

    private String uniqueEmail() {
        return UUID.randomUUID() + "@example.com";
    }
}

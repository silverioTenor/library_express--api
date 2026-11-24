package org.libraryexpress.infrastructure.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import org.libraryexpress.domain.identity.entity.Identity;
import org.libraryexpress.domain.identity.enums.IdentityStatus;
import org.libraryexpress.domain.identity.valueobject.PasswordHash;
import org.libraryexpress.domain.user.entity.User;
import org.libraryexpress.domain.user.enums.Role;
import org.libraryexpress.domain.user.repository.UserRepository;
import org.libraryexpress.infrastructure.config.database.transaction.TransactionContext;
import org.libraryexpress.infrastructure.exception.TransactionException;

public class UserDbRepository implements UserRepository {

    private static final String INSERT_IDENTITY = """
            INSERT INTO identity (id, email, password_hash, status_id, failed_attempts, locked_until)
            VALUES (?, ?, ?, (SELECT id FROM identity_status WHERE name = ?), ?, ?)
            """;

    private static final String INSERT_USER = """
            INSERT INTO admin_user (id, name, identity_id, role_id, created_by)
            VALUES (?, ?, ?, (SELECT id FROM role WHERE name = ?), ?)
            """;

    private static final String SELECT_USER = """
            SELECT u.id AS user_id, u.name, u.created_by, r.name AS role_name,
                   i.id AS identity_id, i.email, i.password_hash, s.name AS status_name,
                   i.failed_attempts, i.locked_until
              FROM admin_user u
              JOIN identity i ON i.id = u.identity_id
              JOIN role r ON r.id = u.role_id
              JOIN identity_status s ON s.id = i.status_id
            """;

    private final DataSource dataSource;

    public UserDbRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void create(User user) {
        if (!TransactionContext.isTransactionActive())
            throw new TransactionException(
                    "Creating a user requires an active transaction: identity and user must be saved together");

        try (Connection connection = dataSource.getConnection()) {
            insertIdentity(connection, user.getIdentity());
            insertUser(connection, user);
        } catch (SQLException e) {
            throw new RuntimeException("Fatal database error during user record insertion: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> getById(UUID id) {
        String query = SELECT_USER + " WHERE u.id = ?";

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setObject(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return Optional.of(mapRowToUser(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fatal database error executing user query by ID: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<User> getByEmail(String email) {
        // Matches the unique functional index ux_identity_email_lower
        String query = SELECT_USER + " WHERE lower(i.email) = lower(?)";

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return Optional.of(mapRowToUser(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Fatal database error executing user query by E-MAIL: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    private void insertIdentity(Connection connection, Identity identity) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_IDENTITY)) {
            statement.setObject(1, identity.getId());
            statement.setString(2, identity.getEmail().value());
            statement.setString(
                    3, identity.getPasswordHash().map(PasswordHash::value).orElse(null));
            statement.setString(4, identity.getStatus().name());
            statement.setInt(5, identity.getFailedAttempts());

            if (identity.getLockedUntil().isPresent())
                statement.setObject(6, identity.getLockedUntil().get().atOffset(ZoneOffset.UTC));
            else statement.setNull(6, Types.TIMESTAMP_WITH_TIMEZONE);

            statement.executeUpdate();
        }
    }

    private void insertUser(Connection connection, User user) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_USER)) {
            statement.setObject(1, user.getId());
            statement.setString(2, user.getName());
            statement.setObject(3, user.getIdentity().getId());
            statement.setString(4, user.getRole().name());

            if (user.getCreatedBy().isPresent())
                statement.setObject(5, user.getCreatedBy().get());
            else statement.setNull(5, Types.OTHER);

            statement.executeUpdate();
        }
    }

    /** Rebuilds the User aggregate, with its Identity, from the joined row. */
    private User mapRowToUser(ResultSet resultSet) throws SQLException {
        OffsetDateTime lockedUntil = resultSet.getObject("locked_until", OffsetDateTime.class);

        Identity identity = Identity.restore(
                resultSet.getObject("identity_id", UUID.class),
                resultSet.getString("email"),
                resultSet.getString("password_hash"),
                IdentityStatus.valueOf(resultSet.getString("status_name")),
                resultSet.getInt("failed_attempts"),
                lockedUntil == null ? null : lockedUntil.toInstant());

        return User.restore(
                resultSet.getObject("user_id", UUID.class),
                resultSet.getString("name"),
                Role.valueOf(resultSet.getString("role_name")),
                identity,
                resultSet.getObject("created_by", UUID.class));
    }
}

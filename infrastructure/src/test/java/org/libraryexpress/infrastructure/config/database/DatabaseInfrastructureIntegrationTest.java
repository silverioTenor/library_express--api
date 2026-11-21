package org.libraryexpress.infrastructure.config.database;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.infrastructure.IntegrationTest;

@IntegrationTest
@DisplayName("Database Infrastructure - Integration Test")
class DatabaseInfrastructureIntegrationTest extends PostgresTestContainerConfig {

    @Test
    @DisplayName("Should verify Flyway migration history records when application schema boots cleanly")
    void shouldReflectSuccessfulMigrationState_whenFlywayExecutesOnFreshContainer() throws Exception {
        String sql = "SELECT success FROM flyway_schema_history WHERE version = '1'";

        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {

            assertTrue(resultSet.next(), "Flyway migration history entry for version 1 must be registered");
            assertTrue(resultSet.getBoolean("success"), "Migration script status descriptor must validate as true");
        }
    }

    @Test
    @DisplayName("Should recover connection pool automatically when the underlying database backend drops connections")
    void shouldRecoverPoolAndExecuteStatement_whenUnderlyingDatabaseConnectionIsForciblyDropped() throws Exception {
        // Force a temporary connection checkout to send the termination signals
        try (Connection systemConnection = dataSource.getConnection();
                Statement killStatement = systemConnection.createStatement()) {

            System.out.println(
                    "[TEST-POOL-RECOVERY] - Simulating network collapse by executing native PostgreSQL backend termination...");

            String killSql = "SELECT pg_terminate_backend(pid) FROM pg_stat_activity "
                    + "WHERE datname = 'library_express_test' AND pid <> pg_backend_pid()";

            killStatement.execute(killSql);
        }

        // Small yield sleep for container socket stabilization
        Thread.sleep(300);

        // Assert - Subsequent borrow from the pool must trigger an automatic eviction and drop healing mechanism
        assertDoesNotThrow(
                () -> {
                    try (Connection freshConnection = dataSource.getConnection();
                            Statement verificationStatement = freshConnection.createStatement()) {
                        verificationStatement.execute("SELECT 1");
                    }
                },
                "HikariCP pool manager must automatically heal from network connectivity losses without manual runtime intervention");
    }
}

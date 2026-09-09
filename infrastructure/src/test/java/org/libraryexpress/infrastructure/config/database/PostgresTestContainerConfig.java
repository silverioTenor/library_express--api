package org.libraryexpress.infrastructure.config.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;

public abstract class PostgresTestContainerConfig {

    protected static final PostgreSQLContainer postgresContainer;
    protected static final DataSource dataSource;

    static {
        System.out.println("[TESTCONTAINERS] - Reading integration profile configuration sheet...");
        Properties testProperties = loadTestProperties();

        System.out.println("[TESTCONTAINERS] - Constructing ephemeral PostgreSQL docker metadata...");

        postgresContainer = new PostgreSQLContainer("postgres:17-alpine");

        // Routing real-time container log buffers straight to standard system stdout channels
        postgresContainer.withLogConsumer(outputFrame ->
                System.out.print("[POSTGRES-DOCKER] " + outputFrame.getUtf8String())
        );

        System.out.println("[TESTCONTAINERS] - Launching container instance...");
        postgresContainer.start();
        System.out.println("[TESTCONTAINERS] - Container initialized on mapped port: " + postgresContainer.getFirstMappedPort());

        // Building the runtime connection pooling stack using properties files parameters
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgresContainer.getJdbcUrl());
        config.setUsername(postgresContainer.getUsername());
        config.setPassword(postgresContainer.getPassword());
        config.setDriverClassName(postgresContainer.getDriverClassName());

        config.setMaximumPoolSize(Integer.parseInt(testProperties.getProperty("hikari.maximum-pool-size", "5")));
        config.setMinimumIdle(Integer.parseInt(testProperties.getProperty("hikari.minimum-idle", "1")));
        config.setIdleTimeout(Long.parseLong(testProperties.getProperty("hikari.idle-timeout", "60000")));
        config.setConnectionTimeout(Long.parseLong(testProperties.getProperty("hikari.connection-timeout", "15000")));

        HikariDataSource hikariDataSource = new HikariDataSource(config);
        dataSource = hikariDataSource;

        System.out.println("[FLYWAY] - Triggering schema creation scripts...");
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();

        flyway.migrate();
        System.out.println("[FLYWAY] - Migration completed. Core infrastructure validation baseline active.");

        // Graceful shutdown orchestration tracking JVM lifecycle completions
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[SHUTDOWN-HOOK] - Terminating test session. Releasing resources...");
            hikariDataSource.close();
            if (postgresContainer != null && postgresContainer.isRunning()) {
                System.out.println("[SHUTDOWN-HOOK] - Destroying PostgreSQL container instance...");
                postgresContainer.stop();
                System.out.println("[SHUTDOWN-HOOK] - Container terminated successfully. Goodbye!");
            }
        }));
    }

    private static Properties loadTestProperties() {
        Properties props = new Properties();
        try (InputStream input = PostgresTestContainerConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                System.err.println("Warning: application.properties not found inside test resources classpath.");
                return props;
            }
            props.load(input);
        } catch (IOException e) {
            System.err.println("Error reading test connection properties file: " + e.getMessage());
        }
        return props;
    }

    @AfterEach
    void tearDownDatabaseState() {
        String sql = "TRUNCATE TABLE tb_loan, tb_book, tb_customer RESTART IDENTITY CASCADE";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(sql);

        } catch (Exception e) {
            System.err.println("[CLEANUP-ERROR] - Failed to clear database tables: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}

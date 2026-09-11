package org.libraryexpress.infrastructure.api.e2e;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;
import org.libraryexpress.infrastructure.api.server.EmbeddedHttpServer;
import org.libraryexpress.infrastructure.config.AppBootstrapper;
import org.libraryexpress.infrastructure.config.logging.Slf4jLoggerAdapter;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Baseline Architectural E2E Test class.
 * Orchestrates Testcontainers PostgreSQL lifecycles alongside our official AppBootstrapper lifecycles.
 */
public abstract class E2EBaseConfig {

    protected static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    private static final AtomicBoolean serverStarted = new AtomicBoolean(false);

    private static final CustomLogger logger;

    static {
        CustomLoggerFactory.initialize(Slf4jLoggerAdapter::new);
        logger = CustomLoggerFactory.getLogger(E2EBaseConfig.class);
    }

    @BeforeAll
    static void setup() {
        if (!postgres.isRunning()) postgres.start();

        if (serverStarted.compareAndSet(false, true)) {

            System.setProperty("DB_NAME", postgres.getDatabaseName());
            System.setProperty("DB_USER", postgres.getUsername());
            System.setProperty("DB_PASSWORD", postgres.getPassword());
            System.setProperty("DB_PORT", postgres.getMappedPort(5432).toString());

            AppBootstrapper.boot();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                postgres.stop();

                System.clearProperty("DB_NAME");
                System.clearProperty("DB_USER");
                System.clearProperty("DB_PASSWORD");
                System.clearProperty("DB_PORT");
            }));
        }

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;
    }

    @AfterAll
    static void cleanDatabaseState() {
        logger.info("Cleaning database...");
        String truncateSql = "TRUNCATE TABLE tb_loan, tb_book, tb_customer RESTART IDENTITY CASCADE";

        String jdbcUrl = postgres.getJdbcUrl();
        String dbUser = postgres.getUsername();
        String dbPassword = postgres.getPassword();

        try (var conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
             var stmt = conn.createStatement()) {

            stmt.executeUpdate(truncateSql);

            logger.info("Database truncated successfully");

        } catch (SQLException e) {
            logger.error("Failed to execute truncate on the database: " + e.getMessage(), e);
        }
    }
}
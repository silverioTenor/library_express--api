package org.libraryexpress.infrastructure;

import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;
import org.libraryexpress.infrastructure.cli.ManagementCli;
import org.libraryexpress.infrastructure.config.AppContext;
import org.libraryexpress.infrastructure.config.database.ConnectionProvider;
import org.libraryexpress.infrastructure.config.database.MigrationRunner;
import org.libraryexpress.infrastructure.config.logging.CorrelationIdSupport;
import org.libraryexpress.infrastructure.config.logging.Slf4jLoggerAdapter;

import javax.sql.DataSource;

/**
 * Main application bootstrapper.
 * Responsible for orchestration, dependency injection initialization, and resource lifecycle management.
 */
public class Application {

    private static CustomLogger logger;

    public static void main(String[] args) {
        // Step 1: Immediate wire injection of the logging Anti-Corruption Layer
        CustomLoggerFactory.initialize(Slf4jLoggerAdapter::new);
        logger = CustomLoggerFactory.getLogger(Application.class);

        logger.info("Starting LibraryExpress infrastructure pipeline...");

        ConnectionProvider connectionProvider = prepareDatabaseConnection();

        AppContext context = new AppContext(connectionProvider);

        logger.info("Application booted successfully! Ready for executions.");

        initCLI(context, connectionProvider);
    }

    private static void initCLI(AppContext context, ConnectionProvider connectionProvider) {
        var mgmt = new ManagementCli(context, connectionProvider);
        mgmt.app();
    }

    private static ConnectionProvider prepareDatabaseConnection() {
        // 1. Initialize the managed connection pool resource
        ConnectionProvider connectionProvider = new ConnectionProvider();

        try {
            DataSource dataSource = connectionProvider.getDataSource();

            // 2. Execute Flyway database schema migrations
            logger.info("Executing schema migrations...");
            MigrationRunner.run(dataSource);
            logger.info("Database schema is fully synchronized!");

            // 3. Register a graceful shutdown hook to release database resources on JVM exit
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    CorrelationIdSupport.start();
                    logger.info("Shutting down connection pool gracefully...");
                    connectionProvider.close();
                    logger.info("Database resources released safely.");
                } finally {
                    CorrelationIdSupport.clear();
                }
            }));

        } catch (Exception e) {
            logger.error("FATAL: Application startup failed dynamically: " + e.getMessage(), e);
            connectionProvider.close();
            System.exit(1);
        }

        return connectionProvider;
    }
}
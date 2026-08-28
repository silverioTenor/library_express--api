package org.libraryexpress.infrastructure.config;

import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;
import org.libraryexpress.infrastructure.api.server.EmbeddedHttpServer;
import org.libraryexpress.infrastructure.cli.ManagementCli;
import org.libraryexpress.infrastructure.config.database.ConnectionProvider;
import org.libraryexpress.infrastructure.config.database.MigrationRunner;
import org.libraryexpress.infrastructure.config.logging.CorrelationIdSupport;
import org.libraryexpress.infrastructure.config.logging.Slf4jLoggerAdapter;

import javax.sql.DataSource;

public class AppBootstrapper {

    private static CustomLogger log;

    private AppBootstrapper() {}

    /**
     * Executes the foundational multi-stage setup sequence and launches the active interface.
     */
    public static void boot() {
        // Stage 1: Immediate framework-agnostic logging ACL registration
        CustomLoggerFactory.initialize(Slf4jLoggerAdapter::new);
        log = CustomLoggerFactory.getLogger(AppBootstrapper.class);

        log.info("Starting LibraryExpress core infrastructure bootstrap sequence...");

        // Stage 2: Initialize database layers and execute Flyway schema migrations
        ConnectionProvider connectionProvider = prepareDatabaseConnection();
        AppContext context = new AppContext(connectionProvider);

        log.info("Infrastructure baseline loaded successfully! Routing to application entrypoint.");

        initializeApiInterface(context);
//        initializeCliInterface(context, connectionProvider);
    }

    private static void initializeApiInterface(AppContext context) {
        EmbeddedHttpServer server = new EmbeddedHttpServer(context);
        server.start();
    }

    private static void initializeCliInterface(AppContext context, ConnectionProvider connectionProvider) {
        var managementCli = new ManagementCli(context, connectionProvider);
        managementCli.app();
    }

    private static ConnectionProvider prepareDatabaseConnection() {
        ConnectionProvider connectionProvider = new ConnectionProvider();

        try {
            DataSource dataSource = connectionProvider.getDataSource();

            log.info("Executing relational schema migrations via Flyway...");
            MigrationRunner.run(dataSource);
            log.info("Database schema state is fully synchronized!");

            // Registering the standard fallback graceful cleanup shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    CorrelationIdSupport.start();
                    log.info("JVM exit signal intercepted. Shutting down connection pool gracefully...");
                    connectionProvider.close();
                    log.info("Database connection resources released cleanly.");
                } finally {
                    CorrelationIdSupport.clear();
                }
            }));

        } catch (Exception e) {
            log.error("FATAL: Application bootstrap failed due to infrastructure collapse: " + e.getMessage(), e);
            connectionProvider.close();
            System.exit(1);
        }

        return connectionProvider;
    }
}

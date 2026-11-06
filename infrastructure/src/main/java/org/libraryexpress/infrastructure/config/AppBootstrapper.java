package org.libraryexpress.infrastructure.config;

import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;
import org.libraryexpress.infrastructure.api.server.EmbeddedHttpServer;
import org.libraryexpress.infrastructure.config.database.ConnectionProvider;
import org.libraryexpress.infrastructure.config.database.MigrationRunner;
import org.libraryexpress.infrastructure.config.logging.LogTrace;
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

        initializeApi(context, connectionProvider);
    }

    private static void initializeApi(AppContext context, ConnectionProvider connectionProvider) {
        EmbeddedHttpServer server = new EmbeddedHttpServer(context);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                LogTrace.start();
                log.info("JVM exit signal intercepted. Executing cascading graceful shutdown sequence...");

                // Step 1: Close the entrance door (No one else enters the application)
                log.info("Stopping HTTP server layer...");
                server.stop();

                // Step 2: Close the application services (Future SesV2Client within the AppContext)
//                log.info("Closing application contexts and clients...");
                // context.close();

                // Step 3: Shut down the database (Ensures that the final queries have run)
                log.info("Shutting down relational connection pool...");
                connectionProvider.close();

                log.info("LibraryExpress core infrastructure released cleanly. Farewell!");
            } catch (Exception e) {
                log.error("Error encountered during graceful shutdown cascade", e);
            } finally {
                LogTrace.clear();
            }
        }));
    }

    private static ConnectionProvider prepareDatabaseConnection() {
        ConnectionProvider connectionProvider = new ConnectionProvider();

        try {
            DataSource dataSource = connectionProvider.getDataSource();

            log.info("Executing relational schema migrations via Flyway...");
            MigrationRunner.run(dataSource);
            log.info("Database schema state is fully synchronized!");

        } catch (Exception e) {
            log.error("FATAL: Application bootstrap failed due to infrastructure collapse: " + e.getMessage(), e);
            connectionProvider.close();
            System.exit(1);
        }

        return connectionProvider;
    }
}

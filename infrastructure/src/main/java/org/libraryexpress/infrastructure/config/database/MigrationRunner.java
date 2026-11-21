package org.libraryexpress.infrastructure.config.database;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;

/**
 * Encapsulates the execution logic for database migrations via Flyway. Ensures the physical schema is fully
 * synchronized before business logic layers ignite.
 */
public class MigrationRunner {

    /** Executes database migration scripts found under the standard resources' folder. */
    public static void run(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration") // Pointing directly to our resources folder
                .baselineOnMigrate(true) // Safeguard for existing database baselines
                .load();

        // Performs schema evolution evaluation and update execution
        flyway.migrate();
    }
}

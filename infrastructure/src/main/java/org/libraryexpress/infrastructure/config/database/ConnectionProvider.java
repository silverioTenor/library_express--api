package org.libraryexpress.infrastructure.config.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.libraryexpress.infrastructure.config.ConfigRegistry;

import javax.sql.DataSource;

public class ConnectionProvider implements AutoCloseable {

    private final HikariDataSource dataSource;

    public ConnectionProvider() {
        String jdbcUrl = String.format(
                "jdbc:postgresql://%s:%s/%s",
                ConfigRegistry.getDbHost(),
                ConfigRegistry.getDbPort(),
                ConfigRegistry.getDbName()
        );

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(ConfigRegistry.getDbUser());
        config.setPassword(ConfigRegistry.getDbPassword());

        if (ConfigRegistry.getAppEnv().equals("prod")) {
            config.addDataSourceProperty("sslmode", "require");
            config.addDataSourceProperty("channelBinding", "require");
        }

        config.setMaximumPoolSize(ConfigRegistry.getMaximumPoolSize());
        config.setMinimumIdle(ConfigRegistry.getMinimumIdle());
        config.setIdleTimeout(ConfigRegistry.getIdleTimeout());
        config.setConnectionTimeout(ConfigRegistry.getConnectionTimeout());
        config.setMaxLifetime(ConfigRegistry.getMaxLifetime());

        this.dataSource = new HikariDataSource(config);
    }

    /**
     * Exposes the configured DataSource abstraction to repositories or migration tools.
     */
    public DataSource getDataSource() {
        return this.dataSource;
    }

    /**
     * Implements AutoCloseable to ensure thread-safe cleanup during shutdown hooks or test tearing steps.
     */
    @Override
    public void close() {
        if (this.dataSource != null && !this.dataSource.isClosed()) {
            this.dataSource.close();
        }
    }
}

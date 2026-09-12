package org.libraryexpress.infrastructure.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;

import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class ConfigRegistry {

    private static final CustomLogger logger = CustomLoggerFactory.getLogger(ConfigRegistry.class);

    private static final Dotenv dotenv;
    private static final Properties properties;

    static {
        dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        properties = new Properties();

        try (InputStream input = ConfigRegistry.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                logger.warn("Warning: application.properties not found. Relying on default parameters.");
            }
        } catch (Exception e) {
            logger.error("Error reading connection properties file: " + e.getMessage(), e);
        }
    }

    private ConfigRegistry() {}

    public static int getApiPort() {
        return Integer.parseInt(resolve("API_PORT", "3000"));
    }

    public static String getDbHost() {
        return resolve("DB_HOST", "localhost");
    }

    public static String getDbPort() {
        return resolve("DB_PORT", "5432");
    }

    public static String getDbName() {
        return resolve("DB_NAME", "library_express");
    }

    public static String getDbUser() {
        return resolve("DB_USER", null);
    }

    public static String getDbPassword() {
        return resolve("DB_PASSWORD", null);
    }

    public static int getMaximumPoolSize() {
        return Integer.parseInt(resolve("hikari.maximum-pool-size", "10"));
    }

    public static int getMinimumIdle() {
        return Integer.parseInt(resolve("hikari.minimum-idle", "2"));
    }

    public static long getIdleTimeout() {
        return Long.parseLong(resolve("hikari.idle-timeout", "600000"));
    }

    public static long getConnectionTimeout() {
        return Long.parseLong(resolve("hikari.connection-timeout", "30000"));
    }

    private static String resolve(String key, String defaultValue) {
        if (key == null || key.isBlank()) return defaultValue;

        return Stream.<Supplier<String>>of(
                        () -> dotenv.get(key),
                        () -> System.getenv(key),
                        () -> System.getProperty(key),
                        () -> properties.getProperty(key)
                )
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .filter(value -> !value.isBlank())
                .findFirst()
                .orElseGet(() -> {
                    if (defaultValue == null || defaultValue.isBlank()) {
                        throw new IllegalStateException(String.format(
                                "Fatal Boot Error: Configuration key [%s] is mandatory but was not defined!", key
                        ));
                    }
                    return defaultValue;
                });
    }
}

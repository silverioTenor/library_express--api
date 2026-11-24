package org.libraryexpress.infrastructure.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;

public final class ConfigRegistry {

    private static final CustomLogger LOG = CustomLoggerFactory.getLogger(ConfigRegistry.class);

    private static final ConcurrentMap<String, String> RESOLVED_VALUES = new ConcurrentHashMap<>();

    public static final String PG_LOCK_NOT_AVAILABLE = "55P03";

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

    public static String getAppEnv() {
        return resolve("APP_ENV", "prod");
    }

    public static String getGoogleBooksApiKey() {
        return resolve("GOOGLE_BOOKS_API_KEY", null);
    }

    public static int getMaximumPoolSize() {
        return Integer.parseInt(resolve("hikari.maximum-pool-size", "10"));
    }

    public static int getMinimumIdle() {
        return Integer.parseInt(resolve("hikari.minimum-idle", "0"));
    }

    public static long getIdleTimeout() {
        return Long.parseLong(resolve("hikari.idle-timeout", "30000"));
    }

    public static long getConnectionTimeout() {
        return Long.parseLong(resolve("hikari.connection-timeout", "30000"));
    }

    public static long getMaxLifetime() {
        return Long.parseLong(resolve("hikari.max-lifetime", "900000"));
    }

    public static long getDbLockTimeOut() {
        return Long.parseLong(resolve("db.lock.timeout-ms", "3000"));
    }

    public static long getArgon2MemoryKib() {
        return Long.parseLong(resolve("argon2.memory-kib", "19456"));
    }

    public static long getArgon2Iterations() {
        return Long.parseLong(resolve("argon2.iterations", "2"));
    }

    public static long getArgon2Parallelism() {
        return Long.parseLong(resolve("argon2.parallelism", "1"));
    }

    private static String resolve(String key, String defaultValue) {
        if (key == null || key.isBlank()) return defaultValue;

        return RESOLVED_VALUES.computeIfAbsent(key, ignored -> resolveUncached(key, defaultValue));
    }

    private static String resolveUncached(String key, String defaultValue) {
        String value = isValid(System.getProperty(key));
        if (value != null) return value;

        value = isValid(System.getenv(key));
        if (value != null) return value;

        value = isValid(Sources.DOTENV.get(key));
        if (value != null) return value;

        value = isValid(Sources.PROPERTIES.getProperty(key));
        if (value != null) return value;

        if (defaultValue == null || defaultValue.isBlank()) {
            throw new IllegalStateException(
                    String.format("Fatal Boot Error: Configuration key [%s] is mandatory but was not defined!", key));
        }

        return defaultValue;
    }

    private static String isValid(String value) {
        if (value == null || value.isBlank()) return null;
        return value;
    }

    private static final class Sources {

        private static final Dotenv DOTENV =
                Dotenv.configure().ignoreIfMissing().load();
        private static final Properties PROPERTIES = loadProperties();

        private static Properties loadProperties() {
            Properties properties = new Properties();

            try (InputStream input =
                    ConfigRegistry.class.getClassLoader().getResourceAsStream("application.properties")) {
                if (input != null) {
                    properties.load(input);
                } else {
                    LOG.warn("Warning: application.properties not found. Relying on default parameters.");
                }
            } catch (IOException e) {
                LOG.error("Error reading connection properties file: " + e.getMessage(), e);
            }

            return properties;
        }
    }
}

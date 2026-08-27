package org.libraryexpress.infrastructure.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public final class ConfigRegistry {

    private static final CustomLogger logger = CustomLoggerFactory.getLogger(ConfigRegistry.class);

    private static final Dotenv dotenv;
    private static final Properties properties;

    static {
        // Step 1: Initialize Dotenv to read the .env file from the project root directory
        dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        // Step 2: Internal helper to load configuration streams directly from resources classpath
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
        return Integer.parseInt(getEnv("API_PORT", "3000"));
    }

    public static String getDbHost() {
        return getEnv("DB_HOST", "localhost");
    }

    public static String getDbPort() {
        return getEnv("DB_PORT", "5432");
    }

    public static String getDbName() {
        return getEnv("DB_NAME", "library_express");
    }

    public static String getDbUser() {
        return getEnv("DB_USER", null);
    }

    public static String getDbPassword() {
        return getEnv("DB_PASSWORD", null);
    }

    public static int getMaximumPoolSize() {
        return Integer.parseInt(getProperty("hikari.maximum-pool-size", "10"));
    }

    public static int getMinimumIdle() {
        return Integer.parseInt(getProperty("hikari.minimum-idle", "2"));
    }

    public static long getIdleTimeout() {
        return Long.parseLong(getProperty("hikari.idle-timeout", "600000"));
    }

    public static long getConnectionTimeout() {
        return Long.parseLong(getProperty("hikari.connection-timeout", "30000"));
    }

    private static String getEnv(String envKey, String defaultValue) {
        return resolve(envKey, null, defaultValue);
    }

    private static String getProperty(String propKey, String defaultValue) {
        return resolve(null, propKey, defaultValue);
    }

    private static String resolve(String envKey, String propKey, String defaultValue) {
        if (envKey != null) {
            String systemEnv = System.getenv(envKey);
            if (systemEnv != null && !systemEnv.isBlank()) return systemEnv;

            if (dotenv != null) {
                String dotenvValue = dotenv.get(envKey);
                if (dotenvValue != null && !dotenvValue.isBlank()) return dotenvValue;
            }

            if ((propKey == null || propKey.isBlank()) && (defaultValue == null || defaultValue.isBlank())) {
                throw new IllegalStateException(String.format(
                        "Fatal Boot Error: Environment configuration key variable [%s] is mandatory but was not defined!",
                        envKey
                ));
            }
        }

        if (propKey != null && properties != null) {
            String propValue = properties.getProperty(propKey);
            if (propValue != null && !propValue.isBlank()) return propValue;
        }

        return defaultValue;
    }

}

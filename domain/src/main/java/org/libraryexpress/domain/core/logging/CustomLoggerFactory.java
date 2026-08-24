package org.libraryexpress.domain.core.logging;

import java.util.function.Function;

/**
 * Registry Factory managing the injection hook for the concrete logging adapter.
 */
public final class CustomLoggerFactory {

    private static Function<Class<?>, CustomLogger> provider;

    private CustomLoggerFactory() {
        // Enforcing static utility factory pattern structures
    }

    /**
     * Boundary registration hook used exclusively by the infrastructure layer bootstrap.
     */
    public static void initialize(Function<Class<?>, CustomLogger> loggerProvider) {
        provider = loggerProvider;
    }

    /**
     * Resolves and provides the clean decoupled logger instance for domain components.
     */
    public static CustomLogger getLogger(Class<?> clazz) {
        if (provider == null) {
            // Safe fallback during bootstrap or lightweight domain unit tests execution
            return new NoOpCustomLogger();
        }
        return provider.apply(clazz);
    }

    /**
     * Defensive fallback implementation to prevent NullPointerExceptions during early isolation tests.
     */
    private static final class NoOpCustomLogger implements CustomLogger {
        @Override public void info(String m, Object... a) {}
        @Override public void warn(String m, Object... a) {}
        @Override public void error(String m, Object... a) {}
        @Override public void debug(String m, Object... a) {}
    }
}

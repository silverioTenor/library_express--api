package org.libraryexpress.infrastructure.config.logging;

import org.libraryexpress.domain.core.logging.CustomLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete Anti-Corruption Layer adapter mapping domain logging interfaces
 * straight into native high-performance SLF4J + Logback runtime contexts.
 */
public class Slf4jLoggerAdapter implements CustomLogger {

    private final Logger slf4jLogger;

    public Slf4jLoggerAdapter(Class<?> clazz) {
        this.slf4jLogger = LoggerFactory.getLogger(clazz);
    }

    @Override
    public void info(String message, Object... args) {
        if (slf4jLogger.isInfoEnabled()) {
            slf4jLogger.info(message, args);
        }
    }

    @Override
    public void warn(String message, Object... args) {
        if (slf4jLogger.isWarnEnabled()) {
            slf4jLogger.warn(message, args);
        }
    }

    @Override
    public void error(String message, Object... args) {
        if (slf4jLogger.isErrorEnabled()) {
            slf4jLogger.error(message, args);
        }
    }

    @Override
    public void debug(String message, Object... args) {
        if (slf4jLogger.isDebugEnabled()) {
            slf4jLogger.debug(message, args);
        }
    }
}

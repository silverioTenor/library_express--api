package org.libraryexpress.domain.core.logging;

public interface CustomLogger {

    void info(String message, Object... args);

    void warn(String message, Object... args);

    void error(String message, Object... args);

    void debug(String message, Object... args);
}

package org.libraryexpress.infrastructure.config.logging;

import org.slf4j.MDC;
import java.util.UUID;

/**
 * Technical helper governing the Mapped Diagnostic Context (MDC) lifecycle.
 * Manages unique correlation identifiers across thread execution frames.
 */
public final class CorrelationIdSupport {

    private static final String CORRELATION_ID_KEY = "correlation_id";

    private CorrelationIdSupport() {}

    /**
     * Generates a unique tracking token and binds it into the active MDC thread scope.
     */
    public static String start() {
        String uniqueId = UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID_KEY, uniqueId);
        return uniqueId;
    }

    public static void start(String correlationId) {
        if (correlationId == null || correlationId.isBlank()) {
            start();
            return;
        }
        MDC.put(CORRELATION_ID_KEY, correlationId);
    }

    public static String get() {
        return MDC.get(CORRELATION_ID_KEY);
    }

    /**
     * Clears the correlation key to protect the thread allocation pool from memory leakages.
     */
    public static void clear() {
        MDC.remove(CORRELATION_ID_KEY);
    }
}

package org.libraryexpress.infrastructure.config.logging;

import org.slf4j.MDC;
import java.util.UUID;

/**
 * Technical helper governing the Mapped Diagnostic Context (MDC) lifecycle.
 * Manages unique correlation identifiers across thread execution frames.
 */
public final class LogTrace {

    private static final String TRACE_ID_KEY = "trace_id";

    private LogTrace() {}

    /**
     * Generates a unique tracking token and binds it into the active MDC thread scope.
     */
    public static String start() {
        String uniqueId = UUID.randomUUID().toString();
        MDC.put(TRACE_ID_KEY, uniqueId);
        return uniqueId;
    }

    public static void start(String traceId) {
        if (traceId == null || traceId.isBlank()) {
            start();
            return;
        }
        MDC.put(TRACE_ID_KEY, traceId);
    }

    public static String get() {
        return MDC.get(TRACE_ID_KEY);
    }

    /**
     * Clears the correlation key to protect the thread allocation pool from memory leakages.
     */
    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
    }
}

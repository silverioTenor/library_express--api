package org.libraryexpress.infrastructure.config.database;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.infrastructure.IntegrationTest;
import org.libraryexpress.infrastructure.config.logging.LogTrace;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;

@IntegrationTest
@DisplayName("MDC Trace ID - Lifecycle Integration Test")
class MdcTraceIdIntegrationTest {

    @Test
    @DisplayName("Should inject, share and cleanly purge tracking trace tokens across execution context loops")
    void shouldManageMdcLifecycleCleanly_whenTriggeringTrackingScopes() {
        // Given (Scenario 1)
        assertNull(MDC.get("trace_id"), "MDC context must start completely clean");

        // When
        LogTrace.start();
        String activeToken = MDC.get("trace_id");

        // Then (Scenario 2)
        assertNotNull(activeToken, "A unique structural tracking UUID token must be generated into the context");
        assertFalse(activeToken.trim().isEmpty());

        // Simulating twin statements executions sharing the exact same tracking scope references
        String secondaryVerificationToken = MDC.get("trace_id");
        assertEquals(activeToken, secondaryVerificationToken, "Every log line in the current execution window must preserve identity trace");

        // When (Scenario 3)
        LogTrace.clear();

        // Then
        assertNull(MDC.get("trace_id"), "MDC metadata context must be completely purged upon operation completion");
    }
}

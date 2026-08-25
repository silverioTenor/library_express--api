package org.libraryexpress.infrastructure.config.database;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.infrastructure.IntegrationTest;
import org.libraryexpress.infrastructure.config.logging.CorrelationIdSupport;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;

@IntegrationTest
@DisplayName("MDC Correlation ID - Lifecycle Integration Test")
class MdcCorrelationIdIntegrationTest {

    @Test
    @DisplayName("Should inject, share and cleanly purge tracking correlation tokens across execution context loops")
    void shouldManageMdcLifecycleCleanly_whenTriggeringTrackingScopes() {
        // Given (Scenario 1)
        assertNull(MDC.get("correlation_id"), "MDC context must start completely clean");

        // When
        CorrelationIdSupport.start();
        String activeToken = MDC.get("correlation_id");

        // Then (Scenario 2)
        assertNotNull(activeToken, "A unique structural tracking UUID token must be generated into the context");
        assertFalse(activeToken.trim().isEmpty());

        // Simulating twin statements executions sharing the exact same tracking scope references
        String secondaryVerificationToken = MDC.get("correlation_id");
        assertEquals(activeToken, secondaryVerificationToken, "Every log line in the current execution window must preserve identity correlation");

        // When (Scenario 3)
        CorrelationIdSupport.clear();

        // Then
        assertNull(MDC.get("correlation_id"), "MDC metadata context must be completely purged upon operation completion");
    }
}

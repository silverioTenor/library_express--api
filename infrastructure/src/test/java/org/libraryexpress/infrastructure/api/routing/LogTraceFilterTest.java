package org.libraryexpress.infrastructure.api.routing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.infrastructure.UnitTest;
import org.libraryexpress.infrastructure.config.logging.LogTrace;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@UnitTest
@DisplayName("LogTraceFilter - Unit Test")
public class LogTraceFilterTest {

    private static final String TRACE_HEADER = "X-Trace-Id";

    private LogTraceFilter filter;
    private HttpContextRequest request;
    private HttpContextResponse response;
    private CustomHttpHandler nextHandler;

    @BeforeEach
    void setUp() {
        filter = new LogTraceFilter();
        request = mock(HttpContextRequest.class);
        response = mock(HttpContextResponse.class, Mockito.RETURNS_SELF);
        nextHandler = mock(CustomHttpHandler.class);
        LogTrace.clear(); // Ensuring isolation across execution frames
    }

    @AfterEach
    void tearDown() {
        LogTrace.clear();
    }

    @Test
    @DisplayName("Should accept and trust the inbound correlation token provided by the client header")
    void shouldAcceptInboundTraceId_whenPresentInRequestHeaders() throws Exception {
        // Arrange
        String existingTrackToken = UUID.randomUUID().toString();
        when(request.getHeader(TRACE_HEADER)).thenReturn(existingTrackToken);

        // Act
        filter.doFilter(request, response, nextHandler);

        // Assert
        // Task Solution: Direct, crisp verification checking if headers and delegation triggers fired correctly
        verify(nextHandler, times(1)).handle(request, response);
        verify(response, times(1)).setHeader(TRACE_HEADER, existingTrackToken);
        assertNull(LogTrace.get(), "MDC context must be completely purged from thread allocation buffers upon execution completion");
    }

    @Test
    @DisplayName("Should generate a unique tracking correlation UUID token automatically when client header is absent")
    void shouldGenerateNewTraceId_whenRequestHeaderIsAbsentOrEmpty() throws Exception {
        // Arrange
        when(request.getHeader(TRACE_HEADER)).thenReturn(null);

        // Act
        doAnswer(invocation -> {
            String activeMdcToken = LogTrace.get();
            assertNotNull(activeMdcToken, "A fallback tracking token must be dynamically seeded into the active execution thread mapping");
            assertFalse(activeMdcToken.isBlank());
            return null;
        }).when(nextHandler).handle(any(), any());

        filter.doFilter(request, response, nextHandler);

        // Assert
        // Verifying if response header method intercept captured any valid generated string entry
        verify(response, times(1)).setHeader(eq(TRACE_HEADER), anyString());
        assertNull(LogTrace.get(), "MDC framework state must clear references to protect virtual thread recycling pipelines");
    }

    @Test
    @DisplayName("Should guarantee complete context clearing protect hooks even when downstream controller handlers fail")
    void shouldEnforceMdcPurge_evenWhenDownstreamHandlerFailsCatastrophically() throws Exception {
        // Arrange
        when(request.getHeader(TRACE_HEADER)).thenReturn("FAULTY-TRACK-ID");
        doThrow(new RuntimeException("Severe database connection dropped during controller frame execution"))
                .when(nextHandler).handle(any(), any());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> filter.doFilter(request, response, nextHandler),
                "Downstream business use case failure signals must bubble up transparently");

        // Core Rationale Verification: Despite the failure, the finally block MUST execute defensive purge steps
        assertNull(LogTrace.get(), "The structural framework must enforce absolute MDC clearance to prevent cross-request leakage pollution");
    }
}

package org.libraryexpress.infrastructure.api.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.libraryexpress.domain.book.exception.BookUnavailableException;
import org.libraryexpress.infrastructure.UnitTest;
import org.libraryexpress.infrastructure.api.contract.ErrorResponse;
import org.libraryexpress.infrastructure.api.enums.HttpStatusCode;
import org.libraryexpress.infrastructure.api.routing.CustomHttpHandler;
import org.libraryexpress.infrastructure.api.routing.HttpContextRequest;
import org.libraryexpress.infrastructure.api.routing.HttpContextResponse;
import org.libraryexpress.infrastructure.exception.InfrastructureErrorType;
import org.libraryexpress.infrastructure.exception.InfrastructureException;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@UnitTest
@DisplayName("Global Exception Handler - Unit Test")
class GlobalExceptionHandlerTest {

    @Test
    @DisplayName("Should map domain business rule exceptions straight into 422 Unprocessable Entity statuses")
    void shouldMapTo422_whenDomainBusinessRuleViolationOccurs() throws Exception {
        // Arrange
        CustomHttpHandler mockedDelegate = mock(CustomHttpHandler.class);
        doThrow(new BookUnavailableException("Target book is checked out")).when(mockedDelegate).handle(any(), any());

        GlobalExceptionHandler handler = new GlobalExceptionHandler(mockedDelegate);
        HttpContextRequest req = mock(HttpContextRequest.class);
        HttpContextResponse res = mock(HttpContextResponse.class, RETURNS_SELF);

        // Act
        handler.handle(req, res);

        // Assert
        verify(res, times(1)).status(HttpStatusCode.UNPROCESSABLE_ENTITY);
        ArgumentCaptor<ErrorResponse> responseCaptor = ArgumentCaptor.forClass(ErrorResponse.class);
        verify(res).json(responseCaptor.capture());

        assertEquals("Target book is checked out", responseCaptor.getValue().message());
    }

    @Test
    @DisplayName("Should completely sanitize technical internal text messages when intercepting unexpected 500 collapses")
    void shouldSanitizeMessage_whenUnexpectedCatastropheOccurs() throws Exception {
        // Arrange
        CustomHttpHandler mockedDelegate = mock(CustomHttpHandler.class);
        doThrow(new NullPointerException("Severe core database socket dropped failure")).when(mockedDelegate).handle(any(), any());

        GlobalExceptionHandler handler = new GlobalExceptionHandler(mockedDelegate);
        HttpContextRequest req = mock(HttpContextRequest.class);
        HttpContextResponse res = mock(HttpContextResponse.class, RETURNS_SELF);

        // Act
        handler.handle(req, res);

        // Assert
        verify(res, times(1)).status(HttpStatusCode.INTERNAL_SERVER_ERROR);
        ArgumentCaptor<ErrorResponse> responseCaptor = ArgumentCaptor.forClass(ErrorResponse.class);
        verify(res).json(responseCaptor.capture());

        // Core Security Checklist Verification: The raw exception context string MUST NOT be leaked to the client
        assertFalse(responseCaptor.getValue().message().contains("database socket dropped"));
        assertTrue(responseCaptor.getValue().message().contains("An unexpected internal server error occurred."));
    }

    @ParameterizedTest
    @CsvSource({
            "RESOURCE_NOT_FOUND, NOT_FOUND",
            "BAD_GATEWAY, BAD_GATEWAY",
            "SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE",
            "GATEWAY_TIMEOUT, GATEWAY_TIMEOUT"
    })
    @DisplayName("Maps infrastructure failures to their corresponding HTTP status")
    void shouldMapInfrastructureFailure_whenGatewayErrorOccurs(
            InfrastructureErrorType errorType,
            HttpStatusCode expectedStatus
    ) throws Exception {
        CustomHttpHandler mockedDelegate = mock(CustomHttpHandler.class);
        doThrow(new InfrastructureException(errorType, "upstream failure"))
                .when(mockedDelegate).handle(any(), any());

        GlobalExceptionHandler handler = new GlobalExceptionHandler(mockedDelegate);
        HttpContextResponse response = mock(HttpContextResponse.class, RETURNS_SELF);

        handler.handle(mock(HttpContextRequest.class), response);

        verify(response).status(expectedStatus);
        ArgumentCaptor<ErrorResponse> responseCaptor = ArgumentCaptor.forClass(ErrorResponse.class);
        verify(response).json(responseCaptor.capture());
        assertEquals(expectedStatus.getCode(), responseCaptor.getValue().status());
        assertEquals("upstream failure", responseCaptor.getValue().message());
    }
}

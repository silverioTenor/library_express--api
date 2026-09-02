package org.libraryexpress.infrastructure.api.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libraryexpress.domain.book.exception.BookUnavailableException;
import org.libraryexpress.infrastructure.UnitTest;
import org.libraryexpress.infrastructure.api.contract.ErrorResponse;
import org.libraryexpress.infrastructure.api.enums.HttpStatusCode;
import org.libraryexpress.infrastructure.api.exception.GlobalExceptionHandler;
import org.libraryexpress.infrastructure.api.routing.CustomHttpHandler;
import org.libraryexpress.infrastructure.api.routing.HttpContextRequest;
import org.libraryexpress.infrastructure.api.routing.HttpContextResponse;
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
}

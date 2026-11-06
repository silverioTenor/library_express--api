package org.libraryexpress.infrastructure.api.exception;

import org.libraryexpress.domain.core.exception.DomainException;
import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;
import org.libraryexpress.infrastructure.api.contract.ErrorResponse;
import org.libraryexpress.infrastructure.api.enums.HttpStatusCode;
import org.libraryexpress.infrastructure.api.routing.CustomHttpHandler;
import org.libraryexpress.infrastructure.api.routing.HttpContextRequest;
import org.libraryexpress.infrastructure.api.routing.HttpContextResponse;
import org.libraryexpress.infrastructure.exception.InfrastructureErrorType;
import org.libraryexpress.infrastructure.exception.InfrastructureException;

import java.io.IOException;

import static org.libraryexpress.infrastructure.api.enums.HttpStatusCode.*;
import static org.libraryexpress.infrastructure.api.enums.HttpStatusCode.BAD_GATEWAY;

public class GlobalExceptionHandler implements CustomHttpHandler {

    private static final CustomLogger logger = CustomLoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final CustomHttpHandler httpHandler;

    public GlobalExceptionHandler(CustomHttpHandler httpHandler) {
        this.httpHandler = httpHandler;
    }

    @Override
    public void handle(HttpContextRequest request, HttpContextResponse response) throws Exception {
        try {
            this.httpHandler.handle(request, response);
        }  catch (Exception e) {
            resolveException(e, response);
        }
    }

    private void resolveException(Exception exception, HttpContextResponse response) throws IOException {
        HttpStatusCode statusCode = switch (exception) {
            case DomainException de -> getErrorType(de);
            case InfrastructureException ie -> getInfrastructureErrorType(ie);
            default -> INTERNAL_SERVER_ERROR;
        };

        if (statusCode.equals(INTERNAL_SERVER_ERROR)) {
            logger.error("Internal error occurred while processing request", exception);
            response.status(INTERNAL_SERVER_ERROR)
                    .json(ErrorResponse.of(500, "An unexpected internal server error occurred. Please contact support."));
            return;
        }

        logger.warn("Application execution request rejected. HTTP Status: [{}]. Reason: [{}]", statusCode, exception.getMessage());
        response.status(statusCode).json(ErrorResponse.of(statusCode.getCode(), exception.getMessage()));
    }

    private HttpStatusCode getErrorType(DomainException de) {
        return switch (de.getErrorType()) {
            case INVALID_DATA -> BAD_REQUEST;
            case RESOURCE_NOT_FOUND -> NOT_FOUND;
            case DATA_CONFLICT -> CONFLICT;
            case BUSINESS_VIOLATION -> UNPROCESSABLE_ENTITY;
        };
    }

    private HttpStatusCode getInfrastructureErrorType(InfrastructureException ie) {
        return switch (ie.getErrorType()) {
            case RESOURCE_NOT_FOUND -> NOT_FOUND;
            case BAD_GATEWAY -> BAD_GATEWAY;
            case GATEWAY_TIMEOUT -> GATEWAY_TIMEOUT;
            case SERVICE_UNAVAILABLE -> SERVICE_UNAVAILABLE;
        };
    }
}

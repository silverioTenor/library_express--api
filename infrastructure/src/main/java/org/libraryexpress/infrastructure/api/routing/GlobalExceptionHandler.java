package org.libraryexpress.infrastructure.api.routing;

import org.libraryexpress.domain.book.exception.BookNotFoundException;
import org.libraryexpress.domain.book.exception.BookUnavailableException;
import org.libraryexpress.domain.book.exception.InvalidIsbnException;
import org.libraryexpress.domain.book.exception.UniqueIsbnViolationException;
import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;
import org.libraryexpress.domain.customer.exception.CustomerNotFoundException;
import org.libraryexpress.domain.customer.exception.InvalidEmailException;
import org.libraryexpress.domain.customer.exception.UniqueEmailViolationException;
import org.libraryexpress.domain.loan.exception.InvalidLoanStatusException;
import org.libraryexpress.domain.loan.exception.LoanLimitReachedException;
import org.libraryexpress.domain.loan.exception.LoanNotFoundException;
import org.libraryexpress.domain.loan.exception.OverdueLoanException;
import org.libraryexpress.infrastructure.api.contract.ErrorResponse;
import org.libraryexpress.infrastructure.api.enums.HttpStatusCode;

import java.io.IOException;

import static org.libraryexpress.infrastructure.api.enums.HttpStatusCode.*;

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
            case InvalidIsbnException e -> BAD_REQUEST;
            case InvalidEmailException e -> BAD_REQUEST;
            case InvalidLoanStatusException  e -> BAD_REQUEST;

            case BookNotFoundException e -> NOT_FOUND;
            case CustomerNotFoundException e -> NOT_FOUND;
            case LoanNotFoundException e -> NOT_FOUND;

            case UniqueIsbnViolationException  e -> CONFLICT;
            case UniqueEmailViolationException  e -> CONFLICT;

            case BookUnavailableException e -> UNPROCESSABLE_ENTITY;
            case OverdueLoanException e -> UNPROCESSABLE_ENTITY;
            case LoanLimitReachedException  e -> UNPROCESSABLE_ENTITY;

            default -> INTERNAL_SERVER_ERROR;
        };

        if (statusCode.equals(INTERNAL_SERVER_ERROR)) {
            logger.error("Internal error occurred while processing request", exception);
            response.status(INTERNAL_SERVER_ERROR)
                    .json(ErrorResponse.of(500, "An unexpected internal server error occurred. Please contact technical support."));
            return;
        }

        logger.warn("Application execution request rejected. HTTP Status: [{}]. Reason: [{}]", statusCode, exception.getMessage());
        response.status(statusCode).json(ErrorResponse.of(statusCode.getCode(), exception.getMessage()));
    }
}

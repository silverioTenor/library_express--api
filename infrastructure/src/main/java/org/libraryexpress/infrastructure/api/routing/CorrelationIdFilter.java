package org.libraryexpress.infrastructure.api.routing;

import org.libraryexpress.infrastructure.config.logging.CorrelationIdSupport;

import java.util.UUID;

public class CorrelationIdFilter implements HttpFilter {
    private static final String CORRELATION_HEADER = "X-Correlation-Id";

    @Override
    public void doFilter(HttpContextRequest request, HttpContextResponse response, CustomHttpHandler next) throws Exception {
        String inboundId = request.getHeader(CORRELATION_HEADER);

        String resolvedId = (inboundId != null && !inboundId.isBlank())
                ? inboundId
                : UUID.randomUUID().toString();

        try {
            CorrelationIdSupport.start(resolvedId);
            response.setHeader(CORRELATION_HEADER, resolvedId);
            next.handle(request, response);
        } finally {
            CorrelationIdSupport.clear();
        }
    }
}

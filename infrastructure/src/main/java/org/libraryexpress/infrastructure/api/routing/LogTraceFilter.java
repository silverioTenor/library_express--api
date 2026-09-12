package org.libraryexpress.infrastructure.api.routing;

import org.libraryexpress.infrastructure.config.logging.LogTrace;

import java.util.UUID;

public class LogTraceFilter implements HttpFilter {
    private static final String TRACE_HEADER = "X-Trace-Id";

    @Override
    public void doFilter(HttpContextRequest request, HttpContextResponse response, CustomHttpHandler next) throws Exception {
        String inboundId = request.getHeader(TRACE_HEADER);

        String resolvedId = (inboundId != null && !inboundId.isBlank())
                ? inboundId
                : UUID.randomUUID().toString();

        try {
            LogTrace.start(resolvedId);
            response.setHeader(TRACE_HEADER, resolvedId);
            next.handle(request, response);
        } finally {
            LogTrace.clear();
        }
    }
}

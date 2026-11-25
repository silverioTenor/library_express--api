package org.libraryexpress.infrastructure.api.server;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import org.libraryexpress.domain.core.logging.CustomLogger;
import org.libraryexpress.domain.core.logging.CustomLoggerFactory;
import org.libraryexpress.infrastructure.api.routes.RouteOrchestrator;
import org.libraryexpress.infrastructure.api.routing.Router;
import org.libraryexpress.infrastructure.config.AppContext;
import org.libraryexpress.infrastructure.config.ConfigRegistry;

public final class EmbeddedHttpServer {

    private static final int SERVER_PORT = ConfigRegistry.getApiPort();

    private static final CustomLogger LOG = CustomLoggerFactory.getLogger(EmbeddedHttpServer.class);

    private final HttpServer server;

    public EmbeddedHttpServer(AppContext context) {
        try {
            Router router = new Router();
            RouteOrchestrator orchestrator = context.orchestrator();
            orchestrator.bindAll(router);

            server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
            server.createContext("/", router);

            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        } catch (Exception e) {
            LOG.error("FATAL: Failed to construct and wire embedded HTTP Server context properties", e);
            throw new IllegalStateException("HTTP Server context initialization failure", e);
        }
    }

    public void start() {
        try {
            server.start();
            LOG.info("Server running on port {}", SERVER_PORT);
        } catch (Exception e) {
            LOG.error("FATAL: Failed to start embedded HTTP Server network listeners", e);
            System.exit(1);
        }
    }

    public void stop() {
        LOG.info("JVM execution signal captured. Tearing down embedded HTTP Server listeners...");
        if (server != null) {
            server.stop(5);
        }
        LOG.info("HTTP Server connection resources cleared safely.");
    }
}

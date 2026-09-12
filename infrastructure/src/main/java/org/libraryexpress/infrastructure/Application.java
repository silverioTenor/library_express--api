package org.libraryexpress.infrastructure;

import org.libraryexpress.infrastructure.config.AppBootstrapper;

/**
 * Main application entrypoint boundary.
 * Acting strictly as the ignition key for the infrastructure bootstrapping layers.
 */
public class Application {

    public static void main(String[] args) {
        // Task Solution: The entrypoint delegates 100% of orchestration and execution to the bootstrapper
        AppBootstrapper.boot();
    }
}
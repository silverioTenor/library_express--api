package org.libraryexpress.infrastructure.api.e2e;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.libraryexpress.infrastructure.config.AppBootstrapper;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Baseline Architectural E2E Test class.
 * Orchestrates Testcontainers PostgreSQL lifecycles alongside our official AppBootstrapper lifecycles.
 */
public abstract class E2EBaseConfig {

    private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @BeforeAll
    static void setup() {
        postgres.start();

        System.setProperty("DB_NAME", postgres.getDatabaseName());
        System.setProperty("DB_USER", postgres.getUsername());
        System.setProperty("DB_PASSWORD", postgres.getPassword());
        System.setProperty("DB_PORT", postgres.getMappedPort(5432).toString());

        AppBootstrapper.boot();

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;
    }

    @AfterAll
    static void tearDown() {
        postgres.stop();

        System.clearProperty("DB_NAME");
        System.clearProperty("DB_USER");
        System.clearProperty("DB_PASSWORD");
        System.clearProperty("DB_PORT");
    }
}
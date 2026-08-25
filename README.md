# Readme

## Library Express

A backend application for library management built in Java.

**Note on language:** every document in this project — this README, `docs/BACKLOG.md`, and everything under `docs/adr/` — is written in English. Portuguese was previously used as the working language for planning documents (`BACKLOG.md`, `VISION.md`); that split has been retired in favor of a single language across the whole project.

This project is part of my Java Journey, a long-term learning path focused on mastering Java and software engineering by evolving a real-world application through incremental development, guided by a real agile process (epics, sprints, user stories, tasks).

## 🎯 Project Goals

- Build a library management system.
- Apply object-oriented programming principles.
- Practice software architecture through incremental refactoring.
- Evolve the application from a simple MVP to a production-ready system.
- Learn technologies only when they solve real problems in the project.

## Features

- Book management
- Customer management
- Loan management (creation, search)
- Business rule validation (book availability, active loan limits)
- Custom exception handling

## 🏗 Current Architecture

The project is organized as a Maven multi-module build. Each layer of the Clean Architecture is its own Maven module, with its own `pom.xml` and its own `src/main`/`src/test` — not just a package convention. This isolates test scope per layer (domain tests never see infrastructure dependencies, for example) and keeps compile-time boundaries between layers enforced by Maven itself, not just by discipline.

```
library_express--api
├── pom.xml (parent/aggregator — packaging: pom)
│
├── domain
│   ├── pom.xml
│   └── src
│       ├── main/java/org.libraryexpress.domain
│       │   ├── book
│       │   ├── customer
│       │   ├── loan
│       │   └── core (shared entities, enums, helpers, repository/validator interfaces)
│       └── test/java/org.libraryexpress.domain (JUnit 5 — entity/contract tests)
│
├── application
│   ├── pom.xml
│   └── src
│       ├── main/java/org.libraryexpress.application
│       │   ├── book (dto, mapper, usecase, validator)
│       │   ├── customer (dto, mapper, usecase)
│       │   └── loan (dto, usecase, validator)
│       └── test/java/org.libraryexpress.application (JUnit 5 + Mockito — usecase tests, mocked repositories)
│
├── infrastructure
│   ├── pom.xml
│   └── src
│       ├── main/java/org.libraryexpress.infrastructure
│       │   ├── cli (current entrypoint/UI)
│       │   ├── config
│       │   ├── exception
│       │   └── repository (JDBC implementations, backed by PostgreSQL)
│       └── test/java/org.libraryexpress.infrastructure
│           ├── unit (@UnitTest)
│           └── integration (@IntegrationTest — Testcontainers against real PostgreSQL)
│
└── coverage-report
    └── pom.xml (packaging: pom — no src; aggregates JaCoCo reports from the 3 modules above, per module)
```

Dependency direction between modules: `infrastructure → application → domain`. `domain` has no dependency on the other two — it's pure business logic and contracts. `coverage-report` depends on all three, but only to aggregate their test coverage data — it contains no production code.

It currently uses plain Java, without dependency injection or application frameworks. This is deliberate: new technologies (a real database, containerization, etc.) are introduced only when they solve a real problem the project has reached — not upfront. Unlike earlier drafts of the roadmap, this is not a temporary state pending a future Spring Boot migration: Library Express stays framework-free for its entire lifecycle by design. The framework step of the author's learning path happens in a separate, Spring-first project instead. Full rationale: ADR 0001.

Architectural decisions (like the module split above, and the framework-free scope) are recorded as ADRs in `docs/adr/`. A high-level C4 view of the modules lives in `docs/architecture/`.

## 🚀 Tech Stack

- Java 21
- Maven (multi-module)
- PostgreSQL 17 (via pure JDBC, no ORM)
- HikariCP (connection pooling)
- Flyway (versioned schema migrations)
- Docker & Docker Compose (multi-stage build, `eclipse-temurin:21-jre-alpine` runtime)
- JUnit 5 (Jupiter)
- Mockito
- Testcontainers (PostgreSQL — infrastructure layer integration tests)
- MapStruct (DTO ↔ entity mapping)
- JaCoCo (test coverage, enforced per module as a CI quality gate)
- GitHub Actions (Continuous Integration)
- SLF4J + Logback (structured JSON logging, MDC-based correlation — Epic E8)

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose

### Clone and Compile

```
git clone https://github.com/silverioTenor/library_express--api.git
cd library_express--api
mvn clean package -DskipTests
```

## 🚀 Running the Application

You can execute the Library Express ecosystem using three different approaches depending on your infrastructure requirements:

### 1. Native Execution via Consolidated Fat JAR (Production Pattern)

The project utilizes the `maven-shade-plugin` inside the infrastructure module to compile all multi-module dependencies into a single, standalone executable binary. Run it directly through the JVM:

```
java -jar infrastructure/target/infrastructure-0.1.4.jar
```

Note: Ensure your local environment variables (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`) are exported or present in a local `.env` file for the database connector to handshake successfully.

### 2. Containerized Execution via Docker Compose (Interactive Development Stack)

To run the full stack (Java Application + PostgreSQL 17) inside isolated networks without pre-installing any local database tools, use the optimized Docker engine wrapper.

Since Library Express is a rich command-line interactive system, it requires a pseudo-TTY connection to capture input streams cleanly. Always boot using the following interactive configuration:

```
# Spins up PostgreSQL in background, builds the application container, and attaches your keyboard stream
docker compose run --service-ports app
```

To tear down the active local network data volumes and stop containers:

```
docker compose down
```

### 3. Development Execution via Maven CLI

For fast compilation loop reviews during ongoing development iterations inside your local host terminal:

```
mvn exec:java
```

The entrypoint lives in the infrastructure module. `exec-maven-plugin` is skipped by default (`exec.skip=true` in the parent POM) and only re-enabled (`exec.skip=false`) in infrastructure — so even though the reactor visits all three modules, it only actually executes there. No `-pl` needed.

## ✅ Running Tests

```
mvn test
```

Runs unit tests across all modules. To run a single module's tests only:

```
mvn test -pl domain
mvn test -pl application
```

Infrastructure integration tests use Testcontainers to spin up a real, ephemeral PostgreSQL container and run Flyway migrations against it — **Docker must be running locally** for these to execute. They run as part of the `verify` phase, separately from fast unit tests:

```
mvn verify -pl infrastructure
```

## 📊 Test Coverage

Each module (`domain`, `application`, `infrastructure`) generates its own JaCoCo report on `mvn test`, at `<module>/target/site/jacoco/index.html`.

For a single consolidated report across all modules:

```
mvn clean verify
```

The aggregated report is generated by the `coverage-report` module at `coverage-report/target/site/jacoco-aggregate/index.html`. It has no source code of its own — it exists solely to depend on the three modules and run JaCoCo's `report-aggregate` goal after they've all been tested, broken down per module. DTOs and auto-generated MapStruct mappers are explicitly excluded from coverage collection metrics to reflect authentic business orchestration health.

Coverage is enforced as a CI quality gate (JaCoCo `check` goal), with thresholds calibrated per module to reflect each layer's testability profile:

| Module | Line | Branch |
|---|---|---|
| domain | 95% | 90% |
| application | 90% | 85% |
| infrastructure | 75% | 65% |

A build that falls below any of these thresholds fails automatically — see the CI section below.

## 🔁 Continuous Integration

Every push and pull request targeting `main` triggers a GitHub Actions workflow (`.github/workflows/ci.yml`) that:

1. Builds the project (`mvn compile`)
2. Runs unit tests (`@UnitTest`)
3. Runs infrastructure integration tests against a real PostgreSQL container via Testcontainers (`@IntegrationTest`)
4. Enforces the per-module JaCoCo coverage thresholds above, failing the build if any module falls short

`main` is protected: direct pushes are disabled, and a pull request can only be merged once the CI check above passes. There is no separate Pull Request review flow yet — merging is gated purely on the automated check.

## 📚 Learning Purpose & Agile Process

Rather than building everything at once, this project evolves through iterative sprints, planned and tracked as a real agile backlog — epics, sprints, user stories (with BDD-style acceptance criteria), and tasks.

Instead of introducing frameworks and architectural patterns from the beginning, each sprint solves a real problem found in the application. New technologies are adopted only when they provide clear value to the project's evolution.

- `docs/BACKLOG.md` — active engineering roadmap: current epic, sprint backlog, BDD acceptance criteria, technical debt, commit conventions.
- `docs/adr/` — Architecture Decision Records, including the decision to keep this project framework-free for its full lifecycle and to retire the long-term vision document below.
- `docs/architecture/` — C4 diagrams and structural documentation.

A long-term product vision document (`VISION.md`) used to live alongside the backlog, describing aspirational, non-executable future expansions (a self-service platform, a marketplace, payments, subscriptions, microservices). It has been retired — Library Express's scope is bounded to what's tracked in `BACKLOG.md`. See `docs/VISION.md` for the retirement note and ADR 0001 for the full rationale.

## 📄 License

MIT
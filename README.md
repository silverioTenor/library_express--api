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

- Book management (manual registration, or automatic ISBN/metadata lookup through Google Books)
- Customer management
- Loan management (creation, return, search, closing of overdue loans)
- Business rule validation (book availability, active loan limits, loan period)
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
│       │   ├── book (entity, value objects, gateway, repository, validator)
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
│       │   ├── api (embedded HTTP server, router, controllers, central exception handler, OpenAPI)
│       │   ├── config
│       │   ├── exception
│       │   ├── gateway (external HTTP clients — Google Books adapter, fake adapter for tests)
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

## 🌐 REST API

The API is served by the application's own embedded HTTP server on the port defined by `API_PORT` (default `3000`).

### Conventions

- **Resource-based URIs**, no version prefix (e.g. `/books`, not `/v1/books`).
- **One listing endpoint per resource:** `GET /books`, `GET /customers` and `GET /loans` are paginated and accept optional filters; there are no separate search routes.
- **Pagination:** paginated list endpoints accept `page` (0-indexed, default `0`) and `limit` (default `10`) query parameters.
- **Deterministic ordering:** every listing is ordered by `created_at, id`, so consecutive pages never repeat or skip records.
- **Creation:** `POST` endpoints answer `201 Created` with a JSON body carrying the identifier of the created resource (the ISBN for books, the `id` for customers and loans). The full resource is read through the corresponding `GET`. No `Location` header is sent.
- **Input validation:** invalid input is rejected with `400`; the `message` lists every violated field.
- **ISBN:** ISBN-10 and ISBN-13 are accepted (hyphens and spaces ignored) and always stored and returned as ISBN-13, digits only. The same book cannot be registered twice under both formats.
- **Error responses:** structured JSON body (`status`, `message`, `timestamp`) on every 4xx/5xx.
- **Trace ID:** every response includes an `X-Trace-Id` header — echoes a client-supplied value or generates one when absent.
- **Audit columns:** `created_at` and `updated_at` exist on every business table, are maintained by the database, and are never exposed by the API.

### Endpoints

| Resource | Method | Path | Description |
|---|---|---|---|
| Books | POST | `/books` | Register a book (manual, or automatic lookup when no ISBN is sent) |
| Books | GET | `/books/{isbn}` | Get a book by ISBN (ISBN-10, ISBN-13 or hyphenated) |
| Books | GET | `/books` | List books (paginated). Filters: `isbn`, `title`, `author`, `statuses`, `publishedYear` |
| Customers | POST | `/customers` | Create a customer |
| Customers | GET | `/customers` | List customers (paginated). Filters: `id`, `name`, `email` |
| Customers | PATCH | `/customers/{id}/update-email` | Update a customer's email |
| Loans | POST | `/loans` | Create a loan |
| Loans | GET | `/loans` | List loans (paginated). Filters: `id`, `customerId`, `isbn`, `statuses` |
| Loans | POST | `/loans/{loanId}/returns` | Return a loan |
| Loans | PATCH | `/loans/{loanId}/close-overdue` | Close an overdue loan (temporary flow) |

### Book registration

`POST /books` works in two modes, decided by the request:

- **Manual** — the request carries an `isbn`. No external call is made; the book is stored with the data sent. `status` is optional and defaults to `AVAILABLE`.
- **Automatic lookup** — the request carries no `isbn`, only `title` and `author`. The application queries Google Books and registers the book with the ISBN-13, authors, categories, publication year and description (at most 500 characters) returned. If no match is found the response is `404`, asking for the data to be reviewed or sent manually; if the provider is unavailable or times out, a gateway-class `5xx` is returned and nothing is persisted.

A duplicate ISBN (in any accepted format) is answered with `409`.

### API Documentation

- Swagger UI: `/docs`
- Raw OpenAPI contract: `/openapi.json`

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
- Swagger
- Nginx (reverse proxy, TLS termination)
- Let's Encrypt / Certbot (TLS certificates, auto-renewed)
- Neon (serverless managed PostgreSQL, production)
- Amazon ECS (EC2 launch type) + Amazon ECR
- Cloudflare (Free plan — DNS, proxy, TLS Full strict, Free Managed Ruleset WAF, rate limiting, Authenticated Origin Pulls)
- AWS Systems Manager (Session Manager — instance access, no SSH)
- Google Books API (book metadata lookup, via `java.net.http` + Jackson)

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

The application is a plain HTTP service: it starts the embedded server on `API_PORT` and has no interactive prompt. You can run it in three ways.

### Configuration

Values are resolved, in this order, from JVM system properties, environment variables, a local `.env` file and `application.properties`. Use `.env.example` as the starting point for a local `.env`.

| Variable | Default | Description |
|---|---|---|
| `API_PORT` | `3000` | Port of the embedded HTTP server |
| `APP_ENV` | `prod` | Runtime profile. `prod` uses the production database settings; `test` swaps the Google Books adapter for a fake one (no external calls) and is used by tests and by local runs that must not consume external resources; any other value (e.g. `development`) is a local profile |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | `localhost`, `5432`, `library_express`, none, none | PostgreSQL connection; `DB_USER` and `DB_PASSWORD` are mandatory |
| `GOOGLE_BOOKS_API` | none | Google Books API key, used by the automatic book lookup. In production it is injected from AWS Secrets Manager through the ECS task definition |
| `hikari.*` | see `application.properties` | HikariCP pool settings (`maximum-pool-size`, `minimum-idle`, `idle-timeout`, `connection-timeout`, `max-lifetime`) |

### 1. Native Execution via Consolidated Fat JAR (Production Pattern)

The project uses the `maven-shade-plugin` inside the infrastructure module to compile all multi-module dependencies into a single, standalone executable JAR. Run it directly through the JVM:

```
java -jar infrastructure/target/infrastructure-<version>.jar
```

Make sure the variables above (at least `DB_USER` and `DB_PASSWORD`) are exported or present in a local `.env` file.

### 2. Containerized Execution via Docker Compose

To run the full stack (Java application + PostgreSQL 17) in isolated containers without installing a local database:

```
docker compose up --build
```

No pseudo-TTY is needed. To stop the containers and remove the local data volume:

```
docker compose down -v
```

### 3. Development Execution via Maven

For a fast loop during development, from the host terminal:

```
mvn exec:java
```

The entrypoint lives in the infrastructure module. `exec-maven-plugin` is skipped by default (`exec.skip=true` in the parent POM) and only re-enabled (`exec.skip=false`) in infrastructure — so even though the reactor visits all three modules, it only actually executes there. No `-pl` needed.

## 🗄 Database Migrations

Schema changes are versioned with Flyway under `infrastructure/src/main/resources/db/migration`. The schema was consolidated into a single `V1` migration during Epic E15, and the production database was recreated empty before that release. From now on, an applied migration is never edited: every change is a new, higher-numbered migration.

## ✅ Running Tests

```
mvn test
```

Runs unit tests across all modules.

## 📊 Test Coverage

Each module (`domain`, `application`, `infrastructure`) generates its own JaCoCo report on `mvn test`, at `<module>/target/site/jacoco/index.html`.

For a single consolidated report across all modules:

```
mvn clean verify
```

The aggregated report is generated by the `coverage-report` module at `coverage-report/target/site/jacoco-aggregate/index.html`. It has no source code of its own — it exists solely to depend on the three modules and run JaCoCo's `report-aggregate` goal after they've all been tested, broken down per module. DTOs and auto-generated MapStruct mappers are explicitly excluded from coverage collection metrics to reflect authentic business orchestration health.

Coverage is enforced as a CI quality gate (JaCoCo `check` goal), using the **Instruction** and **Branch** counters (Instruction coverage measures bytecode granularity and is stricter than line coverage — a deliberate choice over the more common Line metric). Thresholds are calibrated per module to reflect each layer's current testable surface:

| Module | Instruction | Branch |
|---|---|---|
| domain | 90% | 88% |
| application | 95% | 88% |
| infrastructure | 80% | 65% |

Thresholds are set at the measured coverage minus a safety margin, so normal variance does not break the build while real regressions do. The rationale is in ADR 0006, Amendment 2.

A build that falls below any of these thresholds fails automatically — see the CI section below.

## 🔁 Continuous Integration

Every push to `develop` and pull request targeting `main` triggers a GitHub Actions workflow (`.github/workflows/ci.yml`) that:

1. Builds the project (`mvn -B clean verify`)
2. Runs unit tests (`@UnitTest`)
3. Runs infrastructure integration tests against a real PostgreSQL container via Testcontainers (`@IntegrationTest`)
4. Enforces the per-module JaCoCo coverage thresholds above, failing the build if any module falls short

`main` is protected: direct pushes are disabled, and a pull request can only be merged once the required `build-and-test` check passes. There is no separate Pull Request review flow yet — merging is gated purely on the automated check. Pushes straight to `develop` remain unrestricted.

Continuous **Delivery** is handled by a separate workflow (`cd.yml`), triggered only by pushing a version tag (`vX.Y.Z`) — not by every push/merge to `main`. The release flow is manual and explicit: a release branch is cut from `main`, the version is bumped in `pom.xml` before the PR is opened, and after merge the Git tag is created and pushed locally. The tag push builds the Docker image (tagged with the release version), publishes it to ECR, and deploys it to the ECS service on AWS.

## 🌐 Production

The API is live at `https://api.jlibraryexpress.com` — no port needed.

DNS and the public edge are served by **Cloudflare** (Free plan): the `api` record is proxied, TLS runs in Full (strict) mode, the Free Managed Ruleset is active, and a per-IP rate-limiting rule slows abusive traffic. The EC2 instance that hosts the application container is reachable only through Cloudflare: the Security Group accepts 80/443 only from Cloudflare's published ranges, Nginx requires our own client certificate (Authenticated Origin Pulls) and rejects any `Host` other than the API's, and the real client IP is restored from `CF-Connecting-IP`. TLS is terminated by an Nginx reverse proxy on the same instance, using a Let's Encrypt certificate renewed by Certbot. The Nginx configuration is versioned in `.infra/nginx`. Full rationale: ADR 0010.

The database is a managed serverless PostgreSQL on Neon, and the instance schedule and connection-pool settings are tuned to keep the monthly cost minimal.

- Swagger UI: `https://api.jlibraryexpress.com/docs`
- Raw OpenAPI contract: `https://api.jlibraryexpress.com/openapi.json`

## 📚 Learning Purpose & Agile Process

Rather than building everything at once, this project evolves through iterative sprints, planned and tracked as a real agile backlog — epics, sprints, user stories (with BDD-style acceptance criteria), and tasks.

Instead of introducing frameworks and architectural patterns from the beginning, each sprint solves a real problem found in the application. New technologies are adopted only when they provide clear value to the project's evolution — and when a technology is adopted primarily for learning value rather than a real need the project has at its current scale, that trade-off is documented honestly rather than dressed up as a real-world necessity.

- `docs/BACKLOG.md` — active engineering roadmap: current epic, sprint backlog, BDD acceptance criteria, technical debt, commit conventions.
- `docs/adr/` — Architecture Decision Records, including the decision to keep this project framework-free for its full lifecycle and to retire the long-term vision document below.
- `docs/architecture/` — C4 diagrams and structural documentation.

A long-term product vision document (`VISION.md`) used to live alongside the backlog, describing aspirational, non-executable future expansions (a self-service platform, a marketplace, payments, subscriptions, microservices). It has been retired — Library Express's scope is bounded to what's tracked in `BACKLOG.md`. See `docs/VISION.md` for the retirement note and ADR 0001 for the full rationale. Note: a narrow, controlled form of overdue-loan settlement (late fee + daily interest) was later reopened in Epic E12 — this is a scoped exception, not a return of the broader retired vision (marketplace, subscriptions, audiobooks/video remain out of scope). See ADR 0005 once drafted.

## 📄 License

MIT
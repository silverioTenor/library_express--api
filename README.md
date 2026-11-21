# Library Express

Library Express is a framework-free library management API built with Java 21. It manages books, customers, and loans using an embedded HTTP server, direct JDBC persistence, PostgreSQL, and a cleanly separated Maven multi-module architecture.

The project is an incremental learning and engineering environment focused on software architecture, concurrency, persistence, testing, and production deployment. It intentionally avoids Spring and other application frameworks.

## Features

- Register books manually or through Google Books metadata lookup
- Search and paginate books
- Create and search customers
- Update customer email addresses
- Create, return, search, and close loans
- Validate ISBNs and loan periods
- Prevent duplicate ISBN and email records
- Return structured errors with trace identifiers
- Enforce idempotent and concurrency-safe loan transitions
- Serve Swagger UI and an OpenAPI contract

## Architecture

```text
infrastructure -> application -> domain
```

| Module | Responsibility |
|---|---|
| `domain` | Entities, value objects, business rules, validators, repository and gateway ports |
| `application` | Use-case orchestration, DTOs, and entity/DTO mappers |
| `infrastructure` | HTTP server, routing, JDBC repositories, transactions, external gateways, configuration |
| `coverage-report` | Aggregate JaCoCo coverage report |

The project follows Clean Architecture and a ports-and-adapters style:

- Domain logic does not depend on frameworks or persistence.
- Application use cases depend only on domain contracts.
- Infrastructure implements repository, gateway, transaction, and logging ports.
- `AppContext` is the explicit composition root.

See [Project Overview](docs/PROJECT_OVERVIEW.md) for the full architecture and design details.

## Technology Stack

- Java 21
- Maven multi-module build
- PostgreSQL 17
- Pure JDBC
- HikariCP
- Flyway
- JDK embedded HTTP server
- Jackson
- MapStruct
- JUnit 5
- Mockito
- Testcontainers
- JaCoCo
- SLF4J and Logback
- Swagger/OpenAPI
- Docker and Docker Compose
- GitHub Actions
- AWS ECS/ECR and Neon PostgreSQL

## REST API

Base URL:

```text
http://localhost:3000
```

| Resource | Method | Endpoint | Purpose |
|---|---|---|---|
| Books | `POST` | `/books` | Register a book |
| Books | `GET` | `/books/{isbn}` | Find a book |
| Books | `GET` | `/books` | Search paginated books |
| Customers | `POST` | `/customers` | Create a customer |
| Customers | `GET` | `/customers` | Search paginated customers |
| Customers | `PATCH` | `/customers/{id}/update-email` | Update an email |
| Loans | `POST` | `/loans` | Create a loan |
| Loans | `GET` | `/loans` | Search paginated loans |
| Loans | `POST` | `/loans/{loanId}/returns` | Return a loan |
| Loans | `PATCH` | `/loans/{loanId}/close-overdue` | Close an overdue loan |

List endpoints accept zero-based `page` and `limit` parameters. API responses use structured JSON errors and include `X-Trace-Id`.

- Swagger UI: `/docs`
- OpenAPI JSON: `/openapi.json`

## Configuration

Configuration is resolved in this order:

1. JVM system properties
2. Environment variables
3. Local `.env`
4. `application.properties`

```dotenv
API_PORT=4000
APP_ENV=development
DB_HOST=localhost
DB_PORT=5432
DB_NAME=dev_library_express
DB_USER=postgres
DB_PASSWORD=postgres
GOOGLE_BOOKS_API_KEY=
```

Use `.env.example` as a starting point.

`APP_ENV` supports `prod`, `test`, and local values such as `development`. The `test` profile uses a fake Google Books gateway.

## Run Locally

### Prerequisites

- Java 21
- Maven 3.9+
- Docker and Docker Compose

### Docker Compose

```bash
docker compose up --build
```

Stop the application and remove local database data:

```bash
docker compose down -v
```

### Executable JAR

```bash
mvn clean package -DskipTests
java -jar infrastructure/target/infrastructure-<version>.jar
```

### Maven

```bash
mvn exec:java
```

The application entrypoint is in the `infrastructure` module.

## Database

Flyway migrations are stored in:

```text
infrastructure/src/main/resources/db/migration
```

The initial schema contains the `admin_user`, `customer`, `book`, and `loan` tables. Applied migrations must not be edited; new schema changes require a higher-numbered migration.

## Concurrency and Transactions

Loan creation and return operations use explicit database transactions. Repository operations inside the same thread reuse the transaction-bound connection.

Concurrent or repeated loan transitions use conditional updates and idempotent responses so a second request cannot duplicate or reverse a completed transition.

## Testing

Run the unit test suite:

```bash
mvn test
```

Run the integration suite with Testcontainers:

```bash
mvn verify
```

Tests are separated by module and tagged for unit or integration execution:

- `domain`: entities, value objects, and business rules
- `application`: use-case orchestration
- `infrastructure`: JDBC, HTTP routing, transactions, locking, configuration, and PostgreSQL integration

JaCoCo produces module reports after tests:

```text
<module>/target/site/jacoco/index.html
```

An aggregate report is generated at:

```text
coverage-report/target/site/jacoco-aggregate/index.html
```

## Continuous Integration and Delivery

GitHub Actions validates builds, unit tests, integration tests, and JaCoCo thresholds on the `develop` branch and pull requests targeting `main`.

Version tags using `vX.Y.Z` trigger the delivery workflow, which builds the Docker image, publishes it to Amazon ECR, and deploys it to Amazon ECS.

Production infrastructure includes Cloudflare, Nginx, Let's Encrypt, Amazon ECS, Amazon ECR, and Neon PostgreSQL.

## Project Documentation

- [Project overview](docs/PROJECT_OVERVIEW.md)
- [Engineering backlog](docs/BACKLOG.md)
- [Architecture Decision Records](docs/adr/)

## License

MIT

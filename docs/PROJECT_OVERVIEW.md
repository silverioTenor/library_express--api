# Library Express Project Overview

## 1. Purpose

Library Express is a framework-free library management backend written in Java 21. It provides book, customer, and loan management through an embedded HTTP server. The project combines a realistic REST API with incremental architectural evolution, explicit concurrency handling, PostgreSQL persistence, containerization, automated testing, observability, and cloud deployment.

The system is intentionally designed to teach and demonstrate software engineering fundamentals without hiding infrastructure details behind a framework. Maven, JDBC, HTTP handling, dependency composition, transactions, locking, migrations, and logging are implemented and integrated directly.

## 2. Functional Scope

The main features are:

- Register books manually or obtain metadata from Google Books.
- Find, search, and paginate books.
- Create customers and update customer email addresses.
- Find and search customers.
- Create, return, search, and close loans.
- Validate ISBN values and loan periods.
- Prevent unavailable books from being loaned.
- Enforce active-loan limits and overdue-loan restrictions.
- Mark overdue returns and make the book available again.
- Return structured errors and trace identifiers.
- Serve Swagger UI and an OpenAPI contract.

## 3. Module Architecture

The project is a Maven multi-module system. The root module is an aggregator and uses `packaging: pom`; production code is divided into three Maven modules plus an aggregate coverage module.

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


The dependency direction is:

```text
infrastructure -> application -> domain
coverage-report -> domain, application, infrastructure
```

### Domain module

The domain module contains business entities, value objects, domain exceptions, repository contracts, gateways, and domain validators. It has no dependencies on application or infrastructure code.

Main areas:

- `book`: `Book`, ISBN, author, category, metadata, status, repository contract, and availability validation.
- `customer`: `Customer`, email value object, repository contract, and exceptions.
- `loan`: `Loan`, loan status, repository contract, loan eligibility, and loan-period validation.
- `core`: shared exceptions, pagination, logging abstractions, transaction contracts, query results, and utilities.

The entities are mutable only through domain operations, while factory methods separate creation from restoration of persisted state.

### Application module

The application module orchestrates domain operations without knowing PostgreSQL or HTTP-specific details. It contains use cases, request and response DTOs, and MapStruct mappers.

Examples:

- `RegisterBook`, `FindBook`, and `SearchBooks`
- `CreateCustomer`, `FindCustomer`, `SearchCustomers`, and `UpdateCustomerEmail`
- `CreateLoan`, `LoanReturn`, `CloseOverdueLoan`, and `SearchLoans`

Use cases coordinate repository calls, validators, entities, transactions, and response DTOs. They translate orchestration failures into domain or application exceptions.

### Infrastructure module

The infrastructure module implements external boundaries and adapters:

- Embedded JDK HTTP server and custom routing
- REST controllers and route modules
- JSON serialization and deserialization
- Global exception handling and trace-ID filters
- JDBC repositories for PostgreSQL
- HikariCP connection pooling
- Flyway migrations
- Transaction context management
- SLF4J/Logback logging
- Google Books gateway and HTTP adapter
- OpenAPI metadata and Swagger UI
- Application composition and bootstrap

### Coverage module

`coverage-report` contains no production source code. It depends on the three implementation modules and creates an aggregate JaCoCo report.

## 4. Architectural Style

### Clean Architecture

The project follows a layered, ports-and-adapters interpretation of Clean Architecture:

- The domain owns business state and rules.
- The application layer owns use-case orchestration.
- Infrastructure implements interfaces and exposes external protocols.
- Dependencies point inward.
- Core modules do not import framework or database classes.

The Maven module boundaries enforce dependency isolation more strongly than package conventions.

### Hexagonal boundary

Repository interfaces, the metadata gateway, logging abstraction, and transaction manager are ports declared in the domain. JDBC repositories, Google Books gateway, and SLF4J adapters are infrastructure implementations.

This separation allows unit tests to replace external dependencies with mocks or fake implementations.

### Framework-free design

The application remains framework-free by design. There is no Spring or dependency-injection container. `AppContext` acts as the explicit composition root, constructing repositories, validators, use cases, and controllers.

Framework-free HTTP handling is also deliberate: the application uses `com.sun.net.httpserver`, custom route registration, filters, controllers, and response adapters.

## 5. Composition Root and Startup

Startup is handled by `AppBootstrapper` and `AppContext`.

```text
Application
  -> AppBootstrapper
     -> initialize logging
     -> initialize PostgreSQL/HikariCP
     -> run Flyway migrations
     -> compose AppContext
     -> start embedded HTTP server
     -> register JVM shutdown hook
```

`AppContext` constructs the transaction-aware data sources and one set of repositories and use cases. It also selects the Google Books adapter based on the active application environment.

The shutdown hook stops the server, closes the connection pool, and clears trace context.

## 6. Domain Model

### Book

`Book` is identified by a UUID and has an `Isbn` value object, title, authors, publication year, categories, status, and description.

Its identity is based on ISBN, and its status is restricted to the values in `BookStatus`.

The availability validator ensures that a book exists and is available before a loan is created.

### Customer

`Customer` is identified by UUID and has a name and an `Email` value object. Identity, for equality and hashing, is the customer ID.

Email mutation is encapsulated by `changeEmail`, with validation enforced through the value object.

### Loan

`Loan` connects one book to one customer. It contains status, start date, end date, and optional close timestamp.

Loan status transitions include:

- `ACTIVE`
- `OVERDUE`
- `FINISHED`

The domain implementation provides overdue detection based on the loan end date and a clock abstraction. A normal return after the deadline produces `OVERDUE`; a dedicated overdue-close flow produces `FINISHED`.

Late-fee application is reserved for a future implementation.

## 7. Application and Business Rules

### Loan creation

Creating a loan:

1. Validates the requested start and end dates.
2. Runs inside a database transaction.
3. Verifies that the customer exists.
4. Verifies that the book exists and is available.
5. Verifies that the customer can receive another loan.
6. Generates a UUID.
7. Creates the loan.
8. Marks the book as borrowed.
9. Persists both records atomically.

### Loan return

Returning a loan is treated as an idempotent operation:

- A `FINISHED` or `OVERDUE` loan is returned with the appropriate response without repeating updates.
- An `ACTIVE` loan is evaluated against the due date.
- The loan and book changes are committed together.
- Conditional status updates prevent a second concurrent request from applying the same transition twice.

### Overdue closure

Closing an overdue loan:

- Is idempotent when the loan is already `FINISHED`.
- Rejects a loan whose current status is not `OVERDUE`.
- Conditionally updates the loan from `OVERDUE` to `FINISHED`.
- Treats a concurrent update as already complete.

## 8. Persistence Design

PostgreSQL 17 is used as the sole relational database. SQL is written directly with JDBC and `PreparedStatement`; there is no ORM.

### HikariCP

HikariCP supplies a configurable connection pool. The pool settings are resolved from application configuration and environment variables.

### Flyway

Schema evolution is handled with versioned Flyway migrations under `infrastructure/src/main/resources/db/migration`. The initial migration defines:

- `admin_user`
- `customer`
- `book`
- `loan`
- Foreign keys and indexes
- Automatic `updated_at` triggers

Applied migrations are never changed; subsequent changes must use a new migration.

### JDBC repositories

JDBC repositories implement the domain repository contracts. They:

- Convert domain entities to SQL parameters.
- Map `ResultSet` rows back into restored domain entities.
- Use arrays for multi-status filters.
- Use deterministic pagination ordering.
- Count paginated rows with SQL window functions.
- Use conditional updates for state-sensitive operations.

## 9. Transactions and Concurrency

`TransactionManager` is a domain port. `TransactionManagerDb` provides the infrastructure implementation.

A transaction:

1. Acquires a connection.
2. Disables auto-commit.
3. Binds the connection to `TransactionContext`.
4. Executes the use case.
5. Commits on success.
6. Rolls back on failure.
7. Clears context and closes the connection.

Nested calls on the same thread reuse the active transaction through `TransactionContext`.

`TransactionAwareDataSource` keeps repository operations on the transaction-bound connection instead of closing it prematurely.

Concurrency controls include:

- Conditional updates based on the expected persisted status.
- Idempotent use-case responses.
- Pessimistic locking experiments for book rows.
- Explicit locking and concurrent integration tests.

For multi-step operations such as loan creation and return, the database transaction and status guard work together to avoid partial updates and repeated transitions.

## 10. HTTP API

The API uses the JDK embedded HTTP server rather than an external web framework.

### Routing

`Router`:

- Registers HTTP verbs and path templates.
- Converts route parameters into regular expressions.
- Extracts path values into a parameter map.
- Wraps handlers with global exception handling and trace filtering.
- Uses Java pattern matching for route states.

Route modules separate resources:

- Books
- Customers
- Loans
- API documentation

### REST conventions

- Resource-based URLs without a version prefix.
- Paginated list endpoints on `/books`, `/customers`, and `/loans`.
- Zero-based `page` and `limit` pagination parameters.
- Deterministic ordering by creation time and ID.
- `201 Created` with an identifier for creation.
- `400` for invalid input.
- `404` for missing resources.
- `409` for conflicts such as duplicate ISBN or email.
- Structured JSON error responses.
- `X-Trace-Id` on every response.

### Documentation

The API exposes:

- Swagger UI at `/docs`
- Raw OpenAPI JSON at `/openapi.json`

OpenAPI contracts are built by annotations and scanned with Swagger tooling at build time.

## 11. External Integrations

### Google Books

Automatic book registration can query Google Books when the request does not provide an ISBN.

The domain defines `BookMetadataGateway`; infrastructure supplies:

- A native HTTP implementation.
- A Google Books HTTP adapter.
- A metadata parser.
- A factory that selects production or fake behavior.
- A fake gateway for tests and environments that must not call external services.

An external lookup does not persist a book until metadata validation and registration are complete.

### Runtime configuration

Configuration is resolved with this precedence:

1. JVM system properties
2. Environment variables
3. Local `.env` file
4. `application.properties`

Important configuration includes API port, application environment, PostgreSQL credentials, HikariCP settings, and Google Books API key.

## 12. Patterns and Design Practices

### Factory pattern

Domain entities use static factories:

- `create(...)` for new domain state
- `restore(...)` for rehydration from persistence

The constructor is private, centralizing invariants and preventing incomplete reconstruction.

### Value objects

`Isbn`, `Email`, `Author`, `Category`, and `BookMetadata` encapsulate format and validation behavior.

### Repository pattern

Repositories isolate persistence behind domain interfaces. Application use cases never manipulate SQL or JDBC directly.

### Gateway pattern

The Google Books integration is an adapter behind a domain gateway. This keeps provider-specific HTTP and JSON concerns in infrastructure.

### Use-case pattern

Each application operation is represented by a focused use-case class. Use cases are explicit orchestration units with constructor-injected dependencies.

### DTO and mapper pattern

Request and response DTOs keep HTTP payload concerns outside domain entities. MapStruct generates most entity/DTO mappings.

### Explicit exception hierarchy

Domain and infrastructure exceptions are separated. A central HTTP exception handler maps domain and infrastructure failures to stable status codes and structured JSON.

### Anti-corruption adapter

SLF4J is hidden behind the domain logging abstraction. A custom adapter maps the project logger contract to the selected runtime implementation.

### Sealed type and pattern matching

Java 21 features are used for route matching states and sealed route records.

### Conditional update

Conditional SQL updates act as an optimistic state guard for loan transitions. The affected-row count determines whether the operation succeeded or another request already completed it.

## 13. Testing Strategy

Tests are isolated by Maven module and type.

### Domain tests

Domain tests validate entities, value objects, validators, and business rules without infrastructure dependencies.

JUnit 5 and Mockito are used where collaborators need controlled behavior.

### Application tests

Application tests verify use-case orchestration with mocked repositories and gateways.

A passthrough transaction manager allows use cases to execute their transactional block during unit tests without a real database.

### Infrastructure tests

Infrastructure tests cover:

- JDBC repositories
- Transaction behavior
- Pessimistic locks
- HTTP routing and server behavior
- Exception mapping
- Configuration
- OpenAPI output
- Real PostgreSQL containers

Testcontainers starts PostgreSQL for integration tests, and JUnit tags separate unit and integration suites.

### End-to-end tests

REST E2E tests exercise resource workflows through HTTP with real infrastructure components and database behavior.

### Coverage

JaCoCo is applied per module and uses instruction and branch counters.

Configured baseline thresholds are:

| Module | Instructions | Branches |
|---|---:|---:|
| Domain | 90% | 88% |
| Application | 95% | 88% |
| Infrastructure | 80% | 65% |

DTOs and generated MapStruct implementations are excluded to keep coverage focused on hand-written behavior.

## 14. Build and Quality

The build uses:

- Java 21
- Maven reactor
- JUnit 5
- Mockito
- MapStruct
- JaCoCo
- Spotless
- Maven Surefire
- Maven Failsafe
- Maven Shade
- Testcontainers

The main quality flow is:

```text
mvn clean compile
mvn test
mvn verify
```

The CI workflow builds, runs unit tests, runs integration tests, and validates coverage. A merge to protected `main` requires the automated build-and-test check.

## 15. Deployment and Operations

### Docker

The application is packaged as a shaded executable JAR. Docker uses a multi-stage build with Eclipse Temurin:

1. Maven builds and packages the application.
2. The runtime image copies the consolidated infrastructure JAR.
3. The container runs the JAR with Java 21.

Docker Compose provides PostgreSQL and the application locally.

### Continuous delivery

Continuous delivery starts from an explicit version tag.

The tag triggers:

1. Docker image creation.
2. Image publication to Amazon ECR.
3. Deployment to an Amazon ECS service.

### Production infrastructure

The production design includes:

- Amazon ECS with EC2 capacity
- Amazon ECR
- Neon serverless PostgreSQL
- AWS Systems Manager Session Manager
- Cloudflare DNS and WAF
- Nginx as reverse proxy
- Let's Encrypt and Certbot
- Structured JSON logs
- MDC trace identifiers

Cloudflare protects origin access and TLS delivery, while ECS runs the application container.

## 16. Observability

Logging uses SLF4J and Logback.

The system supports:

- Development console logging
- Production JSON logs
- MDC trace identifiers
- Log trace start and clearing
- Echoing or generating `X-Trace-Id`
- Structured operational context

The initial observability scope is request correlation and structured logging rather than distributed metrics or tracing infrastructure.

## 17. API Resource Summary

| Resource | Method | Path | Purpose |
|---|---|---|---|
| Books | POST | `/books` | Register manually or by metadata lookup |
| Books | GET | `/books/{isbn}` | Find a book by ISBN |
| Books | GET | `/books` | Search paginated books |
| Customers | POST | `/customers` | Create a customer |
| Customers | GET | `/customers` | Search paginated customers |
| Customers | PATCH | `/customers/{id}/update-email` | Update a customer email |
| Loans | POST | `/loans` | Create a loan |
| Loans | GET | `/loans` | Search paginated loans |
| Loans | POST | `/loans/{loanId}/returns` | Return a loan |
| Loans | PATCH | `/loans/{loanId}/close-overdue` | Close an overdue loan |

## 18. Architectural Decisions

The `docs/adr` directory records the major decisions:

- Maven multi-module organization
- Permanent framework-free scope
- Domain-owned overdue and locking rules
- Notification ports and future notification adapters
- SLF4J and Logback logging
- Roadmap and settlement consolidation
- JaCoCo and PostgreSQL function decisions
- REST conventions and OpenAPI
- AWS infrastructure
- Cloudflare edge security
- Book metadata lookup and API refinement

The architecture should evolve through ADRs and backlog items rather than through isolated implementation decisions.

## 19. Core Design Characteristics

Library Express is characterized by:

- Strong Maven module boundaries
- Inward-facing dependency direction
- Framework-free composition
- Pure domain entities and value objects
- Explicit use cases
- Port-and-adapter external integrations
- Pure JDBC persistence
- Transaction-aware data access
- Conditional and pessimistic concurrency controls
- Idempotent loan state transitions
- REST convention consistency
- Structured errors and trace IDs
- Real database integration tests
- Coverage-driven CI quality gates
- Containerized delivery
- Incremental, ADR-documented architecture

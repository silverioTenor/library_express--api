# Library Express — Engineering & Coding Standards

## 1. Architecture & Module Boundaries
- **Framework-Free Java 21:** Never introduce Spring, CDI, Hibernate, or any DI/ORM framework. Manual dependency composition happens exclusively in `infrastructure/.../AppContext`.
- **Strict Dependency Direction:** `infrastructure` -> `application` -> `domain`.
  - `domain` (`org.libraryexpress.domain`): Pure Java 21. Entities, Value Objects, Domain Exceptions, Validators, and Ports (Repository interfaces, `BookMetadataGateway`, `TransactionManager`, logging abstraction). Zero external framework/JDBC/SLF4J imports.
  - `application` (`org.libraryexpress.application`): Use cases, Request/Response DTOs (Java `record`), and **MapStruct** mappers. Orchestrates domain objects and `TransactionManager`. Never touches SQL, JDBC, or HTTP classes.
  - `infrastructure` (`org.libraryexpress.infrastructure`): `com.sun.net.httpserver` embedded server, custom `Router`, REST controllers, global exception handler, pure JDBC repositories (`PreparedStatement`), HikariCP, Flyway, SLF4J/Logback, and external HTTP gateways.

## 2. Core Patterns & Conventions
- **Domain Entities:** Private constructors only. Expose static factory methods `create(...)` for new instances and `restore(...)` for persistence rehydration. Mutate state only through explicit domain methods (e.g., `changeEmail`).
- **Persistence & Concurrency:** Pure SQL via JDBC `PreparedStatement` inside `TransactionContext` / `TransactionAwareDataSource`. Use conditional SQL updates (checking affected rows) for idempotent state transitions (e.g., `LoanStatus`: `ACTIVE`, `OVERDUE`, `FINISHED`). Never modify already-applied Flyway migrations; always create a new versioned migration.
- **HTTP & REST:** Use the custom `Router` and Java 21 sealed types / pattern matching for route matching. Do not catch domain exceptions inside controllers unless specifically required; let the central exception handler map exceptions to structured JSON errors and HTTP status codes (`201`, `400`, `404`, `409`), preserving `X-Trace-Id`.

## 3. Readability & Code Design Rules (CRITICAL)
- **No Compacted / Dense Code:** Write spacious, human-readable code. Never compress logic into clever one-liners.
- **Method Length:** Keep methods focused and short (target **<= 40 lines**) keeping the single responsibility principal. Extract complex conditions, SQL parameter bindings, or mapping steps into well-named `private` helper methods.
- **Streams & Conditionals:** Never chain more than 2 operations in a `Stream` or `Optional`. Extract intermediate results into clearly named local variables. Avoid nested ternaries; use explicit `if/else` or Java 21 `switch` expressions.
- **Early Returns:** Use guard clauses at the top of methods to keep nesting depth <= 2 levels.
- **Visual Breathing Room:** Separate logical blocks inside a method (validation, query/execution, mapping, return) with a blank line.

## 4. Testing & Build Quality Gates
- **Tests:** JUnit 5 + Mockito for `domain` and `application` (use passthrough `TransactionManager` in application unit tests). In `infrastructure`, tag unit tests with `@UnitTest` and PostgreSQL Testcontainers tests with `@IntegrationTest`.
- **Test Naming & Descriptions:** Every test method must follow the `should<ExpectedBehavior>_when<Condition>` naming convention (e.g., `shouldThrowException_whenEndDateIsNull`) and include a descriptive `@DisplayName("Should <behavior> when <reason>")` annotation (e.g., `@DisplayName("Should reject a null endDate when the entity requires a non-null due date")`).
- **Test Structure (AAA):** Organize every test method into clear **Arrange**, **Act**, and **Assert** blocks separated by a blank line (without redundant `// Arrange` comments unless the setup is complex). Test one single behavior per test method and avoid asserting unrelated outcomes together.
- **Coverage Targets (JaCoCo):** Domain >= 90% inst / 88% branch; Application >= 95% inst / 88% branch; Infrastructure >= 80% inst / 65% branch.
- **Formatting:** Always run `mvn spotless:apply` (Palantir Java Format, 120-char width) after modifying Java files to ensure clean imports and formatting.

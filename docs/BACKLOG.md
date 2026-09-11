# 📚 Backlog

## Library Express — Agile Backlog

Java study project, evolved incrementally through real sprints.
PO/Scrum Master: Claude · Dev: Silvério

**Note on language:** this document, `README.md`, and every ADR under `docs/adr/` are written in English. Portuguese was previously used as the internal planning language for `BACKLOG.md`/`VISION.md`; that split has been retired in favor of a single language across the whole project. `VISION.md` itself has also been retired — see [0001-keep-library-express-framework-free](./adr/0001-keep-library-express-framework-free.md) for the full rationale.

**Note on detail level:** completed epics are recorded here only as a summary (status, points, sprint). Full detail (Gherkin, tasks, implementation deviations) lives in Git history and in issue closing comments on GitHub Projects — it is not duplicated here, to avoid two diverging sources of truth.

---

## Epics

| ID | Epic | Status |
|---|---|---|
| E0 | Initial organization and cleanup | ✅ Done (Sprint 01) |
| E1 | Foundation — decouple I/O from Services, centralize interaction in the CLI | ✅ Done |
| E2 | MVP — Loan lifecycle | ✅ Done (Sprint 2) |
| E3 | Manual dependency inversion + repository standardization + TD01 | ✅ Done (Sprint 3) |
| E4 | Automated test foundation — JUnit 5 + Mockito | ✅ Done (Sprint 4), with a note — see TD06 |
| E5 | Containerization (Docker) | ⛔ Discontinued — scope absorbed by E6 |
| E6 | Real Persistence (JDBC/PostgreSQL) + Docker Containerization | ✅ Done (Sprint 5) |
| E7 | Real CI — automated tests running as a pipeline gate | ✅ Done (Sprint 6) — resolved TD06 |
| E8 | Structured Logging Foundation (SLF4J + Logback, system-wide) | ✅ Done (Sprint 7) — resolves TD07 |
| E9 | REST API + Documentation (Swagger/OpenAPI) | 🔵 Refined, ready for execution (Sprint 8) |
| E10 | CD — Go Live (Marco 2, on AWS) | ⏳ Backlog (title only) |
| E11 | Log Evolution & Full Observability (Prometheus/Grafana) | ⏳ Backlog (post Go-Live — scope note captured, not refined) |
| E12 | Overdue Enforcement Evolution — Job, Loan Restriction & Settlement | ⏳ Backlog (post Go-Live — scope note captured, not refined) |
| E13 | Notifications (loan created / completed / overdue) | ⏳ Backlog (post Go-Live, title only) |

### Renumbering note (historical — first revision)

The epics from E8 onward were deliberately reordered and renumbered once, prior to E7's closure:

- The Spring Boot migration epic, originally numbered E9, was removed from the Library Express roadmap entirely — not deferred, descoped. Spring adoption moved to the next project (Internet Banking), built Spring-first from day one. Full rationale: ADR [0001](./adr/0001-keep-library-express-framework-free.md).
- Customer Reputation, originally E10 and unordered, was renumbered to E8, moved ahead of Marco 2.
- CD / Go Live, originally E8, was renumbered to E9.
- Notifications was introduced as a new epic (E10).

This first revision was itself superseded by the second revision below, once E7 closed and E8 entered refinement.

### Renumbering note (second revision — post E7)

Following E7's closure, the roadmap from E8 onward was substantially restructured to establish early observability, reduce Go-Live risk, and streamline domain evolution:
- **ADR 0005** governs the overarching roadmap resequencing (unbundling former E8 into E8 through E13), the complete retirement of the "Customer Reputation" concept in favor of a deterministic 30-day restriction rule, and the narrow reintroduction of overdue loan fee settlement.
- **ADR 0006** documents the technical alignment of JaCoCo coverage metrics (Instruction/Branch) and the narrow exception allowing a PL/pgSQL function for atomic multi-table overdue bookkeeping.

Refer to `docs/adr/0005-roadmap-and-settlement-consolidation.md` and `docs/adr/0006-jacoco-and-plpgsql-consolidation.md` for full rationale and implementation boundaries.

---

## Roadmap — Phases and Milestones

### 🌱 Phase 1 — Foundation

Goal: build a solid base, consolidating the domain and eliminating architectural problems before introducing new technology.
Scope: E0, E1, E2, E3.

**🚀 Marco 1 — MVP** ✅ Reached
The system meets the essential functional requirements of a library, via CLI. Closes with E2. Tag `v0.1.1`.

### 🏗 Phase 2 — Software Maturity

Goal: raise the system's quality and reliability, driven by the project's real needs — now also calibrated to generate portfolio value in international (US/Canada) hiring processes.

Theme sequence:

1. Automated test foundation — JUnit 5 + Mockito (E4) ✅ done
2. Real persistence (JDBC/PostgreSQL) + Docker containerization (E6) ✅ done
3. Real CI — tests as a pipeline gate, including infrastructure tests via Testcontainers/TD06 (E7) ✅ done
4. Structured logging foundation, system-wide (E8) ✅ done — resolves TD07
5. REST API + documentation (Swagger/OpenAPI) (E9)
6. Marco 2 — Go Live (E10, packaging CD on top of persistence, Docker, and the documented API already in place)
7. Log evolution — full observability with Prometheus/Grafana (E11), post Go-Live
8. Overdue enforcement evolution — scheduler Job, loan restriction, and settlement/late fees (E12), post Go-Live
9. Notifications (E13), exercising the full CI/CD cycle against an already-deployed system

**E5 + E6 merge (decision on record):** Epic E5 (standalone Docker) was discontinued as its own block. Rationale: containerization only generates real business value once it's wired to real persistence — "containerize a CLI with an in-memory repository" is a weak portfolio narrative compared to "containerize an application with real PostgreSQL, HikariCP, and versioned migrations." E5 remains visible in the Epics table (not removed from the map), marked as discontinued, to preserve historical traceability. All containerization scope was absorbed by E6, which took on the name Real Persistence (JDBC/PostgreSQL) + Docker Containerization.

Database chosen: PostgreSQL (via pure JDBC, no ORM), aligned with AWS's RDS free tier.

**🚀 Marco 2 — Go Live**
First real deployment to production — on AWS (free tier). Delivered together: the CD pipeline (E10), real persistence via JDBC + a Docker image (E6, already in place), and the REST API without a framework (`com.sun.net.httpserver.HttpServer`, no Spring — this project stays framework-free for its entire lifecycle, see ADR [0001](./adr/0001-keep-library-express-framework-free.md)), documented via Swagger/OpenAPI (E9, already in place by this point). The deployment "counts" once there is a real HTTP service receiving traffic, backed by real persisted data, with structured logging (E8) already active.

**Deliberately excluded from Marco 2's scope:** automatic overdue enforcement, loan restriction, and settlement/late fees (E12), and full observability via Prometheus/Grafana (E11). These ship *after* Go-Live, as live iterations exercising the CD pipeline — the same pattern the project always intended for Notifications (E13), now extended to business-logic evolution and observability as well.

Why does Go Live come after tests/Docker/persistence/CI/logging/API? The sequence tells a strong portfolio narrative: tested → containerized → persisted → automated → observable → exposed via a documented API → only then went to production — the way real teams operate. And why iterate *after* going live instead of finishing everything first? Because shipping a lean MVP and then evolving it live, through the same CI/CD gate every other change goes through, is a stronger and more realistic signal than a single big-bang release — it demonstrates comfort operating a system that's already serving traffic.

Why raw tests and raw persistence before a framework? `@SpringBootTest`/Mockito and Spring Data JPA are abstractions over plain JUnit and plain JDBC. Doing the manual path first is deliberate: it forces understanding the mechanism underneath before a framework's convenience hides it. That abstraction is deliberately exercised in the next project (Internet Banking, Spring Boot from day one) rather than inside Library Express — see ADR [0001](./adr/0001-keep-library-express-framework-free.md) for why the Spring migration was removed from this project's own roadmap instead of just being deferred.

### ⚙️ Phase 3 — Professional Software Engineering

Goal: deepen engineering practices on a system that has been in production since Marco 2 — security, observability, performance, scalability, documentation.
Scope: not yet formalized into epics (future backlog). Most of what would traditionally live here (observability, iterative business-logic evolution against a live system) has been pulled forward into Phase 2 as E11–E13, since the project's terminal roadmap ends at E13 with only maintenance-level adjustments afterward — see ADR [0001](./adr/0001-keep-library-express-framework-free.md).

---

## Principles

- The domain always comes first.
- New technology is introduced only when it solves a real problem — or, when deliberately chosen as a learning exercise, is documented honestly as such rather than justified by a real need the project doesn't actually have.
- Every Sprint must produce a functional delivery.
- The architecture evolves alongside the system.
- Learning happens through practice.

**Working rule:** one epic at a time, refined in full detail (BDD + tasks) only once it enters execution. Future epics stay as titles only until their turn comes (just-in-time backlog grooming).

**Process rule (from E4 onward):** before generating any formal backlog artifact (epic breakdown, User Story, tasks) for a new implementation decision or architecture change, alignment with the Dev must be debated and closed in conversation first. Formal Markdown generation (points, Gherkin, tasks, commits) only happens after alignment — never before. This avoids rework from scope drift discovered after the fact. This includes any change that would reverse or narrow an already-Accepted ADR — such a change requires an explicit new or amending ADR, not a silent edit.

---

## Technical Debt

| ID | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             | Points | Status                                 |
|---|---|---|---|
| TD01 | `equals`/`hashCode` contract for Book, Customer, and Loan                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               | 3 | ✅ Resolved (US-304, E3)               |
| TD05 | Fat JAR packaging (`maven-shade-plugin`, manifest with `Main-Class`) — frozen since E3, originally scheduled to resolve only in the (old) Go Live epic. Decision revised: the need for Docker moves the production justification for a single executable artifact earlier — freezing until Go Live no longer made sense.                                                                                                                                                                                                                                | 3 | ✅ Resolved (US-503, E6)               |
| TD06 | Infrastructure layer (in-memory repositories) with no automated test coverage since E4 closed. Intentional deferral: contract and concurrency tests (originally US-404) rewritten with Testcontainers against a real database (Postgres), after E6 (JDBC) delivered the definitive implementation.                                                                                                                                                                                                                                                      | 8 | ✅ Resolved (US-701, E7)               |
| TD07 | Structured logging (SLF4J + Logback). Originally scoped to only the E8 (old) scheduler Job; re-scoped during the second roadmap revision to cover the entire system from the start, ahead of the API and any scheduler — establishing observability as a convention every subsequent epic inherits, rather than retrofitting it under time pressure later. Full observability/tracing (metrics, dashboards) stays explicitly out of scope for this epic — see ADR [0004](./adr/0004-slf4j-logback-without-full-observability.md) and E11. | 10 | ✅ Resolved (US-803, E8) |
| TD08 | Coverage thresholds (Instruction/Branch, per module) were reduced during US-703 to reflect the current testable surface, notably `infrastructure` at 70%/50%. Revisited post-E9 (US-908) against measured coverage — see ADR 0006 Amendment 2. | 5 | ✅ Resolved (US-908, E9) |

---

## 💡 Future Exploration Notes (Not Yet Backlog Items)

Ideas surfaced during refinement that were deliberately **not** turned into epics or TDs — captured here so they aren't lost, without committing points or a sprint slot.

- **Business-level audit trail (DB-backed).** Distinct from technical logging (SLF4J + Logback, ADR [0004](./adr/0004-slf4j-logback-without-full-observability.md), delivered via E8). A "who did what" table for business events was proposed during E8/E12 refinement and intentionally deferred — revisit only after all currently planned epics (through E13) are closed. Not a replacement for structured logging; a separate concern if ever pursued.

---

## 🔵 Epic E9 — REST API + Documentation (Swagger/OpenAPI)

**Sprint:** 8
**Total points:** 35 (5 + 5 + 5 + 5 + 3 + 2 + 5 + 5)
**Status:** 🔵 Refined, ready for execution

### Decisions on record for this epic

- No framework: HTTP layer built on `com.sun.net.httpserver.HttpServer`, with a small hand-rolled router (path/method → `HttpHandler`), per ADR [0001](./adr/0001-keep-library-express-framework-free.md).
- JSON serialization via **Jackson** (already centralized in the parent `dependencyManagement`).
- **Richardson Maturity Model, Level 2**: correct HTTP verbs, resource-based URIs, semantic status codes. No HATEOAS (Level 3) — deliberately out of scope, since the only consumers are the project's own soon-to-be-sunset CLI and local manual testing; no real client benefits from hypermedia navigation. Full rationale: ADR 0008.
- **No URI version prefix** (`/books`, not `/v1/books`) — no external consumer exists yet to justify a stable versioned contract.
- **Pagination:** `page`/`size` query parameters (Spring Data convention), optional with defaults (`page=0`, `size=20`), so the CLI — which sends neither parameter — keeps working unchanged against the first default page.
- This epic exposes the **existing** book/customer/loan usecases over HTTP; no new business rules are introduced.
- Central exception handling maps existing domain/application exceptions to HTTP status codes (400/404/409/etc.) — one dedicated US, not scattered per-handler try/catch.
- Correlation ID (E8's `LogTrace`) moves its entrypoint boundary from the CLI to the HTTP handler: accepts an inbound `X-Correlation-Id` header when present, generates one otherwise, and always echoes it back in the response header.
- OpenAPI documentation generated via `swagger-core` annotations + `swagger-maven-plugin` (build-time static contract) with Swagger UI served as a static resource by the project's own `HttpServer` — not `springdoc-openapi`, which requires a Spring runtime. Full rationale: ADR 0008.
- Coverage priority is **domain > application > infrastructure**, reflecting where business-rule density actually lives. `domain`/`application` thresholds are not expected to drop (no new business logic). The `infrastructure` threshold is **measured after implementation**, not guessed upfront — closes TD08 as an amendment to ADR 0006.
- Test strategy: unit tests per `HttpHandler` (usecases mocked via Mockito) + end-to-end tests via **REST-Assured** against the real embedded `HttpServer` backed by Testcontainers Postgres (Java's closest equivalent to Node's supertest).

### Epic goal

Expose the existing book, customer, and loan usecases through a documented, framework-free REST API — without introducing new business rules — establishing the HTTP boundary that Marco 2 (Go-Live, E10) will deploy.

### Business value

A documented REST API (Swagger/OpenAPI) with conventional pagination, correct status-code semantics, and correlation-aware structured logging is the baseline international hiring panels expect from a backend candidate. Building the HTTP layer without a framework — routing, serialization boundaries, and exception mapping by hand — before Go-Live demonstrates the mechanics that Spring normally hides, directly reinforcing the two-project portfolio narrative (ADR 0001).

### Definition of Done — Epic E9

- [ ] HTTP foundation (router + Jackson wiring) in place (US-901)
- [ ] Book REST endpoints, paginated (US-902)
- [ ] Customer REST endpoints, paginated (US-903)
- [ ] Loan REST endpoints, paginated (US-904)
- [ ] Central exception handler mapping domain/application exceptions to HTTP status codes (US-905)
- [ ] Correlation ID accepted/generated/echoed via HTTP header, reusing E8's MDC support (US-906)
- [ ] OpenAPI contract generated (swagger-core + swagger-maven-plugin) and Swagger UI served (US-907)
- [ ] HTTP layer covered by unit + REST-Assured/Testcontainers e2e tests; `infrastructure` JaCoCo threshold re-measured and raised (US-908)
- [ ] TD08 formally resolved (amendment recorded against ADR 0006)
- [ ] All 8 User Stories in Done status
- [ ] API conventions (pagination, RMM Level 2, no versioning) documented in the README

---

### US-901 — HTTP Foundation: Router, Jackson Wiring, Base Request/Response Contract

**Points:** 5
**Depends on:** — (unblocked, first US of the epic)

**Story:** As a developer, I need a minimal, framework-free HTTP foundation (routing + JSON serialization), so every resource endpoint built afterward plugs into a consistent, already-solved boundary instead of reinventing routing and parsing per handler.

**Scenarios (BDD):**

```gherkin
Feature: HTTP foundation

  Scenario: Router dispatches a request to the correct handler by path and method
    Given a route is registered for GET /books
    When a GET request arrives at /books
    Then the corresponding handler is invoked

  Scenario: Unregistered route returns 404
    Given no route is registered for a given path/method combination
    When a request arrives at that path
    Then the response status is 404 with a structured JSON error body

  Scenario: Request and response bodies are serialized as JSON via Jackson
    Given a handler returns a Java object as its response
    When the response is written
    Then the client receives a valid JSON body with the correct Content-Type header

  Scenario: Pagination parameters have safe defaults
    Given a GET request to a paginated resource without page or size query parameters
    When the request is handled
    Then page defaults to 0 and size defaults to 20
```

**Tasks:**

- Implement a lightweight router (`Map<RouteKey, HttpHandler>`, `RouteKey` = method + path pattern) on top of `com.sun.net.httpserver.HttpServer`
- Wire Jackson `ObjectMapper` as a shared component for request/response (de)serialization
- Implement a `PageRequest`/`PageResponse` contract (page/size params, total count, items) shared across resources
- Define a base JSON error-response shape (status, message, timestamp) reused by later exception handling (US-905)
- Add server bootstrap wiring in the composition root (start/stop lifecycle)

**Commits:**

```
feat(api): US-901 implement lightweight http router
feat(api): US-901 wire jackson object mapper for json serialization
feat(api): US-901 add page request and page response contracts
feat(api): US-901 add base json error response shape
feat(api): US-901 wire http server bootstrap in composition root
```

---

### US-902 — Book REST Endpoints (Paginated)

**Points:** 5
**Depends on:** US-901

**Story:** As an API consumer, I need to create, retrieve, and list books over HTTP, so book management is available beyond the CLI.

**Scenarios (BDD):**

```gherkin
Feature: Book REST endpoints

  Scenario: Create a book
    Given a valid book payload
    When a POST request is sent to /books
    Then the response status is 201 with the created book in the body

  Scenario: Retrieve a book by id
    Given an existing book
    When a GET request is sent to /books/{id}
    Then the response status is 200 with the book's data

  Scenario: List books with pagination
    Given more books exist than the default page size
    When a GET request is sent to /books?page=0&size=10
    Then the response contains at most 10 books and pagination metadata (page, size, totalElements)

  Scenario: Retrieve a non-existent book
    Given no book exists with a given id
    When a GET request is sent to /books/{id}
    Then the response status is 404
```

**Tasks:**

- Implement `BookHttpHandler` (POST /books, GET /books/{id}, GET /books)
- Reuse existing Book usecases/DTOs/mappers (application layer) — no new business logic
- Apply `PageRequest`/`PageResponse` contract from US-901 to the list endpoint
- Register routes in the router

**Commits:**

```
feat(api): US-902 implement book creation endpoint
feat(api): US-902 implement book retrieval by id endpoint
feat(api): US-902 implement paginated book listing endpoint
```

---

### US-903 — Customer REST Endpoints (Paginated)

**Points:** 5
**Depends on:** US-901

**Story:** As an API consumer, I need to create, retrieve, and list customers over HTTP, so customer management is available beyond the CLI.

**Scenarios (BDD):**

```gherkin
Feature: Customer REST endpoints

  Scenario: Create a customer
    Given a valid customer payload
    When a POST request is sent to /customers
    Then the response status is 201 with the created customer in the body

  Scenario: Retrieve a customer by id
    Given an existing customer
    When a GET request is sent to /customers/{id}
    Then the response status is 200 with the customer's data

  Scenario: List customers with pagination
    Given more customers exist than the default page size
    When a GET request is sent to /customers?page=0&size=10
    Then the response contains at most 10 customers and pagination metadata

  Scenario: Create a customer with a duplicate email
    Given a customer already exists with a given email
    When a POST request is sent to /customers with that same email
    Then the response status is 409
```

**Tasks:**

- Implement `CustomerHttpHandler` (POST /customers, GET /customers/{id}, GET /customers)
- Reuse existing Customer usecases/DTOs/mappers — no new business logic
- Apply `PageRequest`/`PageResponse` contract to the list endpoint
- Register routes in the router

**Commits:**

```
feat(api): US-903 implement customer creation endpoint
feat(api): US-903 implement customer retrieval by id endpoint
feat(api): US-903 implement paginated customer listing endpoint
```

---

### US-904 — Loan REST Endpoints (Paginated)

**Points:** 5
**Depends on:** US-901

**Story:** As an API consumer, I need to create and retrieve loans over HTTP, so loan management is available beyond the CLI.

**Scenarios (BDD):**

```gherkin
Feature: Loan REST endpoints

  Scenario: Create a loan
    Given a valid loan request for an available book and eligible customer
    When a POST request is sent to /loans
    Then the response status is 201 with the created loan in the body

  Scenario: Reject a loan for an unavailable book
    Given a book with no available copies
    When a POST request is sent to /loans for that book
    Then the response status is 409 with a message describing the violated rule

  Scenario: Retrieve a loan by id
    Given an existing loan
    When a GET request is sent to /loans/{id}
    Then the response status is 200 with the loan's data

  Scenario: List loans with pagination
    Given more loans exist than the default page size
    When a GET request is sent to /loans?page=0&size=10
    Then the response contains at most 10 loans and pagination metadata
```

**Tasks:**

- Implement `LoanHttpHandler` (POST /loans, GET /loans/{id}, GET /loans)
- Reuse existing Loan usecases/DTOs/mappers/validators — no new business logic
- Apply `PageRequest`/`PageResponse` contract to the list endpoint
- Register routes in the router

**Commits:**

```
feat(api): US-904 implement loan creation endpoint
feat(api): US-904 implement loan retrieval by id endpoint
feat(api): US-904 implement paginated loan listing endpoint
```

---

### US-905 — Central Exception Handler (Domain/Application Exceptions → HTTP Status)

**Points:** 3
**Depends on:** US-901

**Story:** As an API consumer, I need consistent, correct HTTP status codes and error bodies when a request fails, so client error handling doesn't have to guess or parse free-text messages.

**Scenarios (BDD):**

```gherkin
Feature: Central exception handling

  Scenario: Validation failure returns 400
    Given a request payload that fails input validation
    When the request is handled
    Then the response status is 400 with a structured error body describing the violation

  Scenario: Not-found domain exception returns 404
    Given a request referencing an entity that does not exist
    When the request is handled
    Then the response status is 404

  Scenario: Business rule violation returns 409
    Given a request that violates an active business rule (e.g., book unavailable, active loan limit)
    When the request is handled
    Then the response status is 409 with a structured error body naming the violated rule

  Scenario: Unexpected exception returns 500 without leaking internals
    Given an unhandled exception occurs during request processing
    When the response is written
    Then the response status is 500 with a generic error body, and the stack trace is only present in the ERROR log line, not in the response
```

**Tasks:**

- Implement a central `ExceptionMappingHandler`/wrapper applied to every registered route
- Map existing custom exceptions (validation, not-found, business rule violation) to 400/404/409 respectively
- Map unexpected exceptions to 500, using the base error-response shape from US-901
- Ensure ERROR-level logging (from E8's conventions) fires on every 500, without exposing stack traces in the HTTP response

**Commits:**

```
feat(api): US-905 implement central exception mapping handler
feat(api): US-905 map validation exceptions to 400 responses
feat(api): US-905 map not-found exceptions to 404 responses
feat(api): US-905 map business rule violations to 409 responses
feat(api): US-905 map unexpected exceptions to 500 without leaking internals
```

---

### US-906 — Correlation ID via HTTP Header (Accept / Generate / Echo)

**Points:** 2
**Depends on:** US-901

**Story:** As a developer, I need every HTTP request to carry a correlation ID — accepted from the client when provided, generated otherwise, and always returned in the response — so a request can be traced end-to-end in the logs the same way a CLI flow already can since E8.

**Scenarios (BDD):**

```gherkin
Feature: Correlation ID over HTTP

  Scenario: Correlation ID is accepted from the request header
    Given a request arrives with an X-Correlation-Id header
    When the request is handled
    Then that value is placed into MDC and echoed back in the response's X-Correlation-Id header

  Scenario: Correlation ID is generated when absent
    Given a request arrives without an X-Correlation-Id header
    When the request is handled
    Then a new correlation ID is generated, placed into MDC, and returned in the response's X-Correlation-Id header

  Scenario: MDC is cleared after the request completes
    Given a request has finished processing (successfully or with error)
    When the handler returns control to the server
    Then the MDC context is cleared to prevent leaking into unrelated requests
```

**Tasks:**

- Extend the HTTP foundation (US-901) with a correlation filter/wrapper applied to every route
- Reuse `LogTrace` (from E8) — read `X-Correlation-Id` if present, else generate
- Echo the resolved correlation ID back via the `X-Correlation-Id` response header
- Ensure MDC is cleared in a `finally` block per request, avoiding leakage across pooled request-handling threads

**Commits:**

```
feat(api): US-906 implement correlation id http filter
feat(api): US-906 accept inbound x-correlation-id header
feat(api): US-906 echo correlation id in response header
test(api): US-906 validate mdc cleared after request completes
```

---

### US-907 — OpenAPI Documentation (swagger-core + swagger-maven-plugin, Swagger UI)

**Points:** 5
**Depends on:** US-902, US-903, US-904, US-905

**Story:** As an API consumer (or reviewer), I need an accurate, browsable API contract, so I can understand and exercise the API without reading the handler source code.

**Scenarios (BDD):**

```gherkin
Feature: OpenAPI documentation

  Scenario: OpenAPI contract is generated at build time
    Given all resource handlers are annotated with swagger-core annotations
    When the Maven build runs
    Then an openapi.json/yaml contract is generated reflecting all registered endpoints

  Scenario: Swagger UI is reachable
    Given the application is running
    When a browser navigates to /docs
    Then the Swagger UI is rendered, listing all documented endpoints

  Scenario: Generated contract matches actual response shapes
    Given a documented endpoint's response schema
    When the corresponding handler is exercised
    Then the actual JSON response conforms to the documented schema
```

**Tasks:**

- Add `swagger-core` and `swagger-maven-plugin` to the `infrastructure` module
- Annotate Book/Customer/Loan handlers (`@Operation`, `@Parameter`, `@ApiResponse`) including pagination and error responses (400/404/409/500)
- Configure `swagger-maven-plugin` to generate `openapi.json`/`openapi.yaml` at build time
- Serve Swagger UI (static webjar resource) via the project's own `HttpServer` at `/docs`
- Document the RMM Level 2 / no-versioning / pagination conventions in the README

**Commits:**

```
build(docs): US-907 add swagger-core and swagger-maven-plugin
docs(api): US-907 annotate book handlers with openapi metadata
docs(api): US-907 annotate customer handlers with openapi metadata
docs(api): US-907 annotate loan handlers with openapi metadata
feat(docs): US-907 serve swagger ui as static resource
docs(readme): US-907 document rest api conventions
```

---

### US-908 — HTTP Layer Test Suite (Unit + E2E) and TD08 Closure

**Points:** 5
**Depends on:** US-902, US-903, US-904, US-905, US-906

**Story:** As a Product Owner, I need the new HTTP layer covered by both fast unit tests and realistic end-to-end tests, and the `infrastructure` coverage threshold re-measured against real numbers, so the API ships with the same testing discipline as the rest of the system instead of an arbitrary guessed target.

**Scenarios (BDD):**

```gherkin
Feature: HTTP layer test coverage

  Scenario: Handler unit tests exercise routing and error mapping with mocked usecases
    Given a resource handler under test
    When a request is simulated with a mocked usecase dependency
    Then the handler's response status and body match the expected contract

  Scenario: End-to-end tests exercise the real server against real persistence
    Given the embedded HttpServer is running against a Testcontainers PostgreSQL instance
    When a REST-Assured request is sent to a resource endpoint
    Then the full request/response cycle (routing, usecase, persistence, JSON serialization) behaves correctly

  Scenario: Infrastructure coverage threshold reflects the newly measured surface
    Given the HTTP handlers and router are fully covered by unit and e2e tests
    When JaCoCo coverage is measured for the infrastructure module
    Then the check goal threshold is updated to the measured value, not an assumed one
```

**Tasks:**

- Add `rest-assured` as a test-scope dependency in `infrastructure`
- Write unit tests per `HttpHandler` (Book, Customer, Loan, exception mapping) with Mockito-mocked usecases
- Write e2e tests (REST-Assured + Testcontainers) covering the full happy path and key error paths (400/404/409) per resource
- Measure resulting `infrastructure` module Instruction/Branch coverage and set the new JaCoCo `check` threshold accordingly
- Record the new threshold as an amendment to ADR 0006; formally resolve TD08 in `BACKLOG.md`

**Commits:**

```
build(test): US-908 add rest-assured test dependency
test(api): US-908 add unit tests for book handler
test(api): US-908 add unit tests for customer handler
test(api): US-908 add unit tests for loan handler
test(api): US-908 add unit tests for exception mapping handler
test(api): US-908 add e2e tests via rest-assured and testcontainers
build(coverage): US-908 raise infrastructure jacoco thresholds to measured baseline
docs(adr): US-908 amend adr 0006 with e9 infrastructure coverage baseline
```

---

## Placeholder Epics (Titles Only — Not Yet Refined)

Per the just-in-time grooming rule, these exist only as titles (plus any scope notes already captured in conversation) until their turn comes.

### E10 — CD / Go Live (Marco 2, AWS)
Packages the CD pipeline on top of persistence (E6), the documented API (E9), and structured logging (E8) already in place. ADR and formal US breakdown deferred until this epic enters refinement.

### E11 — Log Evolution & Full Observability (placeholder)
Scope note captured during E8 refinement, not yet detailed:
- Prometheus for metrics collection (JVM, application-level counters/gauges)
- Grafana for dashboards/visualization
- Builds on the SLF4J + Logback + JSON foundation from E8 — adds the metrics pillar, not a replacement for structured logging
- Full BDD/tasks deferred until this epic enters active refinement

### E12 — Overdue Enforcement Evolution (placeholder)
Scope note captured during post-E7 refinement (governed by ADR [0005](./adr/0005-roadmap-and-settlement-consolidation.md) and [ADR 0006](./adr/0006-jacoco-and-plpgsql-consolidation.md)):
- Background scheduler Job automatically transitioning overdue loans with explicit locking in Java (ADR [0002](./adr/0002-domain-owned-overdue-status-rule-with-explicit-locking.md)).
- Overdue customer restriction rule (3 overdue loans = 30-day block) and narrow fee settlement (late fee + daily interest) per **[0005](./adr/0005-roadmap-and-settlement-consolidation.md)**.
- Atomic multi-table bookkeeping persisted via a dedicated PL/pgSQL function per **[ADR 0006](./adr/0006-jacoco-and-plpgsql-consolidation.md)**.
- Domain-level notification port introduced (no-op adapter; real adapter ships in E13).
- Full BDD/tasks deferred until this epic enters active refinement.

### E13 — Notifications
Real adapter for the notification port introduced in E12, without touching the Job or the domain rule. Deliberately scheduled post Go-Live to exercise a full live CI/CD cycle. No further detail refined yet.

---

## History of Completed Epics

### E0 — Initial organization and cleanup
✅ Done · Iteration 1 (Jul 07–Jul 20)

### E1 — Foundation
✅ Done
Goal: prepare the application's base to support new interfaces without changing business rules — Services decoupled from I/O, interaction centralized in the CLI.

### E2 — MVP: Loan lifecycle
✅ Done · Sprint 2 · 17 points (US-201 to US-207)
Delivered the full flow via CLI: customer/book registration, loan, and return, respecting business rules (availability, active loan limits). Marco 1 (MVP) reached. Tag `v0.1.1`.
Full detail (Gherkin, tasks, implementation deviations): see Issues #13–#18 on GitHub Projects.

### E3 — Manual dependency inversion + repository standardization
✅ Done · Sprint 3 · 19 points

| US | Description | Points | Status |
|---|---|---|---|
| US-301 | Inject `InMemoryBookRepository` via constructor into Book usecases | 3 | ✅ Done |
| US-302 | Inject `InMemoryLoanRepository` via constructor into Loan usecases | 3 | ✅ Done |
| US-303 | Composition Root for manual usecase wiring | 5 | ✅ Done |
| US-304 | Fix the `equals`/`hashCode` contract for Book, Customer, and Loan (TD01) | 3 | ✅ Done |
| US-305 | Standardize repository naming (remove `I` prefix), convert enum→class, reorganize folders for JDBC | 5 | ✅ Done |

Full detail: see the corresponding Issues on GitHub Projects.

### E4 — Automated Test Foundation (JUnit 5 + Mockito)
✅ Done · Sprint 4 · 15 points (2 + 5 + 8) — revised down from 20 points after scope reallocation

| US | Description | Points | Status |
|---|---|---|---|
| US-401 | Test environment setup: JUnit 5, Mockito, coverage-report (JaCoCo aggregate) | 2 | ✅ Done |
| US-402 | Domain layer tests: Value Objects and entities, zero mock/infra dependency | 5 | ✅ Done |
| US-403 | Application layer tests via Mockito: Book/Loan usecases and validators, fully replacing the manual Fakes | 8 | ✅ Done |

Scope change on record: manual Fakes (`FakeBookRepository`, `FakeLoanRepository`, `FakeCustomerRepository`) were replaced by native Mockito (`@Mock` + `@InjectMocks`). Technical justification recorded as an ADR (US-401 task).

US-404 (Infrastructure layer tests, 5 original points) was removed from E4 and reallocated to E7, for execution with Testcontainers + a real database, aligned with JDBC's arrival in E6 — see TD06.

Full detail (Gherkin, tasks, commits): see the corresponding Issues on GitHub Projects.

### E6 — Real Persistence (JDBC/PostgreSQL) + Docker Containerization
✅ Done · Sprint 5 · 18 points (2 + 8 + 3 + 5)

| US | Description | Points | Status |
|---|---|---|---|
| US-501 | Local development environment with Docker Compose (Postgres) | 2 | ✅ Done |
| US-502 | JDBC Persistence with HikariCP and Flyway | 8 | ✅ Done |
| US-503 | Fat JAR Packaging (resolved TD05) | 3 | ✅ Done |
| US-504 | Application Containerization via Docker Multi-stage + Compose | 5 | ✅ Done |

E5 (standalone Docker) was formally discontinued in favor of this merged scope. TD05 (Fat JAR packaging) resolved via US-503. Full detail (Gherkin, tasks, commits): see the corresponding Issues on GitHub Projects.

### E7 — Real CI (Continuous Integration Gate)
✅ Done · Sprint 6 · 18 points (8 + 5 + 3 + 2)

| US | Description | Points | Status |
|---|---|---|---|
| US-701 | Infrastructure Tests with Testcontainers (resolved TD06) | 8 | ✅ Done |
| US-702 | CI Pipeline Base (GitHub Actions) | 5 | ✅ Done |
| US-703 | Per-Module JaCoCo Coverage Quality Gate | 3 | ✅ Done — amended post-close, see below |
| US-704 | Branch Protection on main | 2 | ✅ Done |

TD06 resolved via US-701 (Testcontainers-based integration tests for `BookDbRepository` and Flyway migrations against real PostgreSQL). CI pipeline (`ci.yml`) builds, tests, and enforces the coverage gate on every push to `develop` and PR into `main`. A `publish-docker` job was present during US-704 implementation and removed — CD scope is out of bounds for this epic, deferred fully to E10.

**US-703 amendment (post-close):** the JaCoCo `check` goal metric is **Instruction**, not Line — a deliberate technical decision (Instruction coverage measures bytecode granularity and is stricter than line coverage). Thresholds were also reduced from the original targets to reflect the current testable surface, most notably `infrastructure` (only the JDBC repository exists, no API layer yet):

| Module | Instruction | Branch |
|---|---|---|
| domain | 85% | 90% |
| application | 85% | 95% |
| infrastructure | 70% | 50% |

This reduction is temporary — see TD08 for the plan to revisit thresholds once E9 (API layer) and E2E tests against the live system (post E10) expand what's actually exercisable.

`main` requires a passing `build-and-test` check and blocks direct pushes (US-704). Full detail (Gherkin, tasks, commits): see the corresponding Issues on GitHub Projects.

### E8 — Structured Logging Foundation (SLF4J + Logback, System-Wide)
✅ Done · Sprint 7 · 10 points (3 + 5 + 2) — resolved TD07

| US | Description | Points | Status |
|---|---|---|---|
| US-801 | SLF4J + Logback Setup with Structured JSON Encoder | 3 | ✅ Done |
| US-802 | Retrofit Logging into Existing Use Cases (book/customer/loan) | 5 | ✅ Done |
| US-803 | Correlation ID Convention via MDC | 2 | ✅ Done |

Logback configured with a JSON structured encoder (`logstash-logback-encoder`) targeting stdout, with separate `logback-dev.xml`/`logback-prod.xml` profiles. Existing book/customer/loan usecases emit INFO/WARN/ERROR logs consistently. Correlation across log lines within an operation is handled via SLF4J's MDC (`LogTrace`), wired at the CLI entrypoint and cleared in a `finally` block. Full observability (metrics, dashboards, tracing) stayed explicitly out of scope — see ADR [0004](./adr/0004-slf4j-logback-without-full-observability.md) — revisited in E11. TD07 formally resolved. Full detail (Gherkin, tasks, commits): see the corresponding Issues on GitHub Projects.

---

## Commit Convention

Follows Conventional Commits, single-line commits (no body/footer — terminal-driven workflow), with the US ID right after the colon:

```
<type>(<scope>): <ID> <description in the imperative, lowercase, no trailing period>
```

**Pull Request flow (since E7 / US-704):** `main` is protected — direct pushes are rejected, and a pull request can only be merged once the required `build-and-test` check passes. Pushes straight to `develop` remain unrestricted, as before. Issues are still closed manually on the board (no auto-close configured).

Multiple commits on the same US: all repeat the same ID (`US-XXX`) at the start of the description.

## Versioning Convention

Follows SemVer (`MAJOR.MINOR.PATCH`):

- `0.y.z` while the project is in early development — internal contracts (architecture, persistence, framework) may still change without notice.
- `1.0.0` is reserved for when the system stabilizes (around Phase 3).
- `alpha`/`beta`/`rc` suffixes only make sense from Marco 2 onward (once the REST API exists).
- `SNAPSHOT` in `pom.xml` during ongoing development; tags/releases use the clean version.

**Tags:**

| Tag | Milestone | Date |
|---|---|---|
| v0.1.1 | Marco 1 — MVP (Epic E2 done) | see Git history |

## Board Conventions

- **Points:** simplified Fibonacci scale (1, 2, 3, 5, 8)
- **Status:** 🔲 To Do · 🟡 In Progress · 🔵 In Review · ✅ Done
- **Story numbering:** `US-{sprint}{sequential}` (e.g., US-401 → Sprint 4, item 1)
- **Technical debt numbering:** `TD-{sequential}`, not tied to a fixed sprint until prioritized
- **BDD scenarios:** Gherkin format (Given/When/Then), used as the formal acceptance criteria for each story

---

**Last update:** Epic E8 (Structured Logging Foundation) closed — Sprint 7, 10 points, resolved TD07. Epic E9 (REST API + Documentation) fully refined and ready for execution — Sprint 8, 35 points across 8 User Stories (US-901–US-908). Key E9 conventions: framework-free HTTP via `com.sun.net.httpserver.HttpServer`, Richardson Maturity Model Level 2 (no HATEOAS), no URI version prefix, `page`/`size` pagination, OpenAPI via `swagger-core`/`swagger-maven-plugin` (no Spring), REST-Assured for e2e tests. These conventions are recorded in ADR 0008 (pending drafting). TD08 (infrastructure coverage thresholds) will be closed at the end of E9 via US-908, based on measured coverage rather than an assumed target.
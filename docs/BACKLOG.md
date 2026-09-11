# 📚 Backlog

## Library Express — Agile Backlog

Java study project, evolved incrementally through real sprints.
PO/Scrum Master: Claude · Dev: Silvério

**Note on language:** this document, `README.md`, and every ADR under `docs/adr/` are written in English. Portuguese was previously used as the internal planning language for `BACKLOG.md`/`VISION.md`; that split has been retired in favor of a single language across the whole project. `VISION.md` itself has also been retired — see `docs/adr/0001-keep-library-express-framework-free.md` for the full rationale.

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
| E8 | Structured Logging Foundation (SLF4J + Logback, system-wide) | 🔵 Refined, ready for execution (Sprint 7) — resolves TD07 |
| E9 | REST API + Documentation (Swagger/OpenAPI) | ⏳ Backlog (title only) |
| E10 | CD — Go Live (Marco 2, on AWS) | ⏳ Backlog (title only) |
| E11 | Log Evolution & Full Observability (Prometheus/Grafana) | ⏳ Backlog (post Go-Live — scope note captured, not refined) |
| E12 | Overdue Enforcement Evolution — Job, Loan Restriction & Settlement | ⏳ Backlog (post Go-Live — scope note captured, not refined) |
| E13 | Notifications (loan created / completed / overdue) | ⏳ Backlog (post Go-Live, title only) |

E7 and E9 are not the same thing. E9 delivers the automated build + deploy — we call it "CD," not "CI/CD," because without tests running as a gate there is no verified integration, only automated delivery. E7 is when that becomes real CI: tests (E4) start running on every push, as a pipeline gate, before E9 exists. E7 is also where technical debt TD06 (infrastructure tests via Testcontainers) gets resolved, building on the JDBC layer already delivered in E6.

### Renumbering note (historical)

The epics from E8 onward were deliberately reordered and renumbered:

- The Spring Boot migration epic, originally numbered E9, is removed from the Library Express roadmap entirely — not deferred, descoped. Spring adoption moves to the next project (Internet Banking), built Spring-first from day one. Full rationale: ADR 0001.
- Customer Reputation, originally E10 and unordered ("no defined priority"), is now E8, moved ahead of Marco 2 — CI (E7) should be mature before a new feature epic ships, and Reputation no longer waits behind Go Live.
- CD / Go Live, originally E8, is renumbered to E9 to make room for E8 above. Its scope is unchanged.
- Notifications is a new epic (E10), scoped separately — see below.

E8 (Reputation) originated from a discussion about the return flow: when a loan is overdue, the customer loses reputation score; after 3 late returns the customer is "flagged" (concept still to be refined); after 5, the customer is blocked for a defined period. This is not technical debt — it is new scope. No fines or money are involved (payments are out of scope for this project entirely, now that the long-term vision document that used to sequence them has been retired — see ADR 0001).

Automatic overdue detection is resolved as: E8 introduces a scheduler-driven background Job. The business rule stays in the domain/application layer (Java), with concurrency safety handled through explicit locking rather than delegating the rule to the database — see ADR 0002. On a successful status transition, the Job calls a domain-level notification port; E8 ships a no-op adapter for that port, and E10 (Notifications) later plugs in the real one — see ADR 0003. Structured logging (SLF4J + Logback, TD07) is absorbed into E8 as well, since the Job is the first component in the system that runs unattended — see ADR 0004.

E10 (Notifications) is new scope: notify the customer by email when a loan is created, completed, or becomes overdue. Deliberately scheduled after Marco 2 (E9) — it is meant to simulate adding a feature to an already-deployed system through the full CI/CD pipeline, not to ship alongside Go Live. It implements the real adapter for the notification port introduced in E8, without touching the Job or the domain rule.

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
4. Structured logging foundation, system-wide (E8) 🔵 current — resolves TD07
5. REST API + documentation (Swagger/OpenAPI) (E9)
6. Marco 2 — Go Live (E10, packaging CD on top of persistence, Docker, and the documented API already in place)
7. Log evolution — full observability with Prometheus/Grafana (E11), post Go-Live
8. Overdue enforcement evolution — scheduler Job, loan restriction, and settlement/late fees (E12), post Go-Live
9. Notifications (E13), exercising the full CI/CD cycle against an already-deployed system

**E5 + E6 merge (decision on record):** Epic E5 (standalone Docker) was discontinued as its own block. Rationale: containerization only generates real business value once it's wired to real persistence — "containerize a CLI with an in-memory repository" is a weak portfolio narrative compared to "containerize an application with real PostgreSQL, HikariCP, and versioned migrations." E5 remains visible in the Epics table (not removed from the map), marked as discontinued, to preserve historical traceability. All containerization scope was absorbed by E6, which took on the name Real Persistence (JDBC/PostgreSQL) + Docker Containerization.

Database chosen: PostgreSQL (via pure JDBC, no ORM), aligned with AWS's RDS free tier.

**🚀 Marco 2 — Go Live**
First real deployment to production — on AWS (free tier: ECS/Fargate or Elastic Beanstalk with Docker; replaces the original Heroku plan, which carries little relevance in the target job market). Delivered together: the CD pipeline (E9), real persistence via JDBC + a Docker image (E6, merged scope), and a minimal API without a framework (`com.sun.net.httpserver.HttpServer`, no Spring — this project stays framework-free for its entire lifecycle, see ADR 0001). The deployment only "counts" once there is a real HTTP service receiving traffic, backed by real persisted data.

Why does Go Live come after tests/Docker/persistence/CI? The sequence tells a strong portfolio narrative: tested → containerized → persisted → automated → only then went to production — the way real teams operate.

Why raw tests and raw persistence before a framework? `@SpringBootTest`/Mockito and Spring Data JPA are abstractions over plain JUnit and plain JDBC. Doing the manual path first is deliberate: it forces understanding the mechanism underneath before a framework's convenience hides it. That abstraction is deliberately exercised in the next project (Internet Banking, Spring Boot from day one) rather than inside Library Express — see ADR 0001 for why the Spring migration was removed from this project's own roadmap instead of just being deferred.

### ⚙️ Phase 3 — Professional Software Engineering

Goal: deepen engineering practices on a system that has been in production since Marco 2 — security, observability, performance, scalability, documentation.
Scope: not yet formalized into epics (future backlog). Given the project's terminal roadmap now ends at E10 (Notifications) with only maintenance-level adjustments afterward, Phase 3 in its original broad sense will not be pursued inside Library Express — see ADR 0001.

---

## Principles

- The domain always comes first.
- New technology is introduced only when it solves a real problem.
- Every Sprint must produce a functional delivery.
- The architecture evolves alongside the system.
- Learning happens through practice.

**Working rule:** one epic at a time, refined in full detail (BDD + tasks) only once it enters execution. Future epics stay as titles only until their turn comes (just-in-time backlog grooming).

**Process rule (from E4 onward):** before generating any formal backlog artifact (epic breakdown, User Story, tasks) for a new implementation decision or architecture change, alignment with the Dev must be debated and closed in conversation first. Formal Markdown generation (points, Gherkin, tasks, commits) only happens after alignment — never before. This avoids rework from scope drift discovered after the fact.

---

## Technical Debt

| ID | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             | Points | Status                                 |
|---|---|---|---|
| TD01 | `equals`/`hashCode` contract for Book, Customer, and Loan                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               | 3 | ✅ Resolved (US-304, E3)               |
| TD05 | Fat JAR packaging (`maven-shade-plugin`, manifest with `Main-Class`) — frozen since E3, originally scheduled to resolve only in the (old) Go Live epic. Decision revised: the need for Docker moves the production justification for a single executable artifact earlier — freezing until Go Live no longer made sense.                                                                                                                                                                                                                                | 3 | ✅ Resolved (US-503, E6)               |
| TD06 | Infrastructure layer (in-memory repositories) with no automated test coverage since E4 closed. Intentional deferral: contract and concurrency tests (originally US-404) rewritten with Testcontainers against a real database (Postgres), after E6 (JDBC) delivered the definitive implementation.                                                                                                                                                                                                                                                      | 8 | ✅ Resolved (US-701, E7)               |
| TD07 | Structured logging (SLF4J + Logback). Originally scoped to only the E8 (old) scheduler Job; re-scoped during the second roadmap revision to cover the entire system from the start, ahead of the API and any scheduler — establishing observability as a convention every subsequent epic inherits, rather than retrofitting it under time pressure later. Full observability/tracing (metrics, dashboards) stays explicitly out of scope for this epic — see ADR [0004](./adr/0004-slf4j-logback-without-full-observability.md) and E11. | 10 | ✅ Resolved (US-803, E8) |
| TD08 | Coverage thresholds (Instruction/Branch, per module) were reduced during US-703 to reflect the current testable surface — notably `infrastructure` at 70%/50%. Revisit and raise thresholds once the API layer (E9) expands what's testable, and ideally once E2E tests against the live system (post E10) exist.                                                                                                                                                                                                                                       | — (to be estimated when revisited) | 🟡 Accepted, tracked for E9/E10        |

---

## 🔵 Epic E7 — Real CI (Continuous Integration Gate)

- **Business-level audit trail (DB-backed).** Distinct from technical logging (SLF4J + Logback, ADR [0004](./adr/0004-slf4j-logback-without-full-observability.md), delivered via E8). A "who did what" table for business events was proposed during E8/E12 refinement and intentionally deferred — revisit only after all currently planned epics (through E13) are closed. Not a replacement for structured logging; a separate concern if ever pursued.

---

## 🔵 Epic E8 — Structured Logging Foundation (SLF4J + Logback, System-Wide)

**Sprint:** 7 (proposed)
**Total points:** 10 (3 + 5 + 2)
**Status:** 🔵 Refined, ready for execution

### Decisions on record for this epic

- No framework: HTTP layer built on `com.sun.net.httpserver.HttpServer`, with a small hand-rolled router (path/method → `HttpHandler`), per ADR [0001](./adr/0001-keep-library-express-framework-free.md).
- JSON serialization via **Jackson** (already centralized in the parent `dependencyManagement`).
- **Richardson Maturity Model, Level 2**: correct HTTP verbs, resource-based URIs, semantic status codes. No HATEOAS (Level 3) — deliberately out of scope, since the only consumers are the project's own soon-to-be-sunset CLI and local manual testing; no real client benefits from hypermedia navigation. Full rationale: ADR 0008.
- **No URI version prefix** (`/books`, not `/v1/books`) — no external consumer exists yet to justify a stable versioned contract.
- **Pagination:** `page`/`size` query parameters (Spring Data convention), optional with defaults (`page=0`, `size=20`), so the CLI — which sends neither parameter — keeps working unchanged against the first default page.
- This epic exposes the **existing** book/customer/loan usecases over HTTP; no new business rules are introduced.
- Central exception handling maps existing domain/application exceptions to HTTP status codes (400/404/409/etc.) — one dedicated US, not scattered per-handler try/catch.
- Correlation ID (E8's `CorrelationIdSupport`) moves its entrypoint boundary from the CLI to the HTTP handler: accepts an inbound `X-Correlation-Id` header when present, generates one otherwise, and always echoes it back in the response header.
- OpenAPI documentation generated via `swagger-core` annotations + `swagger-maven-plugin` (build-time static contract) with Swagger UI served as a static resource by the project's own `HttpServer` — not `springdoc-openapi`, which requires a Spring runtime. Full rationale: ADR 0008.
- Coverage priority is **domain > application > infrastructure**, reflecting where business-rule density actually lives. `domain`/`application` thresholds are not expected to drop (no new business logic). The `infrastructure` threshold is **measured after implementation**, not guessed upfront — closes TD08 as an amendment to ADR 0006.
- Test strategy: unit tests per `HttpHandler` (usecases mocked via Mockito) + end-to-end tests via **REST-Assured** against the real embedded `HttpServer` backed by Testcontainers Postgres (Java's closest equivalent to Node's supertest).

### Epic goal

Establish a structured, machine-parseable logging foundation across the entire system before any new component (API, scheduler, observability stack) is built on top of it, so operational visibility is a built-in convention rather than an afterthought retrofitted later under time pressure.

### Business value

Structured logging (SLF4J + Logback, JSON-encoded, correlation-aware via MDC) is table-stakes in professional Java backend roles — interviewers expect to see this as a baseline, not an advanced topic. Establishing it now, ahead of the API and CD epics, means every subsequent epic inherits observability by convention instead of bolting it on afterward.

### Definition of Done — Epic E8

- [ ] SLF4J + Logback configured with a structured JSON encoder (US-801)
- [ ] Existing book/customer/loan use cases emit INFO/WARN/ERROR logs consistently (US-802)
- [ ] Correlation ID convention via MDC implemented and validated (US-803)
- [ ] Logging conventions documented in the README
- [ ] All 3 User Stories in Done status
- [ ] TD07 formally resolved

---

### US-801 — SLF4J + Logback Setup with Structured JSON Encoder

**Points:** 3
**Depends on:** — (unblocked, first US of the epic)

**Story:** As a developer, I need a structured logging foundation in place, so every component built from this point forward (existing use cases, the future API, the future scheduler) emits machine-parseable logs instead of ad hoc console output.

**Scenarios (BDD):**

```gherkin
Feature: Structured logging foundation

  Scenario: Application emits logs in structured JSON format
    Given the SLF4J + Logback dependencies are configured
    When any log statement is executed
    Then the output is a single-line JSON object containing timestamp, level, logger name, and message

  Scenario: Log level is configurable per environment
    Given a Logback configuration profile for local development
    And a separate profile for containerized/production execution
    When the application starts under each profile
    Then the effective log level matches the profile's configuration
```

**Tasks:**

- Add `logback-classic` and a JSON encoder (`logstash-logback-encoder` or equivalent) to the `infrastructure` module
- Configure `logback.xml` with a JSON encoder targeting stdout
- Define separate profiles: `logback-dev.xml` (human-readable, console) vs `logback-prod.xml` (JSON)
- Document logging conventions in the README (levels: ERROR/WARN/INFO/DEBUG usage criteria)

**Commits:**

```
build(logging): US-801 add slf4j and logback dependencies
build(logging): US-801 configure json structured encoder
build(logging): US-801 add dev and prod logback profiles
docs(readme): US-801 document logging conventions
```

---

### US-802 — Retrofit Logging into Existing Use Cases

**Points:** 5
**Depends on:** US-801

**Story:** As a Product Owner, I need meaningful log output on every existing business flow, so operational visibility isn't limited to newly built features going forward.

**Scenarios (BDD):**

```gherkin
Feature: Logging in existing use cases

  Scenario: Successful loan creation is logged at INFO level
    Given a valid loan creation request
    When the use case completes successfully
    Then an INFO log entry is recorded with the loan and customer identifiers

  Scenario: Business rule violation is logged at WARN level
    Given a loan request that violates an active business rule (e.g., book unavailable)
    When the use case rejects the request
    Then a WARN log entry is recorded with the violated rule and relevant identifiers

  Scenario: Unexpected exception is logged at ERROR level
    Given an unexpected exception occurs during a use case execution
    When the exception propagates
    Then an ERROR log entry is recorded with the exception stack trace
```

**Tasks:**

- Inject `Logger` (via `LoggerFactory.getLogger`) into Book, Customer, and Loan usecases
- Add INFO logging on successful completion of each usecase
- Add WARN logging on business validation failures (existing custom exceptions)
- Add ERROR logging at exception boundaries (CLI layer / composition root)
- Ensure no sensitive data is logged in plain text

**Commits:**

```
feat(logging): US-802 add info logging to book usecases
feat(logging): US-802 add info logging to customer usecases
feat(logging): US-802 add info logging to loan usecases
feat(logging): US-802 add warn logging on business rule violations
feat(logging): US-802 add error logging at exception boundaries
```

---

### US-803 — Correlation ID Convention via MDC

**Points:** 2
**Depends on:** US-801

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
- Reuse `CorrelationIdSupport` (from E8) — read `X-Correlation-Id` if present, else generate
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
Feature: Correlation ID via MDC

  Scenario: Correlation ID is generated at the start of an operation
    Given a use case execution begins
    When no correlation ID is present in the current context
    Then a new correlation ID is generated and placed into MDC

  Scenario: All log lines within the same operation share the correlation ID
    Given a correlation ID has been set in MDC for the current operation
    When multiple log statements are executed during that operation
    Then every log line includes the same correlation ID field

  Scenario: MDC is cleared after the operation completes
    Given an operation has finished (successfully or with error)
    When the use case returns control to the caller
    Then the MDC context is cleared to prevent leaking into unrelated operations
```

**Tasks:**

- Implement a `CorrelationIdSupport` helper (set/get/clear on MDC)
- Wire correlation ID generation at the entrypoint of each CLI flow (composition root boundary)
- Ensure MDC is cleared in a `finally` block to avoid leakage across CLI invocations
- Validate correlation ID appears consistently across all log lines of a single flow

**Commits:**

```
feat(logging): US-803 implement correlation id support via mdc
feat(logging): US-803 wire correlation id generation at cli entrypoint
test(logging): US-803 validate mdc cleared after operation completes
```

---

## Placeholder Epics (Titles Only — Not Yet Refined)

Per the just-in-time grooming rule, these exist only as titles (plus any scope notes already captured in conversation) until their turn comes.

### E9 — REST API + Documentation (Swagger/OpenAPI)
Minimal REST API via `com.sun.net.httpserver.HttpServer` (framework-free, per ADR [0001](./adr/0001-keep-library-express-framework-free.md)), documented with Swagger/OpenAPI. No further detail refined yet.

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

Logback configured with a JSON structured encoder (`logstash-logback-encoder`) targeting stdout, with separate `logback-dev.xml`/`logback-prod.xml` profiles. Existing book/customer/loan usecases emit INFO/WARN/ERROR logs consistently. Correlation across log lines within an operation is handled via SLF4J's MDC (`CorrelationIdSupport`), wired at the CLI entrypoint and cleared in a `finally` block. Full observability (metrics, dashboards, tracing) stayed explicitly out of scope — see ADR [0004](./adr/0004-slf4j-logback-without-full-observability.md) — revisited in E11. TD07 formally resolved. Full detail (Gherkin, tasks, commits): see the corresponding Issues on GitHub Projects.

---

## Commit Convention

Follows Conventional Commits, single-line commits (no body/footer — terminal-driven workflow), with the US ID right after the colon:

```
<type>(<scope>): <ID> <description in the imperative, lowercase, no trailing period>
```

No Pull Request flow yet until E7's branch protection (US-704) lands; commits go straight to `develop` — no auto-close on Issues. When a US is completed, close its Issue manually on the board.

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

**Last update:** Roadmap reorganized from E8 onward per **ADR 0005** (Roadmap Restructuring & Settlement) and **ADR 0006** (JaCoCo Metrics & PL/pgSQL Exception). Epic E8 is currently refined and ready for execution (Structured Logging Foundation, resolving TD07). ADR 0005 and ADR 0006 are officially Accepted.

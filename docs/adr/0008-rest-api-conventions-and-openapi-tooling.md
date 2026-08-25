# ADR 0008: REST API Conventions and OpenAPI Tooling (Epic E9)

## Status

Accepted

## Context

Epic E9 introduces the project's first HTTP boundary, exposing the existing book, customer, and loan use cases via a REST API built on `com.sun.net.httpserver.HttpServer` — no application framework, per ADR [0001](./0001-keep-library-express-framework-free.md). Three design decisions needed a formal record before refinement could proceed to formal User Stories, since each affects contract shape, points estimation, and long-term maintainability:

1. What level of REST maturity (Richardson Maturity Model) the API targets.
2. Whether the API is URI-versioned from day one.
3. How OpenAPI/Swagger documentation is generated without pulling in a framework.

## Decision

### 1. Richardson Maturity Model — Level 2

The API targets **RMM Level 2**: correct use of HTTP verbs (GET/POST/etc.), resource-oriented URIs (`/books`, `/books/{id}`), and semantically correct status codes (200/201/400/404/409/500). **Level 3 (HATEOAS)** is explicitly out of scope.

Rationale: hypermedia controls (response envelopes with `_links`, relation types, client-side link navigation) are a real implementation cost with no corresponding benefit here. The only consumers of this API are the project's own CLI — being sunset to occasional local testing use — and manual/exploratory calls. There is no external client that would ever traverse the API via discovered links instead of a known, documented contract. Adopting Level 3 would be complexity introduced without a real need, which the project's principles explicitly ask to avoid or, if pursued anyway, to document honestly as a learning-only exercise. Level 2 is judged to already deliver the portfolio and interview value expected internationally (correct REST semantics is table-stakes; HATEOAS is a niche, rarely-tested topic in practice).

### 2. No URI version prefix

Endpoints are exposed without a version segment (`/books`, not `/v1/books`).

Rationale: URI versioning exists to protect existing external consumers from breaking changes. This API has none yet — introducing a version prefix now would be speculative design for a scenario (external consumers, contract stability guarantees) that doesn't exist at this stage of the project. This decision can be revisited if E10 (Go-Live) or beyond introduces a real external consumer requiring contract stability.

### 3. OpenAPI documentation via swagger-core + swagger-maven-plugin, not springdoc-openapi

The API is documented using:
- **`swagger-core`** annotations (`@Operation`, `@Parameter`, `@ApiResponse`, etc.) applied directly to HTTP handlers.
- **`swagger-maven-plugin`**, which scans those annotations at build time and generates a static `openapi.json`/`openapi.yaml` contract.
- **Swagger UI**, bundled as a static webjar resource and served by the project's own `HttpServer` at `/docs` — no separate documentation server.

`springdoc-openapi` was considered and rejected: it scans `@RestController`-annotated Spring beans at runtime, which requires a running Spring context. Adopting it would mean pulling the Spring framework into Library Express's classpath solely to generate documentation — directly contradicting ADR 0001's framework-free scope, for a convenience (annotation scanning wired to DI) that provides no learning value proportional to the architectural cost. `swagger-core` is an annotation/model library, not an application framework — it has no DI container, no request-handling runtime, and does not conflict with the project's framework-free design.

This is a case where, per the project's stated principle, adopting a piece of *tooling* (not a framework) is worth the added dependency: automatically generated, always-in-sync API documentation is real production value and a real interview-relevant skill, and it's achievable here without compromising the project's central architectural bet.

## Alternatives Considered

**Javalin** (a lightweight Java web framework, commonly compared to Express.js — thin routing/handler layer over Jetty, with Jackson integration built in) surfaced during independent research **after** this epic had already been refined and formalized into User Stories. It was not evaluated during the original E9 refinement discussion, and its discovery did not trigger a re-opening of this decision.

Javalin sits in a real middle ground between `com.sun.net.httpserver` (fully manual) and Spring (full framework, DI container, annotation-driven scanning): it removes the boilerplate of routing and JSON wiring without imposing dependency injection or convention-based magic. For a project not bound by ADR 0001's framework-free scope, it would be a reasonable, arguably better-engineered choice than a hand-rolled router.

It was not adopted here, for two compounding reasons:
1. **ADR 0001 scope.** Even though Javalin is not a full application framework in the Spring sense, it still replaces exactly the mechanism (`HttpServer` routing, request/response binding) that Epic E9 exists to build and understand manually. Adopting it would remove the specific learning value US-901 (HTTP foundation) targets — understanding what a routing layer actually does before a library abstracts it away, the same rationale already applied to raw JUnit, raw JDBC, and manual DI earlier in the roadmap.
2. **Timing.** By the time Javalin was identified, E9 had already been debated, aligned, and formalized into `BACKLOG.md` per the project's process rule (alignment before formal artifacts). Reversing an already-Accepted decision without a real problem it solves — as opposed to a marginally more convenient one — would not meet the project's bar for introducing new technology.

This alternative is recorded here for traceability and honesty about what was known and considered, not because it changes the decision. `com.sun.net.httpserver.HttpServer` with a hand-rolled router remains the Epic E9 implementation choice.

## Consequences

**Positive:**
- REST conventions stay simple and fast to implement — no hypermedia envelope design, no link-relation catalog to maintain.
- The documented contract (`openapi.json`) is generated from the same annotations that drive the handlers, reducing drift between code and docs.
- Swagger UI is available locally with zero extra infrastructure (served by the project's own server).
- The framework-free architectural bet (ADR 0001) is preserved end-to-end through Marco 2 (Go-Live).

**Negative / trade-offs:**
- If Library Express ever gained real external API consumers, the lack of URI versioning and hypermedia support would require retrofitting — accepted as a deliberate, documented risk given the project's actual scope and audience.
- `swagger-core` annotations add verbosity to handler code compared to Spring's more automatic contract inference — accepted as the cost of avoiding a framework dependency.
- Should the project ever need RMM Level 3 for a real integration need, revisiting this ADR (not silently reversing it) would be required, per the project's ADR discipline.

## Related

- ADR [0001](./0001-keep-library-express-framework-free.md) — Keep Library Express framework-free
- ADR [0004](./0004-slf4j-logback-without-full-observability.md) — SLF4J + Logback without full observability
- ADR [0006](./0006-jacoco-and-plpgsql-consolidation.md) — JaCoCo metrics and PL/pgSQL exception (amended by US-908 with the E9 `infrastructure` coverage baseline)
- `docs/BACKLOG.md` — Epic E9 (US-901 through US-908)
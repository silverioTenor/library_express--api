# ADR 0008 — REST API Documentation Conventions (RMM Level 2, No URI Versioning, OpenAPI via swagger-core)

## Status

Accepted (Epic E9, Sprint 8). Amended once — see **Amendment 1** below.

## Context

Epic E9 exposes the existing book/customer/loan usecases over HTTP, on top of the framework-free foundation established in ADR 0001 (`com.sun.net.httpserver.HttpServer`, no Spring). This required closing four related decisions before implementation could start:

1. What maturity level the API should target (Richardson Maturity Model).
2. Whether the API should carry a version prefix in its URIs.
3. Which tooling generates and serves the OpenAPI/Swagger contract.
4. Whether a framework-based HTTP layer (e.g. Javalin) should be reconsidered for this epic, given it surfaced as a viable alternative only after the framework-free approach (ADR 0001) had already been reaffirmed.

## Decision

### 1. Richardson Maturity Model — Level 2

The API targets **RMM Level 2**: correct HTTP verbs, resource-based URIs, and semantic status codes. **HATEOAS (Level 3) is explicitly out of scope.** The only consumers of this API are the project's own soon-to-be-sunset CLI and local manual testing — no external client exists that would benefit from hypermedia-driven navigation. Introducing Level 3 here would be complexity without a real consumer to justify it, which conflicts with the project's core principle that new technology is adopted only when it solves a real problem (or is documented honestly as a learning exercise, which is not the case here).

### 2. No URI version prefix

Endpoints are exposed as `/books`, `/customers`, `/loans` — **not** `/v1/books`. No external consumer exists yet to justify a stable, versioned contract. Introducing a version prefix now would be speculative design for a compatibility guarantee nobody is depending on. This can be revisited if/when Library Express gains an external consumer with its own release cadence.

### 3. OpenAPI tooling — swagger-core + swagger-maven-plugin (not springdoc-openapi)

The OpenAPI contract is generated at **build time** via `swagger-core` annotations (`@Operation`, `@Parameter`, `@ApiResponse`, `@OpenAPIDefinition`) combined with the `swagger-maven-plugin` (`resolve` goal, bound to the `compile` phase). Swagger UI is served as a static webjar resource by the project's own `HttpServer`, at `/docs`.

`springdoc-openapi` was **not** chosen, as it requires a Spring runtime (`@RestController`, Spring MVC dispatch) to introspect — which would silently reintroduce the framework dependency this project deliberately avoids for its entire lifecycle (ADR 0001). `swagger-core` was chosen instead because its annotation model and plugin tooling are framework-agnostic at the library level, even though its default reader mechanics carry their own dependency — see **Amendment 1**.

### 4. Alternatives considered — Javalin

During refinement, **Javalin** (a lightweight Java web framework with built-in OpenAPI support) surfaced as a technically reasonable alternative to hand-rolling routing on top of `com.sun.net.httpserver.HttpServer`. It was **not adopted**, for two reasons:

- **Timing:** it was discovered only after the framework-free foundation for E9 (router, Jackson wiring — US-901) was already aligned and in progress. Reopening that decision mid-epic without a real problem forcing the change would have meant rework without corresponding value.
- **Learning objective:** US-901's explicit goal is to understand HTTP routing mechanics manually — request dispatch, path matching, handler registration — before any framework abstracts it away. Javalin, while lightweight, would have hidden exactly the mechanism this epic exists to expose. This mirrors the same rationale already recorded in ADR 0001 for deferring the Spring Boot step to the next project.

This is recorded honestly as a reasonable option not taken, rather than omitted from the record — consistent with the project's principle that technology choices favoring a learning objective over convenience are documented as such.

## Consequences

- The API is simple to reason about and test (no framework request lifecycle to mock or understand), at the cost of more manual wiring per endpoint (US-901–US-906).
- No hypermedia navigation exists; API consumers must know endpoint URIs out-of-band (acceptable given the current consumer set).
- No versioning strategy exists yet; a breaking change today would break the CLI immediately — acceptable while the CLI is the only real consumer and is itself scheduled for sunset.
- The OpenAPI contract is regenerated on every `mvn compile`, so it cannot drift from the annotated source by more than one build.

---

## Amendment 1 — JAX-RS Annotations as Build-Time-Only Metadata

**Date:** During US-907 implementation (Epic E9, Sprint 8)
**Status:** Amendment — clarifies and extends the original decision; does **not** reverse it.

### Context for the amendment

When implementing US-907, it became clear that `swagger-maven-plugin`'s default reader (`swagger-jaxrs2`) discovers endpoints by scanning **JAX-RS annotations** (`@Path`, `@GET`, `@POST`, `@PATCH`, `@Consumes`, `@Produces`) on the annotated classes. This mechanic was not explicit when this ADR was first closed — the original decision named the tooling (`swagger-core` + `swagger-maven-plugin`) but not the reader's dependency on JAX-RS annotations to know each endpoint's path and verb. There is no official swagger-core reader for `com.sun.net.httpserver`.

### Decision

JAX-RS annotations (`jakarta.ws.rs-api`) are added to the existing `HttpHandler`-adjacent Controller classes (`BookController`, `CustomerController`, `LoanController`) **purely as reflective metadata for the build-time reader to scan.** This does **not** introduce a JAX-RS runtime:

- No `Application`, `ResourceConfig`, servlet container, or JAX-RS implementation (Jersey, RESTEasy) is added to the runtime classpath in an active capacity.
- The hand-rolled `Router` (US-901) remains the **sole real dispatcher** at runtime — it does not read JAX-RS annotations and is functionally unaware of their presence.
- The `swagger-jaxrs2` reader executes only inside the Maven build process (`compile` phase), never inside the running application.

This is treated as equivalent in kind to using Jackson annotations (`@JsonProperty`) for JSON mapping metadata: a declarative, dependency-light annotation set consumed by tooling, without pulling in the runtime behavior normally associated with that annotation family.

### Known limitation accepted alongside this amendment

Because route registration (`Router.register(HttpVerb, path, handler)` in each `RouteModule`) and the JAX-RS path/verb annotations on Controllers are two independent declarations of the same fact (method + path), **they can drift** if one is changed without the other. This is an accepted, documented trade-off rather than a hidden risk — the alternative (routing via JAX-RS reflection) would reintroduce exactly the routing mechanism ADR 0001 and US-901 deliberately avoid. Discipline (checking both sides when a route changes) is the accepted mitigation; no tooling enforces the consistency automatically.

### Why this is an amendment, not a reversal

The original decision to use `swagger-core` + `swagger-maven-plugin` stands unchanged. This amendment only fills in a mechanical detail — how the chosen plugin's reader identifies endpoints — that was not spelled out originally. No prior decision in this ADR is contradicted or narrowed.
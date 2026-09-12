# ADR 0001: Keep Library Express Framework-Free; Defer Spring Boot to the Next Project

## Status
Accepted

## Context
The original Phase 2 engineering roadmap included an epic (previously numbered E9) for migrating Library Express from raw Java/JDBC to Spring Boot, planned to run after Go Live (Marco 2). Library Express was deliberately built without a framework — manual dependency injection, raw JDBC, hand-rolled test infrastructure — specifically so that the concepts a framework like Spring abstracts away (DI containers, ORM, connection pooling wiring, etc.) would be understood firsthand before being hidden behind annotations.

On review, forcing the Spring migration into this same project was judged inefficient: it would extend an already-scoped learning vehicle indefinitely and dilute its core narrative. A second, framework-first project (an Internet Banking system, built with Spring Boot from day one) is already planned as the next step in the author's Java learning path and career goal of reaching international backend roles.

The long-term product vision document (`VISION.md`) was also reviewed in this context. It was explicitly aspirational and non-executable — it never generated epics directly, only informed low-cost architectural choices in the current roadmap (e.g., the domain/application/infrastructure layering, which incidentally also supports a future Strangler Fig extraction). Retiring it has no material impact on delivered or planned epics.

## Decision
- The Spring Boot migration epic is removed from the Library Express roadmap entirely (not deferred — descoped).
- `VISION.md` is retired. Its long-term expansions (self-service platform, third-party marketplace, payments, subscriptions, audiobook/video media, microservices/EDA) are not planned for this project and the document will no longer be revisited each phase.
- Spring Boot adoption is deferred to the next project (Internet Banking), which will be Spring-first from its initial commit.
- Library Express's terminal roadmap becomes: E4 → E6 → E7 → E8 (Reputation + automatic status Job) → E9 (CD / Go Live on AWS — Marco 2) → E10 (Notifications, post-Marco 2). After E10, the project is frozen; only maintenance-level adjustments are expected afterward.

## Consequences
**Positive**
- Produces a stronger two-project portfolio narrative: Project 1 demonstrates understanding of what a framework abstracts; Project 2 demonstrates production-speed fluency with that framework, built on that understanding.
- Avoids indefinite scope creep in a project that was always meant to be a bounded learning vehicle.
- No rework required on already-shipped epics (E4, E6 in progress) — the decision only affects unstarted scope.

**Negative / Risks**
- A reviewer skimming the Library Express repository alone will not see any Spring Boot usage. This must be mitigated by an explicit README note framing the project as the deliberately framework-free half of a two-project sequence, with a pointer to the Internet Banking project.
- The retired `VISION.md` expansions (marketplace, payments, subscriptions, media, microservices/EDA) will not be pursued in any project unless explicitly revisited in the future.
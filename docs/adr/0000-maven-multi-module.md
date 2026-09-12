# ADR-0000 — Maven Modularization (domain / application / infrastructure)

## Status
**Accepted — retroactively recorded.**
The decision was implemented directly in the `develop` branch, without going through a formal
User Story. This ADR documents the decision after the fact (see TD04 in `BACKLOG.md`) as a
recognized exceptional case—not as a new standard workflow.

**Decision date (implementation):** see commit history in `develop`, during Epic E4.
**ADR recording date:** documentation consolidation post-E3/during E4.

## Context

Up until Epic E3, the **Library Express** project was a single Maven module (`packaging: jar`),
with the `domain`, `application`, and `infrastructure` layers organized merely as Java
packages within `org.libraryexpress`, all sharing a single `src/main` and `src/test`.

This organization was sufficient as long as there was no significant automated test suite.
With the start of Epic E4 (JUnit 5 Automated Testing Foundation), a practical need arose
to isolate tests by layer:

- **Domain** tests (`BookTest`, `CustomerUnitTest`, `LoanTest`) need to validate business
  rules and contracts (`equals`/`hashCode`) without any access to infrastructure
  implementations.
- **Usecase** tests (`application`) require repository test doubles (fakes) but must not
  have access to CLI classes or infrastructure configuration.
- A single `src/test` shared by all layers does not enforce this boundary—nothing
  prevents, for example, a domain test from importing an infrastructure class by
  mistake; this would only be detected via manual review, not by the build process.

Furthermore, the project already used a package organization that mirrored Clean
Architecture (`domain`, `application`, `infrastructure`), but this separation was merely
**logical** (package convention), not **physical** (compilation unit). There was no actual enforcement
preventing `domain` from depending on `infrastructure`.

## Decision

Migrate from a single Maven module to a **multi-module project**, featuring:

- A root aggregator `pom.xml` (`packaging: pom`) with no source code of its own, responsible for
  centralizing `dependencyManagement` (JUnit, MapStruct, Jackson versions) and
  `pluginManagement` (compiler, surefire, exec).
- Three child modules, each with its own `pom.xml` and independent `src/main`/`src/test` directories:
- **`domain`** — entities, enums, helpers, repository/validator interfaces. No
  dependency on `application` or `infrastructure`.
- **`application`** — use cases, DTOs, mappers, application validators. Depends
  only on `domain`.
- **`infrastructure`** — CLI entry point, configuration, concrete repository implementations
  (in-memory). Depends on `domain` and `application`.
- Dependency direction fixed as `infrastructure → application → domain`, now
  **enforced by Maven itself**: an attempt by `domain` to depend on `application` causes
  the build to fail, not just the code review.

## Alternatives considered

| Alternative | Reason for rejection |
|---|---|
| Keep single module, rely only on package conventions + disciplined manual review | Does not scale as the project grows; the boundary between layers relies on human discipline rather than build enforcement. Fails silently. |
| Single module, but with `src/test` segmented by custom source sets (e.g., Gradle-style) | Maven lacks mature native support for multiple source sets per module without additional plugins (e.g., `build-helper-maven-plugin`), which would add complexity comparable to a multi-module setup without the benefit of enforced inter-layer dependencies. |
| Multi-module, but with finer granularity (e.g., one module per feature — `book`, `customer`, `loan`) | Premature for the project's current stage (solo MVP, Phase 2). It would increase build maintenance costs without a proportional benefit—violating the "low-cost, future-aware" principle adopted for the project. |

## Consequences

**Positive**
- Domain tests physically isolated from infrastructure—accidental dependency leakage is impossible.
- Dependency direction between layers (`domain ← application ← infrastructure`) enforced
  by the Maven compiler, not just by convention.
- Each module can run its test suite in isolation (`mvn test -pl domain`), useful for
  rapid feedback cycles during TDD.
- Structure aligns with expectations for professional teams practicing Clean/Hexagonal
  Architecture with actual enforcement—relevant to the international market objective.

**Negative / Accepted Trade-offs**
- Build overhead: `mvn install` must resolve the multi-module reactor, which is slower than
  a single-module build for a project of this size.
- Final packaging (executable `.jar`) becomes more complex—the `infrastructure`
  module alone does not include `domain`/`application` dependencies in the artifact,
  requiring a shade/assembly plugin (see TD05, linked to Epic E8).
  -Three `pom.xml` files to keep versions/dependencies synchronized instead of a single one,
  albeit mitigated by centralized `dependencyManagement` in the parent.

## Notes
This ADR was created as part of technical debt item TD04, explicitly recognized
as an exceptional case (implementation without a prior User Story). It does not set a precedent for
future structural changes to proceed without planning in `BACKLOG.md`.
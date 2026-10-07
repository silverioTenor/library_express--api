## Library Express — Agile Backlog

Java study project, evolved incrementally through real sprints.
PO/Scrum Master: Claude · Dev: Silvério

**Note on language:** this document, `README.md`, and every ADR under `docs/adr/` are written in English. Portuguese was previously used as the internal planning language for `BACKLOG.md`/`VISION.md`; that split has been retired in favor of a single language across the whole project. `VISION.md` itself has also been retired — see 0001-keep-library-express-framework-free for the full rationale.

**Note on detail level:** completed epics are recorded here only as a summary (status, points, sprint). Full detail (Gherkin, tasks, implementation deviations) lives in Git history and in issue closing comments on GitHub Projects — it is not duplicated here, to avoid two diverging sources of truth.

---

## Epics

| ID | Epic | Status |
| --- | --- | --- |
| E0 | Initial organization and cleanup | ✅ Done (Sprint 01) |
| E1 | Foundation — decouple I/O from Services, centralize interaction in the CLI | ✅ Done |
| E2 | MVP — Loan lifecycle | ✅ Done (Sprint 2) |
| E3 | Manual dependency inversion + repository standardization + TD01 | ✅ Done (Sprint 3) |
| E4 | Automated test foundation — JUnit 5 + Mockito | ✅ Done (Sprint 4), with a note — see TD06 |
| E5 | Containerization (Docker) | ⛔ Discontinued — scope absorbed by E6 |
| E6 | Real Persistence (JDBC/PostgreSQL) + Docker Containerization | ✅ Done (Sprint 5) |
| E7 | Real CI — automated tests running as a pipeline gate | ✅ Done (Sprint 6) — resolved TD06 |
| E8 | Structured Logging Foundation (SLF4J + Logback, system-wide) | ✅ Done (Sprint 7) — resolves TD07 |
| E9 | REST API + Documentation (Swagger/OpenAPI) | ✅ Done (Sprint 8) |
| E10 | CD — Go Live (Marco 2, on AWS) | ✅ Done (Sprint 9) |
| E11 | Log Evolution & Full Observability (Prometheus/Grafana) | ⏳ Backlog (post Go-Live — scope note captured, not refined) |
| E12 | Overdue Enforcement Evolution — Job, Loan Restriction & Settlement | ⏳ Backlog (post Go-Live — scope note captured, not refined) |
| E13 | Notifications (loan created / completed / overdue) | ⏳ Backlog (post Go-Live, title only) |
| E14 | Edge Security — Cloudflare (DNS, TLS, origin lock, WAF, rate limiting) | 🔵 Refined, ready for execution (Sprint 10) |
| E15 | Domain & API Refinement (flow corrections, auditing fields, book metadata lookup) | ⏳ Backlog (scope note captured, not refined) |
| E16 | Authentication & RBAC (JWT ES256, refresh/logout, brute-force protection) | ⏳ Backlog (scope note captured, not refined) |
| E17 | Multi-tenancy (tenant isolation, tenant management) | ⏳ Backlog (scope note captured, not refined) |

### Renumbering note (historical — first revision)

The epics from E8 onward were deliberately reordered and renumbered once, prior to E7's closure:

- The Spring Boot migration epic, originally numbered E9, was removed from the Library Express roadmap entirely — not deferred, descoped. Spring adoption moved to the next project (Internet Banking), built Spring-first from day one. Full rationale: ADR 0001.
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

### Renumbering note (third revision — post E10)

After Marco 2, the public API was identified as having no authentication and no edge protection, with mutating endpoints exposed. Security work was prioritized above E11–E13 and split into four epics by nature and dependency:

- **E14 (Edge Security)** is pure infrastructure and runs first: it is small, it protects the already-public API immediately, and the client-IP trust required by E16's brute-force protection depends on its origin lock.
- **E15 (Domain & API Refinement)** runs before E16/E17 on purpose: E17 migrates every business table, and E16 applies permissions per route. Auditing columns, `closed_at`, the unified listing routes, and the stable response contract must exist *before* those two epics so the same tables and routes are not touched twice.
- **E16 (Authentication & RBAC)** precedes **E17 (Multi-tenancy)** because the `tenant_id` travels as a JWT claim.
- **Safeguard between E16 and E17:** only the superAdmin can create admins, and **no admin for an external person is created until E17 closes** — otherwise there would be authentication without data isolation.

Rationale to be recorded in the ADRs listed in section 5.

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
5. REST API + documentation (Swagger/OpenAPI) (E9) ✅ done
6. ~~Marco 2 — Go Live (E10)~~ ✅ done
7. Edge security — Cloudflare (E14)
8. Domain & API refinement (E15)
9. Authentication & RBAC (E16)
10. Multi-tenancy (E17)
11. Log evolution — full observability with Prometheus/Grafana (E11)
12. Overdue enforcement evolution (E12)
13. Notifications (E13)

---

**E5 + E6 merge (decision on record):** Epic E5 (standalone Docker) was discontinued as its own block. Rationale: containerization only generates real business value once it's wired to real persistence — "containerize a CLI with an in-memory repository" is a weak portfolio narrative compared to "containerize an application with real PostgreSQL, HikariCP, and versioned migrations." E5 remains visible in the Epics table (not removed from the map), marked as discontinued, to preserve historical traceability. All containerization scope was absorbed by E6, which took on the name Real Persistence (JDBC/PostgreSQL) + Docker Containerization.

Database chosen: PostgreSQL. Local development and CI (E6/E7) use it via pure JDBC, no ORM. Production (E10) uses a managed serverless Postgres provider (Neon) instead of AWS RDS — see E10's decisions on record below and ADR 0009 (drafted during E10 execution).

**🚀 Marco 2 — Go Live**
First real deployment to production — on AWS (free tier / minimal-cost footprint). Delivered together: the CD pipeline (E10), real persistence (E6, already in place; production instance provided by Neon), and the REST API without a framework (`com.sun.net.httpserver.HttpServer`, no Spring — this project stays framework-free for its entire lifecycle, see ADR 0001), documented via Swagger/OpenAPI (E9, already in place). The deployment "counts" once there is a real HTTP service receiving traffic, backed by real persisted data, with structured logging (E8) already active.

**Deliberately excluded from Marco 2's scope:** automatic overdue enforcement, loan restriction, and settlement/late fees (E12), and full observability via Prometheus/Grafana (E11). These ship *after* Go-Live, as live iterations exercising the CD pipeline — the same pattern the project always intended for Notifications (E13), now extended to business-logic evolution and observability as well.

Why does Go Live come after tests/Docker/persistence/CI/logging/API? The sequence tells a strong portfolio narrative: tested → containerized → persisted → automated → observable → exposed via a documented API → only then went to production — the way real teams operate. And why iterate *after* going live instead of finishing everything first? Because shipping a lean MVP and then evolving it live, through the same CI/CD gate every other change goes through, is a stronger and more realistic signal than a single big-bang release — it demonstrates comfort operating a system that's already serving traffic.

Why raw tests and raw persistence before a framework? `@SpringBootTest`/Mockito and Spring Data JPA are abstractions over plain JUnit and plain JDBC. Doing the manual path first is deliberate: it forces understanding the mechanism underneath before a framework's convenience hides it. That abstraction is deliberately exercised in the next project (Internet Banking, Spring Boot from day one) rather than inside Library Express — see ADR 0001 for why the Spring migration was removed from this project's own roadmap instead of just being deferred.

### ⚙️ Phase 3 — Professional Software Engineering

Goal: deepen engineering practices on a system that has been in production since Marco 2 — security, observability, performance, scalability, documentation.
Scope: not yet formalized into epics (future backlog). Most of what would traditionally live here (observability, iterative business-logic evolution against a live system) has been pulled forward into Phase 2 as E11–E13, since the project's terminal roadmap ends at E13 with only maintenance-level adjustments afterward — see ADR 0001.

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

| ID | Description | Points | Status |
| --- | --- | --- | --- |
| TD01 | `equals`/`hashCode` contract for Book, Customer, and Loan | 3 | ✅ Resolved (US-304, E3) |
| TD05 | Fat JAR packaging (`maven-shade-plugin`, manifest with `Main-Class`) — frozen since E3, originally scheduled to resolve only in the (old) Go Live epic. Decision revised: the need for Docker moves the production justification for a single executable artifact earlier — freezing until Go Live no longer made sense. | 3 | ✅ Resolved (US-503, E6) |
| TD06 | Infrastructure layer (in-memory repositories) with no automated test coverage since E4 closed. Intentional deferral: contract and concurrency tests (originally US-404) rewritten with Testcontainers against a real database (Postgres), after E6 (JDBC) delivered the definitive implementation. | 8 | ✅ Resolved (US-701, E7) |
| TD07 | Structured logging (SLF4J + Logback). Originally scoped to only the E8 (old) scheduler Job; re-scoped during the second roadmap revision to cover the entire system from the start, ahead of the API and any scheduler — establishing observability as a convention every subsequent epic inherits, rather than retrofitting it under time pressure later. Full observability/tracing (metrics, dashboards) stays explicitly out of scope for this epic — see ADR 0004 and E11. | 10 | ✅ Resolved (US-803, E8) |
| TD08 | Coverage thresholds (Instruction/Branch, per module) were reduced during US-703 to reflect the current testable surface, notably `infrastructure` at 70%/50%. Revisited post-E9 (US-908) against measured coverage — see ADR 0006 Amendment 2. | 5 | ✅ Resolved (US-908, E9) |

---

## 💡 Future Exploration Notes (Not Yet Backlog Items)

Ideas surfaced during refinement that were deliberately **not** turned into epics or TDs — captured here so they aren't lost, without committing points or a sprint slot.

- **Business-level audit trail (DB-backed).** Distinct from technical logging (SLF4J + Logback, ADR 0004, delivered via E8). A "who did what" table for business events was proposed during E8/E12 refinement and intentionally deferred — revisit only after all currently planned epics (through E13) are closed. Not a replacement for structured logging; a separate concern if ever pursued.

---

## 5. ADRs planned (numbering provisional)

| ADR | Subject | Epic |
| --- | --- | --- |
| 0010 | Roadmap resequencing (E14–E17 ahead of E11–E13) + Cloudflare edge decisions | E14 |
| 0011 | Authentication: ES256/JWKS, Argon2id, activation token, refresh and blacklist, brute-force backoff | E16 |
| 0012 | Multi-tenancy model: discriminator column, explicit `TenantId`, composite FKs | E17 |
| 0013 | Book metadata lookup: provider port, Open Library default, Google Books optional, LLM discarded | E15 |

---

## Versioning Convention

Follows SemVer (`MAJOR.MINOR.PATCH`):

- `0.y.z` while the project is in early development — internal contracts (architecture, persistence, framework) may still change without notice.
- `1.0.0` is reserved for when the system stabilizes (around Phase 3).
- `alpha`/`beta`/`rc` suffixes only make sense from Marco 2 onward (once the REST API exists).
- `SNAPSHOT` in `pom.xml` during ongoing development; tags/releases use the clean version.

**Tags:**

| Tag | Milestone | Date |
| --- | --- | --- |
| v0.1.1 | Marco 1 — MVP (Epic E2 done) | see Git history |

## Board Conventions

- **Points:** simplified Fibonacci scale (1, 2, 3, 5, 8)
- **Status:** 🔲 To Do · 🟡 In Progress · 🔵 In Review · ✅ Done
- **Story numbering:** `US-{sprint}{sequential}` (e.g., US-401 → Sprint 4, item 1)
- **Technical debt numbering:** `TD-{sequential}`, not tied to a fixed sprint until prioritized
- **BDD scenarios:** Gherkin format (Given/When/Then), used as the formal acceptance criteria for each story

---

**Last update:** Epic E9 (REST API + Documentation) closed — Sprint 8, 35 points, TD08 resolved. Epic E10 (CD / Go Live, Marco 2) fully refined and ready for execution — Sprint 9, 34 points across 8 User Stories (US-1001–US-1008). Key E10 decisions: Amazon ECS with EC2 launch type (not Fargate), no Elastic Beanstalk, no Application Load Balancer (direct Elastic IP + Route 53), no managed RDS (Neon serverless Postgres chosen over Supabase for its automatic idle-resume behavior), single-container ECS task definition, AWS account on the Paid Plan (post-July-2025 credit model, US$200 budget with alerts), and a real CD pipeline (GitHub Actions → ECR → ECS service update). These conventions will be recorded in ADR 0009 (drafted during E10 execution, US-1008).

---

## Epics

Épico E2 — MVP: Ciclo de vida do empréstimo

Épico E3 — Inversão de Dependência Manual + Padronização de Repositórios + TD-01

Épico E4 — Fundação de Testes Automatizados (JUnit 5)

Épico E6 — Persistência Real (JDBC/PostgreSQL) + Containerização DockerÉpico 6

Épico E9 — REST API + Documentation (Swagger/OpenAPI)

Epic E10 — CD / Go Live (Marco 2, AWS)

Epic E14 — Edge Security (Cloudflare)

Epic E15 — Domain & API Refinement

Epic E16 — Authentication & RBAC

Epic E17 — Multi-tenancy
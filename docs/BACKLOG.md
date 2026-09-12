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
| E9 | REST API + Documentation (Swagger/OpenAPI) | ✅ Done (Sprint 8) |
| E10 | CD — Go Live (Marco 2, on AWS) | 🔵 Refined, ready for execution (Sprint 9) |
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
5. REST API + documentation (Swagger/OpenAPI) (E9) ✅ done
6. Marco 2 — Go Live (E10, packaging CD on top of persistence, Docker, and the documented API already in place) 🔵 refined, ready for execution
7. Log evolution — full observability with Prometheus/Grafana (E11), post Go-Live
8. Overdue enforcement evolution — scheduler Job, loan restriction, and settlement/late fees (E12), post Go-Live
9. Notifications (E13), exercising the full CI/CD cycle against an already-deployed system

**E5 + E6 merge (decision on record):** Epic E5 (standalone Docker) was discontinued as its own block. Rationale: containerization only generates real business value once it's wired to real persistence — "containerize a CLI with an in-memory repository" is a weak portfolio narrative compared to "containerize an application with real PostgreSQL, HikariCP, and versioned migrations." E5 remains visible in the Epics table (not removed from the map), marked as discontinued, to preserve historical traceability. All containerization scope was absorbed by E6, which took on the name Real Persistence (JDBC/PostgreSQL) + Docker Containerization.

Database chosen: PostgreSQL. Local development and CI (E6/E7) use it via pure JDBC, no ORM. Production (E10) uses a managed serverless Postgres provider (Neon) instead of AWS RDS — see E10's decisions on record below and ADR 0009 (drafted during E10 execution).

**🚀 Marco 2 — Go Live**
First real deployment to production — on AWS (free tier / minimal-cost footprint). Delivered together: the CD pipeline (E10), real persistence (E6, already in place; production instance provided by Neon), and the REST API without a framework (`com.sun.net.httpserver.HttpServer`, no Spring — this project stays framework-free for its entire lifecycle, see ADR [0001](./adr/0001-keep-library-express-framework-free.md)), documented via Swagger/OpenAPI (E9, already in place). The deployment "counts" once there is a real HTTP service receiving traffic, backed by real persisted data, with structured logging (E8) already active.

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

## 🔵 Epic E10 — CD / Go Live (Marco 2, AWS)

**Sprint:** 9
**Total points:** 34 (3 + 5 + 3 + 2 + 8 + 2 + 8 + 3)
**Status:** 🔵 Refined, ready for execution

### Decisions on record for this epic

- **Orquestração:** Amazon ECS, launch type **EC2** (not Fargate) — cost control is the priority; Fargate has no free tier under any account model.
- **No Elastic Beanstalk.** Discarded in favor of ECS.
- **No Application Load Balancer.** Direct exposure via an **Elastic IP** associated with the EC2 instance + a Route 53 **A** record. ALB is out of scope — no cost/benefit justification without real traffic yet.
- **No managed RDS.** Persistence via **Neon** (serverless managed Postgres, free tier) — chosen over Supabase because Neon's compute **auto-suspends and auto-resumes** without manual intervention; Supabase's free tier **pauses the entire project after 7 days of inactivity**, requiring manual dashboard action — unacceptable for an unattended portfolio deployment.
- **Single-container ECS task definition** (application only) — the database lives outside the cluster, on Neon.
- **AWS account on the Paid Plan**, created July 2026 (post-July-15-2025 model: US$200 credit, no fixed free-hour allowance). Budget configured with 50/80/100% alerts.
- **Real CD:** GitHub Actions builds the image, publishes to **ECR**, and forces a new deployment on the **ECS service** — not an SSH/`docker compose pull` shortcut.
- Neon connection string via **AWS Secrets Manager**, injected into the task definition — never plaintext in the versioned `task-definition.json`.
- Application logs (SLF4J/Logback JSON, E8) shipped to **CloudWatch Logs** via the ECS `awslogs` driver — no additional agent.

### Epic goal

Publish the REST API (E9) to production on AWS, with real persistence via Neon, ECS (EC2 launch type) orchestration, and a CD pipeline that builds, publishes, and deploys on every change to `main` — reaching Marco 2 (Go Live) at the lowest possible cost within the US$200 credit.

### Business value

Demonstrates the full delivery cycle international backend roles expect: containerization, cloud container orchestration, end-to-end CI/CD, secrets management, and operating a publicly exposed real system — without relying on PaaS abstractions (Heroku, Beanstalk) that hide these decisions. Reinforces the two-project portfolio narrative: this project shows what frameworks and PaaS abstract away; the next one (Internet Banking) shows delivery speed with them.

### Definition of Done — Epic E10

- [ ] AWS account on Paid Plan, budget alerts active, IAM baseline configured (US-1001)
- [ ] VPC/subnet/security group/Elastic IP provisioned (US-1002)
- [ ] Neon project provisioned and reachable via SSL connection string (US-1003)
- [ ] ECR repository created and Docker image published (US-1004)
- [ ] ECS cluster (EC2), task definition, and service running the application (US-1005)
- [ ] Domain resolving via Route 53 to the Elastic IP (US-1006)
- [ ] CD pipeline (GitHub Actions → ECR → ECS) functional on push/merge to `main` (US-1007)
- [ ] Post-deploy smoke test validated, logs reaching CloudWatch, infrastructure ADR recorded, README updated (US-1008)
- [ ] All 8 User Stories in Done status
- [ ] Marco 2 — Go Live reached and tagged

---

### US-1001 — AWS Foundation: Billing Safety Net & IAM Baseline

**Points:** 3
**Depends on:** — (unblocked, first US of the epic)

**Story:** As Product Owner, I need the AWS account on the Paid Plan with budget alerts and a least-privilege IAM baseline, so the rest of the epic is executed on a safe foundation without risk of automatic account closure.

**Scenarios (BDD):**

```gherkin
Feature: AWS account and IAM baseline

  Scenario: Account is on the Paid Plan
    Given the AWS account was created after July 15, 2025
    When billing settings are reviewed
    Then the account is confirmed on the Paid Plan, not the auto-closing Free Plan

  Scenario: Budget alert is configured
    Given a monthly budget threshold is defined
    When spending crosses 50%, 80%, or 100% of the threshold
    Then an email alert is sent

  Scenario: Deploy credentials follow least privilege
    Given an IAM user/role dedicated to CI/CD
    When its policy is inspected
    Then it grants only ECR push and ECS service update permissions, nothing broader

  Scenario: EC2 instance has a scoped instance role
    Given the EC2 container instance for the ECS cluster
    When its attached IAM role is inspected
    Then it only grants the permissions required by the ECS agent (ecsInstanceRole managed policy)
```

**Tasks:**

- Confirm account upgrade to the Paid Plan (Billing and Cost Management)
- Create a monthly AWS Budget with 50/80/100% alerts
- Create a dedicated IAM policy for the CD pipeline (ECR: push/pull; ECS: RegisterTaskDefinition, UpdateService, DescribeServices)
- Create an IAM user (access key) or OIDC role for GitHub Actions to consume that policy
- Attach the `AmazonEC2ContainerServiceforEC2Role` managed policy to the EC2 instance role (ecsInstanceRole)

**Commits:**

```
chore(infra): US-1001 configure aws budget with spend alerts
chore(infra): US-1001 create least-privilege iam policy for cd pipeline
chore(infra): US-1001 create ecs instance role for ec2 container instance
docs(adr): US-1001 record aws billing model and iam baseline decisions
```

---

### US-1002 — Networking Foundation: VPC, Security Group, Elastic IP

**Points:** 5
**Depends on:** US-1001

**Story:** As a developer, I need a minimal, secure network (VPC, public subnet, security group, fixed IP), so the ECS cluster's EC2 instance is publicly reachable without exposing unnecessary ports.

**Scenarios (BDD):**

```gherkin
Feature: Networking foundation

  Scenario: EC2 instance is reachable on the API port
    Given the security group allows inbound traffic on the API_PORT
    When a request is sent to the instance's public address
    Then the connection is accepted

  Scenario: SSH access is restricted
    Given the security group's SSH rule
    When its source is inspected
    Then it is restricted to a specific known IP, not 0.0.0.0/0

  Scenario: Elastic IP survives instance stop/start
    Given an Elastic IP is associated with the EC2 instance
    When the instance is stopped and started again
    Then the public IP address remains the same

  Scenario: No NAT Gateway is provisioned
    Given the VPC networking setup
    When resources are reviewed
    Then no NAT Gateway exists, avoiding its fixed hourly cost
```

**Tasks:**

- Use the default VPC (or create a minimal one) with a public subnet and Internet Gateway
- Create a Security Group: inbound API_PORT (0.0.0.0/0), inbound SSH restricted to the Dev's IP, outbound open
- Provision the EC2 instance (t2/t3.micro) as the ECS cluster's container instance (ECS-optimized AMI)
- Allocate and associate an Elastic IP with the instance
- Confirm the absence of a NAT Gateway in the setup

**Commits:**

```
chore(infra): US-1002 provision ec2 instance as ecs container instance
chore(infra): US-1002 configure security group for api and ssh access
chore(infra): US-1002 allocate and associate elastic ip
docs(adr): US-1002 record networking decisions and nat gateway exclusion
```

---

### US-1003 — Neon Database Provisioning & Connectivity

**Points:** 3
**Depends on:** US-1001

**Story:** As a developer, I need a free, managed Postgres database resilient to inactivity, so the application persists real data in production without the cost and operational complexity of RDS.

**Scenarios (BDD):**

```gherkin
Feature: Neon database connectivity

  Scenario: Application connects to Neon over SSL
    Given a Neon connection string with sslmode=require
    When the application starts
    Then HikariCP establishes a pooled connection successfully

  Scenario: Flyway migrations run against Neon on boot
    Given a fresh Neon database with no schema
    When the application starts
    Then Flyway applies all pending migrations before the HTTP server accepts requests

  Scenario: Connection pool respects Neon's free-tier connection limits
    Given HikariCP's configured maximum pool size
    When the application is under normal load
    Then the pool size stays within Neon's free-tier concurrent connection limit

  Scenario: Database resumes automatically after idle suspension
    Given the Neon compute has been idle and auto-suspended
    When a new request reaches the application
    Then the connection succeeds after Neon's automatic resume, without manual intervention
```

**Tasks:**

- Create a Neon project (free tier), obtain the pooled connection string (`-pooler` endpoint)
- Configure `sslmode=require` on the connection string
- Tune HikariCP's `maximumPoolSize` to a conservative value (e.g., 5) compatible with the free-tier limit
- Validate Flyway execution against Neon starting from an empty schema
- Store the connection string in AWS Secrets Manager (not a plaintext environment variable)

**Commits:**

```
chore(infra): US-1003 provision neon postgres project
feat(config): US-1003 configure ssl and pooled connection to neon
chore(config): US-1003 tune hikaricp pool size for neon free tier limits
chore(infra): US-1003 store neon connection string in secrets manager
```

---

### US-1004 — Container Registry & Image Publishing (ECR)

**Points:** 2
**Depends on:** US-1001

**Story:** As a developer, I need a versioned image repository on AWS, so the CD pipeline has a trustworthy source for the image the ECS pulls.

**Scenarios (BDD):**

```gherkin
Feature: Container image publishing

  Scenario: Image is pushed with a traceable tag
    Given a successful Docker build of the application
    When the image is pushed to ECR
    Then it is tagged with the Git commit SHA, not just "latest"

  Scenario: Manual first push validates the pipeline's target
    Given a freshly created ECR repository
    When the existing multi-stage Dockerfile (E6) is built and pushed manually
    Then the image appears in ECR and can be pulled successfully
```

**Tasks:**

- Create an ECR repository (`library-express-api`)
- Validate reuse of the multi-stage Dockerfile (E6) without structural changes
- Perform the first manual push (`docker build` + `aws ecr get-login-password` + `docker push`) to validate the repository before automating
- Define the image tagging convention: `${GIT_SHA}` as the primary tag

**Commits:**

```
chore(infra): US-1004 create ecr repository for application image
chore(infra): US-1004 validate manual image push to ecr
docs(infra): US-1004 document image tagging convention
```

---

### US-1005 — ECS Cluster, Task Definition & Service (EC2 Launch Type)

**Points:** 8
**Depends on:** US-1002, US-1003, US-1004

**Story:** As a developer, I need the application running as a long-lived ECS service on the provisioned EC2 instance, with environment variables and secrets correctly injected, so the system operates resiliently and is restartable.

**Scenarios (BDD):**

```gherkin
Feature: ECS cluster and service

  Scenario: ECS cluster registers the EC2 instance
    Given the ECS agent is running on the EC2 instance
    When the cluster is inspected
    Then the instance appears as an active container instance

  Scenario: Task definition injects the database secret
    Given a task definition referencing the Secrets Manager ARN for the Neon connection string
    When a task is launched
    Then the container receives the resolved secret as an environment variable, not a plaintext value in the definition

  Scenario: Service maintains desired count
    Given an ECS service with desired count 1
    When the running task is stopped unexpectedly
    Then ECS automatically launches a replacement task

  Scenario: Application logs reach CloudWatch
    Given the task definition uses the awslogs log driver
    When the application emits a structured log line
    Then the line appears in the corresponding CloudWatch Logs group
```

**Tasks:**

- Create an ECS cluster (EC2 launch type) associated with the provisioned instance (US-1002)
- Create a single-container task definition: ECR image (US-1004), environment variables, Neon secret via `secrets` (US-1003), `awslogs` log driver
- Map the container port to the host port (bridge mode) matching `API_PORT`
- Create the ECS service (desired count 1) referencing the task definition
- Validate automatic task recovery on failure

**Commits:**

```
chore(infra): US-1005 create ecs cluster with ec2 launch type
chore(infra): US-1005 define ecs task definition with secrets and awslogs
chore(infra): US-1005 create ecs service with desired count one
test(infra): US-1005 validate automatic task recovery on failure
```

---

### US-1006 — DNS & Public Access (Route 53)

**Points:** 2
**Depends on:** US-1002

**Story:** As an API consumer, I need to reach the application through a stable domain, so I don't depend on the instance's raw IP.

**Scenarios (BDD):**

```gherkin
Feature: DNS resolution

  Scenario: Domain resolves to the Elastic IP
    Given a Route 53 A record pointing to the Elastic IP
    When a DNS lookup is performed for the domain
    Then it resolves to the correct Elastic IP address

  Scenario: API is reachable via the domain
    Given the DNS record has propagated
    When a request is sent to the domain on the API_PORT
    Then the response matches what direct IP access returns
```

**Tasks:**

- Create (or reuse) a hosted zone in Route 53
- Create an A record pointing to the instance's Elastic IP
- Validate propagation and access via the domain

**Commits:**

```
chore(infra): US-1006 create route53 hosted zone
chore(infra): US-1006 create a record pointing to elastic ip
docs(infra): US-1006 document public domain access
```

---

### US-1007 — CD Pipeline (GitHub Actions → ECR → ECS Deploy)

**Points:** 8
**Depends on:** US-1004, US-1005

**Story:** As a developer, I need every change merged into `main` to be automatically built, published, and deployed, so the delivery cycle is real and doesn't depend on manual steps.

**Scenarios (BDD):**

```gherkin
Feature: Continuous Deployment pipeline

  Scenario: Merge to main triggers the CD workflow
    Given a pull request is merged into main
    When the CD workflow runs
    Then it builds the Docker image, tags it with the commit SHA, and pushes it to ECR

  Scenario: ECS service is updated with the new image
    Given a new image was pushed to ECR
    When the workflow registers a new task definition revision referencing that image
    Then the ECS service is updated to use the new revision and a new deployment is forced

  Scenario: Deployment waits for stability before finishing
    Given a new ECS deployment has been triggered
    When the workflow polls the service status
    Then it only reports success after the service reaches a stable state with the new task running

  Scenario: Failed deployment does not silently succeed
    Given a new task fails to start (e.g., bad image, missing secret)
    When the service fails to stabilize
    Then the workflow fails visibly instead of reporting success
```

**Tasks:**

- Create `cd.yml` in GitHub Actions, triggered on push/merge to `main` (reusing E7's `build-and-test` as a prior gate)
- Configure AWS authentication in the workflow (IAM user/OIDC created in US-1001)
- Step: build the Docker image + push to ECR tagged with `${GIT_SHA}`
- Step: register a new task definition revision pointing to the new image
- Step: `aws ecs update-service --force-new-deployment` + wait for stabilization (`wait services-stable`)
- Ensure stabilization failure breaks the workflow (non-zero exit code)

**Commits:**

```
ci(cd): US-1007 add github actions workflow for continuous deployment
ci(cd): US-1007 build and push docker image to ecr with commit sha tag
ci(cd): US-1007 register new ecs task definition revision
ci(cd): US-1007 force new ecs deployment and wait for service stability
```

---

### US-1008 — Go-Live Validation & Documentation

**Points:** 3
**Depends on:** US-1005, US-1006, US-1007

**Story:** As Product Owner, I need to validate that the production system works end-to-end and that the infrastructure decision is documented, so Marco 2 can be formally declared reached.

**Scenarios (BDD):**

```gherkin
Feature: Go-live validation

  Scenario: Smoke test validates the live API
    Given the application is running in production
    When a smoke test exercises a basic CRUD flow (e.g., create and retrieve a book) against the public domain
    Then all responses match expected status codes and payloads

  Scenario: Correlation ID and logs are traceable in production
    Given a smoke test request carries a known X-Correlation-Id
    When the corresponding CloudWatch log group is inspected
    Then log lines for that request carry the same correlation id

  Scenario: Infrastructure decisions are recorded
    Given the ECS/EC2/Neon/no-ALB/no-RDS architecture decisions made during E10 refinement
    When docs/adr is reviewed
    Then a new ADR documents these decisions and their rationale
```

**Tasks:**

- Write and run a manual/scripted smoke test against the public domain (basic book/customer/loan flow)
- Verify log arrival and correlation in CloudWatch (end-to-end X-Correlation-Id)
- Draft ADR 0009 (AWS infrastructure: ECS/EC2, no ALB, no RDS, Neon, CD via ECR)
- Update the README (production/deploy section, replacing the "Epic E10, not yet implemented" note in the CI section)
- Tag the Marco 2 release following the SemVer convention (beta suffix, the first time it applies post public API)

**Commits:**

```
test(e2e): US-1008 add production smoke test for core crud flow
docs(adr): US-1008 record aws infrastructure decisions for marco 2
docs(readme): US-1008 document production deployment and cd pipeline
chore(release): US-1008 tag marco 2 go-live release
```

---

## Placeholder Epics (Titles Only — Not Yet Refined)

Per the just-in-time grooming rule, these exist only as titles (plus any scope notes already captured in conversation) until their turn comes.

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

### E9 — REST API + Documentation (Swagger/OpenAPI)
✅ Done · Sprint 8 · 35 points (5 + 5 + 5 + 5 + 3 + 2 + 5 + 5)

| US | Description | Points | Status |
|---|---|---|---|
| US-901 | HTTP Foundation: Router, Jackson Wiring, Base Request/Response Contract | 5 | ✅ Done |
| US-902 | Book REST Endpoints (Paginated) | 5 | ✅ Done |
| US-903 | Customer REST Endpoints (Paginated) | 5 | ✅ Done |
| US-904 | Loan REST Endpoints (Paginated) | 5 | ✅ Done |
| US-905 | Central Exception Handler (Domain/Application Exceptions → HTTP Status) | 3 | ✅ Done |
| US-906 | Correlation ID via HTTP Header (Accept / Generate / Echo) | 2 | ✅ Done |
| US-907 | OpenAPI Documentation (swagger-core + swagger-maven-plugin, Swagger UI) | 5 | ✅ Done |
| US-908 | HTTP Layer Test Suite (Unit + E2E) and TD08 Closure | 5 | ✅ Done |

The HTTP layer was built framework-free on `com.sun.net.httpserver.HttpServer` with a hand-rolled router (path/method → `HttpHandler`), per ADR 0001. Richardson Maturity Model Level 2 achieved (no HATEOAS), no URI version prefix, `page`/`size` pagination with safe defaults — full rationale in ADR 0008, including Amendment 1 (JAX-RS annotations used purely as build-time metadata for `swagger-jaxrs2`, with the hand-rolled `Router` remaining the sole runtime dispatcher). RMM corrections resolved inline during refinement (`GET /customers/search`, `POST /loans/{loanId}/returns`, `PATCH /loans/{loanId}/close-overdue`). OpenAPI contract generated via `swagger-core`/`swagger-maven-plugin`, with the version placeholder resolved by `maven-antrun-plugin` from the POM; Swagger UI served at `/docs`, raw contract at `/openapi.json`. TD08 formally resolved via US-908, with `infrastructure` JaCoCo thresholds re-measured and raised against real coverage (ADR 0006 Amendment 2). README updated with the REST API conventions section. Full detail (Gherkin, tasks, commits): see the corresponding Issues on GitHub Projects.

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

**Last update:** Epic E9 (REST API + Documentation) closed — Sprint 8, 35 points, TD08 resolved. Epic E10 (CD / Go Live, Marco 2) fully refined and ready for execution — Sprint 9, 34 points across 8 User Stories (US-1001–US-1008). Key E10 decisions: Amazon ECS with EC2 launch type (not Fargate), no Elastic Beanstalk, no Application Load Balancer (direct Elastic IP + Route 53), no managed RDS (Neon serverless Postgres chosen over Supabase for its automatic idle-resume behavior), single-container ECS task definition, AWS account on the Paid Plan (post-July-2025 credit model, US$200 budget with alerts), and a real CD pipeline (GitHub Actions → ECR → ECS service update). These conventions will be recorded in ADR 0009 (drafted during E10 execution, US-1008).
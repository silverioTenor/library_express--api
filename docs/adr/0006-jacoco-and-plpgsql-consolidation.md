# ADR 0006: JaCoCo Coverage Metric Alignment and PL/pgSQL Function Exception for Overdue Bookkeeping

## Status
Accepted (Amendment to US-703 and Exception to ADR 0002; Amendments 2 and 3 below)

## Context
Following the closure of Epic E7 and during the refinement of Epic E12, two technical alignment needs emerged:

1. **JaCoCo Metric Discrepancy & Baseline Alignment (US-703)**: US-703 originally referenced Line coverage metrics, but the build was implemented using Instruction coverage (bytecode-level). Comparing bytecode execution to line targets proved inaccurate, and `infrastructure` module branch coverage (50%) reflected a minimal surface area (JDBC repository only) prior to Epic E9's REST API implementation.
2. **Atomic Bookkeeping for Overdue Enforcement**: ADR 0002 established that business rules reside in the Java application layer. However, refining Epic E12 identified a mechanical requirement: recording an overdue loan requires atomic, multi-table side effects—appending to `tb_loan_overdue` and updating `tb_customer_loan_overdue_count` (setting `blocked_at` when the count hits 3). Performing this via multiple JDBC calls added unnecessary boilerplate for purely mechanical execution.

## Decision

- **JaCoCo Metric Confirmation & Threshold Adjustment (US-703 / TD08)**:
  - Formally confirm **Instruction/Branch** as the standard JaCoCo metric across all modules.
  - Temporarily amend quality gate thresholds to reflect honest, measured baselines:

| Module | Instruction | Branch |
|---|---|---|
| `domain` | 85% | 90% |
| `application` | 85% | 95% |
| `infrastructure` | 70% | 50% |

  - Track this adjustment as **TD08** in `BACKLOG.md` for re-evaluation after Epic E9 (REST API) and Epic E10 (Go-Live).
- **Narrow PL/pgSQL Function for Bookkeeping**:
  - Implement a dedicated PL/pgSQL database function strictly for atomic multi-table bookkeeping (`tb_loan_overdue` insertion and `tb_customer_loan_overdue_count` upsert/block).
  - Business decision-making (evaluating overdue status and eligibility) remains strictly in Java, upholding the core tenets of ADR 0002.
  - This constitutes a narrow, explicitly defined exception to ADR 0002's prohibition on database-level business logic.

## Consequences

**Positive**
- CI pipeline quality gates evaluate accurate, bytecode-level coverage without artificial failures.
- Database guarantees multi-table transactional atomicity for mechanical writes without hand-rolled JDBC transaction logic.
- Business rules and testability remain isolated within the Java domain/application layer.

**Negative / Trade-offs**
- Quality thresholds are temporarily lowered, with `infrastructure` branch coverage (50%) remaining a temporary weakness until Epic E9.
- Adds PL/pgSQL code requiring Testcontainers integration tests and database migration tracking.

### Amendment 2 — E9 Infrastructure Coverage Baseline (TD08 Resolution)

**Date:** 2026-09-11
**Context:** Epic E9 (REST API + Documentation) delivered the HTTP handler layer,
expanding infrastructure's testable surface as anticipated when TD08 was opened
during E7 (US-703).

**Measured coverage (post-E9, US-908):**

| Module | Instruction | Branch |
|---|---|---|
| domain | 93% | 91% |
| application | 100% | 92% |
| infrastructure | 87% | 72% |

**Decision:** JaCoCo `check` thresholds are set at measured coverage minus a
safety margin (~3–7 points), not at the measured value itself. Rationale:
a threshold pinned to the exact measured number is brittle — it fails the
build on any natural fluctuation (a single uncovered exception branch
introduced in an unrelated refactor) without adding real regression protection.
A margin preserves rigor while tolerating normal variance.

`infrastructure` specifically receives the more conservative end of the
margin, not a value close to the measured 87%/72%: the current number
reflects HTTP handlers written in the same sprint they're being measured in,
with no maintenance cycle yet to confirm the number is stable rather than a
one-off high-water mark.

**New thresholds (supersedes the US-703 table):**

| Module | Instruction | Branch |
|---|---|---|
| domain | 90% | 88% |
| application | 95% | 88% |
| infrastructure | 80% | 65% |

**Status:** TD08 formally resolved.

### Amendment 3 — E15 `updated_at` Maintained by a Database Trigger

**Date:** 2026-11-11
**Context:** Epic E15 (US-1502) added `created_at` and `updated_at` to every business table. The first refinement kept `updated_at` out of the database logic: each `UPDATE` in the JDBC repositories would set `updated_at = now()` explicitly, because a trigger would widen the PL/pgSQL exception above, which is limited to overdue bookkeeping. During execution that approach proved fragile: every repository method that updates a row has to remember the assignment, and a forgotten one silently leaves a stale timestamp.

**Decision:** `updated_at` is maintained by a `BEFORE UPDATE` trigger (`automatic_updated_at`) on the business tables, and the repositories no longer set it. The narrow exception to ADR 0002 is widened by exactly one purpose: **mechanical audit-timestamp maintenance**. The trigger only stamps the row's modification time; it contains no business decision, so the core tenet of ADR 0002 — business rules live in Java — is untouched.

**Consequences:**

- Positive: a single point of truth for `updated_at`; no repository can forget it; less boilerplate in the JDBC layer.
- Negative: part of the persistence behavior now lives in a migration instead of the repository code, so it is covered by a Testcontainers integration test asserting that `updated_at` advances on update.
- Anything beyond mechanical bookkeeping (overdue recording, audit timestamps) in the database still requires a new amendment or ADR.
# ADR 0006: JaCoCo Coverage Metric Alignment and PL/pgSQL Function Exception for Overdue Bookkeeping

## Status
Accepted (Amendment to US-703 and Exception to ADR 0002)

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

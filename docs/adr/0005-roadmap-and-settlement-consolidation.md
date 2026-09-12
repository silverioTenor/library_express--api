# ADR 0005: Post-E7 Roadmap Restructuring, Scope Adjustments, and Narrow Loan Settlement

## Status
Accepted

## Context
Following the completion of Epic E7 and active refinement for subsequent epics, three core architectural and domain challenges surfaced:

1. **E8 Scope & Over-Engineering**: Epic E8 bundled structured logging (TD07), the overdue-detection background job (ADR 0002), and a multi-tiered reputation score system ("flagged" at 3 late returns, "blocked" at 5). The reputation system lost its purpose following the retirement of `VISION.md` (ADR 0001).
2. **Observability & Deployment Sequence**: Implementing unattended scheduled jobs without a prior structured logging foundation added unnecessary operational complexity. Furthermore, bundling the REST API into the CD/Go-Live epic created an overly large, high-risk release.
3. **Incomplete Overdue Return Flow**: Refinement of overdue enforcement (Epic E12) revealed that returning overdue loans lacked a realistic domain conclusion without accounting for delay fees. While ADR 0001 excluded general payments, omitting fee settlement entirely left overdue returns with an artificial status flag.

## Decision

- **Reputation System Descope & Rule Simplification**: Completely remove the reputation score concept. Replace it with a deterministic rule: 3 overdue loans block a customer from new loans for 30 days, after which the count resets to 0.
- **Roadmap Resequencing & Epic Unbundling**:
    - **E8 (Structured Logging - TD07)**: Promoted to a standalone epic executed first to establish an observability baseline for all subsequent epics.
    - **E9 (REST API & Documentation)**: Separated into its own epic ahead of Continuous Delivery (CD).
    - **E10 (CD / Lean Go-Live)**: Descoped from overdue enforcement and settlement logic to ship a leaner MVP (persistence, Docker, API, logging).
    - **E11 (Full Observability)**: Prometheus and Grafana integration scheduled post Go-Live.
    - **E12 (Overdue Enforcement & Settlement)**: Combines the overdue scheduler job (ADR 0002), loan restriction rule, and the reopened narrow settlement flow post Go-Live.
    - **E13 (Notifications)**: Renumbered from E10 with unchanged scope.
- **Narrow Loan Settlement Scope**:
    - Reopen a strictly bounded settlement model limited to overdue loan returns in Epic E12.
    - The `Loan` entity acquires a base late fee, daily interest rate, and total calculation method.
    - Overdue returns require recording a manual payment (cash or PIX) in `loan_payment_record` before updating loan and book statuses.
    - Out of scope: Marketplaces, subscription billing, online payment gateways, or automated collections.

## Consequences

**Positive**
- Epics E9+ inherit standard structured logging natively.
- Go-Live risk is significantly reduced through a leaner MVP delivery.
- Obsolete speculative domain complexity (reputation scores) is removed.
- Epic E12 gains a complete, realistic domain lifecycle for overdue returns.

**Negative / Trade-offs**
- Epic count increases (E8 through E13), adding backlog management overhead.
- Monetary calculations re-enter the domain, expanding edge-case test surface (e.g., partial day calculations, due-date returns).
- Business logic enforcement and full observability ship post-production launch, requiring strict CI/CD gate discipline.
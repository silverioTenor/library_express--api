# ADR 0007: Post-E7 Roadmap Reorganization

## Status
Accepted

## Context
The roadmap established after ADR 0001 planned Epic E8 as "Customer Reputation + automatic loan-status Job" — a scheduler-driven status transition (ADR 0002) combined with a reputation score system (flagged at 3 late returns, blocked at 5), followed by E9 (CD/Go-Live) and E10 (Notifications).

Refining E8 after E7's closure surfaced two problems with that plan:

1. The "reputation score" concept (a graduated score with a "flagged" intermediate tier) was inherited from the original, since-retired `VISION.md` long-term vision. With that vision gone (ADR 0001), the score concept no longer served a clear purpose — a simpler binary rule covers the same real need.
2. Bundling structured logging (TD07), the scheduler Job, the restriction rule, and — once reopened — settlement (ADR 0005) into one epic made it large and mixed concerns that don't actually depend on each other in the order originally planned. Logging, in particular, is more valuable early (as a foundation every later component inherits) than late (retrofitted once several components already exist without it).

## Decision
- **Drop the reputation/score concept entirely.** Replace it with a single rule: after 3 overdue loans, the customer is restricted from new loans for 30 days; the count resets to 0 once the restriction period ends.
- **Split the former E8 into separate epics, reordered:**
    - Structured logging (SLF4J + Logback, TD07) is promoted to a standalone epic and moved to the front of the remaining sequence — now E8 — so every later epic inherits it by convention.
    - The scheduler Job (ADR 0002), the restriction rule, and settlement (ADR 0005) are merged into a single epic and deliberately scheduled **after** Go-Live — now E12.
- **Introduce a standalone API epic (E9)** ahead of CD, rather than bundling a minimal API directly into the CD epic as originally planned.
- **CD/Go-Live is renumbered to E10.** Marco 2 no longer requires the overdue/restriction/settlement business logic to exist first — the system ships leaner (persistence, Docker, documented API, structured logging already in place) and gains business-logic evolution afterward, as a live iteration through the same CI/CD gate every other change goes through.
- **Introduce a standalone observability epic (E11)**, post Go-Live, to revisit ADR 0004's "no full observability" stance with Prometheus/Grafana once there's a live system worth instrumenting that way.
- **Notifications is renumbered to E13**, purpose unchanged.

## Consequences
**Positive**
- Produces a stronger, more realistic portfolio narrative: ship a lean, observable MVP, then evolve it live — rather than trying to finish all business logic before the first deployment.
- Logging becomes a foundation instead of a retrofit, avoiding rework across the API and scheduler epics that follow it.
- Each epic has a narrower, clearer concern (logging vs. API vs. delivery vs. business-logic evolution vs. observability vs. notifications) instead of one large epic mixing several.

**Negative / Trade-offs**
- A second roadmap renumbering in the same project (see the first revision recorded in `BACKLOG.md`), which adds historical complexity for anyone reading commit/epic history end to end — mitigated by keeping both revisions documented rather than silently overwritten.
- Go-Live (Marco 2) now ships without automatic overdue enforcement, which a reviewer skimming only the Marco 2 milestone might read as an incomplete library system rather than a deliberate staged rollout. Mitigated by this ADR and the E12 epic description making the sequencing intentional, not an oversight.
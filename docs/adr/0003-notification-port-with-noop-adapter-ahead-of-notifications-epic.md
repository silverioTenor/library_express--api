# ADR 0003: Domain-Level Notification Port, with a No-Op Adapter Ahead of the Notifications Epic

## Status
Accepted

## Context
The overdue-status scheduler introduced in E8 needs to trigger a notification once a loan transitions to overdue. However, a dedicated Notifications epic (E10, renumbered) — covering loan-created, loan-completed, and loan-overdue notifications by email — is intentionally scoped for *after* Go Live (Marco 2/E9), specifically to exercise a full CI/CD cycle against an already-deployed system.

This creates a real dependency between an earlier epic (E8) and a later one (E10): E8 needs to *signal* that a notification should happen, but the mechanism to actually send one does not exist yet at that point in the roadmap.

## Decision
E8 introduces a domain-level notification port (an interface, e.g. `LoanStatusChangeNotifier`) that the scheduler invokes after a successful, lock-safe status transition. E8 ships a no-op/logging adapter implementing this port — it records that a notification would have been sent, without sending one. E10 later implements the real adapter (e.g., SMTP/SES-backed) and wires it into the composition root, without any change to the scheduler or the domain rule from E8.

## Consequences
**Positive**
- Low-cost, future-aware design: the interface costs almost nothing to introduce now and avoids reopening E8's code when E10 lands.
- Keeps the port/adapter boundary consistent with the project's existing layering conventions (domain defines the contract, infrastructure provides the implementation).
- E10 becomes a pure "plug a new adapter into an existing port" exercise — a realistic simulation of adding a feature to a live, already-shipped system via CI/CD.

**Negative / Trade-offs**
- Introduces an interface with only a no-op implementation for one full epic cycle (E8 through E9). This is intentional, scoped debt — not accidental complexity — and is closed as soon as E10 starts.
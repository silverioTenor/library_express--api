# ADR 0002: Domain-Owned Overdue Status Rule, with Explicit Locking in Java

## Status
Accepted

## Context
Epic E8 (Reputation, renumbered) introduces a scheduler that automatically transitions loans to an overdue status, replacing a manual/temporary flow. Because the scheduler may run more than once concurrently (e.g., overlapping executions, multiple instances), the status transition needs a concurrency-safety guarantee.

Two approaches were considered:

1. **Delegate the transition to a PostgreSQL function/stored procedure.** The scheduler would simply invoke the function and act on its return value. This trivially guarantees atomicity, since the database enforces it, but it moves a business rule ("when is a loan overdue, and what happens when it becomes overdue") out of the domain/application layer and into infrastructure — breaking the Clean Architecture separation the project has followed consistently since E1–E4.
2. **Keep the rule in the domain/application layer, in Java**, and handle concurrency explicitly at that layer (e.g., pessimistic locking via `SELECT ... FOR UPDATE` issued from the JDBC repository and orchestrated by application code).

Because a primary goal of Library Express is deepening Java and concurrency knowledge — and because the target international hiring bar expects genuine depth on concurrency, not just framework usage — option 2 was chosen despite requiring more implementation and test effort than a single database function call.

## Decision
The overdue-transition business rule stays in the domain/application layer, implemented in Java. Safety against concurrent scheduler executions is handled with explicit locking coordinated from the application code (e.g., pessimistic row locking through the JDBC repository), not by delegating the rule to a PostgreSQL function.

## Consequences
**Positive**
- Preserves the project's consistent layering discipline: business rules remain independent of the persistence mechanism.
- Produces direct, hands-on experience with explicit lock coordination in Java — a concurrency topic with real interview weight for the target job market.
- Keeps the rule unit-testable in isolation, consistent with the project's existing JUnit 5 + Mockito testing philosophy.

**Negative / Trade-offs**
- More application code to write and test than a single stored-procedure call.
- Correctness of the locking strategy is entirely the application's responsibility and must be explicitly validated with concurrent integration tests (Testcontainers) during E8 — this is not optional given the failure mode (double-processing, lost updates) is silent if the locking is wrong.
# ADR 0004: Adopt SLF4J + Logback; No Full Observability/Tracing in This Phase

## Status
Accepted

## Context
The scheduler introduced in E8 is the first component in Library Express that runs unattended — nobody is watching a terminal when it fires. Ad hoc `System.out` output, which has been acceptable for a CLI batch application driven by a human operator, is no longer sufficient once a background process can fail silently.

Full observability (distributed tracing, correlation IDs across services, metrics pipelines) is out of scope for the remaining Library Express roadmap: the project will not reach a microservices/EDA architecture (that evolution was explicitly gated behind a later maturity phase in the now-retired `VISION.md`, see ADR 0001), so the cost of full tracing infrastructure is not justified here.

## Decision
Adopt SLF4J as the logging façade with Logback as the backing implementation. Introduce this as a technical-debt item (TD) inside E8, scoped to structured logging around the scheduler and, where practical, existing use cases. Full observability/tracing remains explicitly out of scope for Library Express.

## Consequences
**Positive**
- Gives meaningful operational visibility to the first unattended component in the system, at low setup cost.
- SLF4J + Logback is the de facto standard logging stack in the target job market — directly relevant experience.

**Negative / Trade-offs**
- No tracing or correlation across future distributed calls; acceptable given the project will not evolve into multiple services.
- Log-based diagnosis only, no structured metrics/alerting — acceptable for a portfolio-scale system, would need revisiting for real production scale.
# 0011 — Book Metadata Lookup, Creation Contract, Factory Pattern and Schema Consolidation (Epic E15)

## Status
Accepted

## Context
After Marco 2 (ADR 0009) and before authentication and multi-tenancy (E16, E17), Epic E15 corrected flow problems found in production and refined the API contract, so that E16 and E17 would not touch the same tables and routes twice. During execution several decisions diverged from the original refinement and were settled with the Dev. This ADR records the final state and the alternatives that were discarded.

The decisions below are grouped by theme. Decisions already described in the E15 epic and carried out as specified (retirement of the CLI, deterministic and unified listings, canonical ISBN-13, `closed_at`, loan creation policy versus invariant) are only summarized at the end.

## Decisions

### Book metadata lookup: Google Books only, behind a gateway port
When a registration request carries no ISBN, the application resolves the book from title and author.

- The contract is the `BookMetadataGateway` port, in the `domain` module next to the other book contracts (`domain/book/gateway`). The use case depends only on the port; the `infrastructure` module provides the Google Books adapter, built on `java.net.http` and Jackson.
- **Google Books is the only provider.** The API key is read from `GOOGLE_BOOKS_API`; in production it is injected from AWS Secrets Manager through the ECS task definition.
- A factory selects the adapter from `APP_ENV`: `test` selects a fake gateway, so tests and local runs never call the external service.
- `POST /books` therefore has two modes. **Manual:** an ISBN is present, no external call is made, and category and description are stored empty. **Automatic:** no ISBN; the title and author are looked up and the book is registered with the ISBN-13, authors, categories, publication year and description returned by the provider. The description is limited to 500 characters.
- No match answers `404` (the user is asked to review the data or register manually). A provider that is unavailable, returns an invalid response or times out is mapped to a gateway-class `5xx` by the central exception handler, and nothing is persisted.
- The column and field are named `description`, not `summary`, because that is the common term.
- Enriching a **manual** registration with provider data (precedence client > provider > default) was discussed and **not implemented** in E15; it is recorded as a future-exploration note together with the `publicationDate` change.

**Alternatives considered**

| Alternative | Outcome |
|---|---|
| Open Library as default and as fallback | Discarded. During implementation its records showed data-quality and integrity problems that compromised what would be stored; the multi-provider ordering and fallback design went with it |
| LLM-based lookup | Discarded in refinement: models return plausible but invented ISBNs |
| Port in the `application` module | Not adopted; the gateway contract sits with the other domain contracts, as the repository interfaces do |

### No cache for metadata lookups
A bounded in-memory cache with a TTL was part of the original design and was **removed**. Caching is a cross-cutting concern of the whole application, not of one feature, and an isolated cache configuration for the lookup was judged the wrong shape. No global cache exists yet, so none is built now. Every automatic registration performs one external request; volume is low and the Cloudflare per-IP rate limit (ADR 0010) already bounds abuse. If a cache is introduced, it will be application-wide.

### Creation contract: `201` with the identifier
Creation endpoints answer `201 Created` with a body carrying only the identifier of the created resource (the ISBN for books, the `id` for customers and loans). The full resource is read through `GET`. The `Location` header and a full resource DTO in the response were considered and not adopted: the identifier already lets the client build the URL, and the extra contract would add work for no consumer that needs it.

Input DTOs validate themselves. The compact constructor of each request record accumulates the violated fields in a local list and throws one `InvalidArgumentException`, which the central handler maps to `400` with the fields listed in `message`.

**Alternatives considered:** a Notification-style collector (a `Violations` class) shared by all DTOs — rejected as excess machinery for a framework-free project; a fail-fast check on the first violation — rejected because it hides the other invalid fields from the client.

### Factories instead of Builders in the domain
The Builder pattern was used incorrectly in the entities, which in a DDD and Clean Architecture design should be created through meaningful factories that enforce their invariants. Entities (`Book`, `Customer`, `Loan`) are now created through static factory methods, and value objects such as `Email` and `Isbn` expose `of()`. The input DTOs receive raw data and perform the conversion to domain types, so a DTO can produce its entity (`toEntity()`).

### Database: audit columns by trigger, consolidated schema
- `created_at` and `updated_at` exist on every business table, are never exposed by the API, and listings are ordered by `created_at, id`. `updated_at` is maintained by a database trigger — see ADR 0006, Amendment 3, which also widens the narrow PL/pgSQL exception by that single, mechanical purpose.
- **The Flyway history was consolidated.** `V1` was rewritten to the final schema (tables renamed, new columns and the trigger included) and `V2` to `V4` were removed. This is possible only because the production database was **recreated empty** before the E15 release; it is not a technique to reuse. **From this release on, an applied migration is never edited.**

### Runtime environment
`APP_ENV` accepts `prod` (default), `test` and local values such as `development`. `test` was introduced during the refactoring to avoid continuous consumption of Neon resources and to support tests, and it also selects the fake metadata gateway.

### Carried out as specified in the epic
- The interactive CLI was retired; the HTTP server is the only entrypoint.
- Listings return ordered `List`s; `GET /customers/search` was merged into `GET /customers`.
- ISBN-10 and ISBN-13 are accepted and normalized to ISBN-13 (digits only) in the `Isbn` value object.
- The 15-day maximum is a creation policy in the loan validator; the entity keeps only the "due date not before start date" invariant, so overdue loans load again. An unknown customer on loan creation answers `404`.
- `closed_at` records the real closing moment on return and on close-overdue.
- Registration without `status` defaults to `AVAILABLE`.

## Consequences

**Positive**
- A predictable contract: ordered pagination, a single listing per resource, validated input with explicit field errors, canonical ISBNs.
- The overdue-loan loading defect is fixed by placing each rule where it belongs.
- The external lookup is isolated behind a port, with a fake for tests, and uses a single, higher-quality source.
- `updated_at` has one source of truth.

**Negative / trade-offs**
- Google Books is a single point of dependency for automatic registration: when it is down, only manual registration (with an ISBN) works.
- Manual registrations have empty category and description; the lookup data is not used to complete them.
- No cache: repeated automatic registrations of the same title repeat the external request.
- The rewritten `V1` means environments built from the old history cannot be migrated in place; they must be recreated. E16 and E17 refinement texts must be read against the new table names and `APP_ENV` values.
- `GOOGLE_BOOKS_API` has a placeholder default in `ConfigRegistry`, so a missing key in production is not detected at startup; the lookup fails at request time instead.
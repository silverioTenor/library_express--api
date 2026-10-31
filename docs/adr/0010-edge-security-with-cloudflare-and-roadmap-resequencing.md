# 0010 — Edge Security with Cloudflare and Post-Marco 2 Roadmap Resequencing

## Status
Accepted

## Context
After Marco 2 (ADR 0009) the REST API is public on `https://api.jlibraryexpress.com` with mutating endpoints (`POST`, `PATCH`) and no authentication. Two needs surfaced at the same time:

1. **Edge protection and cost.** DNS lived in Route 53 (a recurring hosted-zone cost) and nothing filtered traffic before it reached a single `t3.micro` EC2 instance. Volumetric filtering, a baseline WAF and basic rate limiting were missing.
2. **Roadmap order.** The planned next epics (E11–E13: observability, overdue enforcement, notifications) did not address the fact that the public API was open. Authentication and data isolation had to come first, and those in turn depend on the domain and API being stable (E15) and on the client IP being trustworthy (E14).

This ADR records the E14 edge-security decisions and the resulting resequencing of the roadmap.

## Decisions

### DNS and proxy: Cloudflare Free plan, Route 53 decommissioned
DNS moved from Route 53 to Cloudflare (Free plan). The domain registrar is unchanged; only the nameservers were switched. `api.jlibraryexpress.com` is a **proxied** A record pointing at the existing Elastic IP. After validation and a short observation period, the Route 53 hosted zone was backed up (record export) and deleted on 2026-10-12. The observation period was deliberately shorter than a week because the system was being exercised daily during it.

### TLS: Full (strict) with the existing Let's Encrypt certificate
Cloudflare validates the certificate already served by Nginx (ADR 0009). Certbot's HTTP-01 renewal still works behind the proxy (`certbot renew --dry-run` verified).

### Origin lock in layers — an IP allowlist alone is not a boundary
Trusting Cloudflare's published ranges trusts every Cloudflare customer, not only this zone. The origin is therefore protected by three independent layers:

1. **Security Group:** ports 80/443 accept traffic only from Cloudflare's published IPv4/IPv6 ranges. Direct access to the Elastic IP is refused. Outbound rules are unchanged.
2. **Zone-level Authenticated Origin Pulls with our own client certificate**, not the shared Cloudflare-provided one (which only proves the request came from *some* Cloudflare zone). Nginx enforces it with `ssl_client_certificate` and `ssl_verify_client on`.
3. **Host check:** a catch-all Nginx server block closes any request whose `Host` is not `api.jlibraryexpress.com` (status `444`) before it reaches the application.

Certificate details:
- The client certificate chain is a private CA plus a leaf (`CA:FALSE`) signed by it. The leaf and its key are uploaded to Cloudflare through the dashboard (the API path returned `403` on the Free plan); only the **public CA** is stored on the instance (`/etc/nginx/ssl/origin-pull-ca.pem`). A self-signed certificate flagged as a CA was rejected by Cloudflare with "Missing leaf certificate", which is why the CA/leaf split exists.
- **Rotation:** generate a new leaf signed by the same CA and upload it to Cloudflare only; Nginx is untouched. The leaf is valid for 730 days (expires around 2028-10-05 — **if it expires, the API goes down**); the CA is valid for 10 years.
- Observed behavior: with `ssl_verify_client on` the handshake completes and the request is rejected at HTTP level (`400 No required SSL certificate was sent`), rather than the TLS handshake being refused.

### Real client IP
Nginx trusts `CF-Connecting-IP` only from Cloudflare ranges (`set_real_ip_from` + `real_ip_header CF-Connecting-IP`) and forwards the value to the application as `X-Real-IP`. The application consumes it starting with E16 (login logging).

The Cloudflare IP list changes occasionally and lives in **two places** that must be refreshed together: the Security Group rules (80/443) and `cloudflare-realip.conf`.

### Versioned Nginx configuration
Nginx runs as a systemd service on the EC2 host, outside the application container. Its configuration is versioned under `.infra/nginx/` in the repository and deployed manually through Session Manager (consistent with ADR 0009: no IaC).

### WAF and rate limiting within Free-plan limits
- The **Free Managed Ruleset** is enabled (a subset, not the full OWASP ruleset). `/docs` and `/openapi.json` were checked for false positives.
- The Free plan allows **one** rate-limiting rule, counted per client IP, with a 10-second window and a 10-second block, and **no filter by HTTP method**. The single rule therefore matches by path prefix — `/auth/`, `/books`, `/customers`, `/loans` — for all methods (reads are counted too), excludes `/docs` and `/openapi.json`, and allows **20 requests per 10 seconds per IP**. It was validated with a bounded test returning `429`.
- Changing URIs (for example adding `/create`) to make them easier to match was rejected: it would violate the REST resource-based conventions adopted in ADR 0008.
- This rule is a **speed brake, not strong protection.** Real brute-force defense is the per-account exponential backoff of E16; protection against data flooding is the per-tenant quotas of E17.

### No CORS configuration
CORS is enforced by browsers, not servers; it does not affect Postman, curl or scripts. With no CORS headers, third-party sites already cannot read responses through browser JavaScript, and Swagger UI works because "Try it out" is same-origin. Authentication (E16) is the real control over who may call the API.

### Roadmap resequencing
Phase 2 order after Marco 2 becomes **E14 (Edge Security) → E15 (Domain & API Refinement) → E16 (Authentication & RBAC) → E17 (Multi-tenancy) → E11 → E12 → E13**.

| Step | Reason |
|---|---|
| E14 | Small, protects the already-public API immediately; trusting the client IP in E16 depends on the origin lock |
| E15 | E16/E17 touch every route and table; audit columns, `closed_at`, unified listings and a stable response contract must exist first. Also retires the CLI |
| E16 | The tenant identifier travels as a JWT claim in E17 |
| E17 | Data isolation |
| E11–E13 | Resume afterwards, already tenant-aware |

**Safeguard between E16 and E17:** the system will have authentication but no tenant isolation, so **no admin account for an external person is created until E17 closes.** During that window the only admin accounts are the owner's.

## Alternatives Considered
- **Cloudflare Origin CA certificate instead of Let's Encrypt.** It would remove Certbot renewal, but the origin would then be unreachable by any non-Cloudflare client (including direct health checks). Not adopted now; can be revisited.
- **IP allowlisting alone.** Rejected as a security boundary: it trusts every Cloudflare customer. Kept only as the first, noise-reducing layer.
- **Shared Cloudflare-provided Authenticated Origin Pulls certificate.** Rejected: it proves a request came from some Cloudflare zone, not from this one.
- **Keeping Route 53 for DNS behind Cloudflare.** Rejected: recurring cost with no benefit once Cloudflare is authoritative.

## Out of Scope
Bot management, geofencing, custom WAF rules beyond the essentials, Cloudflare Access, Turnstile, caching rules, HSTS, Origin CA, and infrastructure as code.

## Consequences
- The origin cannot be reached except through this Cloudflare zone; scanner noise on the instance drops sharply.
- The API now depends on Cloudflare availability and on a client certificate whose expiry (around 2028-10) must be tracked.
- Two places must be kept in sync when Cloudflare changes its IP ranges.
- Free-plan limits make the rate limit coarse (single rule, 10-second window); it protects the single instance from bursts but is not an authentication defense.
- The public API stays unauthenticated until E16 and is not offered to external accounts until E17 closes; this is accepted explicitly through the safeguard above.

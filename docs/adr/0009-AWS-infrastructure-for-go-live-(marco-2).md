# 0009 — AWS Infrastructure for Go-Live (Marco 2)

## Status
Accepted

## Context
Epic E10 required publishing the REST API (E9) to a real production environment at the lowest cost possible, without Terraform or any IaC tool (explicitly excluded from this project's scope — see note below), and without PaaS abstractions (Heroku, Elastic Beanstalk) that would hide the decisions this epic is meant to demonstrate.

## Decisions

### Orchestration: ECS with EC2 launch type, no Fargate, no Auto Scaling Group
Fargate has no free tier under any AWS account model; EC2 launch type keeps cost bounded to the US$200 credit. The EC2 container instance is provisioned manually (not via an Auto Scaling Group or a capacity provider) and registered into the cluster by setting `ECS_CLUSTER` in `/etc/ecs/ecs.config` via instance user data. **Trade-off accepted:** if the instance itself fails, nothing recreates it automatically — only the ECS service recovers the *task*, not the *machine*. This was judged acceptable for a single-instance, low-traffic portfolio deployment.

The instance runs the ECS-optimized **Amazon Linux 2023** AMI (not AL2, which reached end of support on 2026-06-30), resolved dynamically via `resolve:ssm:/aws/service/ecs/optimized-ami/amazon-linux-2023/recommended/image_id` rather than a pinned AMI ID.

### No Application Load Balancer
Direct exposure via an Elastic IP + Route 53 A record. No cost/benefit justification for an ALB without real production traffic. TLS termination is handled separately — see "HTTPS Termination" below.

### Database: Neon over RDS or Supabase
No managed RDS — Neon (serverless Postgres, free tier) was chosen over Supabase specifically because Neon's compute auto-suspends and auto-resumes on idle without manual intervention; Supabase's free tier pauses the entire project after 7 days of inactivity, requiring manual dashboard action — unacceptable for an unattended portfolio deployment.

Credentials are stored in AWS Secrets Manager as a single multi-key JSON secret (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`), not a single connection-string value, because `ConnectionProvider` (infrastructure layer) already builds the JDBC URL from these separate config values rather than parsing one string. Outside the test environment, HikariCP sets `sslmode=require` and `channelBinding=require` (the latter per Neon's own security recommendation, mitigating MITM risk on the SCRAM-SHA-256 handshake).

### CD Pipeline: GitHub Actions, OIDC, tag-triggered
Authentication from GitHub Actions to AWS uses an OIDC role (`LibraryExpressCDRole`), not an IAM user with a long-lived access key — no static credentials exist in the pipeline. The trust policy restricts `AssumeRoleWithWebIdentity` to this exact repository on the `main` branch.

The CD workflow (`cd.yml`) is triggered **only by a version tag push** (`v*.*.*`), not by every push/merge to `main`. The release flow is: a release branch is created from `main` (not `develop`); the Dev performs a `git cherry-pick` of the desired commits, runs tests locally, and bumps the version in `pom.xml` (removing `-SNAPSHOT`) directly on the release branch, before opening the PR. The PR is reviewed and approved against this already-final state — no automation writes back to the branch after approval, avoiding GitHub's "dismiss stale reviews on new commit" from invalidating the approval. After merge, the Dev creates the Git tag **locally and manually**, matching the version already committed in `main`'s `pom.xml`, and pushes it — this tag push is what triggers deployment. The Docker image is tagged with the Git tag name itself (e.g. `v0.2.0-beta`), not the commit SHA, since the tag already carries 1:1 traceability to the exact commit and is more human-readable.

### HTTPS Termination: Nginx + Let's Encrypt, not ALB or CloudFront
Since an ALB was already excluded, and the application's embedded `com.sun.net.httpserver.HttpServer` has no native production-grade TLS termination, HTTPS without an explicit port (`https://api.jlibraryexpress.com/books`) is handled by an Nginx reverse proxy running on the same EC2 instance, listening on 80/443 and forwarding to `localhost:3000`. The certificate is issued and auto-renewed by Let's Encrypt via Certbot (`certbot --nginx`), validated via the HTTP-01 challenge — possible only because the domain already resolves to the instance's Elastic IP (Route 53, decided earlier in this same epic).

**Alternatives considered:**
- **CloudFront** — would also work (free ACM certificate, managed TLS termination) but introduces an additional managed AWS service into the architecture for a single-instance deployment, adding another moving part to understand/explain without a corresponding operational benefit at this scale.
- **ALB with ACM** — rejected for the same reason the epic already excluded it: no cost/benefit justification without real traffic, and it directly contradicts the already-locked "no ALB" decision.

Nginx was chosen specifically because it keeps the project consistent with its own stated philosophy — explicit, hand-rolled solutions over managed abstractions (the same reasoning behind the framework-free HTTP server and raw JDBC) — and is a widely expected skill in backend hiring processes.

As a consequence, the Security Group no longer exposes port 3000 publicly — only 80/443. Port 3000 remains reachable solely via `localhost`, from Nginx.

### Instance access: AWS Systems Manager Session Manager, not SSH
No port 22 is exposed in the Security Group. Access requires the `AmazonSSMManagedInstanceCore` managed policy attached to the EC2 instance role (`ecsInstanceRole`), present *before* the instance boots. This removes the most commonly attacked port on a publicly reachable EC2 instance entirely, rather than merely restricting its source IP.

### No Infrastructure as Code (Terraform)
Explicitly excluded from this project's scope. E10 is provisioned manually via AWS CLI/Console. Terraform is planned for a future project instead, consistent with this project's pedagogical sequencing (raw mechanism before tooling abstraction) and its framework-free philosophy.

## Consequences
- Lower monthly cost than ALB/RDS/Fargate-based alternatives, within the US$200 credit.
- Manual recovery required if the EC2 instance itself fails (no ASG).
- Manual, explicit release process (local tag creation) trades some automation convenience for auditability and avoids the stale-review-dismissal problem that a fully automated version-bump-then-approve flow would introduce.
- TLS certificate renewal depends on Certbot's systemd timer remaining healthy on a single instance — no redundancy if the instance has an extended outage near the 90-day renewal window.
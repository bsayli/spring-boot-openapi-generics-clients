# Security Policy

We take security seriously and appreciate responsible disclosures.  
If you believe you’ve found a vulnerability, **please follow the process below**.

---

## Supported Versions

We currently provide security fixes for the latest minor release and the `main` branch.

| Version    | Status              |
|-----------:|---------------------|
| `main`     | ✅ Supported        |
| `0.7.x`    | ✅ Supported        |
| `< 0.7.0`  | ❌ Not supported    |

> Note: This project is pre-1.0; interfaces may evolve quickly. Please upgrade to the latest release before reporting issues when possible.

---

## Reporting a Vulnerability

**Do not open a public issue.**  
Instead, choose one of the following private channels:

1. **GitHub Security Advisory (preferred):**  
   Create a private report via **Security → Advisories → Report a vulnerability** in this repo.
2. **Email:**  
   Send details to **baris.sayli@gmail.com** with the subject `SECURITY: <short summary>`.

Please include:

- A clear description of the issue and potential impact.
- A minimal proof-of-concept (PoC) or steps to reproduce.
- Affected version(s) (commit hash or tag) and environment details.
- Any suggested remediation ideas if you have them.

---

## Our Process & Timelines

We aim to respond quickly and keep you informed throughout.

- **Acknowledgement:** within **72 hours**.
- **Triage & Reproduction:** within **5 business days**.
- **Fix Plan:** within **10 business days** for High/Critical; otherwise as scheduled.
- **Release:** a patched version will be published; we may coordinate an embargoed release for impactful issues.

We’ll keep you updated at major milestones (triage result, fix readiness, release timing).

---

## Severity Guidance

We use a pragmatic CVSS-like approach:

- **Critical/High:** RCE, auth bypass, or issues enabling widespread compromise.
- **Medium:** Information disclosure, privilege/DoS limited to a single service.
- **Low:** Hardening gaps, misconfigurations, limited-edge misuse.

Severity influences prioritization and disclosure timing.

---

## Coordinated Disclosure

- We prefer **coordinated disclosure**. Please do not share details publicly until a fix is available.
- With your consent, we’re happy to credit reporters in release notes (name/handle).

---

## Scope

**In scope**
- `customer-service` (server / OpenAPI producer)
- `customer-service-client` (generated client & overlays)
- Templates, schema customizers, and build instructions contained in this repo

**Out of scope**
- Vulnerabilities exclusively within third-party dependencies (report upstream first)
- Demo/test-only code that is not used in production contexts
- Deployment-specific misconfigurations outside the repo

---

## Non-qualifying Reports

To focus on impactful issues, we generally exclude:
- Best-practice suggestions without a practical exploit scenario
- Rate limiting / generic DoS without a novel exploit
- Missing security headers in dev/demo endpoints
- Social engineering or physical attacks

---

## Questions

If you’re unsure whether something qualifies, email **baris.sayli@gmail.com** and we’ll help triage.

Thank you for helping keep the community safe! 🙏
# Security Policy

We take security seriously and appreciate responsible disclosures.
If you believe you’ve found a vulnerability, **please follow the process below**.

---

## 📑 Table of Contents

- [Supported Versions](#supported-versions)
- [Reporting a Vulnerability](#reporting-a-vulnerability)
- [Our Process & Timelines](#our-process--timelines)
- [Severity Guidance](#severity-guidance)
- [Coordinated Disclosure](#coordinated-disclosure)
- [Scope](#scope)
   - [In scope](#in-scope)
   - [Out of scope](#out-of-scope)
- [Non-qualifying Reports](#nonqualifying-reports)
- [Questions](#questions)

---
## Supported Versions

We currently provide security fixes for the latest minor release line and the `main` branch.

| Version   | Status          |
| --------- | --------------- |
| `main`    | ✅ Supported     |
| `0.7.x`   | ✅ Supported     |
| `< 0.7.0` | ❌ Not supported |

> **Note**
> This project is **pre‑1.0**. Public APIs and contracts may evolve quickly.
> Please upgrade to the latest release before reporting issues whenever possible.

---

## Reporting a Vulnerability

**Do not open a public issue.**

Use one of the following **private disclosure channels**:

1. **GitHub Security Advisory (preferred)**
   Use **Security → Advisories → Report a vulnerability** in this repository.

2. **Email**
   Send details to **[baris.sayli@gmail.com](mailto:baris.sayli@gmail.com)** with subject:

   ```text
   SECURITY: <short summary>
   ```

Please include:

* A clear description of the issue and its potential impact
* A minimal proof‑of‑concept (PoC) or reproduction steps
* Affected version(s) (tag or commit hash)
* Environment details if relevant
* Suggested remediation ideas (optional but welcome)

---

## Our Process & Timelines

We aim to handle reports responsibly, transparently, and without unnecessary delay.

* **Acknowledgement:** typically within a few days
* **Triage & Reproduction:** prioritized based on severity and scope
* **Fix Planning:** aligned with impact, determinism, and contract stability
* **Release:** fixes are published once validated

For sensitive issues, **coordinated disclosure** may be used.
Reporters are kept informed at key milestones.

---

## Severity Guidance

We follow a pragmatic, CVSS‑inspired classification:

* **Critical / High**
  Remote code execution, authentication bypass, or vulnerabilities enabling broad compromise

* **Medium**
  Information disclosure, privilege escalation, or DoS limited to a single service or boundary

* **Low**
  Hardening gaps, misconfigurations, or limited edge‑case misuse

Severity directly influences prioritization and release timing.

---

## Coordinated Disclosure

* We prefer **coordinated disclosure**. Please do not share details publicly before a fix is released.
* With your consent, reporters may be credited in release notes or acknowledgements.

---

## Scope

### In scope

* **`openapi-generics-contract`**
  Shared response, paging, and error contracts (`ServiceResponse<T>`, `Meta`, `Page`, RFC 9457 extensions)

* **`customer-service`**
  Spring Boot server / OpenAPI producer

* **`customer-service-client`**
  Generated client, template overlays, and client‑side adapters

* OpenAPI templates, schema customizers, and build instructions contained in this repository

### Out of scope

* Vulnerabilities exclusively caused by **third‑party dependencies** (report upstream first)
* Demo or test‑only code not used in production contexts
* Deployment‑specific misconfigurations outside this repository

---

## Non‑qualifying Reports

To keep focus on impactful issues, we generally exclude:

* Best‑practice recommendations without a realistic exploit scenario
* Generic rate‑limiting or DoS reports without novel attack vectors
* Missing security headers in demo or development endpoints
* Social engineering or physical attack scenarios
* Issues that require modifying **generated code directly** instead of templates or shared contracts

> **Important**
> Generated code is treated as **disposable output**.
> Security fixes must target **contracts, templates, or generators**, not generated artifacts.

---

## Questions

If you’re unsure whether something qualifies as a security issue, contact:

**[baris.sayli@gmail.com](mailto:baris.sayli@gmail.com)**

We’re happy to help triage before a formal report.

---

Thank you for helping keep the community safe 🙏

This policy reflects the project’s core principle:

**Security, like API contracts, should be addressed at clear boundaries — not patched as an afterthought.**

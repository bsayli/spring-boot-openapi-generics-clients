# Security Policy

We take security seriously and appreciate responsible disclosures.
If you believe you’ve found a vulnerability, **please follow the process below**.

---

## 📑 Table of Contents

* [Supported Versions](#supported-versions)
* [Reporting a Vulnerability](#reporting-a-vulnerability)
* [Our Process & Timelines](#our-process--timelines)
* [Severity Guidance](#severity-guidance)
* [Coordinated Disclosure](#coordinated-disclosure)
* [Scope](#scope)

    * [In scope](#in-scope)
    * [Out of scope](#out-of-scope)
* [Non-qualifying Reports](#nonqualifying-reports)
* [Questions](#questions)

---

## Supported Versions

We currently provide security fixes for the latest minor release line and the `main` branch.

| Version   | Status          |
| --------- | --------------- |
| `main`    | ✅ Supported     |
| `0.8.x`   | ✅ Supported     |
| `< 0.8.0` | ❌ Not supported |

> **Note**
> This project is **pre-1.0**. Public APIs and contracts may evolve quickly.
> Please upgrade to the latest release before reporting issues whenever possible.

---

## Reporting a Vulnerability

**Do not open a public issue.**

Use one of the following **private disclosure channels**:

### 1. GitHub Security Advisory (preferred)

Use **Security → Advisories → Report a vulnerability** in this repository.

### 2. Email

Send details to:

```
baris.sayli@gmail.com
```

Subject:

```
SECURITY: <short summary>
```

Please include:

* A clear description of the issue and its potential impact
* A minimal proof-of-concept (PoC) or reproduction steps
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

We follow a pragmatic, CVSS-inspired classification:

### Critical / High

* Remote code execution
* Deserialization vulnerabilities
* Contract bypass enabling unsafe execution paths

### Medium

* Information disclosure
* Schema manipulation leading to incorrect client/server behavior
* DoS within bounded system scope

### Low

* Hardening gaps
* Misconfigurations
* Edge-case misuse without realistic exploit chain

Severity directly influences prioritization and release timing.

---

## Coordinated Disclosure

* We prefer **coordinated disclosure**
* Please do not share details publicly before a fix is released
* Reporters may be credited in release notes upon request

---

## Scope

### In scope

This repository is a **platform**, not an application.

Security concerns are defined at **artifact boundaries** (how users consume the platform), not internal modules only.

---

### Primary entry points (user-facing)

These are the **two main artifacts directly used by consumers**:

* **`openapi-generics-server-starter`** (server side)

    * Entry point for Spring Boot integration
    * Performs **contract → OpenAPI projection**
    * Handles schema generation and generic resolution

* **`openapi-generics-java-codegen-parent`** (client side)

    * Entry point for generated clients
    * Provides generator configuration, templates, and build wiring

---

### Transitive security surface (implicitly pulled)

These artifacts are **not always added directly**, but are part of the runtime and generation chain:

* **`openapi-generics-contract`**

    * Core shared model
    * `ServiceResponse<T>` semantics
    * Pagination (`Page`, `Meta`)
    * Error model (RFC 9457 extensions)

* **`openapi-generics-java-codegen`**

    * Generator implementation
    * Template behavior
    * Type mapping and suppression logic

---

### Dependency flow (critical for security reasoning)

#### Server path

```
openapi-generics-server-starter
  └── openapi-generics-contract
```

#### Client path

```
openapi-generics-java-codegen-parent
  ├── openapi-generics-java-codegen
  └── openapi-generics-contract
```

---

### Platform-level concerns

+ * Misalignment between contract, OpenAPI projection, and generated code

Security issues may arise in:

* Contract violations or ambiguity (`ServiceResponse<T>` semantics)
* Incorrect schema projection (server → OpenAPI)
* Incorrect code generation (OpenAPI → client)
* Template-level behavior and transformation rules
* Loss of determinism between contract, spec, and generated code

---

### Notes

* Consumers typically depend on **only one artifact (server or client entry point)**
* The rest of the platform is **pulled transitively**
* Therefore, security must be evaluated **across the full chain**, not a single module

---

### Out of scope

* Example or sample applications outside the core platform
* Vulnerabilities caused solely by **third-party dependencies** (report upstream first)
* Deployment-specific misconfigurations
* Runtime environment issues unrelated to the platform itself

---

## Non-qualifying Reports

To keep focus on impactful issues, we generally exclude:

* Best-practice recommendations without a realistic exploit scenario
* Generic rate-limiting or DoS claims without a concrete attack vector
* Missing headers or hardening suggestions in non-production contexts
* Social engineering or physical attack scenarios

> **Important**
> Generated code is treated as **disposable output**.
> Security fixes must target **contracts, templates, or generators**, not generated artifacts.

---

## Questions

If you’re unsure whether something qualifies as a security issue, contact:

```
baris.sayli@gmail.com
```

We’re happy to help triage before a formal report.

---

Thank you for helping keep the community safe 🙏

**Security, like API contracts, must be enforced at system boundaries — not patched after the fact.**

# openapi-generics-java-codegen

> Generics-aware OpenAPI Generator extension for contract-aligned Java clients

`openapi-generics-java-codegen` is a **custom OpenAPI Generator extension** that enforces **contract-first client generation**.

It does not try to be smarter than your contract.
It ensures the generator **does not break it**.

The role of this module is strict:

> Prevent OpenAPI Generator from redefining platform-owned models and enforce contract-aligned output.

It is a **build-time component** and is typically used via `openapi-generics-java-codegen-parent`.

---

## Table of Contents

1. [Purpose](#-purpose)
2. [Core Idea](#-core-idea)
3. [What It Does](#-what-it-does)
4. [Result](#-result)
5. [Template Integration](#-template-integration)
6. [How It Is Used](#-how-it-is-used)
7. [Not Intended For Direct Use](#-not-intended-for-direct-use)
8. [Compatibility Matrix](#-compatibility-matrix)
9. [Determinism Guarantees](#-determinism-guarantees)
10. [Design Constraints](#-design-constraints)
11. [Mental Model](#-mental-model)
12. [Related Modules](#-related-modules)
13. [License](#-license)
---

## 🎯 Purpose

Default OpenAPI Generator behavior:

* regenerates response envelopes per endpoint
* flattens or loses generic semantics
* creates model drift between server and client

This module prevents that.

It ensures that generated clients:

* reuse canonical contract types (`ServiceResponse`, `Meta`, `Page`, etc.)
* do NOT regenerate platform-owned models
* remain aligned with server-side contract semantics

It acts as an **enforcement layer** between OpenAPI and generated Java code.

---

## 🧠 Core Idea

> OpenAPI is a projection — not the source of truth

This generator enforces that rule at build time:

* platform-owned models must NOT be generated
* OpenAPI metadata must be interpreted, not materialized

If OpenAPI contains structure that already exists in the contract:

> it is mapped back — not regenerated

---

## ⚙️ What It Does

### 1. Detects non-generatable models

Models marked with:

```
x-ignore-model: true
```

are treated as **platform-owned**.

---

### 2. Suppresses model generation (3-phase strategy)

#### Phase 1 — MARK

* intercepts `fromModel`
* collects ignored model names

#### Phase 2 — LOCAL FILTER

* removes models from current processing batch

#### Phase 3 — GLOBAL REMOVE

* removes models from full generation graph

---

### 3. Cleans imports

* removes references to ignored models from generated classes

---

## 🧱 Result

Generated code:

* references `openapi-generics-contract`
* does NOT duplicate envelope types
* preserves generic semantics (`ServiceResponse<T>`, `Page<T>`)
* remains deterministic and stable

---

## 🧩 Template Integration

This module also provides custom templates under:

```
META-INF/openapi-generics/templates
```

### Core template: `api_wrapper.mustache`

This template:

* wraps generated models
* injects `ServiceResponse<T>`
* handles container types (`Page<T>`)

Example output:

```java
public class CustomerResponse extends ServiceResponse<CustomerDto> {}
```

---

## 🔗 How It Is Used

This module is **not typically used directly**.

Instead, it is wired via:

```
openapi-generics-java-codegen-parent
```

The parent POM:

* registers this generator
* injects templates
* configures OpenAPI Generator plugin

---

## 🚫 Not Intended For Direct Use

End users should NOT:

* reference this module directly
* configure it manually
* override generator behavior

Instead:

> Use the codegen parent — it handles everything

---

## 🔗 Compatibility Matrix

This module is tested with the following versions:

| Component          | Supported Versions |
|--------------------|-------------------|
| Java              | 17+               |
| OpenAPI Generator | 7.x               |

Notes:

* `restclient` library is available starting from **OpenAPI Generator 7.6.0**
* If you use `restclient`, you must use **7.6.0 or newer**
* This module is designed to work across the **OpenAPI Generator 7.x series**
* This is a **build-time module** — no runtime dependency on Spring

---

## 🔒 Determinism Guarantees

This generator ensures:

* ✔ No duplication of contract models
* ✔ Stable model graph
* ✔ Consistent generation output
* ✔ Preservation of generic semantics

Mechanisms:

* controlled model suppression
* explicit extension handling
* no implicit behavior

---

## ⚠️ Design Constraints

The generator:

* depends on vendor extensions (`x-*` fields)
* assumes contract-first design
* is tightly coupled to platform semantics

It is NOT a general-purpose generator.

---

## 🧠 Mental Model

Think of this module as:

> A guardrail inside OpenAPI Generator that prevents contract drift

Not:

* a standalone generator
* a user-facing tool

---

## 🔗 Related Modules

| Module                                 | Role                               |
| -------------------------------------- | ---------------------------------- |
| `openapi-generics-contract`            | Defines canonical models           |
| `openapi-generics-server-starter`      | Produces OpenAPI projection        |
| `openapi-generics-java-codegen`        | Enforces generation rules          |
| `openapi-generics-java-codegen-parent` | Orchestrates build-time generation |

---

## 📜 License

MIT License

---

**Maintained by:**
**Barış Saylı**
[GitHub](https://github.com/bsayli) · [Medium](https://medium.com/@baris.sayli) · [LinkedIn](https://www.linkedin.com/in/bsayli)

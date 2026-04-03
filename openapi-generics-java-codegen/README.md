# openapi-generics-java-codegen

> Generics-aware OpenAPI Generator extension for contract-aligned Java clients

`openapi-generics-java-codegen` is a **custom OpenAPI Generator extension** that enforces **contract-first client generation** by preventing duplication of platform-owned models and enabling deterministic wrapper generation.

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
8. [Determinism Guarantees](#-determinism-guarantees)
9. [Design Constraints](#-design-constraints)
10. [Mental Model](#-mental-model)
11. [Related Modules](#-related-modules)
12. [License](#-license)

---

## 🎯 Purpose

This module ensures that generated clients:

* reuse canonical contract types (`ServiceResponse`, `Meta`, `Page`, etc.)
* do NOT regenerate platform-owned models
* remain aligned with server-side contract semantics

It acts as an **enforcement layer** between OpenAPI and generated Java code.

---

## 🧠 Core Idea

> OpenAPI is a projection — not the source of truth

This generator enforces that:

* platform-owned models must NOT be generated
* OpenAPI metadata must be interpreted, not re-materialized

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

## 🔒 Determinism Guarantees

This generator ensures:

* no duplication of contract models
* stable model graph
* consistent generation output

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

> A filter + enforcement layer inside OpenAPI Generator

Not:

* a standalone generator
* a user-facing tool

---

## 🔗 Related Modules

| Module                                 | Role                               |
| -------------------------------------- | ---------------------------------- |
| `openapi-generics-contract`                         | Defines canonical models           |
| `openapi-generics-server-starter`      | Produces OpenAPI projection        |
| `openapi-generics-java-codegen`        | Enforces generation rules          |
| `openapi-generics-java-codegen-parent` | Orchestrates build-time generation |

---

## 📜 License

MIT License

---

**Maintained by:**
Barış Saylı
[https://github.com/bsayli](https://github.com/bsayli)

# openapi-generics-contract

> **Canonical contract module (Authority Layer) of the OpenAPI Generics platform**

`openapi-generics-contract` is a **framework-agnostic Java library** that defines the
**authoritative response model** used across the OpenAPI Generics platform.

It is the only place where response semantics are defined.
Everything else in the system either **projects**, **interprets**, or **consumes** this model.

The goal is simple:

> Define response semantics **once**, preserve them across the entire lifecycle,
> and never duplicate or reinterpret them.

---

## 📑 Table of Contents

1. [Why This Module Exists](#-why-this-module-exists)
2. [Architectural Positioning (Critical)](#-architectural-positioning-critical)
3. [Design Philosophy](#-design-philosophy)
4. [Core Concepts](#-core-concepts)
5. [Relationship with the Platform](#-relationship-with-the-platform)
6. [Explicit Non-Goals](#-explicit-non-goals)
7. [Dependency](#-dependency)
8. [Versioning Strategy](#-versioning-strategy)
9. [When Should You Use This?](#-when-should-you-use-this)
10. [Design Trade-offs](#-design-trade-offs)
11. [Failure Philosophy](#-failure-philosophy)
12. [Mental Model](#-mental-model)
13. [Summary](#-summary)

---

## 🎯 Why This Module Exists

Most HTTP APIs converge on the same shape:

* payload wrapped with metadata
* pagination and sorting structures
* structured error extensions

But in practice, these are often:

* duplicated across services
* regenerated in clients
* inconsistently evolved over time

This creates long-term problems:

* schema drift between producer and consumer
* duplicated DTO hierarchies
* fragile client regeneration
* unclear ownership of the API contract

`openapi-generics-contract` addresses this by introducing a **single, shared contract**
that both producers and consumers depend on directly.

---

## 🧠 Architectural Positioning (Critical)

Within the platform, this module is the **authority layer**:

| Layer         | Role                                |
| ------------- | ----------------------------------- |
| **Authority** | openapi-generics-contract (this)    |
| Projection    | server starter (OpenAPI generation) |
| Enforcement   | code generation (build-time)        |
| Consumption   | generated clients                   |

### Core Rule

> OpenAPI is a projection. This module is the authority.

### Implications

* OpenAPI MUST NOT redefine these models
* generators MUST NOT re-generate them
* clients MUST reuse them directly

If this boundary is violated, contract drift is reintroduced.

---

## 🧠 Design Philosophy

### Single Source of Truth

The response envelope is:

* not generated
* not copied
* not redefined

It is defined once and reused everywhere.

---

### Contract Before Tooling

Frameworks and generators are **implementation concerns**.

The response model is treated as a **domain-level contract**,
not as a side-effect of tooling.

---

### Framework Neutrality

No dependency on:

* Spring
* Jakarta Web
* OpenAPI annotations

Only minimal JSON annotations are used.

Result:

* portable
* stable
* long-lived

---

### Predictable Evolution

The contract evolves **additively**:

* new fields → optional
* no breaking structural rewrites

---

## 🧱 Core Concepts

### Canonical Success Envelope

```java
ServiceResponse<T>
```

Structure:

* `data` → payload
* `meta` → contextual metadata

Example:

```java
return ServiceResponse.of(customerDto);
```

---

### Response Metadata

`Meta` contains contextual information:

* server time
* sorting
* future extensibility

Design:

* minimal
* extensible
* always present

---

### Pagination Contract

```java
Page<T>
```

Standardizes:

* page index
* page size
* total elements
* navigation flags

Supported canonical shape:

```java
ServiceResponse<Page<T>>
```

Out of scope:

* arbitrary nested generics
* maps and complex containers

Rationale:

* deterministic schema generation
* stable client typing

---

### Sorting Descriptor

```java
Sort(field, direction)
```

Simple, framework-neutral sorting model.

---

### Error Extensions (RFC 9457)

Provides:

* `ProblemExtensions`
* `ErrorItem`

Used inside:

```
application/problem+json
```

This module does NOT implement error handling.

---

## 🔗 Relationship with the Platform

This module is intentionally **independent but central**.

### Used by Server Layer

* server starter reads these types
* projects them into deterministic OpenAPI schemas

---

### Used by Code Generation Layer

* generator maps schemas back to these classes
* prevents model duplication
* enforces contract alignment

---

### Used by Clients

* generated clients extend these types
* ensures type consistency across boundaries

---

### Key Guarantee

> The same contract type flows through server → OpenAPI → client unchanged.

---

## 🚫 Explicit Non-Goals

This module does NOT:

* generate OpenAPI
* implement controllers
* handle HTTP transport
* perform validation
* generate clients

Those responsibilities belong to other platform layers.

---

## 📦 Dependency

```xml
<dependency>
    <groupId>io.github.blueprint-platform</groupId>
    <artifactId>openapi-generics-contract</artifactId>
    <version>0.8.1</version>
</dependency>
```

Usage:

* server → compile
* client → compile / provided

---

## 🔐 Versioning Strategy

Current state: **pre-1.0**

Meaning:

* API may evolve
* breaking changes are possible but controlled

### Important Rule

> Server and client MUST use the same contract version.

---

## 🧩 When Should You Use This?

Use this module if:

* you expose typed REST APIs
* you generate clients from OpenAPI
* you want zero duplication of response wrappers
* you treat response shape as architecture (not implementation detail)

---

## ⚖️ Design Trade-offs

### Limited Scope

Only specific generic shapes are supported.

Gain:

* determinism
* predictability

---

### No Framework Integration

No Spring shortcuts.

Gain:

* long-term stability
* portability

---

### No Runtime Behavior

Only data structures.

Gain:

* explicitness
* no hidden logic

---

## 💥 Failure Philosophy

This module avoids hidden behavior.

If something breaks:

> it should fail loudly in upper layers (server / codegen)

---

## 🧠 Mental Model

Think of this module as:

> The canonical API language shared across your system

Not:

* a utility library
* a DTO collection

---

## 🧾 Summary

`openapi-generics-contract` is:

* the **authority layer** of the platform
* the **single source of truth** for response semantics
* a **framework-agnostic contract module**

Its responsibility is strictly:

> Define response semantics once and preserve them across the entire lifecycle

Nothing more.

---

## 📜 License

MIT License.

---

**Maintained by:**
**Barış Saylı**
[GitHub](https://github.com/bsayli) · [Medium](https://medium.com/@baris.sayli) · [LinkedIn](https://www.linkedin.com/in/bsayli)

# openapi-generics-contract

> **Canonical contract module (Authority Layer) of the OpenAPI Generics platform**

`openapi-generics-contract` is a **framework-agnostic Java library** that defines the
**authoritative response model** used across the OpenAPI Generics platform.

It is the only place where response semantics are defined.
Everything else in the system either **projects**, **reconstructs**, or **consumes** this model.

The goal is simple:

> Define response semantics **once**, preserve them across the entire lifecycle,
> and never duplicate or reinterpret them.

---

## 📑 Table of Contents

1. [Why This Module Exists](#why-this-module-exists)
2. [Architectural Positioning (Critical)](#architectural-positioning-critical)
3. [Design Philosophy](#design-philosophy)
4. [Core Concepts](#core-concepts)
5. [Relationship with the Platform](#relationship-with-the-platform)
6. [Explicit Non-Goals](#explicit-non-goals)
7. [Dependency](#dependency)
8. [Compatibility Matrix](#compatibility-matrix)
9. [Versioning Strategy](#versioning-strategy)
10. [When Should You Use This?](#when-should-you-use-this)
11. [Design Trade-offs](#design-trade-offs)
12. [Failure Philosophy](#failure-philosophy)
13. [Mental Model](#mental-model)

---

## Why This Module Exists

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

## Architectural Positioning (Critical)

Within the platform, this module is the **contract authority** for the built-in response model:

| Layer                  | Role                                                                                                                              |
|------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| **Contract Authority** | Defines the canonical built-in Java contract types: `ServiceResponse<T>`, `Meta`, `Page<T>`, sorting, and error-extension models. |
| Projection             | Server-side integration projects these contract types into OpenAPI metadata.                                                      |
| Reconstruction         | Code generation reconstructs client-side wrapper types that reuse the original contract model.                                    |
| Consumption            | Generated clients and downstream services consume the shared contract types.                                                      |

### Core Rule

> OpenAPI is a projection. The Java contract remains the authority.

### Implications

* OpenAPI SHOULD NOT become the owner of these models.
* Generators SHOULD NOT redefine the built-in response envelope.
* Generated clients SHOULD reuse the shared contract types directly.
* Contract identity should remain stable across server, OpenAPI, generated client, and consumer boundaries.

If this boundary is violated, contract drift is reintroduced.

---

## Design Philosophy

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
* Jackson

The module consists entirely of plain Java types and has no runtime framework dependencies.

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

## Core Concepts

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

## Relationship with the Platform

This module is intentionally **independent but central**.

### Used by Server Layer

* server starter reads these types
* projects them into deterministic OpenAPI schemas

---

### Used by Code Generation Layer

* generator maps schemas back to these classes
* prevents model duplication
* reconstructs contract-aligned client types

---

### Used by Clients

* generated clients extend these types
* ensures type consistency across boundaries

---

### Key Guarantee

> The same contract type flows through server → OpenAPI → client unchanged.

---

## Explicit Non-Goals

This module does NOT:

* generate OpenAPI
* implement controllers
* handle HTTP transport
* perform validation
* generate clients

Those responsibilities belong to other platform layers.

---

## Dependency

```xml
<dependency>
    <groupId>io.github.blueprint-platform</groupId>
    <artifactId>openapi-generics-contract</artifactId>
    <version>1.2.1</version>
</dependency>
```

Usage:

* server → compile
* client → compile / provided

---

## Compatibility Matrix

| Component | Supported Versions |
|-----------|--------------------|
| Java      | 17+                |

---

## Versioning Strategy

Current state: **1.2.1**

The contract module is treated as the stable authority layer of the platform.

Patch and minor releases may add fields or helper APIs, but response semantics
should evolve conservatively and remain backward-compatible whenever possible.

### Important Rule

> Server and generated client SHOULD use the same contract version.

For strict contract alignment, use the same `openapi-generics-contract` version
across producer and generated client modules.

---

## When Should You Use This?

Use this module if:

* you expose typed REST APIs
* you generate clients from OpenAPI
* you want zero duplication of response wrappers
* you treat response shape as architecture (not implementation detail)

---

## Design Trade-offs

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

## Failure Philosophy

This module avoids hidden behavior.

If something breaks:

> it should fail loudly in upper layers (server / codegen)

---

## Mental Model

Think of this module as:

> The canonical API language shared across your system

Not:

* a utility library
* a DTO collection

---

## Summary

`openapi-generics-contract` is:

* the **authority layer** of the platform
* the **single source of truth** for response semantics
* a **framework-agnostic contract module**

Its responsibility is strictly:

> Define response semantics once and preserve them across the entire lifecycle

Nothing more.

---

## License

MIT — see [LICENSE](../LICENSE)

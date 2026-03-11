# api-contract

> **Canonical API response and pagination contract for server–client ecosystems**

`api-contract` is a **framework-agnostic Java library** that defines a stable,
reusable response envelope and pagination model for distributed systems.

It provides a **single, authoritative contract** that can be shared across:

* backend services
* generated API clients
* integration adapters
* consumer applications

The goal is simple:

> Define response semantics **once** and reuse them everywhere.

---

## 🎯 Why This Library Exists

Modern HTTP APIs almost always wrap payloads with additional metadata:

* timestamps
* pagination context
* sorting information
* structured error extensions

In many systems, these wrappers are:

* duplicated per service
* re-generated in clients
* inconsistently evolved over time

This leads to:

* schema drift
* duplicated DTO hierarchies
* brittle client contracts
* unclear ownership of response shape

`api-contract` addresses this by introducing a **shared response contract**
that both producers and consumers can depend on directly.

---

## 🧠 Design Philosophy

This library follows several explicit architectural principles:

### Single source of truth

The response envelope is **not generated**, **not copied**, and **not redefined**.
It is defined once and reused directly.

### Contract before tooling

OpenAPI generators, frameworks, and transport layers are considered
**implementation concerns**.
The response model itself is treated as a **domain contract**.

### Framework neutrality

The module intentionally avoids dependencies on:

* Spring / Jakarta / HTTP abstractions
* OpenAPI annotations
* serialization frameworks beyond minimal JSON hints

This allows the contract to remain:

* stable
* portable
* long-lived

### Predictable evolution

The contract is designed for **additive change**.
Optional fields may be introduced without forcing ecosystem-wide rewrites.

---

## 🧱 Core Concepts

### Canonical Success Envelope

All successful responses share the same structure:

```java
ServiceResponse<T>
```

This envelope provides:

* a generic payload (`data`)
* request-level metadata (`meta`)

Example:

```java
return ServiceResponse.of(customerDto);
```

The metadata component is always initialized and safe to consume.

---

### Response Metadata

`Meta` represents contextual information attached to every response.

Typical use cases include:

* server timestamps
* sorting descriptors
* future extensibility points (trace identifiers, locale hints, etc.)

The metadata model is intentionally **minimal and extensible**.

---

### Pagination Contract

`Page<T>` provides a language-agnostic container for paged results.

It standardizes:

* page index
* page size
* total element count
* navigation flags

This library explicitly recognizes the common nested response shape:

```java
ServiceResponse<Page<T>>
```

Other nested generic combinations remain outside the contract’s guarantees.

This scoped approach helps maintain:

* deterministic schema modeling
* generator-safe evolution
* predictable client typing

---

### Sorting Descriptor

Sorting information is modeled as a simple value object:

```java
Sort(field, direction)
```

This avoids framework-specific pagination constructs while preserving intent.

---

### Error Extensions (RFC 9457)

The library provides structured extension types intended to be embedded
inside `application/problem+json` responses.

These include:

* `ProblemExtensions`
* `ErrorItem`

They are designed to be:

* forward-compatible
* transport-agnostic
* safe for cross-service reuse

The library does **not** implement HTTP error handling itself.

---

## 🚫 Explicit Non-Goals

`api-contract` deliberately does **not**:

* publish OpenAPI specifications
* implement HTTP controllers or filters
* perform validation logic
* generate client SDKs
* enforce transport-layer policies

Those responsibilities belong to higher-level modules or frameworks.

---

## 📦 Dependency

```xml
<dependency>
  <groupId>io.github.bsayli</groupId>
  <artifactId>api-contract</artifactId>
  <version>0.7.7</version>
</dependency>
```

Typical usage:

* **service modules** → compile scope
* **generated clients** → compile or provided scope

---

## 🔐 Versioning Strategy

This project is currently **pre-1.0**.

This implies:

* APIs may evolve
* binary compatibility is considered but not strictly guaranteed
* contract changes are intentional and documented

When upgrading, it is recommended to:

> Align server and client modules to the same contract version.

---

## 🧩 Intended Usage Contexts

This library is particularly useful in systems that must share a **single, stable response contract across server and generated clients**.

Especially when server and generated clients must share the same response semantics.

Typical scenarios include systems that:

* expose typed REST APIs
* rely on OpenAPI-driven client generation
* maintain multiple consumer services
* aim to minimize contract duplication
* treat response structure as a long-term architectural decision

It can also be adopted incrementally in existing codebases.

---

## 📜 License

MIT License.

This library defines **contracts, not runtime behavior**.
Once adopted, these contracts become part of your system’s public surface.

---

**Maintained by:**
**Barış Saylı**
[GitHub](https://github.com/bsayli) · [Medium](https://medium.com/@baris.sayli) · [LinkedIn](https://www.linkedin.com/in/bsayli)

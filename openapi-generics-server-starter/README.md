# openapi-generics-server-starter

> Deterministic OpenAPI projection for contract-first Spring Boot services

`openapi-generics-server-starter` is a **zero-configuration Spring Boot starter** that turns your Java API contract into a **stable, generics-aware OpenAPI specification**.

It does not generate a new model.
It **projects the existing contract** into OpenAPI — deterministically and without loss.

The role of this module is precise:

> Take the canonical contract and publish it as OpenAPI **without redefining it**.

---

## Table of Contents

1. [What Problem It Solves](#-what-problem-it-solves)
2. [What It Does (Automatically)](#-what-it-does-automatically)
3. [How It Works (High Level)](#-how-it-works-high-level)
4. [What You Write vs What You Get](#-what-you-write-vs-what-you-get)
5. [Compatibility Matrix](#-compatibility-matrix)
6. [Usage (Zero Configuration)](#-usage-zero-configuration)
7. [Supported Contract Shapes](#-supported-contract-shapes)
8. [What It Does NOT Do](#-what-it-does-not-do)
9. [Determinism Guarantees](#-determinism-guarantees)
10. [Failure Philosophy](#-failure-philosophy)
11. [Relationship to Other Modules](#-relationship-to-other-modules)
12. [When To Use](#-when-to-use)
13. [Further Reading](#-further-reading)
14. [Mental Model](#-mental-model)
15. [License](#-license)
---

## 🎯 What Problem It Solves

In typical OpenAPI setups:

* response envelopes are duplicated per endpoint
* generators reconstruct models inconsistently
* generic type information is flattened or lost
* client models drift from server contracts

This creates long-term instability:

* duplicated DTO hierarchies
* fragile client regeneration
* unclear contract ownership

This starter eliminates that class of problems by enforcing:

> **Contract → deterministic OpenAPI projection (no duplication, no drift)**

---

## 🧠 What It Does (Automatically)

Once added, the starter:

* detects `ServiceResponse<T>` return types
* resolves nested shapes like `ServiceResponse<Page<T>>`
* generates deterministic wrapper schemas
* injects vendor extensions required for code generation
* guarantees stable schema naming across builds

No annotations. No configuration. No manual schema editing.

---

## ⚙️ How It Works (High Level)

```
Java Contract (ServiceResponse<T>)
        ↓
Runtime Projection Pipeline (this module)
        ↓
OpenAPI (deterministic, lossless)
```

Key constraint:

> OpenAPI is NOT the source of truth — it is a projection of the Java contract

---

## ✍️ What You Write vs What You Get

### You write

```java
ServiceResponse<CustomerDto>
```

### OpenAPI gets

```
ServiceResponseCustomerDto
```

With:

* correct `allOf` composition
* canonical `{ data, meta }` structure
* vendor extensions for contract-aware code generation

No duplication of the envelope.
No loss of generic semantics.

---

## 🔧 Compatibility Matrix

This module is designed to work with the following baseline while remaining forward-compatible within the same major ecosystem.

| Component           | Supported Versions              |
|--------------------|--------------------------------|
| Java               | 17+                            |
| Spring Boot        | 3.4.x, 3.5.x                   |
| springdoc-openapi  | 2.8.x (WebMvc starter)         |
---

## 🚀 Usage (Zero Configuration)

### 1. Add dependency

```xml
<dependency>
  <groupId>io.github.blueprintplatform</groupId>
  <artifactId>openapi-generics-server-starter</artifactId>
  <version>0.8.x</version>
</dependency>
```

### 2. Use contract types

```java
@GetMapping
public ServiceResponse<CustomerDto> getCustomer() {
  return ServiceResponse.of(customer);
}
```

### 3. Done

OpenAPI is automatically enriched.

---

## 📦 Supported Contract Shapes

Supported:

* `ServiceResponse<T>`
* `ServiceResponse<Page<T>>`

Out of scope:

* arbitrary nested generics
* collections as root wrappers
* maps

Rationale:

> keep schema generation deterministic and predictable

---

## 🧱 What It Does NOT Do

This module intentionally does NOT:

* define API contracts (handled by `openapi-generics-contract`)
* generate clients (handled by codegen layer)
* require annotations or configuration
* modify runtime HTTP behavior

It only:

> transforms contract semantics into OpenAPI

---

## 🔒 Determinism Guarantees

The starter guarantees:

* ✔ Same input → same OpenAPI output
* ✔ Stable schema naming
* ✔ No ordering issues
* ✔ No runtime variability
* ✔ No partial schema mutation

Mechanisms:

* single projection pipeline
* authoritative schema derivation
* fail-fast validation

---

## ⚠️ Failure Philosophy

Invalid contract or inconsistent schema state results in:

```
IllegalStateException
```

No:

* silent fallback
* partial generation

Principle:

> Broken contract must fail, not degrade

---

## 🔗 Relationship to Other Modules

| Module                            | Role                               |
| --------------------------------- | ---------------------------------- |
| `openapi-generics-contract`       | Authority (defines response model) |
| `openapi-generics-server-starter` | Projection (this module)           |
| `openapi-generics-java-codegen`   | Client generation                  |

---

## 🧩 When To Use

Use this starter if:

* you return `ServiceResponse<T>` from controllers
* you use Springdoc for OpenAPI
* you generate clients from OpenAPI
* you want a single shared contract across services

---

## 📚 Further Reading

* **[Platform Architecture](../docs/architecture/platform.md)**
  End-to-end system model covering authority → projection → generation → consumption.

* **[Server Architecture](../docs/architecture/server.md)**
  Runtime projection pipeline transforming contract semantics into a deterministic OpenAPI specification.

* **[Client Architecture](../docs/architecture/client.md)**
  Build-time generation pipeline producing contract-aligned clients from the OpenAPI projection.

* **[Error Handling Strategy](../docs/architecture/error-handling.md)**
  RFC 9457-based canonical error model (runtime-first, protocol-driven, OpenAPI-independent).

---

## 🧠 Mental Model

Think of this module as:

> A deterministic compiler from Java contract → OpenAPI

Not:

* a helper library
* an annotation toolkit
* a best-effort enhancer

---

## 📜 License

MIT License

---

**Maintained by:**
**Barış Saylı**
[GitHub](https://github.com/bsayli) · [Medium](https://medium.com/@baris.sayli) · [LinkedIn](https://www.linkedin.com/in/bsayli)

# openapi-generics-server-starter

> Deterministic OpenAPI projection for contract-first Spring Boot services

`openapi-generics-server-starter` is a **zero-configuration Spring Boot starter** that transforms your Java API contract into a **stable, generics-aware OpenAPI specification**.

It works at runtime via Springdoc integration and guarantees that your OpenAPI remains a **lossless projection** of your contract.

---

## Table of Contents

1. [What Problem It Solves](#-what-problem-it-solves)
2. [What It Does (Automatically)](#-what-it-does-automatically)
3. [How It Works (High Level)](#-how-it-works-high-level)
4. [What You Write vs What You Get](#-what-you-write-vs-what-you-get)
5. [Usage (Zero Configuration)](#-usage-zero-configuration)
6. [Supported Contract Shapes](#-supported-contract-shapes)
7. [What It Does NOT Do](#-what-it-does-not-do)
8. [Determinism Guarantees](#-determinism-guarantees)
9. [Failure Philosophy](#-failure-philosophy)
10. [Relationship to Other Modules](#-relationship-to-other-modules)
11. [When To Use](#-when-to-use)
12. [Further Reading](#-further-reading)
13. [Mental Model](#-mental-model)
14. [License](#-license)

---

## 🎯 What Problem It Solves

In typical setups:

* response envelopes are duplicated across services
* OpenAPI generators re-create models inconsistently
* clients drift from server contracts

This starter eliminates those issues by enforcing:

> **Contract → deterministic OpenAPI projection (no duplication, no drift)**

---

## 🧠 What It Does (Automatically)

Once added, the starter:

* detects `ServiceResponse<T>` return types
* discovers controller response shapes
* generates deterministic wrapper schemas
* injects vendor extensions for code generation
* guarantees schema consistency across builds

No annotations. No configuration. No manual schema writing.

---

## ⚙️ How It Works (High Level)

```
Java Contract (ServiceResponse<T>)
        ↓
Runtime Pipeline (this starter)
        ↓
OpenAPI (deterministic projection)
```

Key idea:

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

* proper `allOf` composition
* metadata structure
* vendor extensions for client generation

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

* nested generics
* collections as root wrappers
* maps

Rationale:

> Keep schema generation deterministic and stable

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

* same input → same OpenAPI output
* no ordering issues
* no runtime variability
* no partial schema mutations

Mechanisms:

* single pipeline execution
* authoritative schema generation
* fail-fast validation

---

## ⚠️ Failure Philosophy

Invalid contract or schema state results in:

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
| `openapi-generics-contract`                    | Authority (defines response model) |
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

* Architecture: `openapi-generics-server — Architecture & Internals`
* Platform overview: `openapi-generics-platform — Architecture`
* Client generation: `openapi-generics-java-client — Architecture & Usage`

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
Barış Saylı
[https://github.com/bsayli](https://github.com/bsayli)

---
layout: default
title: Home
nav_order: 1
canonical_url: https://medium.com/@baris.sayli/type-safe-generic-api-responses-with-spring-boot-3-4-openapi-generator-and-custom-templates-ccd93405fb04
---

# Spring Boot OpenAPI Generics — Adoption Hub

> A **reference implementation** for **contract-driven, generics-aware OpenAPI client generation**
> built with Spring Boot, Springdoc, and OpenAPI Generator.

---

## Welcome 👋

This documentation describes a **domain-agnostic, single-contract approach** where
**both server and client** share the same canonical success envelope:

> **Canonical success envelope:** `ServiceResponse<T>`

* No duplicated envelopes
* No parallel client models
* No schema drift between server and client

The result is an **end-to-end, type-safe API boundary** with:

* deterministic OpenAPI output
* explicit, limited generic support
* **RFC 9457-compliant error handling**

---

## 📑 Table of Contents

- [What This Pattern Solves](#-what-this-pattern-solves)
- [Canonical Contract (Single Source of Truth)](#-canonical-contract-single-source-of-truth)
- [High-Level Architecture](#-highlevel-architecture)
- [Thin Wrapper Generation](#-thin-wrapper-generation-conceptual)
- [Error Handling (RFC 9457)](#-error-handling-rfc-9457-first)
- [Getting Started](#-getting-started-conceptual-flow)
- [Toolchain](#-toolchain-reference)
- [Adoption Guides](#-adoption-guides)
- [Design Principles & Outcomes](#-design-principles--outcomes)
- [References & External Links](#-references--external-links)

---


## 💡 What This Pattern Solves

Modern HTTP APIs almost always wrap responses:

* metadata (pagination, sorting, timestamps)
* data payloads
* standardized error structures

Yet most OpenAPI‑based workflows still suffer from fundamental issues:

* generics are flattened or erased
* envelopes are duplicated on the client
* nested containers produce unstable or ambiguous schemas
* client contracts silently diverge from server contracts

This pattern solves these problems by enforcing **one shared contract** and making OpenAPI generation **generics‑aware by design**, not by convention.

---

## 🧱 Canonical Contract (Single Source of Truth)

All **successful responses** — on **both server and client** — use the same envelope:

> `ServiceResponse<T>`

Provided by the shared module:

`io.github.bsayli:api-contract`

This module defines the **only** envelope, paging, and metadata types used across the system.

### Supported Shapes (Guaranteed vs. Default)

| Shape                       | Supported | Notes                                                       |
| --------------------------- | --------- | ----------------------------------------------------------- |
| `ServiceResponse<T>`        | ✅        | Canonical success envelope (explicitly guaranteed)          |
| `ServiceResponse<Page<T>>`  | ✅        | **Guaranteed nested generic**                               |
| `ServiceResponse<List<T>>`  | ⚠️        | Uses OpenAPI Generator default behavior (not part of contract) |
| `ServiceResponse<Map<K,V>>` | ⚠️        | Uses OpenAPI Generator default behavior                     |
| Arbitrary nested generics   | ❌        | Outside the canonical contract                              |

This approach **does not restrict or modify** OpenAPI Generator’s default handling
of standard Java collection types such as `List<T>` or `Map<K,V>`.

It defines **explicit guarantees only** for:
- `ServiceResponse<T>`
- `ServiceResponse<Page<T>>`

All other shapes are intentionally left **outside the canonical contract** to preserve
deterministic schema naming and generator-safe evolution across versions.

---

## 🧩 High‑Level Architecture

```
[any-service]
   └─ publishes OpenAPI 3.1 spec
        └─ enriched with wrapper semantics
              │
              ▼
[generated-client]
   └─ thin wrapper models
        └─ extend ServiceResponse<T>
              │
              ▼
[consumer applications]
   └─ depend only on adapters
```

### Core Principle

> **The OpenAPI specification describes contracts — not implementations.**

Wrapper semantics are expressed via vendor extensions, but **all concrete Java types** come from the shared `api-contract` module.

---

## 🧠 Thin Wrapper Generation (Conceptual)

The server publishes OpenAPI schemas enriched with semantic hints such as:

```
x-api-wrapper: true
x-api-wrapper-datatype: <DomainDto>
x-data-container: Page        # only for Page<T>
x-data-item: <DomainDto>      # only for Page<T>
```

The client build uses a minimal Mustache overlay to generate **thin wrapper classes** like:

```java
public class ServiceResponseFooDto
    extends ServiceResponse<FooDto> {}
```

```java
public class ServiceResponsePageFooDto
    extends ServiceResponse<Page<FooDto>> {}
```

* No envelope logic is duplicated
* No runtime reflection or adapters are required
* All behavior comes from the shared contract

---

## ⚠️ Error Handling (RFC 9457 First)

All non‑2xx responses are modeled as **RFC 9457 Problem Details** and surfaced to the client as a single exception type:

`ApiProblemException`

This exception:

* wraps the decoded `ProblemDetail`
* preserves the HTTP status
* carries full error context for logging and diagnostics

Client‑side error handling therefore mirrors Spring’s server‑side semantics.

---

## 🚀 Getting Started (Conceptual Flow)

1. **Server‑side** publishes a generics‑aware OpenAPI 3.1 contract
2. **Client‑side build** consumes that spec using template overlays
3. **Generated wrappers** extend `ServiceResponse<T>` from `api-contract`
4. **Consumers** interact only with stable adapters

Concrete setup steps are covered in the adoption guides below.

---

## 📦 Toolchain (Reference)

| Component         | Role                    |
|-------------------| ----------------------- |
| Java 21           | Language baseline       |
| Spring Boot 3.5.x | REST runtime            |
| Springdoc         | OpenAPI 3.1 producer    |
| OpenAPI Generator | Client code generation  |
| HttpClient5       | Production HTTP backend |

Exact versions are pinned in the respective adoption guides to ensure reproducibility.

---

## 📚 Adoption Guides

- **[Server-Side Adoption](adoption/server-side-adoption.md)** — Publish a deterministic, generics-aware OpenAPI 3.1 contract.
- **[Client-Side Adoption](adoption/client-side-adoption.md)** — Configure Maven + OpenAPI Generator + Mustache overlays.

Use these guides to integrate the pattern step‑by‑step:

* **Server‑Side Adoption**
  How to expose `ServiceResponse<T>` and publish a deterministic, generics‑aware OpenAPI contract.

* **Client‑Side Adoption — Build Setup**
  How to configure Maven, generator plugins, and Mustache overlays.

* **Client‑Side Adoption — Integration**
  How to consume the generated client safely using adapters and RFC 9457 handling.

Each guide is **domain-agnostic** and focuses on the integration approach rather than concrete domain examples.

---

## 🎯 Design Principles & Outcomes

This approach is built around a small set of **clear, intentional design outcomes**:

* A **single response contract** shared by server and client
* **No duplicated response envelopes** across generated code
* **Predictable schema naming** over time
* A clearly defined scope for generics (pagination via `Page<T>`)
* **RFC 9457–based error modeling** as a first-class concern
* Client generation that remains stable as the API evolves

This is not a tutorial example or a framework experiment.

It is a **reference implementation** for teams who care about
API clarity, long-term maintainability, and keeping server and client
contracts aligned without drift.

---

## 🔗 References & External Links

- 🌐 **GitHub Repository** — [spring-boot-openapi-generics-clients](https://github.com/bsayli/spring-boot-openapi-generics-clients)
- 📘 **Medium** — [We Made OpenAPI Generator Think in Generics](https://medium.com/@baris.sayli/type-safe-generic-api-responses-with-spring-boot-3-4-openapi-generator-and-custom-templates-ccd93405fb04)

---

🛡 Licensed under **MIT**. All modules inherit the same license.

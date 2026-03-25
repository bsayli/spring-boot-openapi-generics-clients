---
layout: default
title: Home
nav_order: 1
---

# Spring Boot OpenAPI Generics — Architecture Adoption Hub

> A **reference architecture adoption guide** for building **contract‑driven, generics‑aware API boundaries**
> using Spring Boot, Springdoc, and OpenAPI Generator.

---

## Welcome 👋

This documentation introduces a **contract lifecycle approach** for designing and evolving HTTP API boundaries where
**server and client share a single canonical success envelope**:

> **Canonical success envelope:** `ServiceResponse<T>`

Rather than treating OpenAPI generation as a tooling detail, this approach treats it as an **architectural concern**.

It enables teams to:

* preserve response contract identity across service boundaries
* avoid duplicated envelope DTO hierarchies
* maintain deterministic client generation behaviour over time
* evolve APIs safely without silent schema drift

The outcome is an **end‑to‑end, type‑safe integration boundary** supported by:

* deterministic OpenAPI publication
* explicit and intentionally limited generic scope
* **RFC 9457‑aligned error semantics**

This hub serves as the **architectural onboarding entry point** for adopting the pattern.

---

## 📑 Table of Contents

* [What This Architecture Solves](#-what-this-architecture-solves)
* [Canonical Contract (Single Source of Truth)](#-canonical-contract-single-source-of-truth)
* [Contract Lifecycle Overview](#-contract-lifecycle-overview)
* [Semantic Wrapper Generation Model](#-semantic-wrapper-generation-model)
* [Error Handling (RFC 9457)](#-error-handling-rfc-9457-first)
* [Adoption Flow (Conceptual)](#-adoption-flow-conceptual)
* [Toolchain Roles](#-toolchain-roles)
* [Adoption Guides](#-adoption-guides)
* [Design Principles & Architectural Outcomes](#-design-principles--architectural-outcomes)
* [References & External Links](#-references--external-links)

---

## 💡 What This Architecture Solves

In mature distributed systems, HTTP responses are rarely raw payloads.
They typically include:

* metadata (pagination, sorting, timestamps)
* structured domain data
* standardized error representations

However, conventional OpenAPI‑driven workflows often introduce architectural friction:

* generic envelope intent is flattened or erased
* client generators duplicate response structures per endpoint
* nested container semantics become ambiguous
* schema naming drifts across specification evolution
* generated clients diverge from runtime server contracts

Over time, these issues lead to:

* brittle integrations
* excessive client boilerplate
* reduced type safety
* unclear contract ownership

This architecture addresses these challenges by establishing **one shared runtime contract** and ensuring that
OpenAPI acts as a **semantic projection layer**, not as the source of truth.

---

## 🧱 Canonical Contract (Single Source of Truth)

All successful responses — on **both producer and consumer sides** — use the same envelope abstraction:

> `ServiceResponse<T>`

Provided by the shared module:

`io.github.bsayli:api-contract:0.7.7`

This artifact defines the **only authoritative response envelope, paging primitives, and metadata model**.

### Supported Shapes (Guaranteed vs Default)

| Shape                       | Supported | Notes                                                 |
| --------------------------- | --------- | ----------------------------------------------------- |
| `ServiceResponse<T>`        | ✅         | Canonical success envelope (explicit guarantee)       |
| `ServiceResponse<Page<T>>`  | ✅         | Deterministic nested generic support                  |
| `ServiceResponse<List<T>>`  | ⚠️        | Generator default behaviour (outside canonical scope) |
| `ServiceResponse<Map<K,V>>` | ⚠️        | Generator default behaviour                           |
| Arbitrary nested generics   | ❌         | Intentionally unsupported                             |

The design goal is **predictable long‑term schema evolution**, not exhaustive generic modelling.

---

## 🧩 Contract Lifecycle Overview

```
[Canonical Contract Definition]
        ↓
[Deterministic OpenAPI Publication]
        ↓
[Semantics‑Aware Client Generation]
        ↓
[Adapter‑Bound Application Consumption]
```

Each stage reinforces **contract identity preservation** and limits the surface area where accidental divergence can occur.

Key principle:

> The OpenAPI specification expresses **contract semantics**, while the shared contract module defines **runtime truth**.

---

## 🧠 Semantic Wrapper Generation Model

The server publishes wrapper schemas enriched with semantic metadata such as:

```
x-api-wrapper: true
x-api-wrapper-datatype: <DomainDto>
x-data-container: Page
x-data-item: <DomainDto>
```

Client generation templates interpret these hints to emit **thin inheritance‑based wrapper classes**:

```java
public class ServiceResponseFooDto
    extends ServiceResponse<FooDto> {}
```

```java
public class ServiceResponsePageFooDto
    extends ServiceResponse<Page<FooDto>> {}
```

This approach ensures:

* envelope logic is never duplicated
* nested generic semantics remain intact
* runtime serialization behaviour stays symmetric

---

## ⚠️ Error Handling (RFC 9457 First)

Non‑2xx responses follow **RFC 9457 Problem Details semantics** and are surfaced to consumers as:

`ApiProblemException`

This guarantees:

* structured error propagation
* consistent diagnostic context
* symmetry between Spring server behaviour and generated client runtime

---

## 🚀 Adoption Flow (Conceptual)

1. A **producer service** publishes a generics‑aware deterministic OpenAPI contract.
2. A **consumer build pipeline** interprets wrapper semantics via controlled template overlays.
3. Generated wrappers bind directly to the shared canonical contract artifact.
4. Application code interacts only with **stable adapter interfaces**.

This separation keeps **generation concerns inside the build boundary**, protecting domain logic from tooling churn.

---

## 📦 Toolchain Roles

| Component          | Architectural Role               |
| ------------------ | -------------------------------- |
| Java 21            | Language baseline                |
| Spring Boot 3.5.x  | Contract runtime & serialization |
| Springdoc          | Deterministic OpenAPI projection |
| OpenAPI Generator  | Governed client code emission    |
| Apache HttpClient5 | Production‑grade HTTP transport  |

Exact versions are pinned within individual adoption guides to ensure reproducible setups.

---

## 📚 Adoption Guides

* **[Server-Side Adoption](adoption/server-side-adoption.md)** — Publish a deterministic, generics‑aware OpenAPI contract.
* **[Client-Side Adoption](adoption/client-side-adoption.md)** — Integrate a generics‑aware client using shared contract semantics.

  * **[Client-Side Build Setup](adoption/client-side-adoption-pom.md)** — Configure Maven, generator templates, and contract bindings.

Each guide focuses on **architectural integration steps**, remaining domain‑agnostic and tooling‑explicit.

---

## 🎯 Design Principles & Architectural Outcomes

This pattern is built around a small set of intentional engineering outcomes:

* a single canonical response contract shared across boundaries
* elimination of duplicated envelope DTO hierarchies
* deterministic schema naming and generator stability
* explicitly scoped nested generic support (`Page<T>`)
* first‑class structured error modelling via RFC 9457
* regeneration‑safe client integration through adapter isolation

The result is a **clear, evolvable API boundary architecture** suitable for long‑lived service ecosystems.

---

## 🔗 References & External Links

* 🌐 **GitHub Repository** — [spring-boot-openapi-generics-clients](https://github.com/bsayli/spring-boot-openapi-generics-clients)
* 📘 **Medium** — [We Made OpenAPI Generator Think in Generics](https://medium.com/@baris.sayli/type-safe-generic-api-responses-with-spring-boot-3-4-openapi-generator-and-custom-templates-ccd93405fb04)

---

🛡 Licensed under **MIT**. All modules inherit the same license.

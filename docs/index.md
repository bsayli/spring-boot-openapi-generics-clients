---
layout: default
title: Home
nav_order: 1
has_toc: true
canonical_url: https://medium.com/@baris.sayli/type-safe-generic-api-responses-with-spring-boot-3-4-openapi-generator-and-custom-templates-ccd93405fb04
---

# Spring Boot OpenAPI Generics — Adoption Hub

> A production‑grade blueprint for **contract‑driven, generics‑aware API boundaries** built with Spring Boot, Springdoc, and OpenAPI Generator.

Welcome 👋
This documentation describes a **domain‑agnostic, single‑contract architecture** where **both server and client** share the same canonical success envelope:

> **Canonical success envelope:** `ServiceResponse<T>`

<div class="callout">

**No duplicated envelopes**  
**No parallel client contracts**  
**No schema drift**

</div>

The result is an **end‑to‑end type‑safe API boundary** with deterministic OpenAPI output, explicit generic rules, and **RFC 9457‑compliant error handling**.

---

## 💡 What This Blueprint Solves

Modern HTTP APIs almost always wrap responses:

* metadata (pagination, sorting, timestamps)
* data payloads
* standardized error structures

Yet most OpenAPI‑based workflows still suffer from fundamental issues:

* generics are flattened or erased
* envelopes are duplicated on the client
* nested containers produce unstable or ambiguous schemas
* client contracts silently diverge from server contracts

This blueprint solves these problems by enforcing **one shared contract** and making OpenAPI generation **generics‑aware by design**, not by convention.

---

## 🧱 Canonical Contract (Single Source of Truth)

All **successful responses** — on **both server and client** — use the same envelope:

> `ServiceResponse<T>`

Provided by the shared module:

`io.github.bsayli:api-contract`

This module defines the **only** envelope, paging, and metadata types used across the system.

### Supported Shapes (Intentional Constraints)

| Shape                       | Supported | Notes                                  |
| --------------------------- | --------- | -------------------------------------- |
| `ServiceResponse<T>`        | ✅         | Canonical success envelope             |
| `ServiceResponse<Page<T>>`  | ✅         | **Only allowed nested generic**        |
| `ServiceResponse<List<T>>`  | ❌         | Treated as raw type (generics ignored) |
| `ServiceResponse<Map<K,V>>` | ❌         | Not supported                          |
| Arbitrary nested generics   | ❌         | Explicitly rejected                    |

These constraints are **deliberate**.
They guarantee deterministic schema names and stable client generation across versions.

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

## ⚠️ Error Handling (RFC 9457‑First)

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

## 📚 Learn More

* [Server-Side Adoption](adoption/server-side-adoption.md)
* [Client-Side Adoption](adoption/client-side-adoption.md)

---

## 📦 Toolchain (Reference)

| Component         | Role                    |
| ----------------- | ----------------------- |
| Java 21           | Language baseline       |
| Spring Boot 3.x   | REST runtime            |
| Springdoc         | OpenAPI 3.1 producer    |
| OpenAPI Generator | Client code generation  |
| HttpClient5       | Production HTTP backend |

Exact versions are pinned in the respective adoption guides to ensure reproducibility.

---

## 📚 Adoption Guides

Use these guides to integrate the blueprint step‑by‑step:

* **Server‑Side Adoption**
  How to expose `ServiceResponse<T>` and publish a deterministic, generics‑aware OpenAPI contract.

* **Client‑Side Adoption — Build Setup**
  How to configure Maven, generator plugins, and Mustache overlays.

* **Client‑Side Adoption — Integration**
  How to consume the generated client safely using adapters and RFC 9457 handling.

Each guide is **domain‑agnostic** and focuses on architectural rules rather than examples.

---

## 🎯 Design Guarantees

This blueprint guarantees:

* **One response contract** across server and client
* **Zero duplicated envelopes**
* **Deterministic schema names**
* **Explicit generic rules (Page‑only nesting)**
* **RFC 9457‑first error handling**
* **Generator‑safe evolution over time**

This is not a tutorial demo.

It is a **reference architecture** for teams who care about API correctness, long‑term maintainability, and zero contract drift.

---

## 🔗 References & External Links

<div class="callout learn-more">
  <ul>
    <li>🌐 <a href="https://github.com/bsayli/spring-boot-openapi-generics-clients" target="_blank" rel="noopener">GitHub Repository</a></li>
    <li>📘 <a href="https://medium.com/@baris.sayli/type-safe-generic-api-responses-with-spring-boot-3-4-openapi-generator-and-custom-templates-ccd93405fb04" target="_blank" rel="noopener">Medium — We Made OpenAPI Generator Think in Generics</a></li>
  </ul>
</div>

---

🛡 Licensed under **MIT**. All modules inherit the same license.

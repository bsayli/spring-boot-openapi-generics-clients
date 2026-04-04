---
layout: default
title: Server-Side Adoption
parent: Adoption Guides
nav_order: 1
---

# Server-Side Adoption — Contract-First OpenAPI Publication

> Publish a **deterministic, generics-aware OpenAPI** from Spring Boot with **one contract and zero duplication**.

A **practical, minimal, and deterministic** guide for exposing a **contract-aligned OpenAPI** from a Spring Boot (WebMVC) service.

This guide is intentionally **action-oriented**: you implement a small set of rules, and the platform guarantees the rest.

---

## 📑 Table of Contents

* [⚡ 60-second quick start](#-60-second-quick-start)
* [🎯 What the server is responsible for](#-what-the-server-is-responsible-for)
* [🧩 The only rule that matters](#-the-only-rule-that-matters)
* [📦 Minimal dependencies](#-minimal-dependencies)
* [✍️ What you actually write](#-what-you-actually-write)
* [🧠 What gets published to OpenAPI](#-what-gets-published-to-openapi)
* [⚠️ Rules (do NOT break these)](#-rules-do-not-break-these)
* [🔍 Quick verification](#-quick-verification)
* [🧠 Mental model](#-mental-model)
* [🚫 What this guide does NOT cover](#-what-this-guide-does-not-cover)
* [🧾 Summary](#-summary)

---

## ⚡ 60-second quick start

You want:

* deterministic OpenAPI output
* no envelope duplication
* generics preserved in generated clients

Do this:

### 1) Add dependency

```xml
<dependency>
  <groupId>io.github.blueprintplatform</groupId>
  <artifactId>openapi-generics-server-starter</artifactId>
</dependency>
```

### 2) Return contract types

```java
ServiceResponse<CustomerDto>
ServiceResponse<Page<CustomerDto>>
```

### 3) Expose OpenAPI

```text
/v3/api-docs.yaml
```

Done.

---

## 🎯 What the server is responsible for

The server has **exactly one responsibility**:

> Publish a **correct, deterministic projection** of the runtime contract.

It does **not**:

* generate clients
* define alternative response models
* adapt for specific generators

It only performs:

```text
Contract → OpenAPI (projection)
```

Everything else (generation, typing, reuse) happens downstream.

---

## 🧩 The only rule that matters

There is **one canonical success envelope**:

```text
ServiceResponse<T>
```

Supported shapes:

```text
ServiceResponse<T>
ServiceResponse<Page<T>>
```

This constraint is what enables:

* deterministic schema generation
* stable naming
* type-safe client reconstruction

---

## 📦 Minimal dependencies

No custom configuration is required.

```xml
<dependency>
  <groupId>io.github.blueprintplatform</groupId>
  <artifactId>openapi-generics-server-starter</artifactId>
</dependency>
```

Assumes:

* Spring Boot (WebMVC)
* Springdoc enabled (default `/v3/api-docs`)

---

## ✍️ What you actually write

You write **only your domain contract**.

### Controller example

```java
@GetMapping("/{id}")
public ResponseEntity<ServiceResponse<CustomerDto>> getCustomer(...) {
  return ResponseEntity.ok(ServiceResponse.of(dto));
}
```

### Pagination example

```java
@GetMapping
public ResponseEntity<ServiceResponse<Page<CustomerDto>>> getCustomers(...) {
  return ResponseEntity.ok(ServiceResponse.of(page));
}
```

That’s it.

No annotations.
No schema configuration.
No wrapper DTOs.

---

## 🧠 What gets published to OpenAPI

From this runtime type:

```java
ServiceResponse<CustomerDto>
```

The system produces a deterministic schema:

```text
ServiceResponseCustomerDto
```

Characteristics:

* stable, predictable naming
* `allOf`-based composition
* vendor extensions for downstream generation (e.g. `x-api-wrapper`)

Important:

> OpenAPI is a **projection artifact** — not the source of truth.

---

## ⚠️ Rules (do NOT break these)

These are **architectural constraints**, not conventions.

### 1. Only constrain the envelope

```text
ServiceResponse<T>
ServiceResponse<Page<T>>
```

---

### 2. Do NOT replace the envelope

❌ Wrong:

```text
CustomerResponse
ApiResponse
PagedResult
```

Replacing the envelope breaks cross-layer consistency and determinism.

---

### 3. Payload is completely free

✔ Valid:

```text
ServiceResponse<CustomerDto>
ServiceResponse<CustomerDeleteResponse>
ServiceResponse<Anything>
```

The system constrains structure — not domain models.

---

### 4. Errors are NOT wrapped

```text
ProblemDetail (RFC 9457)
```

Errors are handled as a protocol, separate from success responses.

---

### 5. Do NOT customize OpenAPI

No:

* manual schemas
* custom annotations
* overrides

The starter owns the projection.

---

## 🔍 Quick verification

Run a request:

```bash
curl http://localhost:8084/.../v1/.../1
```

Expected shape:

```json
{
  "data": { ... },
  "meta": { ... }
}
```

If this is correct, then:

```text
Server → OpenAPI → Client will remain consistent
```

---

## 🧠 Mental model

Think of the server as:

> A deterministic compiler from runtime contract → OpenAPI

Not:

* a schema designer
* a generator configuration layer

---

## 🚫 What this guide does NOT cover

This guide intentionally excludes:

* client generation
* template customization
* generator internals

These belong to the **client-side adoption guide**.

---

## 🧾 Summary

If you remember only this:

```text
Return ServiceResponse<T>
Add the starter
Do nothing else
```

The platform handles:

* OpenAPI projection
* schema stability
* downstream compatibility

---

🛡 MIT License

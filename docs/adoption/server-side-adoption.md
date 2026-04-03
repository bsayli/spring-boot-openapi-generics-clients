---
layout: default
title: Server-Side Adoption
parent: Adoption Guides
nav_order: 1
---

# Server-Side Adoption — Contract-First OpenAPI Publication

> A **practical, minimal, and deterministic** guide for publishing a **generics-aware OpenAPI** from a Spring Boot (WebMVC) service.

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

* deterministic OpenAPI
* no envelope duplication
* generics preserved in clients

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

The server has **one job only**:

> Publish a **correct, deterministic projection** of the runtime contract.

It does NOT:

* generate clients
* define alternative response models
* optimize for generators

It only:

```text
Contract → OpenAPI (projection)
```

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

Everything else is built around this.

---

## 📦 Minimal dependencies

You do NOT need complex setup.

Only:

```xml
<dependency>
  <groupId>io.github.blueprintplatform</groupId>
  <artifactId>openapi-generics-server-starter</artifactId>
</dependency>
```

Assumes:

* Spring Boot (WebMVC)
* Springdoc enabled

---

## ✍️ What you actually write

### Controller example

```java
@GetMapping("/{id}")
public ResponseEntity<ServiceResponse<CustomerDto>> getCustomer(...) {
  return ResponseEntity.ok(ServiceResponse.of(dto));
}
```

### Pagination

```java
@GetMapping
public ResponseEntity<ServiceResponse<Page<CustomerDto>>> getCustomers(...) {
  return ResponseEntity.ok(ServiceResponse.of(page));
}
```

That’s it.

No annotations.
No schema config.
No wrappers.

---

## 🧠 What gets published to OpenAPI

From this:

```java
ServiceResponse<CustomerDto>
```

You get:

```text
ServiceResponseCustomerDto
```

With:

* deterministic naming
* `allOf` composition
* vendor extensions for codegen

Important:

> OpenAPI is a projection — not the source of truth.

---

## ⚠️ Rules (do NOT break these)

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

---

### 3. Payload is completely free

✔ Valid:

```text
ServiceResponse<CustomerDto>
ServiceResponse<CustomerDeleteResponse>
ServiceResponse<Anything>
```

The platform does NOT care about `T`.

---

### 4. Errors are NOT wrapped

```text
ProblemDetail (RFC 9457)
```

---

### 5. Do NOT customize OpenAPI

No:

* manual schemas
* custom annotations
* overrides

Everything is handled by the starter.

---

## 🔍 Quick verification

Run:

```bash
curl http://localhost:8084/.../v1/.../1
```

Expected:

```json
{
  "data": { ... },
  "meta": { ... }
}
```

If this shape is correct:

```text
Server → OpenAPI → Client will be correct
```

---

## 🧠 Mental model

Think of the server as:

> A compiler from runtime contract → OpenAPI

NOT:

* a schema designer
* a generator config layer

---

## 🚫 What this guide does NOT cover

This guide intentionally does NOT cover:

* client generation
* template customization
* generator internals

Those belong to the **client-side adoption**.

---

## 🧾 Summary

If you remember only this:

```text
Return ServiceResponse<T>
Add the starter
Do nothing else
```

Everything else is handled by the platform.

---

🛡 MIT License

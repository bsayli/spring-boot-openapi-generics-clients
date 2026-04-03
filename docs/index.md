---
layout: default
title: Home
nav_order: 1
---

# Spring Boot OpenAPI Generics — Architecture Adoption Hub

> A **practical, onboarding-first guide** for building **contract-driven, generics-aware API boundaries**
> using Spring Boot, Springdoc, and OpenAPI Generator.

---

## 📑 Table of Contents

- [⚡ 60-second quick start (do this first)](#-60-second-quick-start-do-this-first)
- [⚠️ Rules (do NOT break these)](#-rules-do-not-break-these)
    - [1. Only constrain the envelope](#1-only-constrain-the-envelope)
    - [2. Do NOT replace the envelope](#2-do-not-replace-the-envelope)
    - [3. Payload is completely free](#3-payload-is-completely-free)
    - [4. Errors are NOT wrapped](#4-errors-are-not-wrapped)
    - [5. Do NOT customize OpenAPI](#5-do-not-customize-openapi)
- [🧠 Core idea (don’t skip this)](#-core-idea)
- [💡 What this architecture solves](#-what-this-architecture-solves)
- [✅ What you get](#-what-you-get)
- [🔄 Full lifecycle](#-full-lifecycle)
- [🧠 Mental model](#-mental-model)
- [⚙️ How it works (minimal)](#-how-it-works-minimal)
    - [Server](#server)
    - [Codegen](#codegen)
    - [Client](#client)
- [🚀 Adoption flow](#-adoption-flow)
    - [Producer](#producer)
    - [Consumer](#consumer)
    - [Application](#application)
- [📦 Toolchain roles](#-toolchain-roles)
- [🚫 What this is NOT](#-what-this-is-not)
- [📚 Next steps](#-next-steps)
- [🔗 References & External Links](#-references--external-links)
- [🛡 License](#-license)

## ⚡ 60-second quick start (do this first)

You want:

* clean OpenAPI
* type-safe client
* preserved `ServiceResponse<T>`

Do this:

### 1. Server

* return `ServiceResponse<T>` from controllers
* add:

```xml
<dependency>
  <groupId>io.github.blueprintplatform</groupId>
  <artifactId>openapi-generics-server-starter</artifactId>
</dependency>
```

---

### 2. Generate client

* use `openapi-generics-java-codegen-parent`
* point to `/v3/api-docs.yaml`

---

### 3. Use it

```java
ServiceResponse<CustomerDto>
```

Done.

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
ServiceResponse<OrderResult>
ServiceResponse<Anything>
```

---

### 4. Errors are NOT wrapped

```text
ProblemDetail (RFC 9457)
```

---

### 5. Do NOT customize OpenAPI

No annotations.
No schema hacks.

---

## 🧠 Core idea

There is only **one source of truth**:

```text
ServiceResponse<T>
```

Defined in:

```text
openapi-generics-contract
```

Everything else is:

* projection (OpenAPI)
* transformation (codegen)
* consumption (client)

👉 OpenAPI is NOT the contract
👉 Generated models are NOT the contract

---

## 💡 What this architecture solves

Without this approach:

* generics are flattened
* clients duplicate envelopes
* pagination becomes inconsistent
* schema names drift
* server/client diverge

Result:

```text
fragile integrations + broken type safety
```

---

## ✅ What you get

If you follow the rules:

* one shared response model across all layers
* zero envelope duplication
* deterministic OpenAPI
* stable client generation
* preserved generics

---

## 🔄 Full lifecycle

```text
[openapi-generics-contract]              ← authority
        ↓
[server]                    ← returns ServiceResponse<T>
        ↓
[OpenAPI]                   ← projection (NOT truth)
        ↓
[codegen parent]            ← controlled generation
        ↓
[generated client]          ← thin wrappers
        ↓
[adapter]                   ← safe usage
```

---

## 🧠 Mental model

Think of this as:

> A controlled compiler pipeline for API contracts

NOT:

* a Spring trick
* a generator tweak

---

## ⚙️ How it works (minimal)

### Server

```java
return ServiceResponse.of(customerDto);
```

---

### Codegen

Reads:

```text
x-api-wrapper
```

Produces:

```java
class ServiceResponseCustomerDto extends ServiceResponse<CustomerDto>
```

---

### Client

Uses:

```text
ServiceResponse<CustomerDto>
```

---

## 🚀 Adoption flow

### Producer

* return `ServiceResponse<T>`
* expose `/v3/api-docs.yaml`

---

### Consumer

* generate client using parent

---

### Application

* use adapter layer

---

## 📦 Toolchain roles

| Component      | Responsibility           |
| -------------- | ------------------------ |
| openapi-generics-contract   | defines truth            |
| server-starter | OpenAPI projection       |
| codegen-parent | generation orchestration |
| client module  | consumption              |

---

## 🚫 What this is NOT

* not a flexible generator setup
* not a DTO pattern
* not a framework

It is:

> A strict contract pipeline

---

## 📚 Next steps

* **[Server-Side Adoption](adoption/server-side-adoption.md)** — Publish a deterministic, generics‑aware OpenAPI contract.
* **[Client-Side Adoption](adoption/client-side-adoption.md)** — Integrate a generics‑aware client using shared contract semantics.

Each guide focuses on **architectural integration steps**, remaining domain‑agnostic and tooling‑explicit.

---

## 🔗 References & External Links

* 🌐 **GitHub Repository** — [openapi-generics](https://github.com/blueprint-platform/openapi-generics)
* 📘 **Medium** — [We Made OpenAPI Generator Think in Generics](https://medium.com/@baris.sayli/type-safe-generic-api-responses-with-spring-boot-3-4-openapi-generator-and-custom-templates-ccd93405fb04)

## 🛡 License

MIT License
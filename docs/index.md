---
layout: default
title: Home
nav_order: 1
---

# Spring Boot OpenAPI Generics — Architecture Adoption Hub

> Build APIs where generics survive end-to-end —  
> from Spring Boot to OpenAPI to generated clients — without duplication or drift.

A **practical, onboarding-first guide** for building **contract-driven, generics-aware API boundaries**
using Spring Boot, Springdoc, and OpenAPI Generator.

This guide is intentionally **action-oriented**.  
It shows how to adopt the architecture without understanding all internals first —  
while still preserving the underlying model, constraints, and guarantees.

---

## 📑 Table of Contents

* [⚡ Quick Start](#-60-second-quick-start-do-this-first)
* [⚠️ Rules (do NOT break these)](#-rules-do-not-break-these)
* [🧠 Core Idea](#-core-idea)
* [💡 What This Solves](#-what-this-architecture-solves)
* [✅ What You Get](#-what-you-get)
* [🔄 Lifecycle](#-full-lifecycle)
* [⚙️ How It Works](#-how-it-works-minimal)
  * [Server](#server-projection-layer)
  * [Codegen](#codegen-generation-layer)
  * [Client](#client-consumption-layer)
* [🚀 Adoption Flow](#-adoption-flow)
  * [Producer](#1-producer-server)
  * [Consumer](#2-consumer-client-generation)
  * [Application](#3-application-usage)
* [📦 Toolchain Roles](#-toolchain-roles)
* [🧩 Compatibility Matrix](#-compatibility-matrix)
* [🚫 What This Is NOT](#-what-this-is-not)
* [📚 Next Steps](#-next-steps)

---

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
* generate from `/v3/api-docs.yaml` via OpenAPI Generator

---

### 3. Use it

```java
ServiceResponse<CustomerDto>
```

Done.

---

## ⚠️ Rules (do NOT break these)

These are not conventions — they are **architectural constraints**.
Breaking them leads to drift between contract, OpenAPI, and client.

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

Replacing the envelope breaks cross-layer consistency.

---

### 3. Payload is completely free

✔ Valid:

```text
ServiceResponse<CustomerDto>
ServiceResponse<OrderResult>
ServiceResponse<Anything>
```

The system constrains structure — not domain models.

---

### 4. Errors are NOT wrapped

```text
ProblemDetail (RFC 9457)
```

Errors are handled as a protocol, not part of the success envelope.

---

### 5. Do NOT customize OpenAPI

No annotations.
No schema overrides.
No manual intervention.

OpenAPI is a **projection layer**, not a customization surface.

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

Everything else is derived:

* projection (OpenAPI)
* transformation (codegen)
* consumption (client)

👉 OpenAPI is NOT the contract
👉 Generated models are NOT the contract

---

## 💡 What this architecture solves

Without this approach, generics do not survive the toolchain:

```text
OpenAPI:
  ServiceResponse<T> → flattened → lost

Client:
  regenerated → duplicated → inconsistent

Pagination:
  implicit → divergent → unstable
```

This leads to:

* envelope duplication across services and clients
* inconsistent pagination models
* unstable schema naming
* silent drift between server and client contracts

Result:

```text
compile-time illusion, runtime mismatch
```

The system may appear type-safe, but semantics are no longer aligned.

---

## ✅ What you get

If you follow the rules:

* one shared response model across all layers
* zero envelope duplication
* deterministic OpenAPI output
* stable client generation
* preserved generics in generated code

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

* a Spring feature
* a generator tweak
* a documentation trick

---

## ⚙️ How it works (minimal)

This works because generics are not inferred from OpenAPI,
but explicitly reconstructed from contract metadata.

This system is a **contract-driven pipeline**.
Each layer transforms structure, but **none redefine semantics**.

---

### Server (Projection Layer)

Add the starter:

```xml
<dependency>
  <groupId>io.github.blueprintplatform</groupId>
  <artifactId>openapi-generics-server-starter</artifactId>
</dependency>
```

Write only your contract:

```java
return ServiceResponse.of(customerDto);
```

At runtime:

* `ServiceResponse<T>` is discovered from controller signatures
* OpenAPI is generated as a **deterministic projection**
* vendor extensions (e.g. `x-api-wrapper`) are injected

Important:

> OpenAPI does NOT define your model — it carries metadata about your contract

---

### Codegen (Generation Layer)

Use the parent:

```xml
<parent>
  <groupId>io.github.blueprintplatform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
</parent>
```

Minimal plugin:

```xml
<plugin>
  <groupId>org.openapitools</groupId>
  <artifactId>openapi-generator-maven-plugin</artifactId>

  <executions>
    <execution>
      <goals>
        <goal>generate</goal>
      </goals>

      <configuration>
        <inputSpec>path/to/openapi.yaml</inputSpec>
      </configuration>

    </execution>
  </executions>
</plugin>
```

What happens internally:

* OpenAPI is treated as **structured metadata**, not source of truth
* `x-api-wrapper` drives wrapper generation
* contract models are **NOT generated**

Output:

```java
class ServiceResponseCustomerDto extends ServiceResponse<CustomerDto>
```

---

### Client (Consumption Layer)

Use the generated client:

```java
ServiceResponse<CustomerDto>
```

Result:

* generics preserved end-to-end
* no duplicated envelope classes
* consistent contract semantics

---

## 🚀 Adoption flow

### 1. Producer (Server)

* return `ServiceResponse<T>`
* add server starter
* expose `/v3/api-docs.yaml`

---

### 2. Consumer (Client Generation)

* inherit codegen parent
* generate client from OpenAPI

---

### 3. Application (Usage)

* call generated client
* optionally introduce adapter layer

Adapter purpose:

* isolate transport layer
* map API responses to domain logic

---

### Key Insight

> The contract lives in Java.
> OpenAPI only transports structure.
> Codegen reconstructs types — it does not invent them.

---

## 📦 Toolchain roles

| Component                            | Responsibility                                                                 |
| ------------------------------------ | ------------------------------------------------------------------------------ |
| openapi-generics-contract            | **Authority (SSOT)** — defines `ServiceResponse<T>` and core response semantics |
| openapi-generics-server-starter      | **Projection** — transforms contract → deterministic OpenAPI (runtime)         |
| OpenAPI                              | **Transport format** — carries structural metadata (NOT authority, NOT source of truth) |
| openapi-generics-java-codegen        | **Enforcement** — interprets OpenAPI + extensions, suppresses model duplication |
| openapi-generics-java-codegen-parent | **Orchestration** — wires generator, templates, and deterministic build pipeline |
| Generated client                     | **Typed projection** — contract-aligned wrappers used by application code      |

---

---

---

## 🧩 Compatibility Matrix

This platform is designed to work within a **controlled and tested environment**.

| Component            | Supported Version     |
|---------------------|----------------------|
| Java                | 21                   |
| Spring Boot         | 3.4.x, 3.5.x         |
| Springdoc           | 2.8.x                |
| OpenAPI Generator   | 7.x                  |

---

### Notes

* `restclient` support requires **OpenAPI Generator 7.6.0+**
* The platform is tested across the **OpenAPI Generator 7.x series**
* Generator version can be overridden, but must remain compatible with template structure

---

### Important

> This system relies on **controlled generator behavior and template structure**.

Template integration is intentionally explicit and validated at build time.

If upstream changes affect the structure:

* the build will fail
* generation must be revalidated

This is by design — ensuring correctness and preventing silent drift.

---

## 🚫 What this is NOT

* not a flexible generator configuration
* not a DTO pattern library
* not a framework abstraction

It is:

> A strict, deterministic contract pipeline

---

## 📚 Next steps

* **[Server-Side Adoption](adoption/server-side-adoption.md)** — Publish a deterministic, generics‑aware OpenAPI contract.
* **[Client-Side Adoption](adoption/client-side-adoption.md)** — Integrate a generics‑aware client using shared contract semantics.

Each guide focuses on **integration steps**, not theory.

---

## 🔗 References & External Links

* 🌐 **GitHub Repository** — [openapi-generics](https://github.com/blueprint-platform/openapi-generics)
* 📘 **Medium** — [We Made OpenAPI Generator Think in Generics](https://medium.com/@baris.sayli/type-safe-generic-api-responses-with-spring-boot-3-4-openapi-generator-and-custom-templates-ccd93405fb04)

---

## 🛡 License

MIT License

---

## 🧭 Final note

If you remove any constraint in this system,  
you reintroduce drift.

This architecture works because it is intentionally strict.
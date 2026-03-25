---
layout: default
title: Server-Side Adoption
parent: Adoption Guides
nav_order: 1
---


# Server‑Side Adoption — Contract Lifecycle Publication Stage

This guide explains how a Spring MVC service participates in the **contract lifecycle architecture** by publishing a **deterministic, semantics‑explicit OpenAPI 3.1 contract**.

The objective is practical adoption clarity:

* what architectural responsibility belongs to the server
* which concrete components shape the published contract
* how to verify that response semantics remain stable for downstream clients

> **Scope:** Spring MVC (WebMVC) + Springdoc
> **Out of scope:** WebFlux, reactive pipelines, transport resilience concerns

---

## 📑 Table of Contents

* [🎯 Goals](#-goals)
* [🧭 Lifecycle Responsibility of the Server](#-lifecycle-responsibility-of-the-server)
* [✅ Prerequisites](#-prerequisites)
* [🧱 Shared Response Contract](#-shared-response-contract)
* [📦 Dependencies](#-dependencies)
* [🧩 OpenAPI Schema Enrichment Pipeline](#-openapi-schema-enrichment-pipeline)

  * [What gets added to the spec](#what-gets-added-to-the-spec)
  * [1) Baseline schema constants — OpenApiSchemas](#1-baseline-schema-constants--openapischemas)
  * [2) Base contract schema registration — SwaggerResponseCustomizer](#2-base-contract-schema-registration--swaggerresponsecustomizer)
  * [3) Wrapper eligibility detection — ResponseTypeIntrospector](#3-wrapper-eligibility-detection--responsetypeintrospector)
  * [4) Composed wrapper schema construction — ApiResponseSchemaFactory](#4-composed-wrapper-schema-construction--apiresponseschemafactory)
  * [5) Wrapper registration & pagination hints — AutoWrapperSchemaCustomizer](#5-wrapper-registration--pagination-hints--autowrapperschemacustomizer)
  * [6) Deterministic naming model](#6-deterministic-naming-model)
* [🧭 Suggested Package Layout](#-suggested-package-layout)
* [🎯 Architectural Outcome](#-architectural-outcome)

---

## 🎯 Goals

After completing this guide, your Spring MVC service will:

* return all successful responses using **one shared canonical envelope**
* publish a **deterministic OpenAPI 3.1 contract projection**
* enable **thin generics‑aware wrapper generation** on the client side
* preserve **response contract identity** across service boundaries

The server does **not** generate clients.
It publishes **explicit contract guarantees** that downstream consumers can rely on safely.

---

## 🧭 Lifecycle Responsibility of the Server

Within the contract lifecycle architecture, the server is the **semantic authority stage**.

Its responsibility is to:

* define runtime response semantics using the shared canonical envelope
* project those semantics into OpenAPI in a **deterministic and explicit form**
* avoid embedding generator‑specific assumptions into runtime behaviour

This separation ensures:

> Runtime payload handling remains simple, while specification publication becomes architecture‑aware.

OpenAPI therefore acts as a **projection layer of contract semantics**, not the runtime source of truth.

---

## ✅ Prerequisites

You should already have:

* a Spring Boot 3.5.x application using Spring MVC
* Springdoc configured to expose `/v3/api-docs`
* controllers returning domain DTOs

Build examples use Maven for clarity; the architecture itself is build‑tool agnostic.

---

## 🧱 Shared Response Contract

All successful responses use the shared canonical contract module:

```
io.github.bsayli:api-contract
```

Controllers return:

* `ServiceResponse<T>` — canonical success envelope
* `Meta` — response metadata
* `Page<T>` — pagination container
* `Sort` — sorting metadata

These types are **not re‑implemented locally**.
They remain the **single runtime source of truth** for response semantics.

### Supported Contract‑Aware Shapes

| Shape                      | Contract‑aware | Notes                            |
| -------------------------- | -------------- | -------------------------------- |
| `ServiceResponse<T>`       | ✅              | explicitly supported             |
| `ServiceResponse<Page<T>>` | ✅              | deterministic nested generic     |
| `ServiceResponse<List<T>>` | ❌              | published with default semantics |
| arbitrary nested generics  | ❌              | intentionally unsupported        |

Constraining supported shapes ensures:

* stable schema naming
* predictable client generation behaviour
* long‑term evolvability of the contract surface

---

## 📦 Dependencies

A minimal Maven setup sufficient for publishing a contract‑aware OpenAPI specification:

### Spring Boot baseline

```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>3.5.12</version>
</parent>
```

### Required properties

```xml
<properties>
  <java.version>21</java.version>
  <springdoc-openapi-starter.version>2.8.16</springdoc-openapi-starter.version>
  <api-contract.version>0.7.7</api-contract.version>
</properties>
```

### Core dependencies

```xml
<dependencies>
  <dependency>
    <groupId>io.github.bsayli</groupId>
    <artifactId>api-contract</artifactId>
    <version>${api-contract.version}</version>
  </dependency>

  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>

  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>

  <dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>${springdoc-openapi-starter.version}</version>
  </dependency>
</dependencies>
```

These dependencies are sufficient to **publish the contract projection**.

---

## 🧩 OpenAPI Schema Enrichment Pipeline

Thin client wrappers become possible only if the server publishes **semantically enriched wrapper schemas** during Springdoc generation.

This enrichment:

* does not affect runtime JSON payloads
* does not introduce custom serialization logic
* only shapes the **OpenAPI contract projection surface**

Think of this pipeline as the **publication engine room** of the architecture.

---

### What gets added to the spec

At OpenAPI generation time the service contributes:

* detection of controller return types using `ServiceResponse<T>`
* deterministic handling of pagination envelopes
* composed wrapper schemas named `ServiceResponse{DataRef}`
* vendor extensions guiding client generation templates

---

### 1) Baseline schema constants — OpenApiSchemas

**Purpose:** centralize canonical schema names and vendor‑extension keys.

Responsibilities:

* define property names such as `data`, `meta`
* define base schema identifiers: `ServiceResponse`, `Meta`, `Sort`, `ProblemDetail`
* define vendor‑extension keys:

  * `x-api-wrapper`
  * `x-api-wrapper-datatype`
  * `x-data-container`
  * `x-data-item`

This prevents naming drift and keeps schema evolution predictable.

---

### 2) Base contract schema registration — SwaggerResponseCustomizer

**Purpose:** ensure core envelope primitives exist in `#/components/schemas`.

Key behaviour:

* registers canonical schemas such as `Meta`, `Sort`, and the base `ServiceResponse`
* keeps the base envelope intentionally **data‑agnostic**

Reasoning:

> Data typing is made explicit later via composed wrapper schemas.

This separation stabilizes envelope identity across specification evolution.

---

### 3) Wrapper eligibility detection — ResponseTypeIntrospector

**Purpose:** normalize controller return signatures and detect supported generic shapes.

Typical controller signatures may include wrappers such as:

* `ResponseEntity<ServiceResponse<T>>`
* async wrappers (`CompletionStage`, `DeferredResult`, etc.)

This component:

1. unwraps transport or async containers
2. detects whether the inner type is a **contract‑aware shape**

Detection outcomes:

* `ServiceResponse<CustomerDto>` → `CustomerDto`
* `ServiceResponse<Page<CustomerDto>>` → `PageCustomerDto`
* other shapes → ignored (default OpenAPI behaviour)

This step defines **what the server explicitly guarantees in its contract surface**.

---

### 4) Composed wrapper schema construction — ApiResponseSchemaFactory

**Purpose:** create deterministic wrapper schemas binding the canonical envelope to a concrete payload schema.

Produced schemas follow a stable pattern:

* `ServiceResponseCustomerDto`
* `ServiceResponsePageCustomerDto`

Construction model:

* `allOf` composition

  * base `ServiceResponse`
  * typed `data` property referencing the DTO schema

Vendor extensions added here provide semantic hints required by client templates.

---

### 5) Wrapper registration & pagination hints — AutoWrapperSchemaCustomizer

**Purpose:** coordinate controller discovery, eligibility detection, and wrapper schema registration.

Key orchestration steps:

1. scan Spring MVC handler mappings
2. resolve wrapper‑eligible response shapes
3. register composed schemas only if DTO schemas already exist

This guard prevents accidental publication of **phantom wrapper schemas**.

Pagination semantics are surfaced using:

* `x-data-container: Page`
* `x-data-item: <DtoType>`

These hints allow client generators to safely emit nested generic wrappers such as:

```java
class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

---

### 6) Deterministic naming model

Adopt a single naming mental model when reasoning about wrapper schemas.

**Data reference naming**

* `T` → `T`
* `Page<T>` → `PageT`

Examples:

* `ServiceResponse<CustomerDto>` → `CustomerDto`
* `ServiceResponse<Page<CustomerDto>>` → `PageCustomerDto`

**Wrapper schema naming**

Always:

```
ServiceResponse + {DataRef}
```

Examples:

* `ServiceResponseCustomerDto`
* `ServiceResponsePageCustomerDto`

Consistency here directly influences long‑term schema stability.

---

## 🧭 Suggested Package Layout

```text
common/
  openapi/
    OpenApiConfig.java
    OpenApiSchemas.java
    ApiResponseSchemaFactory.java
    SwaggerResponseCustomizer.java

    introspector/
      ResponseTypeIntrospector.java

    autoreg/
      AutoWrapperSchemaCustomizer.java
```

Intent:

* isolate contract publication concerns
* enable lift‑and‑shift reuse across multiple services

---

## 🎯 Architectural Outcome

After adoption, your service:

* publishes a **contract‑explicit OpenAPI projection** aligned with runtime semantics
* preserves **response envelope identity** across producer–consumer boundaries
* bounds schema evolution risk through deterministic wrapper modelling
* enables downstream client generation pipelines to remain **regeneration‑safe**

The server now operates as the **semantic authority stage** of the contract lifecycle.

Consumers can rely on the published specification without reverse‑engineering generator behaviour.

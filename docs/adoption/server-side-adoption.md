---
layout: default
title: Server-Side Adoption
parent: Adoption Guides
nav_order: 1
---

# Server-Side Adoption — Spring MVC + Springdoc

This guide shows how to adopt a **shared success envelope** in a Spring MVC service and publish an **OpenAPI 3.1** contract that enables **thin, type‑safe client generation** — without duplicating response models.

The intent is not to dump all implementation details here. It’s to make the adoption **mentally executable**: what you add, why it matters, and what to verify.

> **Scope**: Spring MVC (WebMVC) + Springdoc
> **Out of scope**: WebFlux, reactive servers

---

## 📑 Table of Contents (Updated)

* [🎯 Goals](#-goals)
* [✅ Prerequisites](#-prerequisites)
* [🧱 Shared Response Contract](#-shared-response-contract)
* [📦 Dependencies](#-dependencies)
* [🧩 OpenAPI Schema Enrichment (Who Does What)](#-openapi-schema-enrichment-who-does-what)
  * [What gets added to the spec](#what-gets-added-to-the-spec)
  * [1) The baseline: shared schema names and constants](#1-the-baseline-shared-schema-names-and-constants)
    * [`OpenApiSchemas`](#openapischemas)
  * [2) The “base contract” schemas](#2-the-base-contract-schemas)
    * [`SwaggerResponseCustomizer`](#swaggerresponsecustomizer)
  * [3) Detecting what should get a wrapper](#3-detecting-what-should-get-a-wrapper)
    * [`ResponseTypeIntrospector`](#responsetypeintrospector)
  * [4) Building the composed wrapper schema](#4-building-the-composed-wrapper-schema)
    * [`ApiResponseSchemaFactory`](#apiresponseschemafactory)
  * [5) Registering wrappers into the OpenAPI components](#5-registering-wrappers-into-the-openapi-components)
    * [`AutoWrapperSchemaCustomizer`](#autowrapperschemacustomizer)
  * [6) Publishing error responses consistently](#6-publishing-error-responses-consistently)
    * [`GlobalErrorResponsesCustomizer`](#globalerrorresponsescustomizer)
  * [7) Deterministic Naming (How to think about schema names)](#7-deterministic-naming-how-to-think-about-schema-names)
    * [Data schema reference name](#data-schema-reference-name)
    * [Wrapper schema name](#wrapper-schema-name)
* [🧭 Suggested Package Layout](#-suggested-package-layout)
* [🎯 Outcome](#-outcome)

---

## 🎯 Goals

After completing this guide, your Spring MVC service will:

* return all successful responses using **one shared envelope**
* publish a **deterministic OpenAPI 3.1** contract that clients can consume safely
* enable **thin wrapper generation** on the client side (no duplicated envelope fields)

The server does **not** generate clients. It publishes **what it guarantees** in the contract.

---

## ✅ Prerequisites

You should already have:

* Spring Boot 3.5.x app using **Spring MVC**
* Springdoc configured for `/v3/api-docs` (JSON/YAML)
* controllers returning DTOs

This guide uses Maven snippets, but the idea is build-tool agnostic.

---

## 🧱 Shared Response Contract

All successful responses are wrapped using a **shared contract module**:

```
io.github.bsayli:api-contract
```

Your service imports and uses:

* `ServiceResponse<T>` — success envelope
* `Meta` — response metadata
* `Page<T>` — paging container
* `Sort` — sorting metadata

These types are **not implemented locally**. Your code reuses them directly.

### Supported Response Shapes

Only the following shapes are treated as **contract-aware** for client generation:

| Shape                      | Contract-aware | Notes                           |
| -------------------------- | -------------- | ------------------------------- |
| `ServiceResponse<T>`       | ✅              | supported and enriched          |
| `ServiceResponse<Page<T>>` | ✅              | supported and enriched          |
| `ServiceResponse<List<T>>` | ❌              | published as-is (defaults)      |
| other nested generics      | ❌              | published as-is (no guarantees) |

This keeps the contract small, deterministic, and generator-friendly.

---

## 📦 Dependencies

This section explains the **minimum but realistic Maven setup** required on the **server side** to:

* expose `ServiceResponse<T>` directly from your controllers,
* publish an **OpenAPI 3.1** specification via Springdoc,
* enrich that specification later using custom OpenAPI customizers.

The goal is clarity: *what you must add*, *why it exists*, and *what you should verify* — without copying a full production `pom.xml`.

---

## 1) Spring Boot Baseline

This guide assumes **Spring Boot 3.5.9** (or another 3.5.x version) using **Spring MVC (WebMVC)**.

Using the Spring Boot parent keeps dependency alignment predictable:

```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>3.5.9</version>
  <relativePath/>
</parent>
```

You do **not** need to declare versions for Spring-managed starters unless you intentionally override them.

---

## 2) Required Properties

Only a small set of properties are required for server-side adoption:

```xml
<properties>
  <java.version>21</java.version>

  <!-- OpenAPI publication -->
  <springdoc-openapi-starter.version>2.8.15</springdoc-openapi-starter.version>

  <!-- Shared response contract -->
  <api-contract.version>0.7.4</api-contract.version>
</properties>
```

Notes:

* Springdoc is versioned explicitly because it is *not* managed by Spring Boot.
* `api-contract` is versioned explicitly because it is your **shared API contract**.

---

## 3) Core Dependencies

The following dependencies are sufficient for publishing a contract-aware OpenAPI specification.

```xml
<dependencies>

  <!-- Shared API contract (used directly by controllers) -->
  <dependency>
    <groupId>io.github.bsayli</groupId>
    <artifactId>api-contract</artifactId>
    <version>${api-contract.version}</version>
  </dependency>

  <!-- Spring MVC / REST controllers -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>

  <!-- Bean validation (commonly used with request/response DTOs) -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>

  <!-- OpenAPI 3.1 publication via Springdoc -->
  <dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>${springdoc-openapi-starter.version}</version>
  </dependency>

</dependencies>
```

### Why each dependency exists

| Dependency                            | Why it is needed                                                                                                         |
| ------------------------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| `api-contract`                        | Provides `ServiceResponse<T>`, `Meta`, `Page<T>`, `Sort`, and RFC 9457 helpers. Controllers return these types directly. |
| `spring-boot-starter-web`             | Enables Spring MVC controllers and JSON serialization.                                                                   |
| `spring-boot-starter-validation`      | Commonly used with request/response DTOs and integrates cleanly with OpenAPI.                                            |
| `springdoc-openapi-starter-webmvc-ui` | Generates `/v3/api-docs` (OpenAPI 3.1) and Swagger UI.                                                                   |

Nothing else is required to *publish* the contract.

---

## 🧩 OpenAPI Schema Enrichment (Who Does What)

To enable **thin wrappers** on the client side, the server enriches the OpenAPI specification **during Springdoc generation time**.

This enrichment does **not** change runtime behavior and does **not** affect the JSON payload. It only makes the published specification:

* **explicit** (what the server publishes is visible in the spec)
* **predictable** (schema names and wrapper shapes are stable)
* **template-friendly** (client templates can bind generics without duplicating the contract)

Below is the mental model of *which class exists for what* in the server module.

---

### What gets added to the spec

At generation time, the server adds:

* detection of controller return types that use `ServiceResponse<T>`
* special handling for pagination as `ServiceResponse<Page<T>>`
* composed wrapper schemas (`ServiceResponse{DataRef}`) for a limited, explicit set of shapes
* vendor extensions that guide client templates (`x-api-wrapper`, `x-data-container`, …)

---

### 1) The baseline: shared schema names and constants

#### `OpenApiSchemas`

**Why it exists:** one place for canonical schema names, property keys, and vendor extension keys.

What it centralizes:

* common properties: `data`, `meta`
* base schema names: `ServiceResponse`, `Meta`, `Sort`, `ProblemDetail`
* vendor extension keys:

  * `x-api-wrapper`
  * `x-api-wrapper-datatype`
  * `x-data-container`
  * `x-data-item`
  * optional: `x-class-extra-annotation`

This keeps the rest of the code *string-safe* and reduces accidental drift.

---

### 2) The “base contract” schemas

#### `SwaggerResponseCustomizer`

**Why it exists:** ensure core envelope schemas exist in `#/components/schemas` even when Springdoc doesn’t materialize them the way we need.

What it does:

* registers `Sort`, `Meta`, and the **base** `ServiceResponse` schema
* keeps the base `ServiceResponse` intentionally simple:

  * `meta` is strongly typed (`#/components/schemas/Meta`)
  * `data` is present but not bound to a specific DTO here

**Reasoning:** the `data` binding is made explicit later in the composed wrapper schemas. This avoids a “free-form data object” ambiguity and keeps the contract consistent.

---

### 3) Detecting what should get a wrapper

#### `ResponseTypeIntrospector`

**Why it exists:** Spring MVC controllers often return nested wrappers (`ResponseEntity<…>`, async types, etc.). This component normalizes those signatures and extracts the *data reference name* used for wrapper schema naming.

What it does:

1. **Unwraps** common wrappers (up to a safe depth) until it reaches `ServiceResponse<T>`.

  * `ResponseEntity<…>`
  * `CompletionStage<…>`, `Future<…>`
  * `DeferredResult<…>`, `WebAsyncTask<…>`
  * (and it ignores reactive wrappers as out-of-scope for this guide)
2. Once it reaches `ServiceResponse<T>`, it determines whether the `T` shape is one of the **explicitly supported** shapes:

  * `T` is a plain DTO type → returns `"CustomerDto"`
  * `T` is `Page<CustomerDto>` → returns `"PageCustomerDto"`
  * anything else (`List<T>`, `Map<K,V>`, `Foo<Bar>`) → returns empty

**What “empty” means:** no auto wrapper schema is registered; Springdoc/OpenAPI Generator defaults apply.

This is the mechanism that keeps the contract small and predictable.

---

### 4) Building the composed wrapper schema

#### `ApiResponseSchemaFactory`

**Why it exists:** wrapper schema creation should be a pure, reusable operation with stable output.

What it produces:

A composed schema like:

* `ServiceResponseCustomerDto`
* `ServiceResponsePageCustomerDto`

Implementation approach:

* `allOf` composition:

  * base `#/components/schemas/ServiceResponse`
  * plus an object that binds `data` to `#/components/schemas/{DataRef}`

Vendor extensions added on the wrapper schema:

* `x-api-wrapper: true`
* `x-api-wrapper-datatype: {DataRef}`
* optional: `x-class-extra-annotation` (to let client wrappers inject an annotation hint)

**Important note:** in your current code, `x-api-wrapper-datatype` is set to the `dataRefName` (e.g. `PageCustomerDto` for pagination). That’s fine as long as client templates treat it as “the data schema ref name”. Pagination item typing is handled separately via `x-data-container` / `x-data-item`.

---

### 5) Registering wrappers into the OpenAPI components

#### `AutoWrapperSchemaCustomizer`

**Why it exists:** it is the coordinator that ties together:

* controller discovery (Spring MVC mappings)
* type introspection (`ResponseTypeIntrospector`)
* wrapper schema creation (`ApiResponseSchemaFactory`)
* optional pagination hints (`x-data-container`, `x-data-item`)

How it works at a glance:

1. Scans Spring MVC handler mappings (`RequestMappingHandlerMapping`) to find controller methods.
2. For each method, asks the introspector for a `dataRefName`.
3. For each returned ref name:

  * **guards** that Springdoc already has a schema for that ref (`schemas.containsKey(ref)`)
  * registers a composed wrapper schema named `ServiceResponse{ref}`
  * enriches vendor extensions for pagination wrappers

Why the guard matters:

* this code never invents data component schemas; it only wraps what Springdoc materialized.
* this prevents “phantom” wrapper schemas that don’t actually correspond to published DTO schemas.

Pagination hints:

* if the `dataRefName` starts with `Page` (container match)
* resolve the container schema, extract the item type via `content.items.$ref`
* add:

  * `x-data-container: Page`
  * `x-data-item: CustomerDto`

These two hints are what allow client templates to generate:

```java
class ServiceResponsePageCustomerDto extends ServiceResponse<Page<CustomerDto>> {}
```

without guessing.

---

### 6) Publishing error responses consistently

#### `GlobalErrorResponsesCustomizer`

**Why it exists:** standardize non-2xx responses as **RFC 9457 Problem Details** in the published OpenAPI contract.

What it does:

* registers `ErrorItem` and `ProblemDetail` component schemas (if missing)
* adds default responses (400, 404, 405, 500) to all operations
* uses `application/problem+json`

This keeps the success path (`ServiceResponse<T>`) and error path (RFC 9457) clearly separated.

---

### 7) Deterministic Naming (How to think about schema names)

You only need one naming model while adopting and debugging:

#### Data schema reference name

Used for binding `data` in wrapper schemas.

* `T` → `T`
* `Page<T>` → `Page` + `T`

Examples:

* `ServiceResponse<CustomerDto>` → `CustomerDto`
* `ServiceResponse<Page<CustomerDto>>` → `PageCustomerDto`

#### Wrapper schema name

Wrapper schemas are composed and always named:

* `ServiceResponse` + `{DataRef}`

Examples:

* `ServiceResponseCustomerDto`
* `ServiceResponsePageCustomerDto`

---

## 🧭 Suggested Package Layout

A minimal, copy-friendly layout showing **where OpenAPI publication ends** and **schema enrichment begins**:

```text
src/main/java/<base.package>/
  common/
    openapi/
      OpenApiConfig.java
      OpenApiConstants.java
      OpenApiSchemas.java
      ApiResponseSchemaFactory.java
      SwaggerResponseCustomizer.java
      GlobalErrorResponsesCustomizer.java

      introspector/
        ResponseTypeIntrospector.java

      autoreg/
        AutoWrapperSchemaCustomizer.java
```

**Intent (brief):**

* `openapi/` → OpenAPI baseline + shared schema primitives
* `introspector/` → decide which response shapes are contract-aware
* `autoreg/` → register wrapper schemas and vendor extensions

This structure keeps OpenAPI concerns isolated and easy to lift into another service.

---

## 🎯 Outcome

Your service now:

* publishes a **stable OpenAPI 3.1 contract**
* uses **one shared success envelope** from `api-contract`
* provides explicit support for **Page-only nested generics**
* enables thin, type-safe client generation without duplicated wrappers

The server publishes **what it guarantees** — and nothing more.

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

## 📑 Table of Contents

* [🎯 Goals](#-goals)
* [✅ Prerequisites](#-prerequisites)
* [🧱 Shared Response Contract](#-shared-response-contract)
* [📦 Dependencies](#-dependencies)
* [🧩 OpenAPI Schema Enrichment](#-openapi-schema-enrichment)

  * [Deterministic Naming Rule](#deterministic-naming-rule)
  * [Response Type Detection](#response-type-detection)
  * [Wrapper Schema Registration](#wrapper-schema-registration)
* [⚠️ Error Handling (RFC 9457)](#-error-handling-rfc-9457)
* [🧪 Example Controller](#-example-controller)
* [🧭 Suggested Package Layout](#-suggested-package-layout)
* [✅ Verification Checklist](#-verification-checklist)
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

* a Spring Boot 3.x app using **Spring MVC**
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

Minimal Maven setup:

```xml
<properties>
  <springdoc-openapi-starter.version>2.8.15</springdoc-openapi-starter.version>
  <api-contract.version>0.7.4</api-contract.version>
</properties>

<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>

  <dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>${springdoc-openapi-starter.version}</version>
  </dependency>

  <dependency>
    <groupId>io.github.bsayli</groupId>
    <artifactId>api-contract</artifactId>
    <version>${api-contract.version}</version>
  </dependency>
</dependencies>
```

Ensure your OpenAPI customizers are inside component scanning.

---

## 🧩 OpenAPI Schema Enrichment

To enable thin wrappers on the client side, the server enriches the OpenAPI spec **at generation time**.

What gets added:

* detection of `ServiceResponse<T>` return types
* optional detection of `Page<T>` as the only supported nested generic
* registration of wrapper schemas for guaranteed shapes
* vendor extensions that guide client templates

### Deterministic Naming Rule

Keep naming predictable and easy to debug.

**Data schema ref name** (used for `data` binding):

* `T` → `T`
* `Page<T>` → `Page` + `T`

Examples:

* `ServiceResponse<CustomerDto>` → data ref: `CustomerDto`
* `ServiceResponse<Page<CustomerDto>>` → data ref: `PageCustomerDto`

**Wrapper schema name** (the composed `ServiceResponse<…>` wrapper):

* `ServiceResponse` + `<dataRef>`

Examples:

* wrapper: `ServiceResponseCustomerDto`
* wrapper: `ServiceResponsePageCustomerDto`

This is the only naming rule you need in your head while adopting and verifying.

---

### Response Type Detection

A small reflection-based component inspects controller return types:

* unwraps `ResponseEntity`, async wrappers, etc.
* stops at `ServiceResponse<T>`
* extracts the **contract-aware** data shape:

| Controller return type           | Contract-aware? | Extracted data ref |
| -------------------------------- | --------------- | ------------------ |
| `ServiceResponse<UserDto>`       | ✅               | `UserDto`          |
| `ServiceResponse<Page<UserDto>>` | ✅               | `PageUserDto`      |
| `ServiceResponse<List<UserDto>>` | ❌               | *(none)*           |

No behavior is overridden for collections like `List<T>` or `Map<K,V>`.

---

### Wrapper Schema Registration

For contract-aware shapes only, register a **composed wrapper schema** and add vendor extensions.

Vendor extensions used by client templates:

```
x-api-wrapper: true
x-api-wrapper-datatype: UserDto
x-data-container: Page      # only for Page<T>
x-data-item: UserDto        # only for Page<T>
```

Important boundaries:

* these extensions do **not** change the JSON payload
* they do **not** affect runtime behavior
* they exist solely to guide client generation

#### Base envelope schema

Keep the base envelope schema minimal and stable.

Recommended approach:

* define `ServiceResponse` and `Meta` once
* keep `data` binding **in the composed wrapper** (where the type is explicit)

This avoids the “data is free-form object” ambiguity in the base schema.

---

## ⚠️ Error Handling (RFC 9457)

Error responses are published using **RFC 9457 Problem Details**.

At OpenAPI generation time, the service:

* registers a `ProblemDetail` schema
* declares standard error responses (400, 404, 500, …)
* uses `application/problem+json`

If you include domain error details, prefer adding them as extension members (for example `errors`) without forcing a special “extensions” envelope.

---

## 🧪 Example Controller

```java
@RestController
@RequestMapping("/v1/users")
class UserController {

  @GetMapping("/{id}")
  ResponseEntity<ServiceResponse<UserDto>> get(@PathVariable long id) {
    UserDto dto = service.get(id);
    return ResponseEntity.ok(ServiceResponse.of(dto, null));
  }
}
```

---

## 🧭 Suggested Package Layout

```text
src/main/java/<base.package>/
  openapi/
    ResponseTypeIntrospector.java
    WrapperSchemaCustomizer.java
    GlobalErrorResponsesCustomizer.java

  controller/
    UserController.java
```

Names are flexible; responsibilities are not.

---

## ✅ Verification Checklist

After startup:

* Swagger UI loads
* `/v3/api-docs` contains:

  * base schemas (`ServiceResponse`, `Meta`, `Page`, …)
  * wrapper schemas for contract-aware responses (e.g. `ServiceResponseUserDto`, `ServiceResponsePageUserDto`)
* vendor extensions appear only on wrapper schemas
* no duplicated envelope models exist in the published contract

If something fails, start by checking the naming rule and whether the introspector extracted the expected data ref.

---

## 🎯 Outcome

Your service now:

* publishes a **stable OpenAPI 3.1 contract**
* uses **one shared success envelope** from `api-contract`
* provides explicit support for **Page-only nested generics**
* enables thin, type-safe client generation without duplicated wrappers

The server publishes **what it guarantees** — and nothing more.

---
layout: default
title: Server-Side Adoption
parent: Adoption Guides
nav_order: 1
---
# Server-Side Adoption — Spring MVC + Springdoc

**Goal:** integrate a contract-driven setup into your Spring MVC service so it:

* returns a unified `{ data, meta }` envelope via **`ServiceResponse<T>`**
* publishes a deterministic, generics-aware **OpenAPI 3.1** contract
* enables **thin, type-safe client generation** without duplicating response models

> Scope: Spring MVC (WebMVC) + Springdoc.
> Out of scope: WebFlux, reactive servers.

---

## Table of Contents

* [Core Principle](#core-principle)
  * [Contract Rules](#contract-rules)
* [What Your Service Will Do](#what-your-service-will-do)
* [Dependencies](#dependencies)
* [Response Envelope](#response-envelope)
* [OpenAPI Schema Infrastructure](#openapi-schema-infrastructure)
  * [OpenApiSchemas](#openapischemas)
  * [SwaggerResponseCustomizer](#swaggerresponsecustomizer)
  * [ApiResponseSchemaFactory](#apiresponseschemafactory)
* [Automatic Wrapper Registration](#automatic-wrapper-registration)
  * [ResponseTypeIntrospector](#responsetypeintrospector)
  * [AutoWrapperSchemaCustomizer](#autowrapperschemacustomizer)
* [Global Error Responses (RFC 9457)](#global-error-responses-rfc-9457)
  * [GlobalErrorResponsesCustomizer](#globalerrorresponsescustomizer)
  * [Optional: Problem Extensions](#optional-problem-extensions)
* [Example Controller](#example-controller)
* [Verification Checklist](#verification-checklist)
* [Minimal Folder Layout](#minimal-folder-layout)
* [Outcome](#outcome)

---

<a id="core-principle"></a>

## Core Principle

This setup is **contract-first and non-negotiable**.

* **`api-contract` is the single source of truth** for:

  * `ServiceResponse`
  * `Meta`
  * `Page`
  * `Sort`
* Both **server** and **client** depend on the same artifact.
* No local copies, forks, or redefinitions.

<a id="contract-rules"></a>

### Contract Rules

* The canonical success envelope is **`ServiceResponse<T>`**.
* Nested generics are supported **only** for:

```text
ServiceResponse<Page<T>>
```

* For any other generic type:

```text
ServiceResponse<List<T>>
ServiceResponse<Map<K,V>>
ServiceResponse<Foo<Bar>>
```

➡️ **Generics are ignored** in schema naming and wrapper typing. Only the **raw type** name is used.

This rule is enforced at **OpenAPI generation time** and is intentional (determinism + generator safety).

---

<a id="what-your-service-will-do"></a>

## What Your Service Will Do

After adoption, your service will:

* Return success responses like:

```json
{
  "data": "<T>",
  "meta": {
    "serverTime": "2025-01-01T12:34:56Z",
    "sort": []
  }
}
```

* Publish `/v3/api-docs(.yaml)` that includes:

  * base schemas (`ServiceResponse`, `Meta`, `Page`, `Sort`)
  * one composed wrapper per discovered `T`
  * vendor extensions required for client-side wrapper typing

---

<a id="dependencies"></a>

## Dependencies

Maven example:

```xml
<properties>
  <!-- Pin only what the Spring Boot parent/BOM does NOT manage -->
  <springdoc-openapi-starter.version>2.8.15</springdoc-openapi-starter.version>

  <!-- Shared contract version (single source of truth) -->
  <api-contract.version>0.7.4</api-contract.version>
</properties>

<dependencies>
<!-- Spring Boot web stack (version managed by Spring Boot parent/BOM) -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- OpenAPI producer (NOT managed by Spring Boot parent/BOM) -->
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>${springdoc-openapi-starter.version}</version>
</dependency>

<!-- Shared response + paging contract (single source of truth) -->
<dependency>
  <groupId>io.github.bsayli</groupId>
  <artifactId>api-contract</artifactId>
  <version>${api-contract.version}</version>
</dependency>
</dependencies>
```

> Make sure your OpenAPI customization packages are included in your application’s component scan.

---

<a id="response-envelope"></a>

## Response Envelope

You **do not implement** the envelope classes locally.

They come from:

```text
io.github.bsayli:api-contract
```

Used types include:

* `ServiceResponse<T>`
* `Meta`
* `Page<T>`
* `Sort`

Your controllers should return `ServiceResponse<T>` (optionally wrapped in `ResponseEntity`).

---

<a id="openapi-schema-infrastructure"></a>

## OpenAPI Schema Infrastructure

This adoption expects a small OpenAPI “schema layer” in your service. The names below are **reference names** (you can rename them), but the responsibilities should remain.

<a id="openapischemas"></a>

### `OpenApiSchemas`

Centralizes schema names and vendor-extension keys.

Vendor extensions:

| Key                      | Purpose                           |
| ------------------------ | --------------------------------- |
| `x-api-wrapper`          | Marks a composed response wrapper |
| `x-api-wrapper-datatype` | Raw `T` schema name               |
| `x-data-container`       | Present **only** for `Page<T>`    |
| `x-data-item`            | Inner item type for `Page<T>`     |

<a id="swaggerresponsecustomizer"></a>

### `SwaggerResponseCustomizer`

Registers base, reusable schemas:

* `ServiceResponse`
* `Meta`

This ensures composed wrappers can safely reference them.

<a id="apiresponseschemafactory"></a>

### `ApiResponseSchemaFactory`

Creates a composed schema per discovered `T`, for example:

```text
ServiceResponseEntityDto
ServiceResponsePageEntityDto
```

Each composed schema typically:

* uses `allOf` with the base `ServiceResponse`
* rebinds the `data` property to the discovered `T` (or `Page<T>`)
* adds `x-api-wrapper` metadata

---

<a id="automatic-wrapper-registration"></a>

## Automatic Wrapper Registration

<a id="responsetypeintrospector"></a>

### `ResponseTypeIntrospector`

* scans controller return types
* unwraps `ResponseEntity`, async wrappers, etc.
* detects `ServiceResponse<T>`
* enforces the **Page-only nested generics** rule

Behavior summary:

| Return type                        | Resulting data schema name |
| ---------------------------------- | -------------------------- |
| `ServiceResponse<EntityDto>`       | `EntityDto`                |
| `ServiceResponse<Page<EntityDto>>` | `PageEntityDto`            |
| `ServiceResponse<List<EntityDto>>` | `List` (raw)               |

<a id="autowrapperschemacustomizer"></a>

### `AutoWrapperSchemaCustomizer`

* runs at OpenAPI generation time
* discovers all `T` values from your controllers
* registers composed wrapper schemas automatically
* adds:

  * `x-data-container`
  * `x-data-item`

➡️ only when `T` is `Page<…>`.

---

<a id="global-error-responses-rfc-9457"></a>

## Global Error Responses (RFC 9457)

<a id="globalerrorresponsescustomizer"></a>

### `GlobalErrorResponsesCustomizer`

Automatically:

* registers a `ProblemDetail` schema
* attaches standard error responses (example set):

  * 400
  * 404
  * 405
  * 500

All with:

```text
Content-Type: application/problem+json
```

Fully compliant with **RFC 9457**.

<a id="optional-problem-extensions"></a>

### Optional: Problem Extensions

If you already have structured domain errors, you may enrich:

```json
{
  "extensions": {
    "errors": [
      { "code": "...", "message": "...", "field": "..." }
    ]
  }
}
```

This is optional and additive.

---

<a id="example-controller"></a>

## Example Controller

Generic example (replace names with your domain):

```java
@RestController
@RequestMapping("/v1/entities")
class EntityController {

  @GetMapping("/{id}")
  ResponseEntity<ServiceResponse<EntityDto>> get(@PathVariable long id) {
    EntityDto dto = service.get(id);
    return ResponseEntity.ok(ServiceResponse.of(dto, null));
  }
}
```

---

<a id="verification-checklist"></a>

## Verification Checklist

After startup:

1. Swagger UI opens
2. `/v3/api-docs` contains:

  * `ServiceResponse`
  * `ServiceResponse<YourDto>` wrappers (composed schemas)
  * `ServiceResponse<Page<YourDto>>` wrappers (composed schemas)
3. Vendor extensions exist where expected
4. No duplicated response envelope DTOs appear in the published contract

---

<a id="minimal-folder-layout"></a>

## Minimal Folder Layout

```text
src/main/java/<your.base>/
  common/openapi/
    OpenApiSchemas.java
    SwaggerResponseCustomizer.java
    ApiResponseSchemaFactory.java
    GlobalErrorResponsesCustomizer.java
    introspector/
      ResponseTypeIntrospector.java
    autoreg/
      AutoWrapperSchemaCustomizer.java

  api/controller/
    ...
```

---

<a id="outcome"></a>

## Outcome

Your service now:

* is **fully contract-aligned** with `api-contract`
* publishes a **deterministic OpenAPI 3.1** spec
* supports **Page-only nested generics**
* produces zero duplicated response envelope models
* is ready for thin, type-safe client generation

This is not a pattern — it is a **contractual architecture decision**.

---
layout: default
title: Client-Side Adoption
parent: Adoption Guides
nav_order: 2
---

# Client‑Side Adoption

This guide explains how to integrate a **generics‑aware OpenAPI client** into *your own application* using a **single canonical contract**, without leaking domain‑specific assumptions from the reference project.

The examples intentionally avoid concrete domain names (like `Customer`) and focus on **reusable adoption principles**.

**Core contract guarantees:**

* Success envelope: **`ServiceResponse<T>`** (shared via **`io.github.bsayli:api-contract`**)
* Nested generics: **supported only for** **`ServiceResponse<Page<T>>`**
* Errors: **RFC 9457** `ProblemDetail`, decoded and raised as **`ApiProblemException`**

> Scope: Spring MVC (WebMVC) applications using OpenAPI Generator `java` (`restclient`) and Spring `RestClient`.

---

## 📑 Table of Contents

* [🎯 Goals](#-goals)
* [✅ Prerequisites](#-prerequisites)
* [🚀 Generate the Client](#-generate-the-client)
* [🧹 Avoid Duplicated Contracts](#-avoid-duplicated-contracts)
* [🧩 Thin Wrapper Generation (Mustache Overlay)](#-thin-wrapper-generation-mustache-overlay)
* [🧠 Contract Semantics (Deterministic by Design)](#-contract-semantics-deterministic-by-design)
* [⚠️ Error Handling (RFC 9457)](#-error-handling-rfc-9457)
* [⚙️ Spring Boot Integration](#-spring-boot-integration)
* [🧩 Adapter Pattern (Recommended)](#-adapter-pattern-recommended)
* [🧪 Example Usage](#-example-usage)
* [🧭 Suggested Folder Structure](#-suggested-folder-structure)
* [✅ Key Points](#-key-points)

---

## 🎯 Goals

* Generate **thin wrapper classes** that extend **`ServiceResponse<T>`** (no duplicated envelopes)
* Preserve **Page‑only nested generics**: `ServiceResponse<Page<T>>`
* Decode non‑2xx responses into **RFC 9457** `ProblemDetail` and throw **`ApiProblemException`**
* Keep generated code isolated behind a **stable adapter interface**

---

## ✅ Prerequisites

* Java 21+
* Maven 3.9+
* An OpenAPI‑producing service exposing `/v3/api-docs.yaml`
* The shared contract dependency available:

```
io.github.bsayli:api-contract
```

---

## 🚀 Generate the Client

This adoption guide assumes **your service already publishes** a valid OpenAPI 3.1 contract.

1. **Download the OpenAPI spec** into your client module:

```bash
curl -s http://<service-host>/<base-path>/v3/api-docs.yaml \
  -o src/main/resources/api-docs.yaml
```

2. **Generate & compile** the client:

> ⚠️ **Important prerequisite**
>
> The generated client **depends on the shared canonical contract**:
>
> ```text
> io.github.bsayli:api-contract
> ```
>
> Your project must already provide this dependency **before** running the build:
>
> * either as a released artifact available from your Maven repository, or
> * as a locally built module (for example via a multi-module or parent build).
>
> If `api-contract` is not resolvable at build time, `mvn clean install` will fail.

> 🔧 **Build & Maven configuration**
>
> This guide focuses on **concepts and integration semantics**, not concrete build wiring.
>
> For the exact **Maven setup, OpenAPI Generator configuration, and template wiring**, see:
> → [Client-Side Adoption — Build & POM Setup](client-side-adoption-pom.md)


```bash
mvn clean install
```

3. **Verify generated output**:

* `target/generated-sources/openapi/src/gen/java`
* Wrapper classes extending the shared contract, for example:

```
ServiceResponseFooDto extends ServiceResponse<FooDto>
ServiceResponsePageFooDto extends ServiceResponse<Page<FooDto>>
```

> The exact DTO names depend on *your* domain model — not on this reference project.

---

## 🧹 Avoid Duplicated Contracts

Because both server and client rely on the shared `api-contract`,
generated client models must be configured to reuse these canonical types
instead of producing parallel DTO implementations.

Add the following to `.openapi-generator-ignore`:

```bash
**/src/gen/java/**/generated/dto/Page*.java
**/src/gen/java/**/generated/dto/ServiceResponse.java
**/src/gen/java/**/generated/dto/ServiceResponseVoid.java
**/src/gen/java/**/generated/dto/Meta.java
**/src/gen/java/**/generated/dto/Sort.java
```

Ensure your generator configuration includes:

```xml
<ignoreFileOverride>${project.basedir}/.openapi-generator-ignore</ignoreFileOverride>
```

---

## 🧩 Thin Wrapper Generation (Mustache Overlay)

Place your template overlays under:

```
src/main/resources/openapi-templates/
```

These overlays are responsible only for **binding generic parameters** to the shared API contract. They do not redefine response fields or invent new envelopes.

### Wrapper template responsibilities

Your wrapper templates should:

* Import **`ServiceResponse`** from `api-contract`
* Import **`Page`** from `api-contract` *only* when `x-data-container: Page` is present
* Extend **`ServiceResponse<T>`** or **`ServiceResponse<Page<T>>`**

The goal is to generate **thin wrappers** that simply connect OpenAPI schema information to the canonical contract types.

### Template (excerpt)

{% raw %}

```mustache
import {{apiContractEnvelope}}.ServiceResponse;
{{#vendorExtensions.x-data-container}}
import {{apiContractPage}}.{{vendorExtensions.x-data-container}};
{{/vendorExtensions.x-data-container}}

public class {{classname}} extends ServiceResponse<
{{#vendorExtensions.x-data-container}}
{{vendorExtensions.x-data-container}}<{{vendorExtensions.x-data-item}}>
{{/vendorExtensions.x-data-container}}
{{^vendorExtensions.x-data-container}}
{{vendorExtensions.x-api-wrapper-datatype}}
{{/vendorExtensions.x-data-container}}
> {}
```

{% endraw %}

> The property name `apiContractEnvelope` intentionally mirrors the Maven configuration to keep template wiring explicit and predictable.

---

## 🧠 Contract Semantics (Deterministic by Design)

This project deliberately limits the scope of what is considered *contract-aware* in order to keep schema naming and client generation predictable over time.

### Canonical success envelope

All successful responses use the shared contract:

```java
ServiceResponse<T>
```

This type is defined once in **`api-contract`** and reused by both server and client. Generated models never redefine its fields.

### Nested generics

Nested generics are treated as contract-aware **only** for:

```java
ServiceResponse<Page<T>>
```

This reflects a conscious design decision:

* Pagination is a common, well-understood use case
* Its semantics are stable across endpoints
* Supporting it explicitly keeps schema names deterministic

All other generic shapes (`List<T>`, `Map<K,V>`, `Foo<Bar>`, etc.) follow OpenAPI Generator’s default behavior during schema naming and model generation.

The contract defines **what is guaranteed**, not every shape Java could theoretically express.

---

## ⚠️ Error Handling (RFC-9457)

Non‑2xx responses are represented using **RFC 9457 Problem Details** and surfaced to consumers as **`ApiProblemException`**.

Error responses are intentionally **not** wrapped in `ServiceResponse`.

Example usage:

```java
try {
  adapter.getResource(id);
} catch (ApiProblemException ex) {
  ProblemDetail pd = ex.getProblem();
  log.warn(
      "API error [status={}, code={}, title={}]",
      ex.getStatus(),
      pd.getErrorCode(),
      pd.getTitle()
  );
}
```

This keeps success and error paths clearly separated:

* success responses use the shared `{ data, meta }` envelope
* error responses follow a standardized, RFC‑defined structure

No runtime tricks or framework magic — just explicit contracts and predictable generation.

---

## ⚙️ Spring Boot Integration

### RestClient status handler

```java
@Configuration
public class ApiClientConfig {

  @Bean
  RestClientCustomizer problemDetailHandler(ObjectMapper om) {
    return builder -> builder.defaultStatusHandler(
        HttpStatusCode::isError,
        (req, res) -> {
          ProblemDetail pd = ProblemDetailSupport.extract(om, res);
          throw new ApiProblemException(pd, res.getStatusCode().value());
        });
  }
}
```

---

## 🧩 Adapter Pattern (Recommended)

Never expose generated APIs directly.
Wrap them behind **your own interface** so regeneration never leaks.

```java
public interface ResourceClientAdapter {
  ServiceResponse<FooDto> getFoo(Long id);
  ServiceResponse<Page<FooDto>> listFoos();
}
```

```java
@Service
public class ResourceClientAdapterImpl implements ResourceClientAdapter {

  private final GeneratedApi api;

  public ResourceClientAdapterImpl(GeneratedApi api) {
    this.api = api;
  }

  @Override
  public ServiceResponse<FooDto> getFoo(Long id) {
    return api.getFoo(id);
  }
}
```

---

## 🧪 Example Usage

```java
var response = adapter.getFoo(42L);
var data = response.getData();
var serverTime = response.getMeta().serverTime();
```

---

## 🧭 Suggested Folder Structure

```
client-module/
  src/main/java/
    adapter/
    adapter/config/
    adapter/support/
    common/problem/
  src/main/resources/
    openapi-templates/
    api-docs.yaml
  .openapi-generator-ignore
  pom.xml
```

---

## ✅ Key Points

* **One contract**: `ServiceResponse<T>` comes from `api-contract`
* **Deterministic generics**: nested only for `Page<T>`
* **No duplication**: generated DTOs never redefine the contract
* **RFC 9457-first**: errors surfaced as `ApiProblemException`
* **Isolation**: adapters protect your application from generator churn

This document explains **how the mechanism is adopted**, not a sample domain.
Your domain types plug in cleanly without requiring changes to the mechanism itself.
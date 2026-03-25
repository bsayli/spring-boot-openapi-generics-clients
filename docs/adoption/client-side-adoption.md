---
layout: default
title: Client-Side Adoption
parent: Adoption Guides
nav_order: 2
---

# Client‑Side Adoption — Contract Lifecycle Interpretation Stage

This guide explains the **client‑side interpretation stage** of the contract lifecycle architecture.

At this stage, a consumer application **interprets a server‑published OpenAPI contract** and integrates a generated client in a way that:

* preserves the **canonical response envelope semantics**
* avoids duplicating shared contract models
* keeps regeneration safe and architecturally governed

The examples are intentionally **domain‑agnostic**.
They describe *how to integrate the mechanism*, not how to model a specific business domain.

---

## 📑 Table of Contents

* [🎯 Architectural Goals](#-architectural-goals)
* [🧭 Lifecycle Responsibility of the Client](#-lifecycle-responsibility-of-the-client)
* [✅ Prerequisites](#-prerequisites)
* [🚀 Client Generation Flow](#-client-generation-flow)
* [🧹 Preventing Contract Duplication](#-preventing-contract-duplication)
* [🧩 Thin Wrapper Generation Strategy](#-thin-wrapper-generation-strategy)
* [🧠 Contract Semantics Interpretation](#-contract-semantics-interpretation)
* [⚠️ Error Handling (RFC 9457)](#-error-handling-rfc-9457)
* [⚙️ Spring Boot Integration](#-spring-boot-integration)
* [🧩 Adapter Boundary Pattern](#-adapter-boundary-pattern)
* [🧪 Example Usage](#-example-usage)
* [🧭 Suggested Folder Structure](#-suggested-folder-structure)
* [🎯 Architectural Outcome](#-architectural-outcome)

---

## 🎯 Architectural Goals

After adopting this approach, your consumer application will:

* interpret a **server‑published canonical success envelope** without redefining it
* generate **thin wrapper models** bound to shared runtime contract types
* preserve deterministic nested generics for **`ServiceResponse<Page<T>>`**
* propagate structured errors using **RFC 9457 Problem Details**
* isolate generated transport code behind **stable application‑owned boundaries**

The client does not redefine contract semantics.
It **interprets and operationalizes** what the server explicitly guarantees.

---

## 🧭 Lifecycle Responsibility of the Client

Within the contract lifecycle architecture, the client has a clearly scoped responsibility.

It must:

* consume the OpenAPI document as a **semantic projection of server contracts**
* bind generated models to the **shared canonical contract artifact**
* avoid introducing parallel response envelope implementations
* isolate generated APIs from core application logic

This separation ensures:

> **Client regeneration remains safe because contract identity is preserved outside generated code.**

---

## ✅ Prerequisites

Before integrating the generated client, ensure the following:

* Java 21+
* Maven 3.9+
* Access to a running service exposing `/v3/api-docs.yaml`
* Availability of the shared contract dependency:

```
io.github.bsayli:api-contract
```

The shared contract must be resolvable **at build time**.

---

## 🚀 Client Generation Flow

The client integration pipeline follows a deterministic sequence.

1. **Retrieve the OpenAPI contract projection** published by the producer service.

```bash
curl -s http://<service-host>/<base-path>/v3/api-docs.yaml \
  -o src/main/resources/api-docs.yaml
```

2. **Generate and compile client sources** using the configured OpenAPI Generator build.

```bash
mvn clean install
```

3. **Verify semantic binding of generated wrappers**:

Generated models should:

* extend `ServiceResponse<T>` from `api-contract`
* preserve nested generics for pagination wrappers
* avoid redefining envelope or paging types

Generated sources appear under:

```
target/generated-sources/openapi/src/gen/java
```

---

## 🧹 Preventing Contract Duplication

Because both producer and consumer share the canonical contract module,
client generation must **not recreate envelope or paging DTOs**.

Configure `.openapi-generator-ignore` accordingly:

```bash
**/generated/dto/ServiceResponse*.java
**/generated/dto/Page*.java
**/generated/dto/Meta.java
**/generated/dto/Sort*.java
```

This ensures a **single runtime contract identity** across modules.

---

## 🧩 Thin Wrapper Generation Strategy

Client templates should generate **inheritance‑based wrapper models** that bind OpenAPI schema information to shared runtime abstractions.

Typical generated structures:

```
ServiceResponseFooDto extends ServiceResponse<FooDto>
ServiceResponsePageFooDto extends ServiceResponse<Page<FooDto>>
```

These wrappers:

* introduce no envelope fields
* introduce no serialization behaviour
* only provide **type‑safe semantic binding**

Template overlays must therefore remain **minimal and declarative**.

---

## 🧠 Contract Semantics Interpretation

Client generation remains deterministic because the contract defines
**a consciously limited generic support scope**.

Contract‑aware shapes:

* `ServiceResponse<T>`
* `ServiceResponse<Page<T>>`

All other generic compositions follow **default generator behaviour**.

This design:

* stabilizes schema naming
* limits template complexity
* reduces long‑term integration risk

The client therefore interprets contract guarantees rather than attempting to generalize all generic forms.

---

## ⚠️ Error Handling (RFC 9457)

Failure responses are modeled using **RFC 9457 Problem Details** and surfaced as a single structured runtime exception.

Example handling pattern:

```java
try {
  adapter.getResource(id);
} catch (ApiProblemException ex) {
  ProblemDetail pd = ex.getProblem();
  log.warn("API error status={} title={}", ex.getStatus(), pd.getTitle());
}
```

This establishes a clear semantic separation:

* success → canonical `{ data, meta }` envelope
* failure → standardized problem contract

---

## ⚙️ Spring Boot Integration

A minimal `RestClient` status handler aligns runtime behaviour with published error semantics.

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

## 🧩 Adapter Boundary Pattern

Generated APIs should never leak into core business layers.

Introduce an application‑owned adapter interface to create a **stable integration boundary**.

```java
public interface ResourceClientAdapter {
  ServiceResponse<FooDto> getFoo(Long id);
  ServiceResponse<Page<FooDto>> listFoos();
}
```

Adapters absorb regeneration churn and protect domain logic.

---

## 🧪 Example Usage

```java
var response = adapter.getFoo(42L);
var dto = response.getData();
var serverTime = response.getMeta().serverTime();
```

---

## 🧭 Suggested Folder Structure

```
client-module/
  adapter/
  adapter/config/
  adapter/support/
  common/problem/
  src/main/resources/
    openapi-templates/
    api-docs.yaml
  .openapi-generator-ignore
```

This layout keeps **transport concerns isolated** and simplifies reuse across services.

---

## 🎯 Architectural Outcome

After completing client‑side adoption:

* generated models remain **contract‑bound and regeneration‑safe**
* response envelope identity is preserved across system boundaries
* pagination semantics remain structurally consistent
* error handling aligns with standardized HTTP problem contracts

The client now participates as the **semantic interpretation stage** of the contract lifecycle.

Integration behaviour becomes predictable because runtime abstractions remain independent from generation tooling.

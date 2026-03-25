# customer-service-client

[![Java 21](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.12-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![OpenAPI Generator](https://img.shields.io/badge/OpenAPI%20Generator-7.21.0-blue?logo=openapiinitiative)](https://openapi-generator.tech/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](../LICENSE)

---

## 📑 Table of Contents

- 🧭 [What this module actually is](#what-this-module-actually-is)
- 🚨 [Architectural problem context](#architectural-problem-context)
- 💡 [Architectural solution model](#architectural-solution-model)
- 🔗 [Contract symmetry with the producer service](#contract-symmetry-with-the-producer-service)
- 🧱 [Deterministic generic scope](#deterministic-generic-scope)
- ⚙️ [Generator behavior control pipeline](#generator-behavior-control-pipeline)
- 🧬 [Thin wrapper inheritance model](#thin-wrapper-inheritance-model)
- 🧩 [Runtime adapter boundary pattern](#runtime-adapter-boundary-pattern)
- ⚠️ [Error contract symmetry](#error-contract-symmetry)
- 🌐 [Production-oriented HTTP client configuration](#productionoriented-http-client-configuration)
- 🚀 [Running the reference generation flow](#running-the-reference-generation-flow)
- 📘 [Summary](#summary)
- 🛡 [License](#license)

---

## What this module actually is

`customer-service-client` is a **reference implementation of contract‑aware OpenAPI client generation** in a Spring Boot ecosystem.

It demonstrates how a consumer can generate a **type‑safe HTTP client that preserves generic response envelope semantics**, instead of flattening or duplicating contract structures.

This module is not a business integration SDK and not a generic REST utility.
It exists to show a **repeatable architectural pattern** for aligning:

* server‑published OpenAPI contracts
* generator template behavior
* runtime client integration boundaries

The result is a deterministic and evolvable client architecture.

---

## Architectural problem context

In many real‑world systems, backend services publish standardized success envelopes such as:

```
ServiceResponse<T>
```

However, typical OpenAPI client generation pipelines introduce several issues:

* envelope DTO duplication across generated models
* loss of nested generic intent (for example pagination wrappers)
* unstable schema naming across spec evolution
* tight coupling to generator internal templates
* regeneration churn leaking into application code

These factors make generated clients harder to evolve safely in production environments.

---

## Architectural solution model

This module demonstrates a **semantic contract preservation strategy** based on:

* deterministic server‑side schema publication
* vendor‑extension signaling of wrapper semantics
* controlled generator template patching
* thin wrapper inheritance instead of model flattening
* adapter‑based runtime isolation

The approach treats **client generation as an architectural discipline**, not merely a build‑time convenience.

---

## Contract symmetry with the producer service

This client is designed to operate together with the `customer-service` module.

The architecture follows a symmetric contract model:

* the server publishes explicit generic envelope semantics
* the client interprets these semantics during code generation
* both sides reuse a shared canonical contract artifact (`api-contract`)

This symmetry ensures:

* identical response envelope identity across boundaries
* predictable integration semantics
* long‑term evolvability of API contracts

---

## Deterministic generic scope

The generation strategy intentionally supports a **small, explicit subset of nested generics**.

Supported response shapes:

* `ServiceResponse<T>`
* `ServiceResponse<Page<T>>`

All other generic compositions follow default generator behavior.

This constraint provides:

* stable wrapper naming
* maintainable template logic
* reduced long‑term technical risk

The design goal is **reliability of supported shapes**, not exhaustive generic coverage.

---

## Generator behavior control pipeline

This module demonstrates a controlled template governance approach.

### 1. Upstream template extraction

The build extracts the original `model.mustache` template from the OpenAPI Generator distribution.

This allows behavior customization without forking the generator.

### 2. Template patch verification

A deterministic patch step injects wrapper‑aware rendering logic.

The build fails fast if the upstream template structure changes, ensuring generator upgrades remain safe and explicit.

### 3. Local template overlay

A minimal Mustache overlay defines how wrapper schemas should be rendered as thin generic inheritance classes.

### 4. Import mapping strategy

Canonical envelope and paging types are **never regenerated**.
Instead, generated models bind directly to the shared contract artifact.

This guarantees a single source of truth for response semantics.

### 5. Ignore hygiene

Generator ignore rules prevent duplicate envelope DTO generation, preserving hierarchy identity.

---

## Thin wrapper inheritance model

Vendor extensions emitted by the producer service guide wrapper generation.

Example generated classes:

```
ServiceResponseCustomerDto
  → extends ServiceResponse<CustomerDto>

ServiceResponsePageCustomerDto
  → extends ServiceResponse<Page<CustomerDto>>
```

No envelope logic is regenerated.

This preserves:

* semantic clarity
* DTO hierarchy stability
* runtime serialization simplicity

---

## Runtime adapter boundary pattern

Generated APIs should not be consumed directly by application business logic.

Instead, introduce an application‑owned adapter interface.

This pattern provides:

* isolation from regeneration churn
* stable integration contracts
* transport abstraction
* improved testability

The module includes a reference adapter implementation demonstrating this boundary.

---

## Error contract symmetry

Error responses are propagated using **RFC‑9457 ProblemDetail semantics**.

The client:

* decodes structured problem payloads
* provides deterministic fallback behavior
* surfaces errors as typed runtime exceptions

This ensures that **error handling semantics remain contract‑aware**, matching server behavior even under degraded upstream conditions.

---

## Production‑oriented HTTP client configuration

The reference configuration demonstrates:

* Apache HttpClient 5 connection pooling
* explicit timeout configuration
* retry suppression
* structured error interception

These defaults represent conservative production‑safe transport settings.

---

## Running the reference generation flow

1. Start the producer service

```
cd customer-service
mvn spring-boot:run
```

2. Fetch the OpenAPI specification

```
cd ../customer-service-client
curl -s http://localhost:8084/customer-service/v3/api-docs.yaml \
  -o src/main/resources/customer-api-docs.yaml
```

3. Generate and compile the client

```
mvn clean install
```

Generated sources appear under:

```
target/generated-sources/openapi/src/gen/java
```

---

## Summary

This module demonstrates that **OpenAPI client generation can preserve generic contract semantics** when treated as an architectural concern.

Key characteristics:

* single canonical response envelope
* deterministic nested generic scope
* thin inheritance‑based wrapper generation
* adapter‑protected runtime integration
* contract‑aware structured error propagation

Together with the producer service, it forms a **practical reference architecture** for generics‑aware, contract‑driven API integration in Spring Boot systems.

---

## License

MIT License — see repository root `LICENSE` file.

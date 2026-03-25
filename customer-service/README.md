# customer-service

[![Java](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.12-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![Build](https://github.com/bsayli/spring-boot-openapi-generics-clients/actions/workflows/build.yml/badge.svg)](https://github.com/bsayli/spring-boot-openapi-generics-clients/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](../LICENSE)

---

## 📑 Table of Contents

- 🔎 [What this module actually is](#what-this-module-actually-is)
- ❗ [Problem statement](#problem-statement)
- ✅ [Solution approach](#solution-approach)
- 🎯 [Supported contract scope](#supported-contract-scope)
- ⚖️ [Runtime vs specification responsibilities](#runtime-vs-specification-responsibilities)
- 🏗️ [Architecture overview](#architecture-overview)
- 🔄 [End-to-end contract flow](#endtoend-contract-flow)
- ⚠️ [Error contract model](#error-contract-model)
- 🚀 [Running the reference service](#running-the-reference-service)
- 🧪 [Verifying the contract](#verifying-the-contract)
- 🔗 [OpenAPI endpoints](#openapi-endpoints)
- 📦 [Related module](#related-module)
- 🧪 [Testing](#testing)
- 🤝 [Contributing](#contributing)
- 🛡️ [License](#license)

---

## What this module actually is

`customer-service` is **not a CRUD demo service** and **not a generic resolution library**.

It is a **contract‑aware OpenAPI publication reference implementation** demonstrating how a Spring Boot service can publish a **deterministic generic response envelope contract** that can be safely consumed by OpenAPI client generators.

The module exists to show a practical, production‑realistic solution for the following architectural challenge:

> How can a backend publish generic response envelopes in OpenAPI while keeping client code generation deterministic, thin, and semantically correct?

This service provides a runnable, production‑grade reference for solving that problem.

---

## Problem statement

In real‑world distributed systems, backend services often standardize successful responses using envelope patterns such as:

```
ServiceResponse<T>
```

However, when publishing OpenAPI specifications:

* Generic information is frequently flattened or lost
* Client generators duplicate envelope fields into every generated model
* Nested generic containers (such as pagination) become ambiguous
* Schema naming becomes unstable across versions
* Server and client contracts drift over time

These issues lead to:

* Boilerplate client models
* Reduced type safety
* Hard‑to‑evolve API contracts
* Increased integration friction

---

## Solution approach

This module demonstrates a **scoped, deterministic contract publication model** based on the following architectural principles:

* Define an explicit **supported response shape subset** instead of attempting to solve arbitrary generics
* Publish canonical envelope schemas as stable specification building blocks
* Compose wrapper schemas deterministically using OpenAPI `allOf`
* Enrich schemas with **vendor extensions** to carry semantic intent to client generators
* Allow client templates to emit **thin generic wrappers** instead of duplicating fields

The result is an **end‑to‑end symmetric contract flow**:

```
Controller return type
   → OpenAPI deterministic schema
       → client generator semantic hints
           → thin generic wrapper class
```

---

## Supported contract scope

This reference implementation intentionally supports a **small and explicit subset** of response shapes.

### Supported

* `ServiceResponse<T>` where `T` is a plain DTO
* `ServiceResponse<Page<T>>` for pagination scenarios

### Non‑goals

The following are intentionally **out of scope**:

* Arbitrary nested generics
* Collection envelopes such as `ServiceResponse<List<T>>`
* Map‑based generic containers
* Generic graph resolution

This scoped strategy ensures:

* Stable schema naming
* Predictable client generation
* Evolvable long‑term API contracts

---

## Runtime vs specification responsibilities

A key architectural property of this module is the strict separation between:

### Runtime behavior

* Controllers return `ServiceResponse<T>` envelopes
* JSON serialization remains standard Spring Boot behavior
* No custom HTTP serialization layer is introduced

### Specification shaping

* Springdoc OpenAPI output is enriched using customizers
* Wrapper schemas are composed at specification level only
* Vendor extensions carry semantic metadata for generators

This ensures that **runtime payload semantics remain simple**, while **contract publication becomes architecture‑aware**.

---

## Architecture overview

The contract publication pipeline consists of clearly separated layers.

### 1. Controller contract layer

Controllers intentionally expose canonical return shapes:

* `ServiceResponse<CustomerDto>`
* `ServiceResponse<Page<CustomerDto>>`

This establishes the **architectural contract boundary**.

### 2. Contract introspection layer

`ResponseTypeIntrospector`:

* Unwraps common async wrappers (`ResponseEntity`, `CompletionStage`, etc.)
* Detects supported generic envelope shapes
* Produces deterministic schema suffix names

This layer defines **what the server officially marks as contract‑aware**.

### 3. Schema composition layer

`AutoWrapperSchemaCustomizer`:

* Registers composed OpenAPI schemas
* Uses `allOf` to extend the canonical `ServiceResponse` base schema
* Avoids generating synthetic component schemas

### 4. Vendor extension signaling layer

Schemas are enriched with semantic hints:

| Extension key              | Meaning                          |
| -------------------------- | -------------------------------- |
| `x-api-wrapper`            | marks schema as contract wrapper |
| `x-api-wrapper-datatype`   | underlying payload schema        |
| `x-data-container`         | present only for `Page<T>`       |
| `x-data-item`              | inner pagination item type       |
| `x-class-extra-annotation` | optional generator hint          |

These extensions form the **semantic bridge between spec and generated client code**.

### 5. Client generation interpretation layer

Custom OpenAPI Generator templates consume these hints to produce classes such as:

```
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

This eliminates envelope duplication and preserves generic type intent.

---

## End‑to‑end contract flow

```
[customer-service]
      ↓ publishes deterministic spec
OpenAPI 3.1 + vendor extensions
      ↓ consumed by generator
Thin generic client wrappers
      ↓ used by consumer services
Type‑safe integration
```

---

## Error contract model

Successful responses use the canonical envelope.

Error responses follow **RFC 9457 Problem Details** using Spring `ProblemDetail`.

This establishes a clean architectural separation:

* Success → envelope contract
* Failure → problem contract

Such separation improves:

* Client error decoding clarity
* API semantic consistency
* Contract evolvability

---

## Running the reference service

This module depends on the shared `api-contract` artifact published to Maven Central.

### Run locally

```
cd customer-service
mvn clean package
java -jar target/customer-service-*.jar
```

Service base URL:

```
http://localhost:8084/customer-service
```

### Run with Docker

```
cd customer-service
docker compose up --build
```

The container image builds only this module and resolves dependencies from Maven Central.

---

## Verifying the contract

Example request:

```
curl -X GET http://localhost:8084/customer-service/v1/customers/1
```

Example successful response:

```json
{
  "data": {
    "customerId": 1,
    "name": "Jane Doe",
    "email": "jane@example.com"
  },
  "meta": {
    "serverTime": "2025-01-01T12:34:56Z",
    "sort": []
  }
}
```

If this shape matches the generated client model expectations, the **contract publication pipeline is working correctly**.

---

## OpenAPI endpoints

* Swagger UI
  [http://localhost:8084/customer-service/swagger-ui/index.html](http://localhost:8084/customer-service/swagger-ui/index.html)

* OpenAPI JSON
  [http://localhost:8084/customer-service/v3/api-docs](http://localhost:8084/customer-service/v3/api-docs)

* OpenAPI YAML
  [http://localhost:8084/customer-service/v3/api-docs.yaml](http://localhost:8084/customer-service/v3/api-docs.yaml)

These specifications are the **canonical source of truth** for generated clients.

---

## Related module

* `customer-service-client` — generated Java client consuming this service’s deterministic OpenAPI contract

Together, these modules demonstrate **spec producer / spec consumer symmetry**.

---

## Testing

```
cd customer-service
mvn verify
```

---

## Contributing

Contributions and architectural discussions are welcome.

Please open an issue or start a discussion in the repository.

---

## License

MIT License — see repository root `LICENSE` file.

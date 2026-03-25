# Spring Boot OpenAPI Generics — Contract Lifecycle Architecture for Type‑Safe API Boundaries

[![Build](https://github.com/bsayli/spring-boot-openapi-generics-clients/actions/workflows/build.yml/badge.svg)](https://github.com/bsayli/spring-boot-openapi-generics-clients/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/tag/bsayli/spring-boot-openapi-generics-clients?logo=github\&label=release)](https://github.com/bsayli/spring-boot-openapi-generics-clients/releases/latest)
[![CodeQL](https://github.com/bsayli/spring-boot-openapi-generics-clients/actions/workflows/codeql.yml/badge.svg)](https://github.com/bsayli/spring-boot-openapi-generics-clients/actions/workflows/codeql.yml)
[![codecov](https://codecov.io/gh/bsayli/spring-boot-openapi-generics-clients/branch/main/graph/badge.svg)](https://codecov.io/gh/bsayli/spring-boot-openapi-generics-clients)
[![Java](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.12-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![OpenAPI Generator](https://img.shields.io/badge/OpenAPI%20Generator-7.21.0-blue?logo=openapiinitiative)](https://openapi-generator.tech/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

<p align="center">
  <img src="docs/images/cover/cover.png" alt="Generics-Aware OpenAPI Contract Lifecycle" width="720"/>
  <br/>
  <em>
    Deterministic generic response contracts — published once, interpreted consistently,
    consumed safely across service boundaries.
  </em>
</p>

---

## 📑 Table of Contents

- 🧭 [Architectural Thesis](#architectural-thesis)
- 🧩 [What This Repository Is (and Is Not)](#what-this-repository-is-and-is-not)
- 🔁 [Contract Lifecycle Model](#contract-lifecycle-model)
- 🚨 [The Problem](#the-problem)
- 🧭 [Where This Architecture Helps](#where-this-architecture-helps)
- 💡 [Core Architectural Idea](#core-architectural-idea)
- 🧱 [Canonical Contract Scope](#canonical-contract-scope)
- 🚫 [Architectural Non-Goals](#architectural-non-goals)
- 🏗 [System Architecture Overview](#system-architecture-overview)
- 🔎 [Proof — Generated Client Models (Before / After)](#proof-generated-client-models-before--after)
- 🧠 [Design Guarantees](#design-guarantees)
- 📦 [Modules](#modules)
- ⚡ [Quick Start](#quick-start)
- 📘 [Adoption Guides](#adoption-guides)
- 🔗 [References](#references)
- 🤝 [Contributing](#contributing)
- 🛡 [License](#license)

---

## Architectural Thesis

Modern distributed systems frequently standardize HTTP responses using generic envelopes such as:

```
ServiceResponse<T>
```

However, conventional OpenAPI-based client generation pipelines often **reinterpret or flatten these contracts**, leading to duplicated DTO hierarchies, loss of nested generic semantics, and unstable integration boundaries.

This repository demonstrates a different architectural stance:

> **API client generation should preserve contract semantics — not redefine them.**

It presents a cohesive **contract lifecycle architecture** where a single canonical response model is:

This repository demonstrates that approach through a minimal but end-to-end reference implementation.

* defined once as a shared runtime abstraction
* published deterministically through an OpenAPI specification
* interpreted semantically during client generation
* consumed safely behind stable application boundaries

---

> **Background reading**
>
> This repository demonstrates the implementation pattern.  
> For the architectural reasoning and real-world context behind it:
>
> - [We Made OpenAPI Generator Think in Generics](https://medium.com/@baris.sayli/type-safe-generic-api-responses-with-spring-boot-3-4-openapi-generator-and-custom-templates-ccd93405fb04)

---

## What This Repository Is (and Is Not)

This repository is presented as a **reference architecture and engineering pattern**,  
not as a production-ready framework or drop-in library.

It is intended to demonstrate:

- how a canonical response contract can be shared across service boundaries
- how OpenAPI can act as a semantic projection layer rather than a contract generator
- how client regeneration can be made deterministic and architecturally safe
- how generic response semantics can be preserved without duplicating envelope models

It is **not intended to provide**:

- a complete platform solution
- a universal OpenAPI generator replacement
- a mandatory architectural prescription for all service ecosystems

Instead, it offers a **focused, evolvable pattern** that teams may adapt,
extend, or selectively adopt based on their integration maturity level.

---

## Contract Lifecycle Model

The repository demonstrates an end‑to‑end contract flow rather than an isolated tooling example.

```
┌────────────────────────────────────┐
│ Canonical Contract Definition      │
│ (shared runtime abstraction)       │
└───────────────┬────────────────────┘
                ↓
┌────────────────────────────────────┐
│ Deterministic OpenAPI Publication  │
│ (semantic projection layer)        │
└───────────────┬────────────────────┘
                ↓
┌────────────────────────────────────┐
│ Semantics-Aware Client Generation  │
│ (thin wrapper inheritance)         │
└───────────────┬────────────────────┘
                ↓
┌────────────────────────────────────┐
│ Adapter-Bound Application Usage    │
│ (stable integration boundary)      │
└────────────────────────────────────┘
```

This lifecycle ensures that **response semantics remain stable across producer and consumer systems**, even as APIs evolve.

---

## The Problem

Typical OpenAPI client generation approaches introduce several long‑term risks:

* generic response envelopes are flattened into endpoint‑specific DTOs
* nested container intent (such as pagination) is lost
* schema naming becomes unstable across specification evolution
* client models drift from server runtime contracts
* regeneration churn leaks into application code

These issues increase integration fragility and reduce architectural clarity.

---

## Where This Architecture Helps

This approach is particularly useful in systems where:

- multiple microservices share a **standard response envelope**
- pagination and metadata must remain **structurally consistent**
- API contracts evolve independently of client release cycles
- teams want to avoid **client-side DTO drift** and duplicated wrappers
- generated clients must remain **safe to regenerate** without breaking application code

Typical examples include:

- internal platform APIs
- BFF layers aggregating multiple backend services
- domain services exposing standardized integration contracts
- organizations adopting **contract-driven integration governance**

In these environments, preserving generic response semantics becomes
an architectural stability concern rather than a tooling preference.

---

## Core Architectural Idea

This project treats the response envelope as a **shared domain contract**, not as a generated artifact.

Key principles:

* the server publishes **semantic intent**, not implementation shapes
* the client **reuses canonical contract types** instead of regenerating them
* OpenAPI acts as a **semantic bridge**, not as a source of runtime truth

Everything revolves around a single stable abstraction:

```
ServiceResponse<T>
```

---

## Canonical Contract Scope

The shared contract module defines deterministic guarantees for supported response shapes.

| Shape                      | Supported | Rationale                               |
| -------------------------- | --------- | --------------------------------------- |
| `ServiceResponse<T>`       | ✅         | canonical success envelope              |
| `ServiceResponse<Page<T>>` | ✅         | deterministic nested generic support    |
| `ServiceResponse<List<T>>` | ⚠️        |  treated as non-contract-aware shape (generator default behavior) |
| Arbitrary nested generics  | ❌         | outside intentional architectural scope |

This constrained design prioritizes **schema stability and long‑term evolvability** over exhaustive generic coverage.

---

## Architectural Non-Goals

This reference architecture intentionally does **not attempt to solve**:

- full generic type preservation for arbitrary nested container shapes
- language-agnostic client generation parity across all ecosystems
- runtime contract negotiation or schema registry responsibilities
- API versioning strategies or backward compatibility governance
- transport-layer concerns such as resilience patterns or service discovery

The focus is deliberately constrained to:

> **preserving deterministic response envelope semantics during OpenAPI-driven client generation.**

By keeping the scope explicit, the architecture remains
predictable, maintainable, and safe to evolve over time.

---

## System Architecture Overview

<p align="center">
  <img src="docs/images/architecture/architectural-diagram.png"
       alt="Contract Lifecycle Architecture"
       width="900"/>
</p>

```
[API Producer Service]
   └─ publishes OpenAPI 3.1 specification
        └─ enriched with wrapper semantics
              ↓
[Generated Client Layer]
   └─ thin generic wrappers extending canonical contract
        └─ HTTP invoker + transport configuration
              ↓
[Consumer Application]
   └─ depends on stable adapter interfaces only
```

### Architectural Design Choices

* **Contract‑first mindset** — runtime contract abstractions are defined independently of generation tools
* **Single envelope identity** — success responses share a unified `{ data, meta }` structure
* **Explicit generic scope** — nested generics are intentionally constrained
* **Generator governance** — minimal template overlays control evolution risk
* **Adapter isolation boundary** — generated APIs are never exposed directly to business layers

---

## Proof: Generated Client Models (Before / After)

> **Illustrative comparison**
>
> The following simplified example highlights the structural difference  
> between conventional OpenAPI generator output and contract-bound wrapper rendering.

### Before — flattened envelope models

In many conventional OpenAPI client generation setups,
endpoint-specific response envelope DTOs are produced,
which may duplicate canonical contract structures
and obscure nested generic intent.

<p align="center">
  <img src="docs/images/proof/generated-client-wrapper-before.png" width="820"/>
</p>

Typical generator output:

```java
class ServiceResponsePageCustomerDto {
  PageCustomerDto data;
  Meta meta;
}
```

This approach:

* duplicates response envelope structure per endpoint
* loses `Page<CustomerDto>` generic semantics
* introduces long-term schema drift risk

---

### After — thin contract-bound wrapper inheritance

<p align="center">
  <img src="docs/images/proof/generated-client-wrapper-after.png" width="820"/>
</p>

With the contract lifecycle architecture:

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

Result:

* canonical envelope is **not regenerated**
* nested generic semantics are **preserved**
* runtime serialization model remains **consistent across producer and consumer**

This demonstrates how OpenAPI becomes a **semantic projection layer**
instead of a contract re-definition mechanism.

---

## Design Guarantees

This reference architecture demonstrates the following engineering guarantees:

* **Contract identity preservation** — server and client share the same runtime envelope types
* **Deterministic schema naming** for supported generic shapes
* **Generator evolution containment** via explicit template governance
* **Semantic symmetry** between success envelopes and RFC 9457 error propagation
* **Integration boundary stability** through adapter‑driven consumption

OpenAPI is treated as a **projection layer of contract semantics**, not as the authoritative runtime model.

---

## Modules

* **[api-contract](api-contract/README.md)**
  Framework‑agnostic canonical response contract including pagination primitives and error extensions.

* **[customer-service](customer-service/README.md)**
  Spring Boot API producer publishing a generics‑aware deterministic OpenAPI specification.

* **[customer-service-client](customer-service-client/README.md)**
  Generated Java client demonstrating semantic contract preservation through thin wrapper inheritance.

Together these modules form a **producer–consumer symmetry pattern** for type‑safe API integration.

---

## Quick Start

### Run the producer service

```
cd customer-service
mvn clean package
java -jar target/customer-service-*.jar
```

### Regenerate the client after contract changes

```
cd customer-service-client
curl -s http://localhost:8084/customer-service/v3/api-docs.yaml \
  -o src/main/resources/customer-api-docs.yaml

mvn clean install
```

Generated sources are written to:

```
customer-service-client/target/generated-sources/openapi/src/gen/java
```

---

## Adoption Guides

Step-by-step integration guides live under [`docs/adoption`](docs/adoption):

- **[Server-Side Adoption](docs/adoption/server-side-adoption.md)** — Publish a deterministic, generics-aware OpenAPI 3.1 contract.
- **[Client-Side Adoption](docs/adoption/client-side-adoption.md)** — Configure Maven, OpenAPI Generator, and Mustache templates.

---

## References

- 📘 **Adoption Guide (GitHub Pages)**  
  [Spring Boot OpenAPI Generics — Adoption Guide](https://bsayli.github.io/spring-boot-openapi-generics-clients/)

- ✍️ **Medium Article**  
  [We Made OpenAPI Generator Think in Generics](https://medium.com/@baris.sayli/type-safe-generic-api-responses-with-spring-boot-3-4-openapi-generator-and-custom-templates-ccd93405fb04)

- 📄 **RFC 9457**  
  [Problem Details for HTTP APIs](https://www.rfc-editor.org/rfc/rfc9457)

---

## Contributing

Architectural discussions, real‑world usage feedback, and evolution proposals are welcome.

Please open an issue or start a discussion in the repository.

👉 [issues](https://github.com/bsayli/spring-boot-openapi-generics-clients/issues)

👉 [Discussions](https://github.com/bsayli/spring-boot-openapi-generics-clients/discussions)

---

## License

Licensed under **MIT** — see [LICENSE](LICENSE).

All modules inherit the same license.

---

**Barış Saylı**
[GitHub](https://github.com/bsayli) · [Medium](https://medium.com/@baris.sayli) · [LinkedIn](https://www.linkedin.com/in/bsayli)

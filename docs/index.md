---
layout: default
title: Home
nav_order: 1
description: "OpenAPI Generics for Spring Boot — keep your API contract intact end-to-end."
permalink: /
---

# OpenAPI Generics for Spring Boot

> Keep your Java API contract intact across OpenAPI projection and generated clients.

OpenAPI Generics is a Java/Spring specialization for preserving generic contract identity across OpenAPI projection and generated clients.

Instead of treating generated OpenAPI models as new contract ownership, OpenAPI Generics treats OpenAPI as a deterministic projection of your Java contract and reconstructs that contract on the client side.

```text
Java Contract
      ↓
OpenAPI Projection
      ↓
Contract Metadata
      ↓
Deterministic Client Reconstruction
```

The result:

> Generated clients reconstruct your contract instead of redefining it.

[Get Started](#get-started) · [Documentation](#documentation) · [Samples](#samples) · [GitHub Repository](https://github.com/blueprint-platform/openapi-generics)

---

## Contents

- [The Problem](#the-problem)
- [Before vs After](#before-vs-after)
- [What's New in 1.2.1](#whats-new-in-121)
- [Key Features](#key-features)
- [How It Works](#how-it-works)
- [Get Started](#get-started)
- [Documentation](#documentation)
- [Samples](#samples)
- [Compatibility](#compatibility)
- [Community](#community)

---

## The Problem

A typical Spring controller returns a generic contract such as:

```java
ResponseEntity<ServiceResponse<Page<CustomerDto>>> getCustomers()
```

A standard OpenAPI client generation flow often materializes that contract as a new generated wrapper model:

```java
class ServiceResponsePageCustomerDto {
    PageCustomerDto data;
    Meta meta;
}
```

The shape may look equivalent, but the original contract identity is no longer preserved.

The envelope is duplicated, generic structure is flattened into generated models, and every service boundary can introduce another reinterpretation of the same contract.

With OpenAPI Generics:

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

The generated wrapper becomes a thin type binding.

The original envelope remains contract authority, while generated code provides transport integration around it.

---

## Before vs After

<table>
<tr>
<td align="center"><b>Default OpenAPI Generator</b></td>
<td align="center"><b>OpenAPI Generics</b></td>
</tr>
<tr>
<td><img src="https://raw.githubusercontent.com/blueprint-platform/openapi-generics/main/docs/images/proof/generated-client-wrapper-before.png" width="450"/></td>
<td><img src="https://raw.githubusercontent.com/blueprint-platform/openapi-generics/main/docs/images/proof/generated-client-wrapper-after.png" width="450"/></td>
</tr>
</table>

**Without OpenAPI Generics**

- duplicated wrapper models
- flattened generic contracts
- growing generated model graphs
- additional mapping layers
- more opportunities for contract drift

**With OpenAPI Generics**

- shared envelope contract
- preserved generic structure
- reusable external DTOs through BYOC
- built-in and application-defined container reconstruction
- deterministic generated clients
- OpenAPI metadata carrying contract identity across the generation boundary

---

## What's New in 1.2.1

OpenAPI Generics 1.2.1 completes the contract-driven reconstruction model by carrying Java envelope identity in the generated OpenAPI document through `x-api-wrapper-type`.

Together with the container identity metadata introduced in 1.2 through `x-data-container-type`, the document now carries the contract identity required by the Java generator to reconstruct both platform-owned and application-owned generic response contracts.

```text
Java Contract
      ↓
OpenAPI Projection
      ↓
Envelope + Container Identity
      ↓
Generated Client Reconstruction
```

For aligned 1.2.1 producer and codegen components, this removes the need to repeat the envelope declaration on the client through `openapi-generics.envelope`.

The same reconstruction model applies across built-in and application-owned contracts, including:

```java
ServiceResponse<Page<CustomerDto>>
ApiResponse<Window<CustomerDto>>
```

### 1.2.1 highlights

- Contract-driven envelope reconstruction through `x-api-wrapper-type`
- No duplicate client-side envelope configuration for aligned 1.2.1 components
- Application-defined generic containers with both built-in and BYOE envelopes
- Dedicated compatibility coverage for standard multipart, binary download, and form-urlencoded transport flows
- Spring Boot 3 and Spring Boot 4 reference-stack verification
- OpenAPI Generator 7.24.0 verification
- Full backward compatibility with 1.2.0 runtime contracts

No contract migration is required for existing 1.2 users.

For the complete release history, see the [Changelog](https://github.com/blueprint-platform/openapi-generics/blob/main/CHANGELOG.md).

---

## Key Features

| Feature                            | Description                                                                 |
|------------------------------------|-----------------------------------------------------------------------------|
| **Contract-first**                 | Java contracts remain the source of truth.                                  |
| **BYOE**                           | Reuse your own response envelope instead of migrating to a platform wrapper. |
| **BYOC**                           | Reuse externally owned DTOs instead of generating duplicates.               |
| **Application-defined containers** | Register custom generic containers such as `Paging<T>` or `Window<T>`.       |
| **Contract-driven reconstruction** | Reconstruct envelope identity directly from projected OpenAPI metadata.      |
| **Container-aware reconstruction** | Built-in and configured containers share one deterministic pipeline.        |
| **Deterministic reconstruction**   | Stable projection, validation, and contract-aligned client reconstruction.   |
| **Generated-source hygiene**       | Deterministic cleanup of duplicate and unused generated imports.             |
| **Fail-fast validation**           | Invalid contract metadata and projection states fail during generation.      |
| **Standard transport compatibility** | Generic reconstruction coexists with standard Java RestClient transport generation. |

---

## How It Works

```text
Spring Boot Application
      ↓
openapi-generics-server-starter
      ↓
OpenAPI + Contract Metadata
      ↓
openapi-generics-java-codegen-parent
      ↓
java-generics-contract
      ↓
Contract-Aligned Java Client
```

The server-side projection phase discovers generic response contracts and publishes the metadata needed to preserve them across OpenAPI.

Key metadata includes:

- `x-api-wrapper`
- `x-api-wrapper-type`
- `x-api-wrapper-datatype`
- `x-data-container`
- `x-data-container-type`
- `x-data-item`
- `x-ignore-model`

The Java code generator consumes that metadata and reconstructs contract-aligned wrapper types.

The generated OpenAPI document remains valid OpenAPI and can still be consumed by standard tooling.

OpenAPI Generics does not fork OpenAPI Generator.

It adds a Java/Spring specialization layer on top of the upstream generator while leaving standard transport generation behavior intact.

---

## Get Started

OpenAPI Generics integrates at two points in the contract lifecycle:

```text
Java Producer Contract
        ↓
OpenAPI Projection
        ↓
Contract-Aware Client Reconstruction
```

### Producer

Add `openapi-generics-server-starter` to the Spring Boot service that owns the Java contract.

The starter participates when Springdoc generates the OpenAPI document. It discovers supported generic response contracts and projects the metadata required for deterministic client reconstruction without changing application request handling.

For the built-in `ServiceResponse<T>` contract, no envelope configuration is required. BYOE envelopes and application-defined generic containers can be declared as part of the producer contract model.

See [Server-Side Adoption](adoption/server-side-adoption.md) for dependency setup, BYOE configuration, custom containers, validation, and projection behavior.

### Client

Use `openapi-generics-java-codegen-parent` with the official OpenAPI Generator Maven plugin and select:

```xml
<generatorName>java-generics-contract</generatorName>
```

Normal OpenAPI Generator choices remain consumer-controlled, including the input specification, client library, package layout, and generator options.

OpenAPI Generics prepares the reconstruction-specific environment and restores the projected Java contract semantics without replacing the ordinary OpenAPI Generator lifecycle.

See [Client-Side Adoption](adoption/client-side-adoption.md) for the complete Maven configuration, BYOC mappings, fallback behavior, and generated-source setup.

---

## Documentation

- [Architectural Rationale](architecture/rationale.md)  
  Why OpenAPI Generics exists, what problem it solves, and the architectural trade-offs behind the project.

- [Architecture](architecture/architecture.md)  
  Internal projection protocol, contract metadata model, reconstruction pipeline, and validation boundaries.

- [Server-Side Adoption](adoption/server-side-adoption.md)  
  Publish generics-aware OpenAPI documents from Spring Boot services without changing runtime behavior.

- [Client-Side Adoption](adoption/client-side-adoption.md)  
  Generate Java clients that reconstruct generic contracts instead of redefining them.

- [Compatibility & Support Policy](compatibility.md)  
  Supported and verified Java, Spring Boot, Springdoc, OpenAPI Generator, and build-time boundaries.

- [GitHub Repository](https://github.com/blueprint-platform/openapi-generics)  
  Source code, releases, issues, and discussions.

---

## Samples

Repository samples validate the complete contract lifecycle:

```text
Producer
    ↓
OpenAPI
    ↓
Generated Client
    ↓
Consumer Runtime
```

The maintained sample suite covers Spring Boot 3 and Spring Boot 4 reference stacks, built-in `ServiceResponse<T>` contracts, BYOE envelopes, application-defined generic containers, type coverage, and standard transport interoperability.

[Explore the sample projects](https://github.com/blueprint-platform/openapi-generics/tree/main/samples) for runnable end-to-end examples.

---

## Compatibility

OpenAPI Generics currently supports:

- **Java:** 17+
- **Spring Boot:** 3.4.x, 3.5.x, and 4.x
- **springdoc-openapi:** 2.x with Spring Boot 3.x, and 3.x with Spring Boot 4.x
- **OpenAPI Generator:** 7.x
- **Server integration:** Spring WebMvc
- **Client generation:** Maven

The repository maintains exact Spring Boot 3, Spring Boot 4, and OpenAPI Generator reference baselines to verify compatibility without narrowing support to those exact versions.

See the authoritative [Compatibility & Support Policy](compatibility.md) for the full matrix, verified reference baselines, and support boundaries.

---

## Community

OpenAPI Generics is open source under the MIT License.

- [GitHub Discussions](https://github.com/blueprint-platform/openapi-generics/discussions)
- [GitHub Issues](https://github.com/blueprint-platform/openapi-generics/issues)
- [GitHub Repository](https://github.com/blueprint-platform/openapi-generics)

Questions, bug reports, design discussions, and real-world adoption feedback are welcome.

---

<!-- Scarf tracking pixel - Docs Home -->
<img referrerpolicy="no-referrer-when-downgrade"
src="https://static.scarf.sh/a.png?x-pxid=6d755e89-9777-4da0-9e21-018c95a2755d&page=docs-home"
width="1" height="1" alt=""
style="display:none;" />
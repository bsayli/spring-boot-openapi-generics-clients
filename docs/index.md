---
layout: default
title: Home
nav_order: 1
description: "OpenAPI Generics for Spring Boot — keep your API contract intact end-to-end."
permalink: /
---

# OpenAPI Generics for Spring Boot

> Keep your Java API contract intact across OpenAPI projection and generated clients.

OpenAPI Generics is a contract-preserving OpenAPI generation platform for Spring Boot that keeps your Java contracts consistent from server implementation to generated client.

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

OpenAPI Generics 1.2.1 completes the contract-driven reconstruction model by allowing the generated OpenAPI document to carry the envelope identity required by the Java client generator.

The new `x-api-wrapper-type` vendor extension preserves the fully qualified Java envelope type in projected wrapper schemas.

It complements the container identity metadata introduced in 1.2 through `x-data-container-type`.

```text
Java Contract
      ↓
OpenAPI Projection
      ↓
Envelope Identity
Container Identity
Payload / Item Identity
      ↓
Generated Client Reconstruction
```

For aligned 1.2.1 producer and codegen components, the Java generator can now reconstruct the envelope directly from the OpenAPI document.

That removes the need to repeat the same envelope declaration on the client side through `openapi-generics.envelope`.

```text
Producer configuration
        ↓
Java envelope contract
        ↓
x-api-wrapper-type
        ↓
OpenAPI document
        ↓
Generated client
```

Built-in response contracts continue to work unchanged:

```java
ServiceResponse<T>
ServiceResponse<List<T>>
ServiceResponse<Set<T>>
ServiceResponse<Page<T>>
```

The same reconstruction model applies to application-owned envelopes:

```java
ApiResponse<T>
ApiResponse<List<T>>
ApiResponse<Set<T>>
ApiResponse<Page<T>>
```

Application-defined generic containers introduced in 1.2 remain part of the same projection and reconstruction pipeline:

```yaml
openapi-generics:
  envelope:
    type: io.example.contract.ApiResponse

  containers:
    - type: io.example.contract.Paging
      item-property: content

    - type: io.example.contract.Window
      item-property: items
```

This supports contract shapes such as:

```java
ApiResponse<Paging<CustomerDto>>
ApiResponse<Window<CustomerDto>>
```

Application-defined containers are independent from BYOE and work with the built-in envelope as well:

```java
ServiceResponse<Window<CustomerDto>>
ServiceResponse<Batch<CustomerDto>>
```

### 1.2.1 highlights

- Contract-driven envelope reconstruction through `x-api-wrapper-type`
- No duplicate client-side envelope configuration for aligned 1.2.1 components
- Application-defined generic container validation with both built-in and BYOE envelopes
- Dedicated transport compatibility coverage for multipart, binary download, and form-urlencoded flows
- Dedicated server-starter exception hierarchy for configuration, descriptor validation, projection, and generated contract validation failures
- Spring Boot 3.5.16 and Springdoc 2.9.0 alignment for the Spring Boot 3 reference line
- Spring Boot 4.1.0, Springdoc 3.1.0, and Java 25 LTS reference coverage
- OpenAPI Generator 7.24.0 verification
- Full backward compatibility with 1.2.0 runtime contracts

No contract migration is required for existing 1.2 users.

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
| **Deterministic generation**       | Stable projection, validation, and generated client output.                  |
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

```text
Projection
   │
   ▼
OpenAPI Document
   │
   ▼
Reconstruction
```

The generated OpenAPI document remains valid OpenAPI and can still be consumed by standard tooling.

OpenAPI Generics does not fork OpenAPI Generator.

It adds a Java/Spring specialization layer on top of the upstream generator while leaving standard transport generation behavior intact.

---

## Get Started

### Producer

Add the server starter:

```xml
<dependency>
    <groupId>io.github.blueprint-platform</groupId>
    <artifactId>openapi-generics-server-starter</artifactId>
    <version>1.2.1</version>
</dependency>
```

The starter participates only when Springdoc generates the OpenAPI document.

It does not intercept application requests or alter endpoint runtime behavior.

For the built-in `ServiceResponse<T>` envelope, no envelope configuration is required.

If your application owns its response envelope:

```yaml
openapi-generics:
  envelope:
    type: io.example.contract.ApiResponse
```

Optional application-defined containers can be registered on the producer as part of the same contract model.

---

### Client

Use the OpenAPI Generics codegen parent:

```xml
<parent>
    <groupId>io.github.blueprint-platform</groupId>
    <artifactId>openapi-generics-java-codegen-parent</artifactId>
    <version>1.2.1</version>
</parent>
```

Configure the generator:

```xml
<generatorName>java-generics-contract</generatorName>
```

With aligned 1.2.1 producer and codegen components, no duplicate client-side envelope declaration is required.

Generate the client:

```bash
mvn clean install
```

The resulting wrappers reuse the original contract types instead of redefining them.

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

Repository samples validate the complete:

```text
Producer
    ↓
OpenAPI
    ↓
Generated Client
    ↓
Consumer Runtime
```

lifecycle.

- [Sample Projects](https://github.com/blueprint-platform/openapi-generics/tree/main/samples)  
  Spring Boot 3 and Spring Boot 4 end-to-end stacks, built-in `ServiceResponse<T>` coverage, BYOE coverage, type coverage, and transport compatibility validation.

Current sample topology includes:

```text
samples
├── domain-contracts
├── spring-boot-3
├── spring-boot-4
├── type-coverage
│   ├── service-response
│   └── byoe-response
└── transport-coverage
```

The `transport-coverage` sample verifies standard Java RestClient generation for:

- `multipart/form-data`
- `application/octet-stream`
- `application/x-www-form-urlencoded`

while generics-aware response reconstruction remains enabled.

---

## Compatibility

Published platform artifacts target:

- Java 17+
- Spring Boot WebMvc
- springdoc-openapi WebMvc integration
- OpenAPI Generator 7.x
- Maven-based client generation

The 1.2.1 reference verification line includes:

- Spring Boot 3.5.16 with Springdoc 2.9.0
- Spring Boot 4.1.0 with Springdoc 3.1.0
- Java 21 for Spring Boot 3 samples
- Java 25 LTS for Spring Boot 4 reference samples
- OpenAPI Generator 7.24.0

Support policy is broader than a single reference baseline.

See the full [Compatibility & Support Policy](compatibility.md) for the authoritative matrix and support boundaries.

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

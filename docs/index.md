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

Instead of treating OpenAPI as the source of truth, OpenAPI Generics treats it as a deterministic projection of your Java contract.

```text
Java Contract
      ↓
OpenAPI Projection
      ↓
Deterministic Client Reconstruction
```

The result is simple:

> Generated clients reconstruct your contract instead of redefining it.

[Get Started](#get-started) · [Samples](#samples) · [GitHub Repository](https://github.com/blueprint-platform/openapi-generics)

---

## Contents

- [The Problem](#the-problem)
- [Before vs After](#before-vs-after)
- [What's New in 1.2](#whats-new-in-12)
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

Default OpenAPI Generator usually materializes a new wrapper model:

```java
class ServiceResponsePageCustomerDto {
    PageCustomerDto data;
    Meta meta;
}
```

The envelope is duplicated, generics are flattened, and every service boundary introduces another copy of the same contract.

With OpenAPI Generics:

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

The generated wrapper becomes a thin type binding while the original contract remains the single source of truth.

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
- flattened generic types
- growing model graph
- additional mapping layers

**With OpenAPI Generics**

- shared contract envelope
- preserved generic structure
- reusable external DTOs (BYOC)
- deterministic generated clients

---

## What's New in 1.2

OpenAPI Generics 1.2 extends the container-aware reconstruction model introduced in 1.1.

The platform now supports application-defined generic containers while keeping the same contract-first projection and deterministic client reconstruction pipeline.

Built-in response shapes continue to work unchanged:

```java
ServiceResponse<T>
ServiceResponse<List<T>>
ServiceResponse<Set<T>>
ServiceResponse<Page<T>>
```

The same reconstruction model is also available when using your own shared response envelope:

```java
ApiResponse<T>
ApiResponse<List<T>>
ApiResponse<Set<T>>
ApiResponse<Page<T>>
```

Applications can now register custom generic container contracts, for example:

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

Configured containers participate in the same projection, metadata generation, and client reconstruction pipeline as the built-in container types.

Highlights:

- Application-defined generic container support
- Java container identity preservation through `x-data-container-type`
- Cleaner generated Java sources
- Improved projection metadata validation
- Expanded regression coverage
- Full backward compatibility with 1.1.x

No migration is required for existing users.

---

## Key Features

| Feature                            | Description                                                          |
|------------------------------------|----------------------------------------------------------------------|
| **Contract-first**                 | Java remains the source of truth.                                    |
| **BYOE**                           | Reuse your own response envelope.                                    |
| **BYOC**                           | Reuse externally owned DTOs.                                         |
| **Application-defined containers** | Register custom generic containers.                                  |
| **Container-aware reconstruction** | Built-in and configured containers share one deterministic pipeline. |
| **Deterministic generation**       | Stable projection, validation, and generated clients.                |
| **Generated-source hygiene**       | Cleaner Java imports and generated artifacts.                        |

---

## How It Works

```text
Spring Boot
      ↓
openapi-generics-server-starter
      ↓
OpenAPI + Vendor Extensions
      ↓
openapi-generics-java-codegen-parent
      ↓
java-generics-contract
      ↓
Contract-Aligned Java Client
```

Projection publishes contract semantics through vendor extensions.

Client generation consumes those semantics and reconstructs the original Java contract instead of creating alternative models.

The generated OpenAPI document remains standard OpenAPI and is fully consumable by existing tooling.

---

## Get Started

### Producer

```xml
<dependency>
    <groupId>io.github.blueprint-platform</groupId>
    <artifactId>openapi-generics-server-starter</artifactId>
    <version>1.2.0</version>
</dependency>
```

### Client

```xml
<parent>
    <groupId>io.github.blueprint-platform</groupId>
    <artifactId>openapi-generics-java-codegen-parent</artifactId>
    <version>1.2.0</version>
</parent>
```

Configure:

```xml
<generatorName>java-generics-contract</generatorName>
```

Generate:

```bash
mvn clean install
```

---

## Documentation

- [Architectural Rationale](architecture/rationale.md)  
  Why OpenAPI Generics exists, what problem it solves, and the architectural trade-offs behind the project.

- [Server-Side Adoption](adoption/server-side-adoption.md)  
  Publish generics-aware OpenAPI documents from Spring Boot services without changing runtime behavior.

- [Client-Side Adoption](adoption/client-side-adoption.md)  
  Generate Java clients that reconstruct generic contracts instead of redefining them.

- [Architecture](architecture/architecture.md)  
  Internal projection and reconstruction model of OpenAPI Generics.

- [Compatibility & Support Policy](compatibility.md)  
  Supported Java, Spring Boot, springdoc-openapi, OpenAPI Generator, and build-time boundaries.

- [GitHub Repository](https://github.com/blueprint-platform/openapi-generics)  
  Source code, releases, issues, and discussions.

---

## Samples

Repository samples demonstrate the complete producer → OpenAPI → generated client → consumer lifecycle.

- [Sample Projects](https://github.com/blueprint-platform/openapi-generics/tree/main/samples)  
  Includes Spring Boot 3 and Spring Boot 4 end-to-end samples, built-in `ServiceResponse<T>` coverage, BYOE coverage, and type-coverage validation.

---

## Compatibility

Supported platforms:

- Java 17+
- Spring Boot 3.4.x / 3.5.x / 4.x
- Spring WebMvc
- springdoc-openapi
- OpenAPI Generator 7.x

See the full [Compatibility & Support Policy](compatibility.md).

---

## Community

- [GitHub Discussions](https://github.com/blueprint-platform/openapi-generics/discussions)
- [GitHub Issues](https://github.com/blueprint-platform/openapi-generics/issues)

OpenAPI Generics is released under the MIT License.

<!-- Scarf tracking pixel - Docs Home -->
<img referrerpolicy="no-referrer-when-downgrade"
src="https://static.scarf.sh/a.png?x-pxid=6d755e89-9777-4da0-9e21-018c95a2755d&page=docs-home"
width="1" height="1" alt=""
style="display:none;" />
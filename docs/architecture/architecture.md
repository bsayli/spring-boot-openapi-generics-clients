---
layout: default
title: Architecture
nav_exclude: true
---

# Architecture

> Internal architecture of OpenAPI Generics.

OpenAPI Generics preserves contract semantics across the complete Java → OpenAPI → Client lifecycle.

Its architecture is based on a simple principle:

> Java contracts are the source of truth.  
> OpenAPI is a deterministic projection of those contracts.  
> Generated clients reconstruct the original contract from projected metadata.

---

## Contents

- [Architecture Overview](#architecture-overview)
- [Projection Protocol](#projection-protocol)
- [Projection](#projection)
- [Client Generation](#client-generation)
- [Deterministic Pipeline](#deterministic-pipeline)
- [Validation and Failure Boundaries](#validation-and-failure-boundaries)
- [Supported Scope](#supported-scope)
- [Module Map](#module-map)
- [Mental Model](#mental-model)

---

## Architecture Overview

OpenAPI Generics consists of two independent phases connected by the generated OpenAPI document.

```text
Spring Boot Service
        │
        ▼
openapi-generics-server-starter
        │
        ▼
OpenAPI + Contract Metadata
        │
        ▼
openapi-generics-java-codegen
        │
        ▼
Generated Java Client
```

The server publishes the metadata required to preserve contract identity.

The client generator consumes that metadata to reconstruct contract-aligned Java types without requiring the same envelope information to be configured again on the client side.

The generated OpenAPI document therefore acts as the protocol boundary between projection and reconstruction.

---

## Projection Protocol

Contract semantics are projected through OpenAPI vendor extensions.

A projected generic wrapper schema may contain metadata such as:

```yaml
x-api-wrapper: true
x-api-wrapper-type: io.example.contract.ServiceResponse
x-api-wrapper-datatype: PageCustomerDto
x-data-container: Page
x-data-container-type: io.github.blueprintplatform.openapi.generics.contract.paging.Page
x-data-item: CustomerDto
x-ignore-model: true
```

The extension model is intentionally flat and semantic.

The principal extensions are:

- `x-api-wrapper` — marks a projected schema as a generic response wrapper
- `x-api-wrapper-type` — preserves the fully qualified Java envelope type
- `x-api-wrapper-datatype` — identifies the projected wrapper payload datatype
- `x-data-container` — identifies generic container semantics
- `x-data-container-type` — preserves the fully qualified Java container type
- `x-data-item` — identifies the concrete item or payload type
- `x-ignore-model` — marks infrastructure schemas that should not become standalone generated models

Together, these extensions preserve the identities required for deterministic client reconstruction while keeping the document valid OpenAPI.

Envelope identity is carried by `x-api-wrapper-type`.

This completes the same contract-driven metadata model used for container identity through `x-data-container-type`.

```text
Envelope identity
        +
Container identity
        +
Payload / item identity
        │
        ▼
Deterministic client reconstruction
```

For aligned 1.2.1 producer and codegen components, the envelope type no longer needs to be repeated through client-side `openapi-generics.envelope` configuration.

---

## Projection

`openapi-generics-server-starter` analyzes Java response contracts and projects their semantics into OpenAPI.

Its responsibilities include:

- response contract discovery
- generic type introspection
- supported wrapper unwrapping
- generic container resolution
- wrapper schema projection
- contract metadata enrichment
- infrastructure schema exclusion
- generated contract validation

Built-in response contracts include:

```java
ServiceResponse<T>
ServiceResponse<List<T>>
ServiceResponse<Set<T>>
ServiceResponse<Page<T>>
```

Applications may also register their own generic container contracts.

Example:

```yaml
openapi-generics:
  containers:
    - type: io.example.contract.Paging
      item-property: content

    - type: io.example.contract.Window
      item-property: items
```

Configured containers participate in the same discovery, descriptor, projection, metadata, and reconstruction model as built-in containers.

Examples include:

```java
ServiceResponse<Window<CustomerDto>>
ServiceResponse<Batch<CustomerDto>>
```

Application-defined containers are independent from BYOE.

Applications that own their envelope can configure it on the producer:

```yaml
openapi-generics:
  envelope:
    type: io.example.contract.ApiResponse
```

The projected wrapper schema then carries the envelope identity through `x-api-wrapper-type`, allowing the generated OpenAPI document to become the source of reconstruction metadata for the client generator.

The server starter operates only as part of OpenAPI document generation.

It does not intercept application requests and does not alter endpoint runtime behavior.

---

## Client Generation

`GenericAwareJavaCodegen` extends OpenAPI Generator with contract awareness.

Its responsibilities include:

- contract-driven wrapper reconstruction
- envelope type reconstruction from `x-api-wrapper-type`
- container-aware reconstruction
- application-defined container reconstruction from `x-data-container-type`
- BYOE support
- BYOC support
- infrastructure model filtering
- deterministic generated-source hygiene

Generated wrappers intentionally remain thin.

Example:

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

The generated wrapper does not redefine the envelope contract.

It binds concrete generic parameters to an existing contract type.

The same reconstruction model applies to application-owned envelopes:

```java
public class ApiResponseWindowCustomerDto
    extends ApiResponse<Window<CustomerDto>> {}
```

The envelope and configured container types must be available on the client classpath when the generated wrapper references them.

OpenAPI Generics specializes the upstream Java generator rather than maintaining a fork.

The upstream generator continues to own standard client and transport generation behavior, while OpenAPI Generics adds the metadata-aware reconstruction layer required for generic contract preservation.

---

## Deterministic Pipeline

The complete lifecycle is:

```text
Java Contract
      ↓
Type Introspection
      ↓
OpenAPI Projection
      ↓
Contract Metadata
      ↓
Generated Client Reconstruction
      ↓
Contract-Aligned Java Types
```

For a given supported contract and configuration, projection and reconstruction are designed to remain deterministic.

OpenAPI acts as the boundary between the two phases:

```text
Producer
   │
   │  publishes contract metadata
   ▼
OpenAPI Document
   │
   │  consumed by code generation
   ▼
Generated Client
```

This separation prevents the client generator from depending directly on producer implementation details.

In 1.2.1, envelope identity joins container identity as contract metadata carried through that boundary.

As a result, aligned producer and codegen components reconstruct the wrapper hierarchy from the document itself instead of duplicating envelope configuration on both sides.

---

## Validation and Failure Boundaries

The projection and reconstruction pipelines are intentionally fail-fast.

The server starter validates configuration and projected contract state before publishing unsupported or ambiguous metadata.

Its failure model distinguishes platform-specific categories such as:

- configuration failures
- descriptor validation failures
- projection failures
- generated contract validation failures

These exception categories improve diagnostic precision without changing the runtime scope of the starter.

The code generation pipeline also performs deterministic validation and generated-source hygiene to detect incompatible template or metadata changes during the build rather than at application runtime.

Repository verification covers the complete lifecycle:

```text
Producer
    ↓
OpenAPI
    ↓
Generated Client
    ↓
Consumer Runtime
```

Verification includes:

- built-in `ServiceResponse<T>` reconstruction
- BYOE reconstruction
- built-in containers
- application-defined containers
- `x-api-wrapper-type` generation and consumption
- `x-data-container-type` generation and consumption
- removal of redundant client-side envelope configuration
- BYOC external model reuse
- infrastructure model filtering
- generated-source hygiene
- standard HTTP transport compatibility

Transport coverage verifies that generics-aware reconstruction does not interfere with standard OpenAPI Generator Java RestClient behavior for:

- `multipart/form-data`
- `application/octet-stream`
- `application/x-www-form-urlencoded`

---

## Supported Scope

Built-in platform contracts:

```java
ServiceResponse<T>
ServiceResponse<List<T>>
ServiceResponse<Set<T>>
ServiceResponse<Page<T>>
```

Supported capabilities include:

- BYOE — Bring Your Own Envelope
- BYOC — Bring Your Own Contract
- application-defined generic containers
- deterministic OpenAPI contract metadata
- contract-driven envelope reconstruction
- container-aware reconstruction
- contract-aware Java client generation
- generated-source hygiene
- fail-fast contract validation

The current architectural scope is intentionally focused on:

- Java 17+ for published platform artifacts
- Spring Boot WebMvc
- springdoc-openapi WebMvc integration
- OpenAPI Generator 7.x
- Maven-based client generation

These boundaries define the platform and tooling surface OpenAPI Generics currently owns. Exact verified framework and generator versions are maintained separately from the architecture definition.

For the authoritative compatibility matrix, verified reference baselines, and support policy, see [Compatibility & Support Policy](../compatibility.md).

---

## Module Map

| Module                                                                                                                                          | Responsibility                                                                  |
|-------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------|
| [`openapi-generics-contract`](https://github.com/blueprint-platform/openapi-generics/tree/main/openapi-generics-contract)                       | Shared contracts and platform-owned generic types                               |
| [`openapi-generics-server-starter`](https://github.com/blueprint-platform/openapi-generics/tree/main/openapi-generics-server-starter)           | Spring Boot contract discovery, projection, metadata enrichment, and validation |
| [`openapi-generics-java-codegen`](https://github.com/blueprint-platform/openapi-generics/tree/main/openapi-generics-java-codegen)               | Contract-aware OpenAPI Generator specialization and reconstruction              |
| [`openapi-generics-java-codegen-parent`](https://github.com/blueprint-platform/openapi-generics/tree/main/openapi-generics-java-codegen-parent) | Generator orchestration, template lifecycle, and generated-source hygiene       |
| [`openapi-generics-platform-bom`](https://github.com/blueprint-platform/openapi-generics/tree/main/openapi-generics-platform-bom)               | Dependency alignment                                                            |

---

## Mental Model

```text
Java Contract
      ↓
OpenAPI Projection
      ↓
Contract Metadata
      ↓
Contract Reconstruction
```

The producer owns the Java contract.

OpenAPI transports the metadata required to preserve that contract across the generation boundary.

The generated client reconstructs the contract instead of redefining it.

OpenAPI Generics does not make generated code the contract authority.

It preserves the original contract identity end-to-end.

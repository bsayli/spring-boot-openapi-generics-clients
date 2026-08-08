---
layout: default
title: Server-Side Adoption
parent: Adoption Guides
nav_order: 1
has_toc: false
---

# Server-Side Adoption

> Publish a generics-aware OpenAPI document from your Spring Boot service without changing runtime behavior.

This guide explains how to enable OpenAPI Generics on the producer side.

For client generation, see [Client-Side Adoption](./client-side-adoption.md).  
For implementation details, see [Architecture](../architecture/architecture.md).

---

## Contents

- [Quick Start](#quick-start)
- [What the Starter Does](#what-the-starter-does)
- [Supported Controller Shapes](#supported-controller-shapes)
- [Application-Defined Generic Containers](#application-defined-generic-containers)
- [BYOE — Bring Your Own Envelope](#byoe)
- [Published Contract Metadata](#published-contract-metadata)
- [Validation and Failure Behavior](#validation-and-failure-behavior)
- [Verification](#verification)
- [Further Reading](#further-reading)

---

## Quick Start

Add the starter:

```xml
<dependency>
    <groupId>io.github.blueprint-platform</groupId>
    <artifactId>openapi-generics-server-starter</artifactId>
    <version>1.2.1</version>
</dependency>
```

Write controller methods normally:

```java
@GetMapping("/{id}")
public ResponseEntity<ServiceResponse<CustomerDto>> getCustomer(...) {
    return ResponseEntity.ok(ServiceResponse.of(service.findById(id)));
}
```

Container payloads are supported as well:

```java
ServiceResponse<List<CustomerDto>>
ServiceResponse<Set<CustomerDto>>
ServiceResponse<Page<CustomerDto>>
```

No controller-specific OpenAPI Generics annotations or runtime response adapters are required for supported shapes.

Application-defined generic containers are also supported when registered through the optional `openapi-generics.containers` configuration.

---

## What the Starter Does

The starter runs only when Springdoc generates an OpenAPI document, for example:

```text
/v3/api-docs
/v3/api-docs.yaml
```

It:

- discovers supported generic response contracts
- introspects wrapper, container, and payload types
- projects generic wrapper schemas into OpenAPI
- preserves envelope identity through `x-api-wrapper-type`
- preserves container identity through `x-data-container-type`
- publishes the remaining OpenAPI Generics vendor extensions
- validates configuration, descriptors, projection state, and generated contract metadata
- marks infrastructure or contract-owned schemas so they can be excluded from generated client models

It does **not**:

- intercept HTTP requests
- change runtime serialization
- modify controller behavior
- replace Spring MVC request handling
- alter standard endpoint transport behavior

Projection pipeline:

```text
Java Contract
      ↓
Response Introspection
      ↓
Envelope / Container Resolution
      ↓
OpenAPI Projection
      ↓
Contract Metadata Enrichment
      ↓
Contract Validation
      ↓
Validated OpenAPI Document
```

The generated OpenAPI document becomes the boundary between server-side projection and client-side reconstruction.

---

## Supported Controller Shapes

Built-in contracts include:

```java
ServiceResponse<T>
ServiceResponse<List<T>>
ServiceResponse<Set<T>>
ServiceResponse<Page<T>>
```

BYOE envelopes participate in the same response-shape model.

Application-defined generic containers such as `Paging<T>`, `Window<T>`, or `Batch<T>` participate in the same projection pipeline when registered through configuration.

Examples:

```java
ServiceResponse<Window<CustomerDto>>
ServiceResponse<Batch<CustomerDto>>
```

and, with a BYOE envelope:

```java
ApiResponse<Paging<CustomerDto>>
ApiResponse<Window<CustomerDto>>
```

Supported asynchronous wrappers are unwrapped automatically during response type introspection, including:

```java
CompletionStage<T>
Future<T>
DeferredResult<T>
WebAsyncTask<T>
```

OpenAPI Generics reconstructs supported generic contract shapes after the framework wrapper has been unwrapped.

---

## Application-Defined Generic Containers

Applications may register their own generic container contracts.

Example:

```yaml
openapi-generics:
  containers:
    - type: io.example.contract.Paging
      item-property: content

    - type: io.example.contract.Window
      item-property: items
```

Each configured container contributes:

- the fully qualified Java container type
- the property that contains the generic item collection
- the metadata required to preserve container identity through OpenAPI

Configured containers participate in the same descriptor, projection, metadata, and reconstruction model as built-in container types.

The projected OpenAPI metadata preserves the Java container type through:

```yaml
x-data-container-type: io.example.contract.Window
```

This prevents the client generator from requiring container-specific reconstruction logic.

Application-defined containers are independent from BYOE.

You may use them with the built-in platform envelope:

```java
ServiceResponse<Window<CustomerDto>>
```

or with an application-owned envelope:

```java
ApiResponse<Window<CustomerDto>>
```

---

## BYOE — Bring Your Own Envelope

Use your own shared response envelope instead of `ServiceResponse<T>`.

Configure the envelope on the producer:

```yaml
openapi-generics:
  envelope:
    type: io.example.contract.ApiResponse
```

You can combine BYOE with application-defined generic containers:

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

The configured envelope becomes the contract owner for projected wrapper schemas.

In 1.2.1, its fully qualified Java identity is preserved through:

```yaml
x-api-wrapper-type: io.example.contract.ApiResponse
```

This is significant because the generated OpenAPI document now carries the envelope identity required by the Java code generator.

For aligned 1.2.1 producer and codegen components, the same envelope no longer needs to be configured again on the client side through `openapi-generics.envelope`.

```text
Producer configuration
        ↓
Java envelope contract
        ↓
x-api-wrapper-type
        ↓
OpenAPI document
        ↓
Generated client reconstruction
```

The producer remains responsible for publishing the envelope metadata.

The generated client is responsible for consuming it.

The envelope type itself must still be available on the generated client's classpath when generated wrapper classes extend it.

---

## Published Contract Metadata

OpenAPI Generics projects contract semantics through vendor extensions.

A built-in container wrapper may look like:

```yaml
x-api-wrapper: true
x-api-wrapper-type: io.github.blueprintplatform.openapi.generics.contract.response.ServiceResponse
x-api-wrapper-datatype: PageCustomerDto
x-data-container: Page
x-data-container-type: io.github.blueprintplatform.openapi.generics.contract.paging.Page
x-data-item: CustomerDto
```

An application-defined container with a BYOE envelope may look like:

```yaml
x-api-wrapper: true
x-api-wrapper-type: io.example.contract.ApiResponse
x-api-wrapper-datatype: WindowCustomerDto
x-data-container: Window
x-data-container-type: io.example.contract.Window
x-data-item: CustomerDto
```

The principal metadata fields are:

- `x-api-wrapper` — identifies a projected generic wrapper schema
- `x-api-wrapper-type` — preserves the fully qualified Java envelope type
- `x-api-wrapper-datatype` — identifies the projected wrapper payload datatype
- `x-data-container` — identifies container semantics
- `x-data-container-type` — preserves the fully qualified Java container type
- `x-data-item` — identifies the concrete item or payload type
- `x-ignore-model` — marks infrastructure or contract-owned schemas that should not become generated models

Infrastructure schemas that are contract-owned or externally provided may therefore also be marked with:

```yaml
x-ignore-model: true
```

Together, these metadata allow the client generator to reconstruct the original Java contract deterministically.

---

## Validation and Failure Behavior

The server starter follows fail-fast validation.

Invalid configuration or unsupported projected contract state fails while the OpenAPI document is being generated rather than producing ambiguous metadata for downstream code generation.

The 1.2.1 server-side failure model distinguishes platform-specific categories covering:

- configuration failures
- generic container descriptor validation failures
- projection failures
- generated contract validation failures

This exception model improves categorization while preserving the existing fail-fast behavior and diagnostic intent.

Typical validation includes:

- envelope configuration validity
- configured container type validity
- configured container generic structure
- configured container item-property validity
- supported response shape discovery
- projected wrapper metadata consistency
- generated contract metadata validation

Because the starter participates only in OpenAPI document generation, these failures belong to the contract publication path rather than normal HTTP request processing.

---

## Verification

After starting the application, verify that the generated OpenAPI document contains OpenAPI Generics metadata.

### Built-in container example

```yaml
x-api-wrapper: true
x-api-wrapper-type: io.github.blueprintplatform.openapi.generics.contract.response.ServiceResponse
x-api-wrapper-datatype: PageCustomerDto
x-data-container: Page
x-data-container-type: io.github.blueprintplatform.openapi.generics.contract.paging.Page
x-data-item: CustomerDto
```

### Application-defined container example

```yaml
x-api-wrapper: true
x-api-wrapper-type: io.github.blueprintplatform.openapi.generics.contract.response.ServiceResponse
x-api-wrapper-datatype: WindowCustomerDto
x-data-container: Window
x-data-container-type: io.example.contract.Window
x-data-item: CustomerDto
```

### BYOE example

```yaml
x-api-wrapper: true
x-api-wrapper-type: io.example.contract.ApiResponse
x-api-wrapper-datatype: PagingCustomerDto
x-data-container: Paging
x-data-container-type: io.example.contract.Paging
x-data-item: CustomerDto
```

Contract-owned infrastructure schemas may also contain:

```yaml
x-ignore-model: true
```

For 1.2.1, producer verification should confirm that:

- wrapper schemas contain `x-api-wrapper-type`
- configured containers preserve their Java identity through `x-data-container-type`
- built-in and BYOE envelopes publish the same reconstruction metadata model
- application-defined containers work with both built-in and BYOE envelopes
- the OpenAPI document is generated successfully without introducing runtime request behavior changes

The repository includes end-to-end verification across producer → OpenAPI → generated client → consumer flows.

---

## Further Reading

- [Client-Side Adoption](./client-side-adoption.md)
- [Architecture](../architecture/architecture.md)
- [Compatibility & Support Policy](../compatibility.md)
- [GitHub Discussions](https://github.com/blueprint-platform/openapi-generics/discussions)

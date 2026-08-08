# openapi-generics-server-starter

> Contract-aware OpenAPI projection for Spring Boot services.

`openapi-generics-server-starter` projects supported Java response contracts into OpenAPI while preserving the metadata required for deterministic client reconstruction.

It does not define contracts or generate clients.

Its responsibility is simple:

> **Java contract → OpenAPI projection**

---

## Contents

- [What It Does](#what-it-does)
- [Supported Response Shapes](#supported-response-shapes)
- [Projection Pipeline](#projection-pipeline)
- [Generated Metadata](#generated-metadata)
- [Usage](#usage)
- [BYOE](#byoe)
- [Out of Scope](#out-of-scope)

---

## What It Does

The starter runs when Springdoc generates an OpenAPI document.

It:

- discovers supported generic response contracts
- projects wrapper and container metadata into OpenAPI
- publishes OpenAPI Generics vendor extensions
- validates generated contract metadata

The generated document remains standard OpenAPI and can be consumed by any OpenAPI tooling.

---

## Supported Response Shapes

Built-in contracts:

```java
ServiceResponse<T>
ServiceResponse<List<T>>
ServiceResponse<Set<T>>
ServiceResponse<Page<T>>
```

BYOE supports the same response shapes:

```java
ApiResponse<T>
ApiResponse<List<T>>
ApiResponse<Set<T>>
ApiResponse<Page<T>>
```

Application-defined generic containers (for example `Paging<T>` or `Window<T>`) are also supported when registered through configuration.

---

## Projection Pipeline

```text
Java Contract
      ↓
Contract Discovery
      ↓
OpenAPI Projection
      ↓
Vendor Extensions
      ↓
Validated OpenAPI Document
```

The pipeline executes only while generating `/v3/api-docs` or `/v3/api-docs.yaml`.

---

## Generated Metadata

The starter enriches wrapper schemas with canonical metadata used for deterministic client reconstruction.

Example:

```yaml
x-api-wrapper: true
x-api-wrapper-type: io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse
x-api-wrapper-datatype: PageCustomerDto
x-data-container: Page
x-data-container-type: io.github.blueprintplatform.openapi.generics.contract.paging.Page
x-data-item: CustomerDto
```

Infrastructure or externally provided models are marked with:

```yaml
x-ignore-model: true
```

---

## Usage

```xml
<dependency>
    <groupId>io.github.blueprint-platform</groupId>
    <artifactId>openapi-generics-server-starter</artifactId>
    <version>1.2.1</version>
</dependency>
```

---

## BYOE

Configure your existing response envelope:

```yaml
openapi-generics:
  envelope:
    type: io.example.contract.ApiResponse

  # Optional
  containers:
    - type: io.example.contract.Paging
      item-property: content

    - type: io.example.contract.Window
      item-property: items
```

The configured envelope becomes the published contract.

Optional application-defined containers participate in the same projection model as the built-in `List<T>`, `Set<T>`, and platform `Page<T>` containers.

---

## Out of Scope

This module does not:

- define contract types
- generate Java clients
- modify runtime HTTP behavior
- perform business validation
- support arbitrary nested generic graphs
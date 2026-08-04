# openapi-generics-java-codegen

> Contract-aware Java generator extension for OpenAPI Generics.

`openapi-generics-java-codegen` provides the custom OpenAPI Generator implementation used by OpenAPI Generics.

It registers the generator named:

```text
java-generics-contract
```

This module is build-time only. In normal usage, consumers do not depend on it directly. It is wired through:

```text
openapi-generics-java-codegen-parent
```

Its responsibility is:

> **OpenAPI metadata → contract-aligned Java client types**

---

## Contents

- [What It Does](#what-it-does)
- [Generation Pipeline](#generation-pipeline)
- [Wrapper Reconstruction](#wrapper-reconstruction)
- [BYOE](#byoe)
- [BYOC](#byoc)
- [Wrapper Metadata](#wrapper-metadata)
- [Container Metadata](#container-metadata)
- [Model Filtering](#model-filtering)
- [Out of Scope](#out-of-scope)
- [Mental Model](#mental-model)

---

## What It Does

This module extends OpenAPI Generator’s Java client generation with OpenAPI Generics semantics.

It:

- detects wrapper schemas marked with `x-api-wrapper`
- derives envelope metadata from OpenAPI wrapper metadata
- reconstructs generic container payloads from OpenAPI metadata
- imports container Java types through `x-data-container-type`
- reuses externally owned DTOs through BYOC mappings
- filters infrastructure schemas marked with `x-ignore-model`
- generates thin contract-aligned wrapper classes

It consumes metadata already present in the OpenAPI document.

It does not discover server-side Java contracts or create OpenAPI schemas.

---

## Generation Pipeline

```text
OpenAPI Models
      ↓
Ignored / External Model Resolution
      ↓
Wrapper Metadata Resolution
      ↓
Container Metadata Consumption
      ↓
External Import Resolution
      ↓
Wrapper Reconstruction
      ↓
Generated Java Client
```

The generator operates entirely on OpenAPI metadata and reconstructs Java types that preserve the original generic contract shape.

---

## Wrapper Reconstruction

OpenAPI wrapper schemas are reconstructed as thin Java subclasses.

Simple wrapper:

```java
public class ServiceResponseCustomerDto
    extends ServiceResponse<CustomerDto> {
}
```

Built-in container wrapper:

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {
}
```

BYOE wrapper:

```java
public class ApiResponseListCustomerDto
    extends ApiResponse<List<CustomerDto>> {
}
```

Application-defined container wrapper:

```java
public class ApiResponsePagingCustomerDto
    extends ApiResponse<Paging<CustomerDto>> {
}
```

The wrapper superclass is derived directly from the `x-api-wrapper-type` vendor extension.

Generated wrappers intentionally contain no behavior. Their role is to bind generic parameters to contract-owned envelope and container types.

---

## BYOE

Bring Your Own Envelope (BYOE) is configured only on the producer side.

During OpenAPI projection the producer publishes the resolved Java envelope identity as metadata:

```yaml
x-api-wrapper-type: io.example.contract.ApiResponse
```

The generator derives the wrapper superclass directly from this metadata.

Default envelope:

```java
ServiceResponse<T>
```

Configured envelope example:

```java
ApiResponse<T>
```

No client-side envelope configuration is required.

The envelope type simply needs to be available on the generated client's compile classpath.

---

## BYOC

BYOC maps OpenAPI model names to externally owned Java types.

```xml
<additionalProperty>
  openapi-generics.response-contract.CustomerDto=io.example.contract.CustomerDto
</additionalProperty>
```

Effect:

- the mapped model is not generated
- wrapper classes import the external type
- generated clients reuse the contract-owned DTO

This keeps DTO ownership outside generated code.

---

## Wrapper Metadata

Wrapper reconstruction is driven by canonical OpenAPI vendor extensions.

Example:

```yaml
x-api-wrapper: true
x-api-wrapper-type: io.example.contract.ApiResponse
x-api-wrapper-datatype: PagingCustomerDto
```

The generator uses:

- `x-api-wrapper` to identify projected wrapper schemas
- `x-api-wrapper-type` as the fully qualified Java wrapper type
- `x-api-wrapper-datatype` as the resolved generic payload type published by the producer

The OpenAPI document is the single source of truth for wrapper identity.

---

## Container Metadata

Container reconstruction is driven by OpenAPI vendor extensions.

Built-in container example:

```yaml
x-api-wrapper: true
x-api-wrapper-type: io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse
x-api-wrapper-datatype: PageCustomerDto
x-data-container: Page
x-data-container-type: io.github.blueprintplatform.openapi.generics.contract.paging.Page
x-data-item: CustomerDto
```

Application-defined container example:

```yaml
x-api-wrapper: true
x-api-wrapper-type: io.example.contract.ApiResponse
x-api-wrapper-datatype: PagingCustomerDto
x-data-container: Paging
x-data-container-type: io.example.contract.Paging
x-data-item: CustomerDto
```

The generator uses:

- `x-data-container` as the generic container name used in the generated wrapper
- `x-data-container-type` as the fully qualified Java import
- `x-data-item` as the generic item type

This allows built-in and application-defined generic containers to be reconstructed through the same template path.

---

## Model Filtering

Some schemas exist only as OpenAPI projection artifacts.

They must not become generated DTOs.

The generator filters models marked with:

```yaml
x-ignore-model: true
```

It also filters models registered through BYOC.

Ignored model imports are removed from generated wrapper models so wrappers do not reference non-generated classes.

---

## Out of Scope

This module does not:

- run Springdoc
- inspect controller return types
- create OpenAPI schemas
- orchestrate Maven plugin phases
- patch upstream templates
- own runtime application behavior

Those responsibilities belong to the server starter or the codegen parent.

---

## Mental Model

```text
OpenAPI + Vendor Extensions
        ↓
Contract-Aware Java Generation
        ↓
Thin Wrapper Types
```

This module does not generate additional business models.

It generates only the minimum Java wrapper types required to preserve the original generic contract defined by the OpenAPI document.
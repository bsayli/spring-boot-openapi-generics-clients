# type-coverage

Focused end-to-end validation samples for **OpenAPI Generics**.

Unlike the Spring Boot integration samples, the projects in this directory are not intended to demonstrate application architecture, business workflows, or general framework usage patterns.

Their purpose is to provide deterministic validation environments for verifying the core OpenAPI Generics pipeline and preventing regressions across future releases.

The suite validates that generic response contracts move through the complete lifecycle while preserving both **contract ownership** and **generic type identity**.

```text
Producer
    ↓
OpenAPI Projection
    ↓
Generated Client
    ↓
Consumer Runtime
```

## Table of Contents

- [Purpose](#purpose)
- [Available Samples](#available-samples)
- [Sample Boundaries](#sample-boundaries)
    - [service-response](#service-response)
    - [byoe-response](#byoe-response)
- [Validation Scope](#validation-scope)
- [Regression Role](#regression-role)
- [Mental Model](#mental-model)

## Purpose

The `type-coverage` suite exists to verify that OpenAPI Generics preserves Java contract semantics throughout the complete producer-to-consumer pipeline.

It focuses on:

- OpenAPI projection correctness
- vendor extension generation
- generated client reconstruction
- runtime deserialization
- consumer-side type compatibility
- contract ownership preservation
- regression protection across future releases

The objective is not endpoint functionality.

The objective is deterministic verification of **generic contract fidelity**.

## Available Samples

| Sample | Purpose |
| --- | --- |
| [`service-response`](service-response/README.md) | Validates the built-in `ServiceResponse<T>` envelope together with platform-provided and application-owned generic containers. |
| [`byoe-response`](byoe-response/README.md) | Validates Bring Your Own Envelope (BYOE) using an application-owned `ApiResponse<T>` contract together with application-owned generic containers. |

Each sample contains its own:

```text
contract
producer
client
consumer
spec
```

The producer publishes the OpenAPI contract, the client is generated from that contract, and the consumer verifies that the reconstructed types work correctly at runtime.

## Sample Boundaries

### service-response

The `service-response` sample validates the platform-provided response envelope:

```java
ServiceResponse<T>
```

Coverage includes:

```java
ServiceResponse<T>

ServiceResponse<List<T>>

ServiceResponse<Set<T>>

ServiceResponse<Page<T>>

ServiceResponse<Window<T>>

ServiceResponse<Batch<T>>
```

`Page<T>` is provided by OpenAPI Generics.

`Window<T>` and `Batch<T>` are application-owned generic containers registered with the server-side projection pipeline.

This sample verifies that application-owned generic containers can participate in the same projection and reconstruction pipeline while continuing to use the built-in `ServiceResponse<T>` envelope.

### byoe-response

The `byoe-response` sample validates a completely application-owned response envelope:

```java
ApiResponse<T>
```

Coverage includes:

```java
ApiResponse<T>

ApiResponse<List<T>>

ApiResponse<Set<T>>

ApiResponse<Page<T>>

ApiResponse<Paging<T>>

ApiResponse<Window<T>>
```

The envelope itself is application-owned.

The nested `Paging<T>` and `Window<T>` containers are also application-owned.

This sample verifies that both the response envelope and nested generic containers can remain under application ownership while OpenAPI Generics preserves their generic relationships through projection, client generation, and runtime reconstruction.

## Validation Scope

Across the suite, the samples verify:

- generic envelope reconstruction
- scalar payload handling
- value payload handling
- enum payload handling
- DTO payload handling
- `List<T>` reconstruction
- `Set<T>` reconstruction
- platform-provided generic container reconstruction
- application-owned generic container reconstruction
- BYOE reconstruction
- ignored infrastructure model handling
- external model handling
- generated wrapper inheritance
- generated Java type safety
- runtime deserialization
- producer → OpenAPI → generated client → consumer compatibility

The suite intentionally covers different contract ownership models.

Platform-provided envelope:

```text
ServiceResponse<T>
        +
platform/application-owned containers
```

Application-owned envelope:

```text
ApiResponse<T>
        +
platform/application-owned containers
```

This separation makes regressions easier to isolate and prevents one ownership model from masking failures in another.

## Regression Role

The `type-coverage` projects act as executable regression suites for the OpenAPI Generics contract pipeline.

They are intended to detect regressions in:

- response type introspection
- OpenAPI schema projection
- vendor extension generation
- vendor extension consistency
- wrapper metadata generation
- generic wrapper reconstruction
- container-aware reconstruction
- generated Java typing
- generated-source hygiene
- ignored model processing
- runtime deserialization
- producer → generated client compatibility
- generated client → consumer compatibility

before such regressions reach downstream integrations.

The samples therefore validate more than successful client generation.

They verify the complete contract lifecycle:

```text
Java Contract
      ↓
Response Introspection
      ↓
OpenAPI Projection
      ↓
Contract Metadata
      ↓
Generated Client Reconstruction
      ↓
Runtime Deserialization
      ↓
Consumer Usage
```

## Mental Model

The central invariant of the suite is simple:

```text
Original Java Contract
          ↓
OpenAPI Projection
          ↓
Generated Client
          ↓
Consumer Runtime
          ↓
Same Contract Shape
```

For the built-in envelope:

```text
ServiceResponse<Window<TypeSummaryDto>>
                ↓
        OpenAPI Projection
                ↓
        Generated Client
                ↓
ServiceResponse<Window<TypeSummaryDto>>
```

For BYOE:

```text
ApiResponse<Paging<TypeSummaryDto>>
               ↓
       OpenAPI Projection
               ↓
       Generated Client
               ↓
ApiResponse<Paging<TypeSummaryDto>>
```

The generated client may introduce thin binding types where required by OpenAPI Generator, but those generated types do not become the owners of the original envelope or generic container contracts.

The purpose of the `type-coverage` suite is to continuously verify that **generic contract shape, ownership, and type identity remain intact**, regardless of whether the envelope or nested generic containers are platform-provided or application-owned.
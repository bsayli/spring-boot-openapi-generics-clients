# service-response-type-coverage

> End-to-end verification sample for OpenAPI projection, client generation, and runtime reconstruction of `ServiceResponse<T>` contracts.

This sample validates that **generic type information survives the complete Producer → OpenAPI → Generated Client → Consumer pipeline** while using the built-in `ServiceResponse<T>` envelope together with both platform-provided and application-owned generic containers.

## Table of Contents

- [What This Sample Validates](#what-this-sample-validates)
- [Modules](#modules)
- [Covered Response Shapes](#covered-response-shapes)
    - [Scalar Payloads](#scalar-payloads)
    - [Value Payloads](#value-payloads)
    - [Object Payloads](#object-payloads)
    - [List Payloads](#list-payloads)
    - [Set Payloads](#set-payloads)
    - [Platform Container Payloads](#platform-container-payloads)
    - [Application-owned Container Payloads](#application-owned-container-payloads)
    - [Asynchronous Controller Wrappers](#asynchronous-controller-wrappers)
- [Running the Sample](#running-the-sample)
- [Quick Smoke Test](#quick-smoke-test)
- [Verification Endpoints](#verification-endpoints)
- [Mental Model](#mental-model)

## What This Sample Validates

The sample exercises the complete OpenAPI Generics lifecycle:

```text
Producer
    ↓
OpenAPI Projection
    ↓
Generated Client
    ↓
Consumer Runtime
```

It verifies that:

- OpenAPI projection preserves generic semantics.
- Generated wrappers preserve Java type identity.
- Runtime deserialization reconstructs the expected generic contracts.
- The consumer uses only generated client artifacts.
- No manual DTO mapping or wrapper reconstruction is required.

## Modules

```text
service-response-type-coverage
├── contract
├── producer
├── client
└── consumer
```

| Module | Responsibility |
| --- | --- |
| `contract` | Application-owned generic container contracts used by every module |
| `producer` | Publishes `ServiceResponse`-based endpoints |
| `client` | Generated Java client |
| `consumer` | Consumes the generated client without manual reconstruction |

## Covered Response Shapes

### Scalar Payloads

```java
ServiceResponse<String>
ServiceResponse<Boolean>
ServiceResponse<Integer>
ServiceResponse<Long>
ServiceResponse<BigDecimal>
```

### Value Payloads

```java
ServiceResponse<UUID>
ServiceResponse<LocalDate>
ServiceResponse<OffsetDateTime>
ServiceResponse<CoverageStatus>
```

### Object Payloads

```java
ServiceResponse<AddressDto>
ServiceResponse<TypeProfileDto>
```

### List Payloads

```java
ServiceResponse<List<TypeSummaryDto>>
ServiceResponse<List<CoverageStatus>>
```

### Set Payloads

```java
ServiceResponse<Set<TypeSummaryDto>>
ServiceResponse<Set<CoverageStatus>>
```

### Platform Container Payloads

```java
ServiceResponse<Page<TypeSummaryDto>>
ServiceResponse<Page<CoverageStatus>>
```

### Application-owned Container Payloads

```java
ServiceResponse<Window<TypeSummaryDto>>
ServiceResponse<Window<CoverageStatus>>

ServiceResponse<Batch<TypeSummaryDto>>
ServiceResponse<Batch<CoverageStatus>>

CompletionStage<ServiceResponse<TypeSummaryDto>>

DeferredResult<ServiceResponse<Page<TypeSummaryDto>>>
```

This demonstrates that application-owned generic containers can be reconstructed without replacing the built-in `ServiceResponse<T>` envelope.

### Asynchronous Controller Wrappers

```java
CompletionStage<ServiceResponse<TypeSummaryDto>>

DeferredResult<ServiceResponse<Page<TypeSummaryDto>>>
```

These endpoints verify the two asynchronous unwrapping branches used by the server-side
response introspection pipeline:

- `CompletionStage<T>` also represents the `Future<T>` branch.
- `DeferredResult<T>` also represents the `WebAsyncTask<T>` branch.

The asynchronous wrappers are server-side controller concerns only. They are removed before
generic contract projection, so the generated client reuses the existing
`ServiceResponseTypeSummaryDto` and `ServiceResponsePageTypeSummaryDto` binding types rather than
introducing asynchronous client-side contract types.

## Running the Sample

### Start Producer

```bash
cd producer
mvn spring-boot:run
```

Swagger UI:

```text
http://localhost:8074/type-coverage/service-response/swagger-ui/index.html
```

OpenAPI document:

```text
http://localhost:8074/type-coverage/service-response/v3/api-docs.yaml
```

### Start Consumer

```bash
cd consumer
mvn spring-boot:run
```

Use the verification endpoints below to validate the generated client at runtime.

## Quick Smoke Test

### Producer Verification

```bash
curl http://localhost:8074/type-coverage/service-response/types/scalars/string

curl http://localhost:8074/type-coverage/service-response/types/lists/summaries

curl http://localhost:8074/type-coverage/service-response/types/sets/statuses

curl http://localhost:8074/type-coverage/service-response/types/pages/summaries

curl http://localhost:8074/type-coverage/service-response/types/windows/summaries

curl http://localhost:8074/type-coverage/service-response/types/batches/statuses

curl http://localhost:8074/type-coverage/service-response/types/async/summary

curl http://localhost:8074/type-coverage/service-response/types/async/paged-summaries
```

Expected generic contracts:

```java
ServiceResponse<String>

ServiceResponse<List<TypeSummaryDto>>

ServiceResponse<Set<CoverageStatus>>

ServiceResponse<Page<TypeSummaryDto>>

ServiceResponse<Window<TypeSummaryDto>>

ServiceResponse<Batch<CoverageStatus>>
```

### Consumer Verification

```bash
curl http://localhost:8075/type-coverage/service-response-consumer/types/scalars/string

curl http://localhost:8075/type-coverage/service-response-consumer/types/lists/summaries

curl http://localhost:8075/type-coverage/service-response-consumer/types/sets/statuses

curl http://localhost:8075/type-coverage/service-response-consumer/types/pages/summaries

curl http://localhost:8075/type-coverage/service-response-consumer/types/windows/summaries

curl http://localhost:8075/type-coverage/service-response-consumer/types/batches/statuses
```

Successful responses verify that generic type information survives the entire pipeline unchanged.

## Verification Endpoints

### Scalars

```text
/types/scalars/*
```

### Values

```text
/types/values/*
```

### Objects

```text
/types/objects/*
```

### Lists

```text
/types/lists/*
```

### Sets

```text
/types/sets/*
```

### Platform Pages

```text
/types/pages/*
```

### Application-owned Containers

```text
/types/windows/*
/types/batches/*
```

### Asynchronous Controller Wrappers

```text
/types/async/summary
/types/async/paged-summaries
```

The sample validates:

```java
ServiceResponse<T>
ServiceResponse<List<T>>
ServiceResponse<Set<T>>
ServiceResponse<Page<T>>
ServiceResponse<Window<T>>
ServiceResponse<Batch<T>>
```

The producer additionally verifies supported asynchronous controller wrappers around direct
and paged response contracts. These wrappers are unwrapped before OpenAPI projection and do not
change the generated client contract shape.

Coverage spans primitive, value, enum, DTO, collection, platform container,
application-owned generic container, and asynchronous controller scenarios.

## Mental Model

```text
ServiceResponse<Window<TypeSummaryDto>>
                ↓
        OpenAPI Projection
                ↓
        Generated Client
                ↓
        Consumer Runtime
                ↓
ServiceResponse<Window<TypeSummaryDto>>
```

The same reconstruction flow applies across every supported response shape.

The objective is to continuously verify that **contract ownership and generic type identity remain intact** throughout the complete OpenAPI Generics pipeline.

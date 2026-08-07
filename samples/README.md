# samples

Minimal, runnable sample set for the OpenAPI Generics platform.

These samples demonstrate how a Java-based contract is exposed through OpenAPI, consumed through a generated client, and reused by a downstream service without structural drift.

The focus is contract flow:

```text
Producer Service
      ↓
Java Contract
      ↓
OpenAPI Projection
      ↓
Generated Client
      ↓
Consumer Service
```

The generated client reconstructs the original contract shape instead of redefining equivalent wrapper models.

## Table of Contents

- [Overview](#overview)
- [Sample Layout](#sample-layout)
- [Available Sample Types](#available-sample-types)
- [Prerequisites](#prerequisites)
- [Spring Boot 3 Stack](#spring-boot-3-stack)
- [Spring Boot 4 Stack](#spring-boot-4-stack)
- [Type Coverage Samples](#type-coverage-samples)
- [Transport Coverage Sample](#transport-coverage-sample)
- [Local Maven Build](#local-maven-build)
- [What to Observe](#what-to-observe)
- [Notes](#notes)
- [Summary](#summary)

## Overview

Each runnable integration stack contains three layers:

| Layer | Responsibility |
| --- | --- |
| **Producer** | Defines the Java API contract and publishes the OpenAPI document. |
| **Generated client** | Is generated from the OpenAPI document using OpenAPI Generics. |
| **Consumer** | Uses the generated client and exposes downstream verification endpoints. |

The same contract moves through all layers.

No manual wrapper reconstruction is required in the consumer.

No DTO translation layer is required between producer and consumer.

## Sample Layout

```text
samples
├── domain-contracts
├── spring-boot-3
│   ├── customer-service
│   ├── customer-service-client
│   └── customer-service-consumer
├── spring-boot-4
│   ├── customer-service
│   ├── customer-service-client
│   └── customer-service-consumer
├── type-coverage
│   ├── service-response
│   └── byoe-response
└── transport-coverage
```

| Directory | Purpose |
| --- | --- |
| `domain-contracts` | Shared domain and contract types used by the sample stacks. |
| `spring-boot-3` | Runnable Spring Boot 3 producer → generated client → consumer stack. |
| `spring-boot-4` | Runnable Spring Boot 4 producer → generated client → consumer stack. |
| `type-coverage` | Focused validation suites for supported generic response shapes and regression coverage. |
| `transport-coverage` | Focused validation suite for transport compatibility and regression coverage. |

## Available Sample Types

The repository contains three kinds of samples.

| Sample Type | Purpose |
| --- | --- |
| **Integration stacks** | Runnable producer → generated client → consumer applications for Spring Boot 3 and Spring Boot 4. |
| **Type coverage samples** | Focused regression suites validating generic response reconstruction across supported contract shapes. |
| **Transport coverage sample** | Focused regression suite validating standard HTTP transport compatibility together with generic response reconstruction. |

The integration stacks are intended for first-time users who want to run the platform end-to-end.

The type-coverage samples validate the core OpenAPI Generics reconstruction pipeline across many response shapes.

The transport-coverage sample validates that the custom `java-generics-contract` generator remains compatible with standard OpenAPI Generator transport behavior.

## Prerequisites

For Docker-based sample execution:

- Docker 20+
- Docker Compose v2

For local Maven builds:

- Java 25
- Maven 3.9+

The sample suite includes both Java 21 and Java 25 modules. Building the complete `samples` reactor therefore requires JDK 25.

Published OpenAPI Generics platform artifacts continue to target Java 17+.

## Spring Boot 3 Stack

Use this stack for the standard Spring Boot 3 validation path.

It runs the complete OpenAPI Generics lifecycle:

```text
Spring Boot 3 Producer
        ↓
OpenAPI Document
        ↓
Generated Java Client
        ↓
Spring Boot 3 Consumer
```

### Services

| Service | Port |
| --- | ---: |
| Producer | 8084 |
| Consumer | 8085 |

### Start

```bash
cd spring-boot-3
docker compose up --build -d
```

### Verify Producer

OpenAPI document:

```bash
curl http://localhost:8084/customer-service/v3/api-docs.yaml
```

Swagger UI:

```text
http://localhost:8084/customer-service/swagger-ui/index.html
```

### Verify Consumer

```bash
curl http://localhost:8085/customer-service-consumer/customers/1
```

```bash
curl "http://localhost:8085/customer-service-consumer/customers?page=0&size=5"
```

The consumer uses the generated client to call the producer.

The response should preserve the same contract shape across producer, OpenAPI, generated client, and consumer.

### Stop

```bash
docker compose down
```

## Spring Boot 4 Stack

Use this stack when validating the Spring Boot 4 runtime path.

It runs the same lifecycle:

```text
Spring Boot 4 Producer
        ↓
OpenAPI Document
        ↓
Generated Java Client
        ↓
Spring Boot 4 Consumer
```

### Services

| Service | Port |
| --- | ---: |
| Producer | 8094 |
| Consumer | 8095 |

### Start

```bash
cd spring-boot-4
docker compose up --build -d
```

### Verify Producer

OpenAPI document:

```bash
curl http://localhost:8094/customer-service/v3/api-docs.yaml
```

Swagger UI:

```text
http://localhost:8094/customer-service/swagger-ui/index.html
```

### Verify Consumer

```bash
curl http://localhost:8095/customer-service-consumer/customers/1
```

```bash
curl "http://localhost:8095/customer-service-consumer/customers?page=0&size=5"
```

The behavior is intentionally equivalent to the Spring Boot 3 stack.

### Stop

```bash
docker compose down
```

## Type Coverage Samples

The `type-coverage` directory contains focused validation suites for the core OpenAPI Generics reconstruction pipeline.

Unlike the Spring Boot integration stacks, these samples are not intended to demonstrate application architecture or business workflows.

They validate contract preservation across supported response shapes, including:

- scalar payloads
- value payloads
- enum payloads
- DTO payloads
- `List<T>`
- `Set<T>`
- built-in `Page<T>`
- application-owned generic containers
- platform-owned `ServiceResponse<T>`
- user-owned BYOE `ApiResponse<T>`

Available samples:

| Sample | Purpose |
| --- | --- |
| `type-coverage/service-response` | Validates the platform-provided `ServiceResponse<T>` contract. |
| `type-coverage/byoe-response` | Validates Bring Your Own Envelope using a user-owned `ApiResponse<T>` contract. |

See [`type-coverage/README.md`](type-coverage/README.md) for the complete validation matrix.

## Transport Coverage Sample

The `transport-coverage` directory validates that OpenAPI Generics remains transport-neutral.

Rather than introducing new transport behavior, it verifies that the custom `java-generics-contract` generator preserves the standard transport functionality provided by the upstream Java RestClient generator.

The initial transport matrix covers:

- `multipart/form-data`
- `application/octet-stream`
- `application/x-www-form-urlencoded`

while simultaneously validating generic response reconstruction where JSON responses are returned.

Available sample:

| Sample | Purpose |
| --- | --- |
| `transport-coverage` | Validates transport compatibility together with generic response reconstruction. |

See [`transport-coverage/README.md`](transport-coverage/README.md) for transport scenarios, validation flow, and regression scope.

## Local Maven Build

From the `samples` directory:

```bash
mvn clean install
```

This builds every sample module and verifies that generated client artifacts compile successfully.

Individual services can also be started directly from their module directories when needed.

## What to Observe

The important part is not the sample business domain.

The important part is that the contract shape remains stable across the full lifecycle.

Observe that:

- the producer owns the Java contract
- OpenAPI acts as a projection of that contract
- the generated client reconstructs the wrapper shape
- the consumer uses generated artifacts directly
- no manual DTO mapping is required between producer and consumer
- paginated generic responses keep their generic structure
- generated wrappers extend reusable contract types instead of redefining equivalent models

For example:

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

The generated class binds the generic parameters.

It does not become the owner of the envelope structure.

## Notes

- Samples use in-memory data.
- No external database is required.
- The sample domain is intentionally simple.
- Spring Boot 3 and Spring Boot 4 stacks are intentionally equivalent.
- Type-coverage samples validate generic reconstruction across supported contract shapes.
- Transport-coverage validates compatibility with standard OpenAPI Generator transport behavior.
- You do not need to run every sample simultaneously.

## Summary

These samples provide runnable environments for validating the OpenAPI Generics contract lifecycle.

Together they demonstrate:

- end-to-end producer → generated client → consumer integration
- generic response reconstruction
- Bring Your Own Envelope (BYOE)
- transport compatibility with standard OpenAPI Generator behavior

```text
Java Contract
      ↓
OpenAPI Projection
      ↓
Generated Client
      ↓
Consumer Reuse
```

The result is stable contract identity across service boundaries while preserving both generic semantics and standard transport behavior.
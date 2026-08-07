# byoe-response-type-coverage

> End-to-end verification sample for OpenAPI projection, client generation, and runtime reconstruction of user-owned `ApiResponse<T>` contracts.

This sample demonstrates the **Bring Your Own Envelope (BYOE)** capabilities of OpenAPI Generics.

Instead of using the platform-provided response envelope, the producer exposes endpoints using a completely application-owned generic contract:

```java
ApiResponse<T>
```

The sample verifies that OpenAPI Generics can:

- project user-owned generic response envelopes into OpenAPI
- reconstruct generic type information during client generation
- generate strongly typed Java clients
- deserialize responses back into the original application contract
- support scalar, value, object, collection, built-in page, and application-owned generic container payloads

without requiring changes to the original response contract.

---

## Table of Contents

- [What This Sample Validates](#what-this-sample-validates)
- [Modules](#modules)
- [User-Owned Envelope](#user-owned-envelope)
- [Covered Response Shapes](#covered-response-shapes)
- [Running the Sample](#running-the-sample)
- [Verification Endpoints](#verification-endpoints)
- [Mental Model](#mental-model)

---

## What This Sample Validates

The sample exercises the complete OpenAPI Generics flow:

```text
User-Owned Contract
        ↓
Spring Endpoint
        ↓
OpenAPI Projection
        ↓
Generated Client
        ↓
Consumer Runtime
```

The consumer uses only generated client artifacts.

No manual DTO mapping.

No manual envelope reconstruction.

No manual generic container reconstruction.

No custom deserialization code.

The generated client automatically reconstructs the original application contract together with any nested generic payload types.

---

## Modules

```text
byoe-response-type-coverage
├── contract
├── producer
├── client
└── consumer
```

| Module   | Responsibility                                                   |
|----------|------------------------------------------------------------------|
| contract | User-owned `ApiResponse` and generic payload container contracts |
| producer | Publishes `ApiResponse`-based endpoints                          |
| client   | Generated Java client                                            |
| consumer | Uses the generated client and exposes verification endpoints     |

---

## User-Owned Envelope

The sample uses a completely application-owned response envelope.

```java
public class ApiResponse<T> {

    private int status;

    private String message;

    private T data;

    private List<ApiError> errors;

}
```

Error model:

```java
public record ApiError(
    String code,
    String message
) {}
```

These classes are not provided by OpenAPI Generics.

OpenAPI Generics projects and reconstructs them while preserving the original application contract.

---

## Covered Response Shapes

### Scalar Payloads

```java
ApiResponse<String>
ApiResponse<Boolean>
ApiResponse<Integer>
ApiResponse<Long>
ApiResponse<BigDecimal>
```

### Value Payloads

```java
ApiResponse<UUID>
ApiResponse<LocalDate>
ApiResponse<OffsetDateTime>
ApiResponse<CoverageStatus>
```

### Object Payloads

```java
ApiResponse<AddressDto>
ApiResponse<TypeProfileDto>
```

### List Payloads

```java
ApiResponse<List<TypeSummaryDto>>
ApiResponse<List<CoverageStatus>>
```

### Set Payloads

```java
ApiResponse<Set<TypeSummaryDto>>
ApiResponse<Set<CoverageStatus>>
```

### Built-in Page Payloads

```java
ApiResponse<Page<TypeSummaryDto>>
ApiResponse<Page<CoverageStatus>>
```

using the built-in platform `Page<T>` contract.

### Application-owned Generic Container Payloads

```java
ApiResponse<Paging<TypeSummaryDto>>
ApiResponse<Paging<CoverageStatus>>

ApiResponse<Window<TypeSummaryDto>>
ApiResponse<Window<CoverageStatus>>
```

These payloads verify that nested application-owned generic containers are projected into OpenAPI and reconstructed by the generated client without requiring custom serialization, mapping, or adapter code.

---

## Running the Sample

### Start Producer

```bash
cd producer
mvn spring-boot:run
```

Swagger UI:

```text
http://localhost:8076/type-coverage/byoe-response/swagger-ui/index.html
```

OpenAPI document:

```text
http://localhost:8076/type-coverage/byoe-response/v3/api-docs.yaml
```

### Start Consumer

```bash
cd consumer
mvn spring-boot:run
```

Consumer URL:

```text
http://localhost:8077/type-coverage/byoe-response-consumer
```

---

## Quick Smoke Test

After starting both producer and consumer, a representative subset of endpoints can be called directly to verify that the generated client reconstructs the expected generic response types correctly.

These requests provide a fast end-to-end verification of:

- OpenAPI projection
- generated client generation
- user-owned envelope reconstruction
- nested generic payload reconstruction
- runtime deserialization
- consumer integration

### Producer Verification

```bash
curl http://localhost:8076/type-coverage/byoe-response/types/scalars/string

curl http://localhost:8076/type-coverage/byoe-response/types/lists/summaries

curl http://localhost:8076/type-coverage/byoe-response/types/sets/statuses

curl http://localhost:8076/type-coverage/byoe-response/types/pages/summaries

curl http://localhost:8076/type-coverage/byoe-response/types/paging/summaries

curl http://localhost:8076/type-coverage/byoe-response/types/windows/summaries
```

Expected generic response shapes:

```java
ApiResponse<String>

ApiResponse<List<TypeSummaryDto>>

ApiResponse<Set<CoverageStatus>>

ApiResponse<Page<TypeSummaryDto>>

ApiResponse<Paging<TypeSummaryDto>>

ApiResponse<Window<TypeSummaryDto>>
```

### Consumer Verification

```bash
curl http://localhost:8077/type-coverage/byoe-response-consumer/types/scalars/string

curl http://localhost:8077/type-coverage/byoe-response-consumer/types/lists/summaries

curl http://localhost:8077/type-coverage/byoe-response-consumer/types/sets/statuses

curl http://localhost:8077/type-coverage/byoe-response-consumer/types/pages/summaries

curl http://localhost:8077/type-coverage/byoe-response-consumer/types/paging/summaries

curl http://localhost:8077/type-coverage/byoe-response-consumer/types/windows/summaries
```

The consumer uses only generated client artifacts.

No manual DTO mapping, custom deserialization, envelope reconstruction, or generic container reconstruction exists in the consumer application.

---

## Verification Endpoints

### Scalars

```text
/types/scalars/string
/types/scalars/boolean
/types/scalars/integer
/types/scalars/long
/types/scalars/decimal
```

### Values

```text
/types/values/uuid
/types/values/date
/types/values/datetime
/types/values/enum
```

### Objects

```text
/types/objects/address
/types/objects/profile
```

### Lists

```text
/types/lists/summaries
/types/lists/statuses
```

### Sets

```text
/types/sets/summaries
/types/sets/statuses
```

### Built-in Pages

```text
/types/pages/summaries
/types/pages/statuses
```

### Application-owned Generic Containers

```text
/types/paging/summaries
/types/paging/statuses

/types/windows/summaries
/types/windows/statuses
```

These endpoints verify that the generated client correctly reconstructs:

```java
ApiResponse<T>
ApiResponse<List<T>>
ApiResponse<Set<T>>
ApiResponse<Page<T>>
ApiResponse<Paging<T>>
ApiResponse<Window<T>>
```

across primitive, value, enum, DTO, collection, built-in page, and application-owned generic container payloads.

---

## Mental Model

```text
ApiResponse<Paging<TypeSummaryDto>>
                ↓
OpenAPI Projection
                ↓
Generated Client
                ↓
Consumer Deserialization
                ↓
ApiResponse<Paging<TypeSummaryDto>>
```

The same reconstruction flow applies to every supported response shape, including application-owned generic payload containers nested inside the user-owned response envelope.
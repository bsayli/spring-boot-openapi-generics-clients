# transport-coverage

> End-to-end verification that OpenAPI Generics preserves standard OpenAPI Generator Java transport behavior while reconstructing generic response contracts.

Unlike the `type-coverage` samples, which validate generic contract reconstruction across different response shapes, this sample validates that the custom `java-generics-contract` generator does **not** interfere with standard OpenAPI Generator transport behavior.

`type-coverage` answers:

> **Which generic contract shapes survive projection and reconstruction?**

`transport-coverage` answers:

> **Do standard HTTP transport behaviors continue to work while generic response reconstruction is enabled?**

The initial transport matrix covers:

- `multipart/form-data`
- `application/octet-stream`
- `application/x-www-form-urlencoded`

while also verifying `ServiceResponse<T>` reconstruction wherever JSON responses are returned.

No custom multipart, binary, or form transport implementation is introduced by OpenAPI Generics.

The objective is deterministic verification that transport behavior and generic response reconstruction coexist without regression.

## Table of Contents

- [What This Sample Validates](#what-this-sample-validates)
- [Modules](#modules)
- [Covered Transport Scenarios](#covered-transport-scenarios)
    - [Multipart Upload](#multipart-upload)
    - [Binary Download](#binary-download)
    - [Form URL Encoded](#form-url-encoded)
- [Running the Sample](#running-the-sample)
- [Quick Smoke Test](#quick-smoke-test)
- [Design Boundary](#design-boundary)
- [Mental Model](#mental-model)

## What This Sample Validates

The sample exercises the transport layer of the complete OpenAPI Generics pipeline.

```text
Producer
    ↓
OpenAPI Projection
    ↓
Generated Client
    ↓
Consumer Runtime
```

The sample validates:

- multipart request generation
- binary download generation
- form-urlencoded request generation
- generic response reconstruction
- producer → OpenAPI → generated client → consumer compatibility

Unlike the `type-coverage` suite, the focus here is transport compatibility rather than generic response shape coverage.

## Modules

```text
transport-coverage
├── contract
├── producer
├── client
├── consumer
└── spec
```

| Module | Responsibility |
| --- | --- |
| `contract` | Request and response DTOs shared across the sample |
| `producer` | Publishes representative transport endpoints and generates the OpenAPI snapshot |
| `client` | Generates a Java RestClient using `java-generics-contract` |
| `consumer` | Uses only generated client artifacts to verify runtime behavior |
| `spec` | Committed OpenAPI snapshot generated from the producer API |

## Covered Transport Scenarios

### Multipart Upload

Producer contract:

```java
ResponseEntity<ServiceResponse<UploadResultDto>> upload(
    @RequestPart("file") MultipartFile file,
    @RequestPart("metadata") UploadMetadataDto metadata)
```

Validation flow:

```text
MultipartFile + JSON metadata
            ↓
Spring Endpoint
            ↓
OpenAPI Projection
            ↓
Generated Java RestClient
            ↓
Multipart request generation
            +
ServiceResponse<UploadResultDto> reconstruction
            ↓
Consumer Runtime
```

This verifies multipart request generation together with generic response reconstruction.

### Binary Download

Producer contract:

```java
ResponseEntity<Resource> download(String documentId)
```

Validation flow:

```text
ResponseEntity<Resource>
            ↓
OpenAPI Projection
            ↓
application/octet-stream
            ↓
Generated Java RestClient
            ↓
Resource
            ↓
Consumer Runtime
```

This verifies that binary downloads continue to use the standard Spring `Resource` abstraction without interference from generic response reconstruction.

### Form URL Encoded

Producer contract:

```java
ResponseEntity<ServiceResponse<RegistrationResultDto>> register(
    String name,
    String category)
```

Validation flow:

```text
application/x-www-form-urlencoded
            ↓
Generated Java RestClient
            ↓
Form request generation
            +
ServiceResponse<RegistrationResultDto> reconstruction
            ↓
Consumer Runtime
```

This verifies standard form request generation together with generic response reconstruction.

The generated OpenAPI snapshot also verifies that transport metadata and the OpenAPI Generics vendor extensions required by the generated client remain stable across future changes.

## Running the Sample

### Start Producer

```bash
cd producer
mvn spring-boot:run
```

Swagger UI:

```text
http://localhost:8078/transport-coverage/swagger-ui/index.html
```

OpenAPI document:

```text
http://localhost:8078/transport-coverage/v3/api-docs.yaml
```

### Start Consumer

```bash
cd consumer
mvn spring-boot:run
```

## Quick Smoke Test

### Multipart Through Producer

```bash
curl -X POST \
  http://localhost:8078/transport-coverage/transport/multipart \
  -F 'file=@README.md;type=text/plain' \
  -F 'metadata={"description":"multipart regression","category":"contract"};type=application/json'
```

### Multipart Through Consumer

```bash
curl -X POST \
  'http://localhost:8079/transport-coverage-consumer/transport/multipart?fileName=contract.txt&description=multipart%20regression&category=contract'
```

### Binary Through Consumer

```bash
curl http://localhost:8079/transport-coverage-consumer/transport/binary/sample
```

### Form Through Producer

```bash
curl -X POST \
  http://localhost:8078/transport-coverage/transport/form \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data 'name=blueprint&category=platform'
```

### Form Through Consumer

```bash
curl -X POST \
  'http://localhost:8079/transport-coverage-consumer/transport/form?name=blueprint&category=platform'
```

Together these requests verify that standard OpenAPI Generator transport behavior remains intact while generic response reconstruction is performed by OpenAPI Generics.

## Design Boundary

This sample intentionally remains transport-focused.

It validates coexistence between OpenAPI Generics and the standard Java RestClient transport implementation.

It intentionally does **not** introduce:

- Bring Your Own Envelope (BYOE)
- application-owned generic container scenarios
- request-side generic reconstruction
- custom multipart templates
- custom transport serialization logic
- WebFlux or reactive transport support

If a transport scenario requires generator-specific behavior rather than verifying compatibility with the upstream OpenAPI Generator implementation, it should be introduced as a dedicated platform capability or regression test rather than expanding the scope of this sample.

## Mental Model

```text
Producer Transport Endpoint
            ↓
OpenAPI Projection
            ↓
Generated Java Client
            ↓
Consumer Runtime
            ↓
Standard Transport Behavior
            +
Generic Response Reconstruction
```

The purpose of the `transport-coverage` sample is to continuously verify that OpenAPI Generics preserves standard transport behavior while reconstructing generic response contracts throughout the complete producer → OpenAPI → generated client → consumer pipeline.
---
title: Compatibility & Support Policy
nav_exclude: true
---

# Compatibility & Support Policy

This page defines the officially supported runtime and build-time scope for **OpenAPI Generics 1.2.1**.

It describes what the platform supports, what the repository verifies, and where responsibility remains with the surrounding OpenAPI and Spring ecosystem.

---

## Contents

- [Supported Scope](#supported-scope)
- [Compatibility Matrix](#compatibility-matrix)
- [Verified Reference Baselines](#verified-reference-baselines)
- [Verified Capabilities](#verified-capabilities)
- [Support Policy](#support-policy)
- [Out of Scope](#out-of-scope)
- [Further Reading](#further-reading)

---

## Supported Scope

OpenAPI Generics currently supports:

- Java 17+
- Spring Boot WebMvc applications
- springdoc-openapi WebMvc starter
- OpenAPI Generator 7.x
- Maven-based Java client generation

Published OpenAPI Generics artifacts continue to target Java 17+.

The repository also maintains newer reference stacks to verify compatibility with current framework generations without raising the minimum Java requirement for published platform artifacts.

The supported built-in response contract model includes:

```java
ServiceResponse<T>
ServiceResponse<List<T>>
ServiceResponse<Set<T>>
ServiceResponse<Page<T>>
```

BYOE envelopes participate in the same projection and reconstruction model:

```java
YourEnvelope<T>
YourEnvelope<List<T>>
YourEnvelope<Set<T>>
YourEnvelope<Page<T>>
```

`Page<T>` refers to:

```java
io.github.blueprintplatform.openapi.generics.contract.paging.Page<T>
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

Configured containers participate in the same projection, metadata enrichment, and deterministic reconstruction pipeline as built-in container types.

They may be used with either the built-in `ServiceResponse<T>` envelope or an application-owned BYOE envelope.

For aligned 1.2.1 producer and codegen components, envelope identity is carried by the generated OpenAPI document through `x-api-wrapper-type`. Client generation therefore does not require the same envelope type to be declared again through `openapi-generics.envelope`.

---

## Compatibility Matrix

### Runtime — Server-Side Projection

| Java | Spring Boot | springdoc-openapi | Scope  | Status    |
|------|-------------|-------------------|--------|-----------|
| 17+  | 3.4.x       | 2.8.x             | WebMvc | Supported |
| 17+  | 3.5.x       | 2.9.x             | WebMvc | Supported |
| 17+  | 4.x         | 3.x               | WebMvc | Supported |

The matrix defines the supported version lines. Exact versions exercised by the repository are documented separately below as verified reference baselines.

### Build-Time — Client Generation

| Java | OpenAPI Generator | Build Tool | Status    |
|------|-------------------|------------|-----------|
| 17+  | 7.x               | Maven      | Supported |

The client generation parent provides a tested default OpenAPI Generator version, but consumers may override `openapi-generator.version` within the supported 7.x line.

OpenAPI Generics 1.2.1 is verified with OpenAPI Generator **7.24.0**.

---

## Verified Reference Baselines

The following reference stacks are exercised by repository samples and maintained verification paths for the 1.2.1 release line.

| Platform line | Java | Spring Boot | springdoc-openapi | Purpose |
|---------------|------|-------------|-------------------|---------|
| Spring Boot 3 | 21   | 3.5.16      | 2.9.0             | Primary Spring Boot 3 integration and type-coverage validation |
| Spring Boot 4 | 25   | 4.1.0       | 3.1.0             | Spring Boot 4 compatibility reference stack |

These are verified reference baselines, not minimum consumer requirements.

Published OpenAPI Generics artifacts continue targeting Java 17+, while sample modules intentionally exercise newer Java releases where appropriate.

Spring Boot 4 samples use Java 25 LTS and the Spring Boot 4.1 `jarmode=tools` layered extraction model in Docker-based verification.

---

## Verified Capabilities

The supported scope is validated through repository samples, dedicated type-coverage modules, contract snapshots, transport-coverage modules, and end-to-end producer/client/consumer verification.

Verified contract capabilities include:

- default `ServiceResponse<T>` projection
- `Page<T>`, `List<T>`, and `Set<T>` container projection
- application-defined generic container registration
- configurable container descriptor validation
- configurable container projection and reconstruction
- application-defined containers used with the built-in `ServiceResponse<T>` envelope
- application-defined containers used with BYOE envelopes
- preservation of Java container identity through `x-data-container-type`
- preservation of Java envelope identity through `x-api-wrapper-type`
- contract-driven envelope reconstruction without duplicate client-side envelope configuration
- BYOE envelope projection and reconstruction
- BYOC external DTO reuse
- generated wrapper reconstruction
- ignored infrastructure model filtering
- generated-source hygiene and deterministic Java client output
- end-to-end producer → OpenAPI → generated client → consumer validation

Verified OpenAPI metadata includes:

- `x-api-wrapper`
- `x-api-wrapper-type`
- `x-api-wrapper-datatype`
- `x-data-container`
- `x-data-container-type`
- `x-data-item`
- `x-ignore-model`

The 1.2.1 release line also verifies server-starter failure categorization across:

- configuration failures
- configured container descriptor validation failures
- projection failures
- generated contract validation failures

These failures use platform-specific exception types while preserving existing fail-fast behavior and diagnostic messages.

### Transport compatibility

OpenAPI Generics specializes generic JSON contract reconstruction. It does not replace the standard transport model provided by OpenAPI Generator.

The dedicated `transport-coverage` sample verifies that generics-aware reconstruction remains compatible with standard Java RestClient generation for:

- `multipart/form-data`
- `application/octet-stream`
- `application/x-www-form-urlencoded`

Verified scenarios include:

- multipart file upload with a structured JSON part together with `ServiceResponse<T>` reconstruction
- binary download using the standard Spring `Resource` abstraction without wrapper interference
- form-urlencoded request generation together with generic response reconstruction

Reference samples:

```text
samples/spring-boot-3
samples/spring-boot-4
samples/type-coverage/service-response
samples/type-coverage/byoe-response
samples/transport-coverage
```

---

## Support Policy

A combination is officially supported when:

- it appears in the compatibility matrix
- it stays within the documented platform scope
- it is covered by samples, regression tests, or another maintained verification path

The compatibility matrix describes supported version lines. Verified reference baselines identify exact combinations exercised by the repository for the current release.

Inside that boundary, OpenAPI Generics owns:

- generics-aware OpenAPI projection
- OpenAPI Generics vendor extension metadata
- envelope identity projection through `x-api-wrapper-type`
- container identity projection through `x-data-container-type`
- application-defined generic container registration and validation
- contract-aware Java client generation
- wrapper reconstruction
- container-aware reconstruction
- BYOE and BYOC resolution
- generated-source hygiene
- fail-fast validation of projected and generated contract assumptions
- fail-fast detection of upstream template patch drift

Outside that boundary, behavior belongs to the surrounding ecosystem, such as:

- HTTP client library internals
- standard OpenAPI Generator transport behavior
- Jackson customization
- unrelated Spring configuration
- downstream application architecture

OpenAPI Generics does not claim ownership of those behaviors merely because they are exercised by repository compatibility samples.

### Producer and codegen alignment

OpenAPI Generics 1.2.1 preserves all 1.2.0 runtime contracts.

However, the 1.2.1 contract-driven envelope reconstruction model expects producer and codegen components to be upgraded together when relying on `x-api-wrapper-type` to eliminate duplicate client-side envelope configuration.

Existing 1.2 contracts do not require migration.

---

## Out of Scope

The following are not currently supported:

- Spring WebFlux
- Gradle-native client generation
- non-Java server frameworks
- non-Java client reconstruction
- arbitrary nested generic graphs
- automatic inference of unregistered custom generic containers

Standard OpenAPI tooling can still consume the generated OpenAPI document.

Tools that do not understand OpenAPI Generics metadata simply ignore the vendor extensions.

---

## Further Reading

- [Adoption Guides](index.md)
- [Architecture](architecture/architecture.md)
- [README](../README.md)
- [GitHub Discussions](https://github.com/blueprint-platform/openapi-generics/discussions)

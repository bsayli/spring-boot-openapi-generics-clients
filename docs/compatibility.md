---
title: Compatibility & Support Policy
nav_exclude: true
---

# Compatibility & Support Policy

This page defines the officially supported runtime and build-time scope for **OpenAPI Generics 1.2.1**.

It distinguishes supported platform and contract boundaries from the exact reference stacks exercised by the repository, and clarifies where responsibility remains with the surrounding OpenAPI and Spring ecosystem.

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

### Platform Scope

OpenAPI Generics currently supports:

- Java 17+
- Spring Boot WebMvc applications
- springdoc-openapi WebMvc starter
- OpenAPI Generator 7.x
- Maven-based Java client generation

Published OpenAPI Generics artifacts continue to target Java 17+.

The repository also maintains newer reference stacks to verify compatibility with current framework generations without raising the minimum Java requirement for published platform artifacts.

### Contract Scope

The supported built-in response contract shapes include:

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

`Page<T>` refers to the platform-owned `io.github.blueprintplatform.openapi.generics.contract.paging.Page<T>` contract.

Application-defined generic containers are also supported when explicitly registered. They participate in the same projection, metadata enrichment, and deterministic reconstruction pipeline as built-in container types and may be used with either the built-in `ServiceResponse<T>` envelope or a BYOE envelope.

For aligned 1.2.1 producer and codegen components, envelope identity is carried by the generated OpenAPI document through `x-api-wrapper-type`; client generation does not require the envelope type to be declared again through `openapi-generics.envelope`.

---

## Compatibility Matrix

### Runtime — Server-Side Projection

| Java | Spring Boot | springdoc-openapi | Scope  | Status    |
|------|-------------|-------------------|--------|-----------|
| 17+  | 3.4.x       | 2.8.x             | WebMvc | Supported |
| 17+  | 3.5.x       | 2.9.x             | WebMvc | Supported |
| 17+  | 4.x         | 3.x               | WebMvc | Supported |

The matrix defines supported version lines. Exact combinations exercised by the repository are documented separately as verified reference baselines.

### Build-Time — Client Generation

| Java | OpenAPI Generator | Build Tool | Status    |
|------|-------------------|------------|-----------|
| 17+  | 7.x               | Maven      | Supported |

The client generation parent provides a tested default OpenAPI Generator version, while consumers may override `openapi-generator.version` within the supported 7.x line.

OpenAPI Generics 1.2.1 is verified with OpenAPI Generator **7.24.0**.

---

## Verified Reference Baselines

The following reference stacks are exercised by repository samples and maintained verification paths for the 1.2.1 release line.

| Platform line | Java | Spring Boot | springdoc-openapi | Purpose                                                        |
|---------------|------|-------------|-------------------|----------------------------------------------------------------|
| Spring Boot 3 | 21   | 3.5.16      | 2.9.0             | Primary Spring Boot 3 integration and type-coverage validation |
| Spring Boot 4 | 25   | 4.1.0       | 3.1.0             | Spring Boot 4 compatibility reference stack                    |

These are verified reference baselines, not minimum consumer requirements.

Published OpenAPI Generics artifacts continue targeting Java 17+, while sample modules intentionally exercise newer Java releases where appropriate.

Spring Boot 4 samples use Java 25 LTS and the Spring Boot 4.1 `jarmode=tools` layered extraction model in Docker-based verification.

---

## Verified Capabilities

The supported scope is validated through repository samples, regression tests, contract snapshots, transport-coverage modules, and end-to-end producer/client/consumer verification.

Verified capability families include:

- built-in `ServiceResponse<T>` projection and reconstruction
- built-in `Page<T>`, `List<T>`, and `Set<T>` container support
- application-defined generic container registration, validation, projection, and reconstruction
- BYOE envelope projection and contract-driven reconstruction
- BYOC external model reuse
- preservation of Java envelope and container identity
- generated wrapper reconstruction and ignored infrastructure-model filtering
- generated-source hygiene and deterministic Java client output
- end-to-end producer → OpenAPI → generated client → consumer validation

The public OpenAPI Generics metadata exercised by these verification paths includes:

- `x-api-wrapper`
- `x-api-wrapper-type`
- `x-api-wrapper-datatype`
- `x-data-container`
- `x-data-container-type`
- `x-data-item`
- `x-ignore-model`

The reference verification paths also cover fail-fast configuration, container descriptor, projection, and generated-contract validation behavior.

### Transport Compatibility

OpenAPI Generics specializes contract-aware reconstruction of generic Java response types without replacing the standard transport model provided by OpenAPI Generator.

The dedicated `transport-coverage` verification path confirms interoperability with standard Java RestClient generation for:

- `multipart/form-data`, including structured JSON parts alongside generic response reconstruction
- `application/octet-stream` binary downloads using the standard Spring `Resource` abstraction
- `application/x-www-form-urlencoded` requests alongside generic response reconstruction

These scenarios verify that generics-aware specialization does not interfere with standard transport generation; transport behavior itself remains owned by OpenAPI Generator.

Reference verification areas include:

```text
samples/spring-boot-3
samples/spring-boot-4
samples/type-coverage/service-response
samples/type-coverage/byoe-response
samples/transport-coverage
```

---

## Support Policy

A platform combination is officially supported when it falls within a version line declared in the compatibility matrix and remains inside the documented platform scope.

Support for a declared version line is maintained through the project's compatibility strategy, which may include representative reference stacks, regression tests, contract snapshots, integration samples, and other maintained verification paths. Individual patch or minor combinations within a supported line are not required to appear as exact repository reference stacks unless explicitly documented otherwise.

The compatibility matrix defines supported version lines. Verified reference baselines identify exact combinations exercised by the repository for the current release and provide concrete compatibility reference points; they do not narrow the supported ranges to those exact versions.

Inside the documented compatibility boundary, OpenAPI Generics owns:

- generic contract projection and its OpenAPI Generics vendor-extension metadata
- preservation of Java envelope and container identity
- application-defined generic container registration and validation
- contract-aware Java reconstruction, including BYOE envelope reconstruction and BYOC external model resolution
- generated wrapper correctness and reconstruction-specific source hygiene
- fail-fast validation of OpenAPI Generics contract assumptions and upstream template patch drift

Outside that boundary, behavior remains owned by the surrounding ecosystem, including:

- HTTP client library internals
- standard OpenAPI Generator transport behavior
- Jackson customization
- unrelated Spring configuration
- downstream application architecture

OpenAPI Generics does not claim ownership of those behaviors merely because they are exercised by repository compatibility samples.

### Producer and Codegen Alignment

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

---
title: Compatibility & Support Policy
nav_exclude: true
---

# Compatibility & Support Policy

This page is the authoritative compatibility and support reference for OpenAPI Generics.

It defines:

- the platform and build-tool lines currently supported
- the exact reference stacks verified by the repository
- what “supported” means for versions inside those lines
- the boundary between OpenAPI Generics-owned behavior and surrounding ecosystem behavior

For usage and architecture details, see the adoption and architecture guides rather than treating this page as a feature reference.

---

## Contents

- [Supported Platform Matrix](#supported-platform-matrix)
- [Verified Reference Baselines](#verified-reference-baselines)
- [Supported Contract Scope](#supported-contract-scope)
- [Support Policy](#support-policy)
- [Compatibility Notes](#compatibility-notes)
- [Out of Scope](#out-of-scope)
- [Further Reading](#further-reading)

---

## Supported Platform Matrix

### Server-Side Projection

| Java | Spring Boot | springdoc-openapi | Integration | Status    |
|------|-------------|-------------------|-------------|-----------|
| 17+  | 3.4.x       | 2.8.x             | WebMvc      | Supported |
| 17+  | 3.5.x       | 2.8.x, 2.9.x      | WebMvc      | Supported |
| 17+  | 4.x         | 3.x               | WebMvc      | Supported |

Published OpenAPI Generics artifacts target Java 17+.

The matrix describes supported version lines. It does not mean that every patch combination inside a supported line is maintained as a separate repository sample.

### Client-Side Reconstruction

| Java | OpenAPI Generator | Build integration | Status    |
|------|-------------------|-------------------|-----------|
| 17+  | 7.x               | Maven             | Supported |

The Maven codegen parent provides a tested OpenAPI Generator default while allowing consumers to select another version within the supported 7.x compatibility line.

The selected OpenAPI Generator version is kept aligned across generator execution, generator dependencies, and upstream template extraction used by the reconstruction pipeline.

---

## Verified Reference Baselines

Verified reference baselines are exact combinations exercised by maintained repository verification paths. They are compatibility reference points, not minimum consumer requirements and not the only supported combinations.

### Server Reference Stacks

| Platform line | Java | Spring Boot | springdoc-openapi | Purpose                                                       |
|---------------|------|-------------|-------------------|---------------------------------------------------------------|
| Spring Boot 3 | 21   | 3.5.16      | 2.9.0             | Primary Spring Boot 3 integration and type-coverage reference |
| Spring Boot 4 | 25   | 4.1.0       | 3.1.0             | Spring Boot 4 compatibility reference                         |

### Client Generation Reference

OpenAPI Generics 1.2.1 is verified with OpenAPI Generator **7.24.0**.

Repository verification covers the complete contract lifecycle, including producer projection, OpenAPI metadata, Java reconstruction, generated-source validation, and consumer integration.

Standard Java RestClient transport interoperability is also exercised for multipart, binary download, and form-urlencoded scenarios without transferring ownership of those transport behaviors to OpenAPI Generics.

---

## Supported Contract Scope

The supported contract surface includes:

- the built-in `ServiceResponse<T>` envelope
- built-in `List<T>`, `Set<T>`, and platform `Page<T>` container shapes
- BYOE application-owned envelopes
- BYOC reuse of externally owned Java models
- explicitly registered application-defined generic containers
- deterministic preservation and reconstruction of envelope and container identity

BYOE envelopes participate in the same supported response-shape model as the built-in envelope.

Application-defined generic containers participate in the same projection and reconstruction model when explicitly registered and available to the generated client.

OpenAPI Generics does not infer arbitrary generic graphs or unregistered custom container semantics.

For configuration examples and supported usage patterns, see the server-side and client-side adoption guides.

---

## Support Policy

A platform combination is officially supported when it:

- falls within a version line declared in the supported platform matrix
- remains inside the documented OpenAPI Generics platform and contract scope
- relies on behavior owned by OpenAPI Generics rather than unsupported assumptions about surrounding frameworks or generated transport code

Support for a declared version line is maintained through representative reference stacks, regression tests, contract snapshots, integration samples, and other maintained verification paths.

An exact version combination does not need to appear as a dedicated sample to remain inside a supported line unless explicitly documented otherwise.

Verified reference baselines therefore answer:

> Which exact combinations does the repository continuously exercise as reference points?

The supported matrix answers a different question:

> Which version lines does the project currently support?

### Ownership Boundary

Inside the documented compatibility boundary, OpenAPI Generics owns:

- generic contract projection and OpenAPI Generics metadata
- preservation of Java envelope and container identity
- application-defined generic container registration and validation
- contract-aware Java reconstruction, including BYOE and BYOC semantics
- generated wrapper correctness and reconstruction-specific source hygiene
- fail-fast validation of OpenAPI Generics contract assumptions
- fail-fast detection of supported upstream template patch incompatibilities

The surrounding ecosystem continues to own:

- HTTP client library internals
- ordinary OpenAPI Generator API, model, authentication, and transport behavior
- serialization-library behavior and customization
- unrelated Spring configuration
- downstream application architecture

Compatibility samples may exercise those ecosystem behaviors to verify interoperability, but OpenAPI Generics does not take ownership of them.

---

## Compatibility Notes

### Producer and Codegen Alignment

Contract metadata is the boundary between producer projection and client reconstruction.

When relying on `x-api-wrapper-type` for contract-driven envelope reconstruction, producer and codegen components should be kept aligned so the client can consume the metadata model published by the producer.

OpenAPI Generics 1.2.1 preserves 1.2.0 runtime contracts. Existing 1.2 contracts do not require migration.

### OpenAPI Generator Version Ownership

OpenAPI Generics declares the supported OpenAPI Generator compatibility line and provides a tested default through the Maven codegen parent.

Consumers choose the OpenAPI Generator version they run within that supported line.

The project does not use its platform BOM to manage OpenAPI Generator or OpenAPI Generator plugin versions.

### OpenAPI Interoperability

The generated document remains valid OpenAPI.

OpenAPI tooling that does not understand OpenAPI Generics metadata can still consume the document and ignore the vendor extensions.

---

## Out of Scope

The following are not currently part of the supported scope:

- Spring WebFlux
- first-class Gradle client build integration
- non-Java server frameworks
- non-Java client reconstruction
- arbitrary nested generic graphs
- automatic inference of unregistered custom generic containers
- custom OpenAPI component schema names that differ from the Java simple type name, including names declared through `@Schema(name = "...")`

These boundaries describe the current supported product surface; they do not imply ownership of adjacent ecosystem capabilities.

---

## Further Reading

- [Server-Side Adoption](adoption/server-side-adoption.md)
- [Client-Side Adoption](adoption/client-side-adoption.md)
- [Architecture](architecture/architecture.md)
- [README](../README.md)
- [GitHub Discussions](https://github.com/blueprint-platform/openapi-generics/discussions)

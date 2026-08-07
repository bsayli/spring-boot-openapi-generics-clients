# Changelog

All notable changes to this project will be documented in this file.

This project follows a contract-first release discipline:

- MAJOR — contract reset
- MINOR — backward-compatible capability expansion
- PATCH — bug fixes, dependency alignment, documentation, and build/release hygiene

---

## [Unreleased]

### Patch release

OpenAPI Generics 1.2.1 simplifies contract-driven wrapper reconstruction by making the generated OpenAPI document the single source of truth for envelope identity.

This release removes redundant client-side envelope configuration, completes end-to-end validation for application-defined generic containers with the built-in `ServiceResponse<T>` envelope, and continues platform verification and dependency alignment without changing runtime behavior.

### Added

- Added the `x-api-wrapper-type` vendor extension to preserve the fully qualified Java envelope type in projected wrapper schemas.
- Added contract-driven envelope reconstruction in the Java code generator based on `x-api-wrapper-type`.
- Added end-to-end validation for application-defined generic containers used with the built-in `ServiceResponse<T>` envelope.
- Expanded sample coverage for producer → OpenAPI → generated client → consumer validation using application-owned generic containers.

### Changed

- Removed the need for client-side `openapi-generics.envelope` configuration when using aligned 1.2.1 producer and codegen components.
- Unified envelope reconstruction with the existing contract-driven metadata model used for generic containers.
- Upgraded Spring Boot to **3.5.16**.
- Upgraded the Spring Boot 4 sample baseline from **4.0.7** to **4.1.0**.
- Upgraded Springdoc to **2.9.0** for the Spring Boot 3 platform line and **3.1.0** for Spring Boot 4 samples.
- Updated Spring Boot 4 sample Docker images for the `jarmode=tools` layered extraction model introduced in Spring Boot 4.1.
- Updated configuration property validation to use container-element `@Valid` semantics across supported Spring Boot lines.
- Upgraded OpenAPI Generator to **7.24.0**.
- Updated codegen module and sample documentation to reflect the contract-driven envelope metadata model.

### Quality & Verification

- Added regression coverage for `x-api-wrapper-type` metadata generation and consumption.
- Added verification that generated clients reconstruct wrapper inheritance without client-side envelope configuration.
- Verified built-in `ServiceResponse<T>` and BYOE envelope generation using the same metadata-driven reconstruction pipeline.
- Verified producer → OpenAPI → generated client → consumer flows after removing redundant client-side envelope configuration.
- Verified the Spring Boot 4 integration stack on Spring Boot 4.1.0 with Springdoc 3.1.0.

### Compatibility

- Fully backward compatible with all 1.2.0 runtime contracts.
- Producer and codegen components are expected to be upgraded together as part of the 1.2.1 release line.
- Java 17+
- Spring Boot 3.x / 4.x
- OpenAPI Generator 7.x
- Maven-based client generation

---

## [1.2.0] - 2026-06-27

### Minor release

OpenAPI Generics 1.2.0 extends the contract-first platform with application-defined generic containers, richer projection metadata, and deterministic generated-source hygiene.

This release generalizes the container infrastructure introduced in 1.1, allowing applications to contribute their own generic container contracts while preserving the same projection, metadata enrichment, and client reconstruction pipeline.

All 1.1.x contracts remain fully backward compatible.

### Added

- Added support for application-defined generic container contracts through configuration.
- Added startup validation for configured generic containers, including generic type and collection property verification.
- Added support for configurable container item properties, enabling custom contracts such as `Paging<T>` and `Window<T>`.
- Added `x-data-container-type` vendor extension to preserve the fully qualified Java container type during OpenAPI projection.
- Added container identity preservation throughout response introspection, projection, metadata enrichment, and client reconstruction.
- Added deterministic generated-source hygiene phase to the Java code generation pipeline.
- Added automated cleanup of duplicate and unused imports in generated Java sources.
- Added comprehensive validation for configured container discovery, projection metadata generation, and reconstruction behavior.

### Changed

- Generalized the container model to support both built-in and application-defined generic containers through a unified descriptor model.
- Unified built-in and configured containers under the same projection, metadata, and reconstruction pipeline.
- Improved projection metadata by preserving resolved Java container identity alongside semantic container information.
- Improved generated Java source quality through deterministic post-generation cleanup.
- Updated documentation, architecture diagrams, samples, and adoption guides to cover configurable generic containers and enriched projection metadata.

### Quality & Verification

- Added regression coverage for application-defined generic container registration and validation.
- Added verification for `x-data-container-type` generation across supported container types.
- Added unit and integration tests covering configurable container projection, metadata enrichment, and reconstruction.
- Added regression coverage for generated-source hygiene and deterministic code generation.
- Verified producer → OpenAPI → generated client → consumer flows for both built-in and configured generic container contracts.
- Verified full backward compatibility with all 1.1.x response contracts.

### Compatibility

- Fully backward compatible with all 1.1.x releases.
- Java 17+
- Spring Boot 3.x / 4.x
- OpenAPI Generator 7.x
- Maven-based client generation

---

## [1.1.0] - 2026-06-21

### Minor release

OpenAPI Generics 1.1.0 introduces container-aware contract reconstruction and deterministic OpenAPI snapshot validation.

This release expands the platform's supported contract model while preserving backward compatibility with all 1.0.x contracts.

### Added

- Introduced first-class generic container infrastructure across projection and client generation.
- Added container-aware metadata model for projection, OpenAPI enrichment, and code generation.
- Added support for:
    - `ServiceResponse<List<T>>`
    - `ServiceResponse<Set<T>>`
    - `ServiceResponse<Page<T>>`
- Added support for the same container model in BYOE envelopes:
    - `YourEnvelope<List<T>>`
    - `YourEnvelope<Set<T>>`
    - `YourEnvelope<Page<T>>`
- Added container-aware wrapper reconstruction in generated Java clients.
- Added configurable container registration support for client-side reconstruction.
- Added deterministic OpenAPI snapshot validation across sample projects.
- Added committed OpenAPI specification snapshots as validation artifacts.
- Added automated specification drift detection during CI validation.
- Added projection metadata assertions for generated OpenAPI documents.

### Changed

- Generalized container handling into a dedicated platform capability instead of container-specific implementations.
- Unified projection and reconstruction behavior through shared container metadata.
- Extended wrapper naming and reconstruction logic to support container-aware response types.
- Expanded type coverage samples to validate container-based response contracts.
- Updated documentation, adoption guides, architecture references, and compatibility documentation for the container model.

### Quality & Verification

- Added end-to-end validation for:
    - `ServiceResponse<T>`
    - `ServiceResponse<List<T>>`
    - `ServiceResponse<Set<T>>`
    - `ServiceResponse<Page<T>>`
- Added equivalent BYOE validation coverage.
- Added OpenAPI snapshot regression validation for Spring Boot 3 and Spring Boot 4 sample stacks.
- Added verification coverage for projection metadata generation, vendor extension consistency, wrapper reconstruction, runtime deserialization, and consumer integration.
- Verified producer → OpenAPI → generated client → consumer flows across all supported response shapes.

### Compatibility

- Fully backward compatible with all 1.0.x releases.
- Java 17+
- Spring Boot 3.x / 4.x
- OpenAPI Generator 7.x
- Maven-based client generation

---

## [1.0.3] - 2026-06-13

### Patch release

OpenAPI Generics 1.0.3 is a platform hardening and verification update for the 1.0.x GA line.

No contract changes.  
No public API behavior changes.  
No generated client structure changes.

Fully compatible with the 1.0.0, 1.0.1, and 1.0.2 GA contracts.

### Changed

- Added dedicated type coverage sample suites for canonical `ServiceResponse<T>` and custom BYOE response envelopes.
- Added focused validation scenarios covering generic wrapper reconstruction, projection correctness, and generated client behavior.
- Expanded sample documentation with dedicated type coverage guides and verification flows.
- Added missing BYOE OpenAPI specifications required for complete sample validation.
- Upgraded platform modules and samples to Spring Boot 3.5.15.
- Upgraded OpenAPI Generator alignment to 7.23.0.
- Upgraded Spring Boot 4 samples to Spring Boot 4.0.7.
- Simplified sample module metadata and improved sample build structure consistency.
- Added direct enum payload support documentation and clarified enum component requirements.

### Quality & Verification

- Enforced reusable OpenAPI component requirements for direct enum generic payloads through `@Schema(enumAsRef = true)` validation.
- Prevented unsupported inline enum payloads from producing generic wrapper reconstruction metadata.
- Fixed generator lifecycle handling by removing ignored models before upstream global model graph processing.
- Eliminated misleading null-model warnings caused by intentionally ignored schemas during client generation.
- Ensured deterministic OpenAPI Generator version alignment across plugin execution, runtime dependencies, and template extraction.
- Added regression coverage for enum payload discovery, ignored model processing, and generator version consistency.
- Verified Spring Boot 3 and Spring Boot 4 sample pipelines after framework upgrades.
- Verified Docker-based sample execution and end-to-end producer/client/consumer flows.
- Closed all known 1.0.x platform issues related to enum payload validation, ignored model processing, and generator version alignment.

### Compatibility

- Java 17+
- Spring Boot 3.x / 4.x
- OpenAPI Generator 7.x
- Maven-based client generation

---

## [1.0.2] - 2026-05-16

### Patch release

OpenAPI Generics 1.0.2 is a maintenance and quality update for the 1.0.x GA line.

No contract changes.  
No public API behavior changes.  
No generated client structure changes.

Fully compatible with the 1.0.0 and 1.0.1 GA contracts.

### Changed

- Reworked README and documentation site content for clearer problem-to-proof-to-adoption flow.
- Clarified server-side and client-side adoption guides.
- Documented that `openapi-generics-server-starter` runs only during Springdoc OpenAPI document generation.
- Clarified Bring Your Own Envelope (BYOE) and Bring Your Own Contract (BYOC) semantics and configuration.
- Clarified fallback behavior for reverting to standard OpenAPI Generator output.
- Improved sample documentation and run-and-verify instructions.
- Centralized version management and strengthened parent POM alignment.
- Added Spotless-based source formatting to the build.

### Quality & Verification

- Hardened external model registry validation for BYOC mappings, including invalid and empty FQCN detection with diagnostic warnings.
- Added unit tests for invalid and edge-case external model configurations.
- Stabilized Spring Boot 3 and Spring Boot 4 sample pipelines.
- Improved Docker-based sample builds for self-contained local verification.
- Strengthened CI workflows for snapshot and local-install validation.

---

## [1.0.1] - 2026-05-01

### Patch release

OpenAPI Generics 1.0.1 is a maintenance and release-hygiene update for the 1.0.x GA line.

No contract changes.  
No public API behavior changes.  
No generated client structure changes.

Fully compatible with the 1.0.0 GA contract.

### Changed

- Refined Maven POM hygiene across platform modules.
- Kept the root aggregator lifecycle-safe while preserving shared plugin management.
- Clarified build ownership between the root aggregator, platform modules, and the codegen parent.
- Updated OpenAPI Generator alignment to 7.23.0.
- Upgraded Spring Boot baseline from 3.5.13 to 3.5.15 across platform modules and samples.
- Documented upstream template governance and fail-fast patch validation.
- Strengthened dependency alignment and explicitly controlled transitive versions where necessary.

### Quality & Verification

- Added JaCoCo and Codecov integration.
- Added unit and edge-case coverage for server-side projection and wrapper enrichment behavior.
- Verified Spring Boot 3 and Spring Boot 4 sample pipelines.

---

## [1.0.0] - 2026-04-18

### General Availability

OpenAPI Generics 1.0.0 marks the General Availability release of the contract-first generics platform.

### Highlights

- Contract-first architecture with Java as the source of truth.
- OpenAPI treated strictly as a projection layer.
- Generics-aware client generation.
- Bring Your Own Envelope (BYOE).
- Bring Your Own Contract (BYOC).
- Deterministic contract-to-spec-to-client pipeline.
- Spring Boot 3 and Spring Boot 4 sample coverage.

### Compatibility

- Java 17+
- Spring Boot 3.x / 4.x
- OpenAPI Generator 7.x
- Maven-based client generation
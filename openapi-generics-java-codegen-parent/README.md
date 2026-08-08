# openapi-generics-java-codegen-parent

> Build-time orchestration for deterministic, contract-aware Java client generation.

`openapi-generics-java-codegen-parent` is the primary client-side integration point for OpenAPI Generics.

It wires together:

- upstream OpenAPI Generator
- `openapi-generics-java-codegen`
- platform templates
- generated-source hygiene
- generated source registration

Its responsibility is:

> **OpenAPI spec → deterministic contract-aware Java client**

---

## Contents

- [What It Provides](#what-it-provides)
- [Generation Pipeline](#generation-pipeline)
- [Usage](#usage)
- [Contract Alignment](#contract-alignment)
- [Generated-Source Hygiene](#generated-source-hygiene)
- [Compatibility Mode](#compatibility-mode)
- [User-Controlled Configuration](#user-controlled-configuration)
- [Out of Scope](#out-of-scope)

---

## What It Provides

By inheriting this parent, a client project gets:

- deterministic template extraction
- fail-fast template patching
- platform template overlay
- `java-generics-contract` generator wiring
- generated-source cleanup
- generated source registration
- BYOE and BYOC integration support

Consumers do not need to assemble the generation lifecycle manually.

---

## Generation Pipeline

```text
OpenAPI spec
      ↓
Extract upstream templates
      ↓
Patch wrapper insertion points
      ↓
Overlay OpenAPI Generics templates
      ↓
Run java-generics-contract
      ↓
Clean generated Java sources
      ↓
Register generated sources
      ↓
Compile
```

If the upstream template structure changes and the wrapper patch cannot be applied, the build fails fast instead of generating incorrect client code.

---

## Usage

Inherit the parent:

```xml
<parent>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>1.2.1</version>
  <relativePath/>
</parent>
```

Configure OpenAPI Generator normally, but use:

```xml
<generatorName>java-generics-contract</generatorName>
```

Then build:

```bash
mvn clean install
```

Generated sources are registered automatically from:

```text
target/generated-sources/openapi/src/gen/java
```

---

## Contract Alignment

### BYOE — Bring Your Own Envelope

```xml
<additionalProperties>
  <additionalProperty>
    openapi-generics.envelope=io.example.contract.ApiResponse
  </additionalProperty>
</additionalProperties>
```

Generated wrappers extend the configured envelope instead of the default `ServiceResponse<T>`.

### BYOC — Bring Your Own Contract

```xml
<additionalProperties>
  <additionalProperty>
    openapi-generics.response-contract.CustomerDto=io.example.contract.CustomerDto
  </additionalProperty>
</additionalProperties>
```

Mapped DTOs are reused from the external contract module instead of being regenerated.

---

## Generated-Source Hygiene

After generation, the parent runs a generated-source cleanup phase.

This removes duplicate and unused imports and formats generated Java sources deterministically.

The cleanup applies only to generated OpenAPI sources under:

```text
target/generated-sources/openapi/src/gen/java/**/*.java
```

This improves generated artifact quality without changing contract semantics.

---

## Compatibility Mode

Disable OpenAPI Generics orchestration:

```xml
<openapi.generics.skip>true</openapi.generics.skip>
```

Use this for:

- output comparison
- debugging
- migration validation

To fully return to stock OpenAPI Generator behavior, use the standard `java` generator instead of `java-generics-contract`.

---

## User-Controlled Configuration

Consumers control normal OpenAPI Generator configuration:

- input specification
- client library
- package names
- config options
- BYOE and BYOC mappings
- OpenAPI Generator version within the supported 7.x line

The parent controls the deterministic generation lifecycle.

---

## Out of Scope

This module does not:

- define runtime contracts
- inspect Spring controllers
- generate OpenAPI documents
- replace OpenAPI Generator
- own application runtime behavior

Its role is build-time orchestration only.
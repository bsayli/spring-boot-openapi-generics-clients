# openapi-generics-java-codegen-parent

> **Build-time orchestration layer for deterministic, contract-aligned OpenAPI client generation**

`openapi-generics-java-codegen-parent` is a **parent POM** that turns OpenAPI client generation from a loosely configured tool into a **controlled, deterministic build pipeline**.

It is the **primary entry point for consumers**.

Users do not configure generators manually.
They inherit this parent and get a fully wired, contract-aligned system.

---

## Table of Contents

1. [Purpose](#-purpose)
2. [Architectural Role](#-architectural-role)
3. [What It Provides](#-what-it-provides)
4. [Build-Time Pipeline](#-build-time-pipeline)
5. [Key Components](#-key-components)
6. [Usage](#-usage)
7. [Configuration Boundaries](#-configuration-boundaries)
8. [Compatibility Matrix](#-compatibility-matrix)
9. [What Users Should NOT Do](#-what-users-should-not-do)
10. [Design Constraints](#-design-constraints)
11. [Design Trade-offs](#-design-trade-offs)
12. [Failure Philosophy](#-failure-philosophy)
13. [Mental Model](#-mental-model)
14. [Summary](#-summary)

---

## 🎯 Purpose

OpenAPI client generation is often treated as a configuration problem.

In practice, that leads to:

* inconsistent generator setups across projects
* duplicated envelope models
* fragile regeneration
* drift between client and server contracts

This module exists to remove that variability.

It provides a **single, controlled build pipeline** that enforces:

* contract-first client generation
* deterministic template behavior
* consistent output across all consumers

Core idea:

> OpenAPI generation is not configuration — it is orchestration.

---

## 🧠 Architectural Role

Within the platform:

| Layer      | Module                            | Role                       |
| ---------- | --------------------------------- | -------------------------- |
| Authority  | `openapi-generics-contract`       | Defines response semantics |
| Projection | `openapi-generics-server-starter` | Produces OpenAPI           |
| Execution  | **this module**                   | Orchestrates generation    |
| Rendering  | `openapi-generics-java-codegen`   | Shapes final code          |

This module is **NOT a library**.

It is:

> A build-time execution environment for OpenAPI client generation.

---

## ⚙️ What It Provides

By inheriting this parent, users automatically get a fully wired generation pipeline.

### 1. Custom Generator Binding

```
generatorName = java-generics-contract
```

Backed by:

* `openapi-generics-java-codegen`

This ensures contract-aware generation behavior without manual configuration.

---

### 2. Template Pipeline

The system prepares an **effective template set** at build time:

```
[extract upstream template]
        ↓
[patch]
        ↓
[overlay custom templates]
```

No manual template management.
No local overrides required.

---

### 3. Contract Mapping

Mappings are injected automatically:

```
ServiceResponse → openapi-generics-contract
Meta → openapi-generics-contract
Page → openapi-generics-contract
```

Result:

> Generated models reuse canonical contract classes instead of duplicating them.

---

### 4. Deterministic Build Execution

The parent enforces:

* fixed plugin versions
* fixed execution phases
* stable template structure

Guarantee:

> Same input → same generated output

---

### 5. Fail-Fast Safety

The build fails if structural assumptions break:

* upstream template structure changes
* wrapper injection is missing

Example failure:

```
OpenAPI template patch FAILED
```

This prevents silent, hard-to-debug inconsistencies.

---

## 🔄 Build-Time Pipeline

The full generation flow:

```
OpenAPI spec
   ↓
Parent POM (this module)
   ↓
Template extraction
   ↓
Template patch (api_wrapper injection)
   ↓
Template overlay
   ↓
OpenAPI generator execution
   ↓
Generated sources (contract-aligned)
```

Important:

* all steps are deterministic
* no runtime behavior exists
* output is reproducible across environments

---

## 🧩 Key Components

### Template Extraction

Extracts `model.mustache` from upstream OpenAPI Generator.

---

### Template Patch

Injects:

```
{{#vendorExtensions.x-api-wrapper}}{{>api_wrapper}}{{/vendorExtensions.x-api-wrapper}}
```

This enables wrapper-based generation aligned with contract semantics.

---

### Template Overlay

Adds custom templates:

* `api_wrapper.mustache`

---

### Generator Configuration

Automatically configures:

* generator name
* template directory
* import mappings

No user intervention required.

---

### Generated Sources Registration

Ensures generated code is compiled as part of the project lifecycle.

---

## 📦 Usage

### 1. Inherit Parent

```xml
<parent>
  <groupId>io.github.blueprintplatform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>0.8.0</version>
</parent>
```

---

### 2. Configure OpenAPI Generator Plugin

Minimal working configuration:

```xml
<plugin>
  <groupId>org.openapitools</groupId>
  <artifactId>openapi-generator-maven-plugin</artifactId>

  <executions>
    <execution>
      <id>generate-client</id>
      <phase>generate-sources</phase>
      <goals>
        <goal>generate</goal>
      </goals>

      <configuration>

        <inputSpec>${project.basedir}/src/main/resources/your-api-docs.yaml</inputSpec>

        <library>your-library-choice</library>
        <apiPackage>your.api.package</apiPackage>
        <modelPackage>your.model.package</modelPackage>
        <invokerPackage>your.invoker.package</invokerPackage>

        <configOptions>
          <useSpringBoot3>true</useSpringBoot3>
          <serializationLibrary>your-choice</serializationLibrary>
          <openApiNullable>false</openApiNullable>
        </configOptions>

      </configuration>

    </execution>
  </executions>
</plugin>
```

No generator wiring.
No template configuration.

---

**Notes**

* You must provide your own package structure
* You must choose a library supported by OpenAPI Generator
* `serializationLibrary` supports:

    * `jackson`
    * `jsonb`
    * `gson`
* These settings affect only transport and serialization
* Contract-aware generation is independent of these settings

---

### 3. Build

```
mvn clean install
```

Generated sources are added automatically to the compilation phase.

---

## 🧠 Configuration Boundaries

This module intentionally separates responsibilities.

### User-Controlled (Safe)

```xml
<inputSpec>...</inputSpec>
<library>...</library>
<apiPackage>...</apiPackage>
<modelPackage>...</modelPackage>
<invokerPackage>...</invokerPackage>
<openapi-generator.version>...</openapi-generator.version>
```

These control:

* input specification
* HTTP client / transport layer
* package structure
* generator version

---

### Platform-Controlled (Do NOT Override)

The parent already provides and wires:

* `maven-resources-plugin` (template overlay)
* `maven-dependency-plugin` (template extraction)
* `maven-antrun-plugin` (template patching)
* `openapi-generator-maven-plugin` (core execution)

Including:

```xml
<generatorName>java-generics-contract</generatorName>
<templateDirectory>...</templateDirectory>
<importMappings>...</importMappings>
```

These ensure:

* contract preservation (`ServiceResponse<T>`, `Page<T>`)
* deterministic wrapper generation
* model reuse instead of duplication

### Important

> If you override these, you are leaving the contract-safe execution path.

---

## 🔗 Compatibility Matrix

This module is tested with the following baseline:

| Component         | Version |
| ----------------- | ------- |
| Java              | 21      |
| OpenAPI Generator | 7.x     |

Notes:

* `restclient` library is available starting from **OpenAPI Generator 7.6.0**
* If you use `restclient`, you must use **7.6.0 or newer**
* The parent defines a default generator version, but users can override it
* The system is designed to remain stable across OpenAPI Generator 7.x versions

---

## 🚫 What Users Should NOT Do

Users should NOT:

* override templates
* change generator name
* modify import mappings
* inject custom model logic

Reason:

> The system is intentionally controlled to guarantee determinism.

---

## 🔐 Design Constraints

### No Generator Fork

Uses the official OpenAPI Generator.

---

### No Runtime Behavior

Everything happens at build time.

---

### No Contract Duplication

All core models come from `openapi-generics-contract`.

---

### Controlled Template System

Templates are patched, validated, and version-controlled.

---

## ⚖️ Design Trade-offs

### Reduced Flexibility

Users cannot freely customize generation.

Gain:

* stability
* predictability

---

### Template Patch Dependency

Relies on upstream template structure.

Mitigation:

* fail-fast validation

---

### Parent-Centric Model

All behavior is centralized.

Implication:

* strong consistency
* limited local overrides

---

## 💥 Failure Philosophy

The system prefers failure over inconsistency.

Build fails when:

* templates drift
* contract assumptions break

Principle:

> Incorrect generation is worse than no generation.

---

## 🧠 Mental Model

Think of this module as:

> A deterministic build-time compiler configuration for OpenAPI client generation

Not:

* a reusable library
* a plugin collection
* a convenience wrapper

---

## 🧾 Summary

`openapi-generics-java-codegen-parent` is:

* the **consumer entry point**
* the **execution orchestrator**
* the **determinism enforcer**

Its responsibility is strictly:

> Transform OpenAPI projection into contract-aligned Java client code via a controlled build pipeline

Nothing more.

---

## 📜 License

MIT License.

---

**Maintained by:**
**Barış Saylı**
[GitHub](https://github.com/bsayli) · [Medium](https://medium.com/@baris.sayli) · [LinkedIn](https://www.linkedin.com/in/bsayli)

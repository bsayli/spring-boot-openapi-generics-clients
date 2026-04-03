# openapi-generics-java-codegen-parent

> **Build-time orchestration layer for deterministic, contract-aligned OpenAPI client generation**

`openapi-generics-java-codegen-parent` is a **parent POM** that turns OpenAPI client generation into a **controlled, deterministic build pipeline**.

It is the **primary entry point for consumers**.

Users do not configure generators manually.
They inherit this parent and get a fully wired system.

---

## Table of Contents

1. [Purpose](#-purpose)
2. [Architectural Role](#-architectural-role)
3. [What It Provides](#-what-it-provides)
4. [Build-Time Pipeline](#-build-time-pipeline)
5. [Key Components](#-key-components)
6. [Usage](#-usage)
7. [What Users Should NOT Do](#-what-users-should-not-do)
8. [Design Constraints](#-design-constraints)
9. [Design Trade-offs](#-design-trade-offs)
10. [Failure Philosophy](#-failure-philosophy)
11. [Mental Model](#-mental-model)
12. [Summary](#-summary)

---

## 🎯 Purpose

This module exists to:

* eliminate ad-hoc OpenAPI generator setup
* enforce **contract-first client generation**
* provide **deterministic template control**
* standardize build-time behavior across all consumers

Core idea:

> OpenAPI generation is not configuration — it is orchestration.

---

## 🧠 Architectural Role

Within the platform:

| Layer      | Module          | Role                       |
| ---------- | --------------- | -------------------------- |
| Authority  | `openapi-generics-contract`  | Defines response semantics |
| Projection | server starter  | Produces OpenAPI           |
| Execution  | **this module** | Orchestrates generation    |
| Rendering  | templates       | Shape final code           |

This module is **NOT a library**.

It is:

> A build-time execution environment for OpenAPI generation.

---

## ⚙️ What It Provides

By inheriting this parent, users automatically get:

### 1. Custom Generator Binding

```
generatorName = java-generics-contract
```

Backed by:

* `openapi-generics-java-codegen`

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

No manual template management required.

---

### 3. Contract Mapping

Mappings are injected automatically:

```
ServiceResponse → openapi-generics-contract
Meta → openapi-generics-contract
Page → openapi-generics-contract
```

Result:

> Generated models reuse contract classes instead of duplicating them.

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

The build will fail if:

* upstream template structure changes
* wrapper injection is missing

Example failure:

```
OpenAPI template patch FAILED
```

This prevents silent breakage.

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

---

## 🧩 Key Components

### Template Extraction

Extracts `model.mustache` from upstream OpenAPI generator.

---

### Template Patch

Injects:

```
{{#vendorExtensions.x-api-wrapper}}{{>api_wrapper}}{{/vendorExtensions.x-api-wrapper}}
```

This enables wrapper-based generation.

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
* additional properties

---

### Generated Sources Registration

Ensures generated code is compiled as part of the project.

---

## 📦 Usage

### 1. Inherit Parent

```xml
<parent>
  <groupId>io.github.blueprintplatform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>0.8.0-SNAPSHOT</version>
</parent>
```

---

### 2. Configure OpenAPI Generator Plugin

Only minimal configuration is required:

```xml
<plugin>
  <groupId>org.openapitools</groupId>
  <artifactId>openapi-generator-maven-plugin</artifactId>

  <executions>
    <execution>
      <goals>
        <goal>generate</goal>
      </goals>

      <configuration>
        <inputSpec>path/to/openapi.yaml</inputSpec>
        <library>restclient</library>
      </configuration>

    </execution>
  </executions>
</plugin>
```

No generator wiring needed.

---

### 3. Build

```
mvn clean install
```

Generated sources will be available automatically.

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

The platform uses the official OpenAPI generator.

---

### No Runtime Behavior

Everything happens at build time.

---

### No Contract Duplication

All core models come from `openapi-generics-contract`.

---

### Controlled Template System

Templates are patched and validated.

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

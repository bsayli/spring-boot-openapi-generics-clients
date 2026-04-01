# OpenAPI Generics Platform — Architecture Decisions (0.8.x)

## Table of Contents

1. [Vision](#vision)
2. [High-Level Platform Structure](#highlevel-platform-structure)
3. [Core Architectural Principle](#core-architectural-principle)
4. [Contract Ownership Principle](#contract-ownership-principle)
5. [Architectural Layers (Authority → Projection → Consumption)](#architectural-layers-authority--projection--consumption)
6. [Supported Contract Shapes (Explicit Scope)](#supported-contract-shapes-explicit-scope)
7. [Server Platform Design (Deterministic Pipeline)](#server-platform-design-deterministic-pipeline)
8. [Code Generation Platform Design (Execution Model)](#code-generation-platform-design-execution-model)
9. [Dependency & Packaging Strategy (BOM + Starter + Codegen Parent)](#dependency--packaging-strategy-bom--starter--codegen-parent)
10. [Configuration Philosophy](#configuration-philosophy)
11. [Generator Orchestration Strategy](#generator-orchestration-strategy)
12. [Deterministic Wrapper Semantics](#deterministic-wrapper-semantics)
13. [Developer Experience Goal](#developer-experience-goal)
14. [Adoption Strategy Principle](#adoption-strategy-principle)
15. [Long-Term Evolution Axis](#long-term-evolution-axis)
16. [Final Architectural Identity](#final-architectural-identity)

---

## Vision

Provide a deterministic, contract‑aware OpenAPI generation and client consumption platform
for Spring Boot microservice ecosystems.

Goals:

* eliminate copy‑paste DTO envelopes
* guarantee schema stability
* hide template complexity from end users
* enable thin generated wrappers
* enforce build-time determinism

This platform is NOT a generic Java generics resolver.

It is:

> An OpenAPI contract determinism layer with a build-time execution model

---

## High‑Level Platform Structure

```
spring-boot-openapi-generics-clients
│
├── openapi-generics-bom
├── api-contract
├── openapi-generics-server-starter
├── openapi-generics-java-codegen   <-- build-time execution layer
├── customer-service (sample producer)
├── customer-service-client (sample consumer)
└── pom.xml (aggregator)
```

Key shift:

> Client generation is no longer modeled as a library system, but as a build-time execution system.

---

## Core Architectural Principle

The platform enforces:

> Contract-aware, deterministic response envelope semantics across the full lifecycle

Across:

* Spring controllers
* OpenAPI publication
* Generator templates
* Generated clients

Guarantees:

* no envelope duplication
* deterministic schema composition
* stable client generation

---

## Contract Ownership Principle

The response envelope is owned by `api-contract`, not by OpenAPI.

OpenAPI is treated as a projection layer, not as a source of truth.

Implications:

* Generators must not define or reinterpret response structures
* Clients must not duplicate envelope models
* Server and client share the same canonical contract

This eliminates schema drift and ensures deterministic API evolution.

---

## Architectural Layers (Authority → Projection → Consumption)

### 1. Contract Layer (Authority)

Module: `api-contract`

* framework-agnostic
* long-lived
* semantic source of truth

Defines:

* `ServiceResponse<T>`
* `Page<T>`
* envelope semantics

This layer is the **only authority**.

---

### 2. Server Layer (Projection)

Module: `openapi-generics-server-starter`

Transforms:

> Contract → deterministic OpenAPI specification

Responsibilities:

* discover response types
* introspect contract shapes
* generate wrapper schemas
* enforce schema correctness

This layer is:

* deterministic
* idempotent
* specification-only

---

### 3. Code Generation Layer (Consumption via Execution)

Module: `openapi-generics-java-codegen`

Transforms:

> OpenAPI → generated client code (via controlled build execution)

This is NOT a typical library layer.

It is:

> A build-time orchestration and template control system

Responsibilities:

* prepare effective templates (extract + patch + overlay)
* enforce deterministic template behavior
* inject contract-aware mappings (imports, wrappers)
* control generator environment via pluginManagement
* ensure fail-fast behavior on upstream template changes

Key property:

> Users do not configure generation — they inherit it.

---

## Supported Contract Shapes (Explicit Scope)

Supported:

* `ServiceResponse<T>`
* `ServiceResponse<Page<T>>`

Out of scope:

* `List<T>` wrappers
* `Map<K,V>` wrappers
* nested generics
* polymorphic containers

This constraint ensures:

* deterministic naming
* stable generation
* bounded complexity

---

## Server Platform Design (Deterministic Pipeline)

The server starter is built around a **single deterministic pipeline**.

### Execution Model

```
OpenApiCustomizer
        ↓
OpenApiPipelineOrchestrator
        ↓
[1] Base Schema Registration
[2] Discovery
[3] Introspection
[4] Wrapper Processing
[5] Validation (fail-fast)
```

### Key Guarantees

* single execution path
* no multiple customizers
* no ordering hacks
* idempotent execution
* fail-fast validation

---

### Pipeline Stages

#### 1. Base Schema Registration

Ensures canonical schemas exist:

* ServiceResponse
* ServiceResponseVoid
* Meta
* Sort

---

#### 2. Discovery

Extracts controller return types via pluggable strategy.

---

#### 3. Introspection

Determines contract-aware types and produces deterministic schema names.

---

#### 4. Wrapper Processing

Authoritative schema generation.

Key rule:

> No merge, no patch — only deterministic overwrite

---

#### 5. Validation

Final contract enforcement with fail-fast guarantees.

---

## Code Generation Platform Design (Execution Model)

### Core Insight

The platform has evolved from:

```
library-based client tooling
```

into:

```
build-time controlled code generation system
```

---

### Execution Flow

```
OpenAPI Spec
   ↓
Codegen Parent (pluginManagement)
   ↓
Template Preparation (extract → patch → overlay)
   ↓
OpenAPI Generator Execution
   ↓
Generated Sources (deterministic)
```

---

### Responsibilities of Codegen Parent

* centralizes plugin configuration
* enforces template structure
* injects vendor-extension-aware behavior
* manages generator lifecycle phases
* validates upstream compatibility

---

### What It Explicitly Avoids

* no custom generator fork
* no runtime behavior
* no hidden magic

---

## Dependency & Packaging Strategy (BOM + Starter + Codegen Parent)

### BOM (openapi-generics-bom)

Provides:

* version alignment
* ecosystem boundary
* controlled dependency surface

---

### Server Starter

Provides:

* zero-config integration
* deterministic OpenAPI projection

---

### Codegen Parent (Critical)

Provides:

* generator orchestration
* template control
* lifecycle standardization

Acts as:

> Distribution mechanism for build-time behavior

---

### Aggregator

Provides:

* internal cohesion
* module orchestration

---

## Configuration Philosophy

Default:

> zero configuration

Users should NOT:

* modify templates
* tweak generator internals

Optional:

* additional annotations (extension hooks)

---

## Generator Orchestration Strategy

Chosen model:

> CONTROLLED EXTERNAL GENERATOR

Platform does NOT fork or replace the generator.

Instead it:

* prepares deterministic inputs
* controls templates
* standardizes execution via parent POM

Result:

> Generator behaves deterministically without being owned

---

## Deterministic Wrapper Semantics

Wrappers use:

* OpenAPI `allOf`

Vendor extensions:

* x-api-wrapper
* x-api-wrapper-datatype
* x-data-container
* x-data-item

These act as:

> internal contract signals between server and codegen layers

---

## Developer Experience Goal

### Server

User adds:

```
openapi-generics-server-starter
```

Then returns:

```
ServiceResponse<T>
```

Everything else is automatic.

---

### Client

User:

* inherits codegen parent
* provides OpenAPI spec
* runs build

User does NOT:

* touch templates
* manage imports
* fix wrappers

---

## Adoption Strategy Principle

The platform must feel:

* invisible
* deterministic
* boring

Users should feel:

> their OpenAPI simply became correct

---

## Long-Term Evolution Axis

Future capabilities:

* reactive support
* spec validation tooling
* contract linting
* Gradle support
* multi-language engines

All must be:

> optional and non-breaking

---

## Final Architectural Identity

This platform is:

> A deterministic OpenAPI contract projection and build-time code generation system

It is NOT:

* a generator fork
* a template hack
* a client library bundle

It is:

> an architecture product with strict boundaries, deterministic behavior, and controlled execution

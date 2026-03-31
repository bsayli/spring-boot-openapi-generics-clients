# OpenAPI Generics Platform — Architecture Decisions (0.8.x)

## Table of Contents

1. [Vision](#vision)
2. [High-Level Platform Structure](#highlevel-platform-structure)
3. [Core Architectural Principle](#core-architectural-principle)
4. [Architectural Layers (Authority → Projection → Consumption)](#architectural-layers-authority--projection--consumption)
5. [Supported Contract Shapes (Explicit Scope)](#supported-contract-shapes-explicit-scope)
6. [Server Platform Design (Deterministic Pipeline)](#server-platform-design-deterministic-pipeline)
7. [Client Platform Design](#client-platform-design)
8. [Dependency & Packaging Strategy (BOM + Starter)](#dependency--packaging-strategy-bom--starter)
9. [Configuration Philosophy](#configuration-philosophy)
10. [Generator Orchestration Strategy](#generator-orchestration-strategy)
11. [Deterministic Wrapper Semantics](#deterministic-wrapper-semantics)
12. [Developer Experience Goal](#developer-experience-goal)
13. [Adoption Strategy Principle](#adoption-strategy-principle)
14. [Long-Term Evolution Axis](#long-term-evolution-axis)
15. [Final Architectural Identity](#final-architectural-identity)

---

## Vision

Provide a deterministic, contract‑aware OpenAPI generation and client consumption platform
for Spring Boot microservice ecosystems.

Goals:

* eliminate copy‑paste DTO envelopes
* guarantee schema stability
* hide template complexity from end users
* enable thin generated wrappers
* keep generator orchestration external and transparent

This platform is NOT a generic Java generics resolver.

It is:

> An OpenAPI contract determinism layer

---

## High‑Level Platform Structure

```
spring-boot-openapi-generics-clients
│
├── openapi-generics-bom
├── api-contract
├── openapi-generics-server-starter
├── openapi-generics-client-parent
├── openapi-generics-java-engine
├── openapi-generics-client-starter
├── customer-service (sample producer)
├── customer-service-client (sample consumer)
└── pom.xml (aggregator)
```

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

### 3. Client Layer (Consumption)

Modules:

* `openapi-generics-client-parent`
* `openapi-generics-java-engine`
* `openapi-generics-client-starter`

Consumes:

> OpenAPI → generated typed clients

Uses:

* vendor extensions
* template overlays

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

Extracts controller return types via pluggable strategy:

* MVC strategy (default)
* extensible for other frameworks

---

#### 3. Introspection

Determines contract-aware types:

* unwraps async/transport wrappers
* detects supported shapes
* produces deterministic schema names

---

#### 4. Wrapper Processing

Authoritative schema generation:

* always rebuilds wrapper schemas
* replaces existing definitions
* applies enrichment (e.g. Page<T>)

Key rule:

> No merge, no patch — only deterministic overwrite

---

#### 5. Validation

Final contract enforcement:

* required schemas exist
* wrapper structure is valid
* vendor extensions are present

Violations fail fast.

---

### Design Principles

* pipeline is the single source of execution truth
* components are replaceable, orchestration is not
* schema creation is centralized

---

## Client Platform Design

### openapi-generics-client-parent

* plugin management
* lifecycle defaults
* version alignment

---

### openapi-generics-java-engine

* template overlays
* wrapper generation logic
* language-specific adjustments

Pure capability module.

---

### openapi-generics-client-starter

* exposes contract classes
* simplifies onboarding

Does NOT:

* run generator
* control lifecycle

---

## Dependency & Packaging Strategy (BOM + Starter)

### BOM (openapi-generics-bom)

Provides:

* version alignment
* ecosystem boundary
* controlled dependency surface

Includes:

* api-contract
* server-starter
* springdoc alignment

---

### Starter

Provides:

* zero-config integration
* auto-configuration
* plug-and-play behavior

---

### Aggregator

Provides:

* internal build cohesion
* module orchestration

---

### Design Goal

> Eliminate dependency chaos and enforce platform consistency

---

## Configuration Philosophy

Default:

> zero configuration

Optional:

* extra annotation for generated wrappers

Non-goals:

* HTTP behavior configuration
* serialization control

These belong to the application layer.

---

## Generator Orchestration Strategy

Chosen model:

> SAFE MODEL — Pre-Generator Preparation

Platform does NOT control:

* OpenAPI Generator plugin

Platform ensures:

* deterministic schema
* template compatibility

User owns generator execution.

---

## Deterministic Wrapper Semantics

Wrappers use:

* OpenAPI `allOf`

Structure:

* base envelope reference
* data override

Vendor extensions:

* x-api-wrapper
* x-api-wrapper-datatype
* x-data-container
* x-data-item

These act as:

> internal contract signals between server and client layers

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

* inherits parent
* runs generator

No template awareness required.

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

> A deterministic OpenAPI contract projection and consumption system

It is NOT:

* a generator fork
* a template hack
* a generics playground

It is:

> an architecture product with strict boundaries and deterministic behavior

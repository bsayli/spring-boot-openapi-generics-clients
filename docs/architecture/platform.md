# openapi-generics-platform — Architecture (0.8.x)

This document defines the **complete architectural model of the OpenAPI Generics Platform**.

It describes how **contract, projection, and generation** operate as a single deterministic system.

The platform unifies:

* server-side projection (runtime → OpenAPI)
* client-side generation (OpenAPI → Java client)
* shared contract authority (SSOT)

This document is the **top-level architecture reference**.

---

## Table of Contents

1. [Vision](#1-vision)
2. [Architectural Identity](#2-architectural-identity)
3. [System Boundaries (Authority vs Projection vs Consumption)](#3-system-boundaries-authority-vs-projection-vs-consumption)
4. [High-Level Platform Structure](#4-high-level-platform-structure)
5. [End-to-End Lifecycle (Contract → OpenAPI → Client)](#5-end-to-end-lifecycle-contract--openapi--client)
6. [Core Architectural Principles](#6-core-architectural-principles)
7. [Contract Ownership Model](#7-contract-ownership-model)
8. [Server Architecture (Projection Layer)](#8-server-architecture-projection-layer)
9. [Client Architecture (Generation Layer)](#9-client-architecture-generation-layer)
10. [Build-Time Execution Model](#10-build-time-execution-model)
11. [Dependency & Distribution Strategy](#11-dependency--distribution-strategy)
12. [Determinism Guarantees (Platform-Wide)](#12-determinism-guarantees-platform-wide)
13. [Vendor Extensions as Cross-Layer Protocol](#13-vendor-extensions-as-cross-layer-protocol)
14. [Supported Contract Scope](#14-supported-contract-scope)
15. [Developer Experience Model](#15-developer-experience-model)
16. [Design Trade-offs](#16-design-trade-offs)
17. [Failure Philosophy](#17-failure-philosophy)
18. [Evolution Strategy](#18-evolution-strategy)
19. [Mental Model](#19-mental-model)
20. [Final Summary](#20-final-summary)

---

## 1. Vision

Provide a **deterministic, contract-first API platform** where:

* Java contract is the single source of truth
* OpenAPI is a **lossless projection**, not an authority
* clients are generated without redefining models

This directly addresses common failure modes in API ecosystems:

* duplicated DTO hierarchies
* schema drift between server and client
* unstable client regeneration
* generator-specific coupling

Goals:

* eliminate DTO duplication
* enforce schema stability
* guarantee deterministic generation
* hide generator complexity from users
* enable near zero-configuration adoption

---

## 2. Architectural Identity

The platform is NOT:

* an OpenAPI generator fork
* a template customization toolkit
* a client SDK bundle

The platform IS:

> A contract-first, deterministic projection and generation system

It treats OpenAPI as a **transport format**, not a modeling authority.

---

## 3. System Boundaries (Authority vs Projection vs Consumption)

| Layer     | Responsibility                 | Nature             |
| --------- | ------------------------------ | ------------------ |
| Contract  | Semantic authority (SSOT)      | Stable, long-lived |
| Server    | Projection (runtime → OpenAPI) | Deterministic      |
| Generator | Enforcement (OpenAPI → code)   | Build-time         |
| Client    | Consumption                    | Generated          |

### Core Rule

> Authority MUST NOT exist outside the contract layer.

### Implication

* no schema ownership in OpenAPI
* no model ownership in generator
* no duplication in clients

---

## 4. High-Level Platform Structure

```text
openapi-generics
│
├── openapi-generics-contract                         # Contract authority (SSOT)
├── openapi-generics-platform-bom                    # Version alignment (ecosystem boundary)
├── openapi-generics-server-starter                  # Server projection layer (runtime → OpenAPI)
├── openapi-generics-java-codegen                    # Generator engine (OpenAPI → code)
├── openapi-generics-java-codegen-parent             # Build-time orchestration (plugin + templates)
│
├── samples                                          # Reference implementations (non-authoritative)
│   ├── customer-service                             # Sample producer (contract → OpenAPI)
│   └── customer-service-client                      # Sample consumer (OpenAPI → client usage)
│
└── pom.xml                                          # Root aggregator
```

---

### Structural Intent

The platform is deliberately split into two domains:

```text
Platform (authoritative)
   ├── Contract
   ├── Server (projection)
   ├── Generator (enforcement)
   └── BOM (distribution)

Samples (demonstration only)
   ├── Producer example
   └── Consumer example
```

### Key Principle

> Only the platform defines behavior. Samples only demonstrate usage.

This separation ensures:

* no leakage of sample logic into core modules
* stable architectural boundaries
* controlled evolution of platform behavior

---

## 5. End-to-End Lifecycle (Contract → OpenAPI → Client)

```
Java Contract (Authority)
        ↓
Server Starter (Projection)
        ↓
OpenAPI (Deterministic Projection)
        ↓
Codegen Parent + Generator
        ↓
Generated Java Client
```

### Key Insight

> Each layer transforms — none reinterpret.

Every step is a **mechanical transformation**, not a semantic rewrite.

---

## 6. Core Architectural Principles

### 6.1 Contract First

* contract defines semantics
* all downstream artifacts are derived

---

### 6.2 Determinism

* same input → same output
* no ordering dependency
* no hidden behavior

---

### 6.3 Explicitness

* no implicit inference beyond defined rules
* all transformations are controlled

---

### 6.4 Separation of Concerns

* runtime (server) and build-time (generation) are strictly isolated

---

## 7. Contract Ownership Model

Owned by: `openapi-generics-contract`

Defines canonical types:

* `ServiceResponse<T>`
* `Meta`
* `Page<T>`

### Rule

> OpenAPI MUST NOT redefine these types.

### Implication

* clients reuse contract classes directly
* no duplicated model graphs
* no schema drift

---

## 8. Server Architecture (Projection Layer)

Transforms:

> Contract → OpenAPI

### Model

* pipeline-based
* single-pass execution
* deterministic output

### Responsibilities

* discover controller return types
* resolve contract shapes
* generate wrapper schemas
* inject vendor extensions
* enforce projection invariants

---

## 9. Client Architecture (Generation Layer)

Transforms:

> OpenAPI → Java Client

### Model

* build-time execution
* template-driven generation
* generator-controlled behavior

### Responsibilities

* suppress contract-owned models
* generate thin wrapper classes
* inject generics correctly
* reuse contract classes via import mappings

---

## 10. Build-Time Execution Model

```
OpenAPI Spec
   ↓
Codegen Parent (pluginManagement)
   ↓
Template Pipeline (extract → patch → overlay)
   ↓
Generator Execution
   ↓
Generated Sources
```

### Key Property

> Users do not assemble the pipeline — they inherit it.

This removes configuration variance across projects.

---

## 11. Dependency & Distribution Strategy

### BOM

* defines version alignment
* establishes ecosystem boundary

### Server Starter

* integrates projection at runtime

### Codegen Parent

* orchestrates build-time generation

### Contract

* provides shared semantic layer

---

## 12. Determinism Guarantees (Platform-Wide)

The platform guarantees:

* stable OpenAPI output
* stable client generation
* reproducible builds

Mechanisms:

* single pipeline execution
* explicit mappings
* controlled template system

---

## 13. Vendor Extensions as Cross-Layer Protocol

Key extensions:

* `x-api-wrapper`
* `x-api-wrapper-datatype`
* `x-data-container`
* `x-data-item`
* `x-ignore-model`

### Role

> Internal protocol between projection and generation layers

They carry **semantic intent across boundaries without redefining models**.

---

## 14. Supported Contract Scope

Supported:

* `ServiceResponse<T>`
* `ServiceResponse<Page<T>>`

Not supported:

* nested generics
* arbitrary collections
* maps

### Rationale

* bounded complexity
* deterministic schema generation
* stable client typing

---

## 15. Developer Experience Model

### Server Side

User:

* adds starter dependency
* returns `ServiceResponse<T>`

System:

* generates OpenAPI automatically

---

### Client Side

User:

* inherits parent POM
* provides OpenAPI spec
* runs build

System:

* generates contract-aligned client

---

## 16. Design Trade-offs

### Restricted Flexibility

Users cannot arbitrarily customize generation.

Gain:

* predictability
* consistency

---

### Template Patching

Relies on upstream template structure.

Trade-off:

* higher control vs upstream coupling

---

### No Generator Fork

Uses official OpenAPI Generator.

Trade-off:

* stability vs deep customization

---

## 17. Failure Philosophy

System behavior:

* invalid contract → fail fast
* invalid projection → fail fast

No:

* silent fallback
* partial generation

### Principle

> Incorrect output is worse than no output

---

## 18. Evolution Strategy

Potential extensions:

* reactive support
* validation tooling
* multi-language client generation

### Rule

> All evolution must preserve determinism and contract authority

---

## 19. Mental Model

Think of the platform as:

> A deterministic compiler pipeline spanning runtime and build-time

Not:

* a helper library
* a set of templates

---

## 20. Final Summary

The platform is:

* contract-first
* deterministic
* pipeline-driven
* build-time enforced

Its responsibility is strictly:

> Ensure contract semantics flow unchanged from server to client

Nothing more.

# openapi-generics-platform — Architecture (0.8.x)

This document defines the **complete architectural model of the OpenAPI Generics Platform**.

It unifies:

* server-side projection (runtime → OpenAPI)
* client-side generation (OpenAPI → Java client)
* shared contract authority

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
* OpenAPI is a lossless projection
* clients are generated without redefining models

Goals:

* eliminate DTO duplication
* enforce schema stability
* guarantee deterministic generation
* hide generator complexity from users
* enable zero-config adoption

---

## 2. Architectural Identity

The platform is NOT:

* an OpenAPI generator fork
* a template customization toolkit
* a client SDK bundle

The platform IS:

> A contract-first, deterministic projection and generation system

---

## 3. System Boundaries (Authority vs Projection vs Consumption)

| Layer     | Responsibility                 | Nature             |
| --------- | ------------------------------ | ------------------ |
| Contract  | Semantic authority (SSOT)      | Stable, long-lived |
| Server    | Projection (runtime → OpenAPI) | Deterministic      |
| Generator | Enforcement (OpenAPI → code)   | Build-time         |
| Client    | Consumption                    | Generated          |

### Rule

> Authority MUST NOT exist outside the contract layer.

---

## 4. High-Level Platform Structure

```
spring-boot-openapi-generics-clients
│
├── api-contract
├── openapi-generics-platform-bom
├── openapi-generics-server-starter
├── openapi-generics-java-codegen
├── openapi-generics-java-codegen-parent
├── customer-service (sample producer)
├── customer-service-client (sample consumer)
└── aggregator
```

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

---

## 6. Core Architectural Principles

### 6.1 Contract First

* contract defines semantics
* everything else is derived

### 6.2 Determinism

* same input → same output
* no implicit behavior

### 6.3 Explicitness

* no inference beyond defined rules

### 6.4 Separation of Concerns

* runtime vs build-time are isolated

---

## 7. Contract Ownership Model

Owned by: `api-contract`

Defines:

* `ServiceResponse<T>`
* `Meta`
* `Page<T>`

### Rule

> OpenAPI MUST NOT redefine these types.

### Implication

* no duplication in clients
* no schema drift

---

## 8. Server Architecture (Projection Layer)

Transforms:

> Contract → OpenAPI

### Model

* pipeline-based
* single execution
* deterministic

### Responsibilities

* discover response types
* introspect contract shapes
* generate wrapper schemas
* inject vendor extensions
* enforce invariants

---

## 9. Client Architecture (Generation Layer)

Transforms:

> OpenAPI → Java Client

### Model

* build-time execution
* template-driven
* generator-controlled

### Responsibilities

* suppress contract models
* generate thin wrappers
* inject generics
* reuse contract classes

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

> Users do not configure the system — they inherit it.

---

## 11. Dependency & Distribution Strategy

### BOM

* version alignment
* ecosystem boundary

### Server Starter

* runtime integration

### Codegen Parent

* build-time orchestration

### Contract

* shared semantic layer

---

## 12. Determinism Guarantees (Platform-Wide)

The platform guarantees:

* stable OpenAPI output
* stable client generation
* no ordering dependency

Mechanisms:

* single pipeline execution
* explicit mappings
* template control

---

## 13. Vendor Extensions as Cross-Layer Protocol

Extensions:

* x-api-wrapper
* x-api-wrapper-datatype
* x-data-container
* x-data-item
* x-ignore-model

### Role

> Internal protocol between server and generator

---

## 14. Supported Contract Scope

Supported:

* ServiceResponse<T>
* ServiceResponse<Page<T>>

Not supported:

* nested generics
* collections
* maps

### Reason

* determinism
* bounded complexity

---

## 15. Developer Experience Model

### Server

User:

* adds starter
* returns ServiceResponse

Everything else is automatic.

### Client

User:

* inherits parent
* provides spec
* runs build

---

## 16. Design Trade-offs

### Restricted flexibility

* ensures predictability

### Template patching

* risk vs control

### No generator fork

* stability vs control

---

## 17. Failure Philosophy

System behavior:

* invalid contract → fail
* invalid projection → fail

No:

* silent fallback
* partial output

---

## 18. Evolution Strategy

Future extensions:

* reactive support
* validation tooling
* multi-language generation

### Rule

> Extensions must preserve determinism

---

## 19. Mental Model

Think of the platform as:

> A deterministic compiler pipeline across runtime and build-time

Not:

* a helper library
* a template tweak

---

## 20. Final Summary

The platform is:

* contract-first
* deterministic
* pipeline-driven
* build-time enforced

Its responsibility is:

> Ensure contract semantics flow unchanged from server to client

Nothing more.

# openapi-generics-server — Architecture & Internals (0.8.x)

This document explains **how the server starter works at runtime** and clarifies its **architectural role inside the platform**.

It is the implementation-level counterpart of `ARCHITECTURE.md`, updated to reflect the current design decisions and boundaries.

---

## What changed in this version?

This revision makes the following explicit:

* OpenAPI is a **projection layer**, not an authority
* The system is a **deterministic compiler**, not an enhancer
* Vendor extensions form an **internal DSL (generation protocol)**
* Wrapper schemas are **authoritatively reconstructed**, never patched
* The pipeline is a **single execution unit**, not a distributed lifecycle

---

## Table of Contents

1. [Runtime Execution Model](#1-runtime-execution-model)
2. [System Boundary (Authority vs Projection)](#2-system-boundary-authority-vs-projection)
3. [Pipeline Overview](#3-pipeline-overview)
4. [Stage Breakdown (Code-Level Mapping)](#4-stage-breakdown-code-level-mapping)
5. [Type System Strategy](#5-type-system-strategy)
6. [Wrapper Schema Strategy](#6-wrapper-schema-strategy)
7. [Vendor Extensions as Internal DSL](#7-vendor-extensions-as-internal-dsl)
8. [Determinism Guarantees](#8-determinism-guarantees)
9. [Extension Model (Controlled Extensibility)](#9-extension-model-controlled-extensibility)
10. [Design Trade-offs](#10-design-trade-offs)
11. [Failure Philosophy](#11-failure-philosophy)
12. [Mental Model](#12-mental-model)
13. [Summary](#13-summary)

---

## 1. Runtime Execution Model

Entry point:

```
OpenApiCustomizer → OpenApiPipelineOrchestrator::run
```

Execution flow:

1. Springdoc builds base OpenAPI model
2. Customizer is invoked
3. Pipeline executes once per OpenAPI instance

Execution guard:

* identity-based tracking
* prevents duplicate execution

### Invariant

> The pipeline runs exactly once per OpenAPI document

---

## 2. System Boundary (Authority vs Projection)

### Authority

* Java contract (`ServiceResponse<T>`, `Meta`, etc.)
* This is the **single source of truth (SSOT)**

### Projection

* OpenAPI document
* Generated schemas

### Rule

> OpenAPI MUST be a lossless, deterministic projection of the contract.

### Implication

* OpenAPI cannot introduce new semantics
* OpenAPI cannot override contract meaning
* OpenAPI cannot become authoritative

---

## 3. Pipeline Overview

```
[1] BaseSchemaRegistrar
[2] ResponseTypeDiscoveryStrategy
[3] ResponseTypeIntrospector
[4] WrapperSchemaProcessor
[5] SchemaGenerationControlMarker
[6] OpenApiContractGuard
```

### Properties

Each stage is:

* deterministic
* isolated in responsibility
* side-effect controlled

### Orchestrator Responsibility

* defines execution order
* enforces single execution path
* contains NO schema logic

---

## 4. Stage Breakdown (Code-Level Mapping)

### 4.1 BaseSchemaRegistrar

Responsibility:

* ensure canonical base schemas exist

Schemas:

* ServiceResponse
* ServiceResponseVoid
* Meta
* Sort

Design:

* idempotent
* non-overwriting

---

### 4.2 ResponseTypeDiscoveryStrategy

Responsibility:

* discover runtime response types

Default:

* MVC-based discovery

Important:

* no contract logic
* no OpenAPI logic

Output:

```
Set<ResolvableType>
```

---

### 4.3 ResponseTypeIntrospector

Responsibility:

* detect contract-aware shapes
* derive deterministic schema references

Supported shapes:

* ServiceResponse<T>
* ServiceResponse<Page<T>>

Rejected:

* nested generics
* arbitrary collections
* maps

### Key Rule

> Only explicitly supported shapes are processed. Everything else is ignored.

---

### 4.4 WrapperSchemaProcessor

Responsibility:

* authoritative wrapper schema creation

Behavior:

* ALWAYS rebuilds schema
* replaces existing schema

### Critical Design Rule

> Existing schemas are non-authoritative and MUST be replaced.

Naming:

```
ServiceResponse + DataType
```

---

### 4.5 SchemaGenerationControlMarker

Responsibility:

* control model generation

Behavior:

* marks schemas with `x-ignore-model`

Purpose:

* keep schemas in OpenAPI
* prevent code generation duplication

---

### 4.6 OpenApiContractGuard

Responsibility:

* validate contract integrity

Validations:

* base schemas exist
* wrapper structure is correct
* required extensions are present

Failure:

* immediate exception

---

## 5. Type System Strategy

The system intentionally supports a **restricted generic subset**.

### Supported

* ServiceResponse<T>
* ServiceResponse<Page<T>>

### Rejected

* nested generics
* List<T>
* Map<K,V>

### Rationale

* deterministic naming
* stable code generation
* bounded complexity

---

## 6. Wrapper Schema Strategy

Wrappers are built using:

```
allOf composition
```

Structure:

1. base schema reference
2. data override

### Rule

* no mutation
* no partial merge
* no inheritance tricks

---

## 7. Vendor Extensions as Internal DSL

Extensions:

* x-api-wrapper
* x-api-wrapper-datatype
* x-data-container
* x-data-item
* x-ignore-model

### Interpretation

These extensions form a:

> **flat, deterministic DSL between server and code generator**

### Important

* NOT public API
* MUST remain stable

---

## 8. Determinism Guarantees

The system guarantees:

* same input → same output
* no ordering dependency
* no runtime variability

Mechanisms:

* single pipeline entry
* LinkedHashSet ordering
* no distributed hooks

---

## 9. Extension Model (Controlled Extensibility)

Extension points:

### Discovery Strategy

* support additional frameworks

### Enricher

* support new container types

### Rule

> Extensions MUST preserve determinism.

---

## 10. Design Trade-offs

### Limited Generics

* gain: determinism
* loss: flexibility

---

### Authoritative Overwrite

* gain: correctness
* loss: backward compatibility

---

### No Reflection

* gain: predictability
* loss: dynamic capability

---

### Generator Decoupling

* gain: modularity
* loss: convenience

---

## 11. Failure Philosophy

Behavior:

* invalid contract → fail immediately

No:

* silent fallback
* partial fixes

### Principle

> A broken contract must fail, not degrade.

---

## 12. Mental Model

Think of the system as:

> A deterministic compiler from runtime contract to OpenAPI

Not:

* a patch layer
* a best-effort enhancer

---

## 13. Summary

The server starter is:

* deterministic
* contract-driven
* pipeline-based

Its responsibility is strictly:

> Transform contract semantics into a stable OpenAPI projection

Nothing more.

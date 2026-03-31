# openapi-generics-server-starter — Internals (0.8.x)

This document explains **how the server starter actually works at runtime**.

It is the implementation-level counterpart of `ARCHITECTURE.md`.

Focus:

* execution flow
* component responsibilities
* deterministic guarantees
* design trade-offs

---

## Table of Contents

1. [Runtime Execution Flow](#1-runtime-execution-flow)
2. [Pipeline Overview](#2-pipeline-overview)
3. [Stage Breakdown (Code-Level Mapping)](#3-stage-breakdown-code-level-mapping)
    - [3.1 BaseSchemaRegistrar](#31-baseschemaregistrar)
    - [3.2 ResponseTypeDiscoveryStrategy](#32-responsetypediscoverystrategy)
    - [3.3 ResponseTypeIntrospector](#33-responsetypeintrospector)
    - [3.4 WrapperSchemaProcessor](#34-wrapperschemaprocessor)
    - [3.5 WrapperSchemaEnricher](#35-wrapperschemaenricher)
    - [3.6 OpenApiContractGuard](#36-openapicontractguard)
4. [Type System Strategy](#4-type-system-strategy)
5. [Wrapper Schema Strategy](#5-wrapper-schema-strategy)
6. [Vendor Extensions Contract](#6-vendor-extensions-contract)
7. [Determinism Guarantees](#7-determinism-guarantees)
8. [Extension Points](#8-extension-points)
9. [Design Trade-offs](#9-design-trade-offs)
10. [Failure Philosophy](#10-failure-philosophy)
11. [Mental Model](#11-mental-model)
12. [Summary](#12-summary)

---

## 1. Runtime Execution Flow

The entire system is triggered via **Springdoc integration**.

Entry point:

```
OpenApiCustomizer → OpenApiPipelineOrchestrator::run
```

Lifecycle:

1. Springdoc builds base OpenAPI model
2. `OpenApiCustomizer` is invoked
3. Pipeline executes once per OpenAPI instance

Execution guard:

* identity-based tracking
* prevents duplicate runs

Key property:

> The pipeline is executed exactly once per OpenAPI document

---

## 2. Pipeline Overview

```
[1] BaseSchemaRegistrar
[2] ResponseTypeDiscoveryStrategy
[3] ResponseTypeIntrospector
[4] WrapperSchemaProcessor
[5] OpenApiContractGuard
```

Each stage is:

* deterministic
* side-effect controlled
* independently replaceable

The orchestrator defines **ordering and execution**, not behavior.

### Invariant

The pipeline does not infer behavior.
It strictly enforces contract boundaries.

---

## 3. Stage Breakdown (Code-Level Mapping)

### 3.1 BaseSchemaRegistrar

Responsibility:

* ensure canonical schemas exist

Schemas:

* ServiceResponse
* ServiceResponseVoid
* Meta
* Sort

Design:

* idempotent
* non-overwriting

Input:

* OpenAPI

Output:

* guaranteed base schemas

---

### 3.2 ResponseTypeDiscoveryStrategy

Responsibility:

* discover controller return types

Default implementation:

* `MvcResponseTypeDiscoveryStrategy`

Mechanism:

* scans `RequestMappingHandlerMapping`
* extracts `ResolvableType`

Important:

* no contract logic
* no OpenAPI logic

Output:

* Set<ResolvableType>

---

### 3.3 ResponseTypeIntrospector

Responsibility:

* detect contract-aware shapes
* produce deterministic schema reference names

Steps:

1. unwrap transport wrappers
2. detect `ServiceResponse<T>`
3. validate supported shapes
4. derive schema name

Supported unwrap layers:

* ResponseEntity
* CompletionStage / Future
* DeferredResult / WebAsyncTask

Constraints:

* max unwrap depth = 8

Rationale:

* prevents pathological recursion

Output:

* Optional<String> (schema ref name)

---

### 3.4 WrapperSchemaProcessor

Responsibility:

* authoritative wrapper schema generation

Behavior:

* always rebuilds schema
* replaces existing schema
* delegates enrichment

Key rule:

> Existing schemas are treated as non-authoritative and are always replaced

Naming:

```
ServiceResponse + DataRef
```

Example:

```
ServiceResponseCustomerDto
```

---

### 3.5 WrapperSchemaEnricher

Responsibility:

* detect container semantics
* attach metadata via extensions

Default container:

* Page

Extensions:

* x-data-container
* x-data-item

Detection strategy:

* prefix-based (e.g. PageCustomerDto)
* strict match (avoids false positives)

---

### 3.6 OpenApiContractGuard

Responsibility:

* fail-fast validation

Validations:

* base schemas exist
* wrapper structure uses allOf
* required extensions exist

Failure mode:

* throws IllegalStateException

Design:

* minimal
* invariant-focused

---

## 4. Type System Strategy

The system supports a **strict subset of generics**.

Supported:

* ServiceResponse<T>
* ServiceResponse<Page<T>>

Rejected:

* nested generics
* collections
* maps

Rationale:

* deterministic naming
* bounded complexity
* generator stability

---

## 5. Wrapper Schema Strategy

Wrappers are constructed via:

* OpenAPI `allOf`

Structure:

1. base envelope reference
2. data override

Example:

```
allOf:
  - $ref: ServiceResponse
  - properties:
      data:
        $ref: CustomerDto
```

Key decisions:

* no inheritance tricks
* no schema mutation
* no partial merge

---

## 6. Vendor Extensions Contract

Extensions act as **internal protocol**.

Core extensions:

* x-api-wrapper
* x-api-wrapper-datatype
* x-data-container
* x-data-item

Purpose:

* bridge server → generator → client

Important:

* not public API
* must remain stable

---

## 7. Determinism Guarantees

The system guarantees:

* same input → same OpenAPI output
* no ordering dependency
* no runtime variability

Mechanisms:

* LinkedHashSet for ordering
* single pipeline entry
* no distributed hooks

---

## 8. Extension Points

Supported extension areas:

### ResponseTypeDiscoveryStrategy

* add new framework support

### WrapperSchemaEnricher

* support new container types

Design rule:

> extension must not break determinism

---

## 9. Design Trade-offs

### Limited Generic Support

Trade-off:

* simplicity vs flexibility

Decision:

* choose determinism

---

### Authoritative Overwrite

Trade-off:

* compatibility vs correctness

Decision:

* always overwrite

---

### No Reflection

Trade-off:

* power vs predictability

Decision:

* use ResolvableType only

---

### No Generator Coupling

Trade-off:

* convenience vs independence

Decision:

* keep generator external

---

## 10. Failure Philosophy

System behavior:

* invalid contract → fail immediately

No:

* silent fallback
* partial fixes

Reason:

> broken contract is worse than failing fast

---

## 11. Mental Model

Think of the system as:

> A deterministic compiler from runtime types to OpenAPI schemas,
> with strict contract boundary enforcement

Not:

* a patch layer
* a best-effort enhancer

---

## 12. Summary

The server starter is:

* a pipeline
* deterministic
* contract-driven

Its responsibility is strictly:

> Transform contract semantics into a stable OpenAPI representation

Nothing more.

# openapi-generics-java-client — Architecture & Usage (0.8.x)

This document explains the **client-side architecture, build pipeline, and usage model** of the generics-aware OpenAPI system.

It covers:

* `openapi-generics-java-codegen`
* `openapi-generics-java-codegen-parent`
* template system (`api_wrapper.mustache`)
* end-user consumption model

---

## Table of Contents

1. [System Overview](#1-system-overview)
2. [Architectural Positioning (Authority vs Projection)](#2-architectural-positioning-authority-vs-projection)
3. [End-to-End Pipeline](#3-end-to-end-pipeline)
4. [Module Responsibilities](#4-module-responsibilities)
5. [Generator Design (`openapi-generics-java-codegen`)](#5-generator-design-openapi-generics-java-codegen)
6. [Template System (`api_wrapper.mustache`)](#6-template-system-api_wrappermustache)
7. [Parent POM as Build Orchestrator](#7-parent-pom-as-build-orchestrator)
8. [Build-Time Template Pipeline](#8-build-time-template-pipeline)
9. [Contract Mapping Strategy](#9-contract-mapping-strategy)
10. [Determinism Guarantees](#10-determinism-guarantees)
11. [Usage Model (Consumer Perspective)](#11-usage-model-consumer-perspective)
12. [Design Trade-offs](#12-design-trade-offs)
13. [Failure Philosophy](#13-failure-philosophy)
14. [Mental Model](#14-mental-model)
15. [Summary](#15-summary)
---

## 1. System Overview

The client system is a **build-time transformation pipeline** that produces contract-aligned Java clients from OpenAPI.

It is NOT a standard OpenAPI codegen usage.

Instead, it enforces:

> Contract-first architecture with deterministic projection

---

## 2. Architectural Positioning (Authority vs Projection)

| Layer                          | Role                       |
| ------------------------------ | -------------------------- |
| Java Contract (`openapi-generics-contract`) | **Authority (SSOT)**       |
| OpenAPI                        | Projection (metadata only) |
| Generator                      | Enforcement layer          |
| Templates                      | Rendering layer            |
| Client Code                    | Projection output          |

Key rule:

> OpenAPI must NOT redefine platform-owned types.

---

## 3. End-to-End Pipeline

```
OpenAPI (projection)
   ↓
Custom Generator (java-generics-contract)
   ↓
Template Patch + Overlay
   ↓
Generated Client (contract-aligned)
```

Important:

* OpenAPI is NOT used as a model source
* OpenAPI is used as a **structural signal**

---

## 4. Module Responsibilities

### 4.1 `openapi-generics-java-codegen`

Responsibility:

* extend OpenAPI Generator
* enforce contract boundaries
* suppress platform-owned models

---

### 4.2 `openapi-generics-java-codegen-parent`

Responsibility:

* orchestrate build pipeline
* inject templates
* bind generator
* standardize configuration

Critical role:

> This module is the **actual product surface**

---

## 5. Generator Design (`openapi-generics-java-codegen`)

Custom generator:

```
GenericAwareJavaCodegen extends JavaClientCodegen
```

### Key behavior

#### Phase 1 — MARK

* detect `x-ignore-model`
* collect ignored model names

#### Phase 2 — LOCAL FILTER

* remove models from template processing

#### Phase 3 — GLOBAL REMOVE

* remove models from generation graph

---

### Design outcome

> Models can exist in OpenAPI but are not generated

This enables:

* reference usage
* zero duplication of contract models

---

## 6. Template System (`api_wrapper.mustache`)

Core template:

```
public class {{classname}} extends ServiceResponse<...>
```

Behavior:

* wraps generated models
* injects generics
* reuses contract types

### Container-aware generation

```
Page<T> vs T
```

via:

* `x-data-container`
* `x-data-item`

---

### Design principle

> Generate structure, reuse behavior

---

## 7. Parent POM as Build Orchestrator

The parent POM wires everything together.

User action:

```
<parent>
  openapi-generics-java-codegen-parent
</parent>
```

Result:

* generator is injected
* templates are patched
* contract mappings are applied

No manual setup required.

---

## 8. Build-Time Template Pipeline

### Step 1 — Extract upstream template

```
openapi-generator → model.mustache
```

### Step 2 — Patch

* inject `api_wrapper` hook
* enforce structure

### Step 3 — Overlay

* add custom templates

### Step 4 — Execute generator

---

### Safety

* patch validation exists
* build fails if upstream template changes

---

## 9. Contract Mapping Strategy

Mapping is explicit:

```
ServiceResponse → openapi-generics-contract
Meta → openapi-generics-contract
Page → openapi-generics-contract
```

Mechanism:

* `importMappings`
* `additionalProperties`

---

### Result

Generated code:

* references contract
* does NOT re-generate it

---

## 10. Determinism Guarantees

The system guarantees:

* same OpenAPI → same output
* no runtime variation
* no implicit behavior

Mechanisms:

* controlled pipeline
* fixed template structure
* explicit mappings

---

## 11. Usage Model (Consumer Perspective)

### Minimal setup

1. Add parent:

```
<parent>
  openapi-generics-java-codegen-parent
</parent>
```

2. Configure plugin:

```
openapi-generator-maven-plugin
```

3. Provide OpenAPI spec

---

### What user gets

* contract-aligned client
* generics-safe wrappers
* zero duplication

---

## 12. Design Trade-offs

### Limited flexibility

* only supported shapes allowed
* prevents ambiguity

---

### Template patching

* fragile vs upstream
* mitigated with fail-fast

---

### No model generation for core types

* requires contract dependency

---

## 13. Failure Philosophy

System behavior:

* invalid structure → fail build
* missing contract → fail build

No:

* silent fallback
* partial generation

---

## 14. Mental Model

Think of the system as:

> A deterministic compiler from OpenAPI projection to contract-aligned Java client

NOT:

* a generic OpenAPI generator
* a template tweak layer

---

## 15. Summary

The client system is:

* contract-first
* generator-enforced
* template-driven
* deterministic

Its responsibility is strictly:

> Transform OpenAPI projection into a contract-compliant Java client

Nothing more.

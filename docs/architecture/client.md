---
title: openapi-generics-java-client — Architecture & Usage (0.8.x)
nav_exclude: true
---

# openapi-generics-java-client — Architecture & Usage (0.8.x)

This document explains the **client-side architecture, build pipeline, and usage model** of the generics-aware OpenAPI system.

It focuses on how OpenAPI is transformed into a **contract-aligned Java client** without redefining models or losing type semantics.

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

The client system is a **build-time transformation pipeline** that produces **contract-aligned Java clients** from OpenAPI.

This is not conventional OpenAPI usage.

Instead of treating OpenAPI as a model source, the system treats it as a **projection artifact carrying structural intent**.

It enforces:

> Contract-first client generation with deterministic output

Result:

* no duplicated envelope models
* generics preserved end-to-end
* stable regeneration across builds

---

## 2. Architectural Positioning (Authority vs Projection)

| Layer                                       | Role                       |
| ------------------------------------------- | -------------------------- |
| Java Contract (`openapi-generics-contract`) | **Authority (SSOT)**       |
| OpenAPI                                     | Projection (metadata only) |
| Generator                                   | Enforcement layer          |
| Templates                                   | Rendering layer            |
| Client Code                                 | Projection output          |

### Core Rule

> OpenAPI must NOT redefine platform-owned types.

### Implication

* contract types are reused, not generated
* OpenAPI carries structure, not ownership

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

### Important

* OpenAPI is NOT a model authority
* OpenAPI acts as a **structural signal for generation**

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

* orchestrate the build pipeline
* inject templates
* bind custom generator
* standardize generation behavior

### Critical role

> This module is the **primary integration surface for users**

---

## 5. Generator Design (`openapi-generics-java-codegen`)

Custom generator:

```
GenericAwareJavaCodegen extends JavaClientCodegen
```

### Core behavior

#### Phase 1 — MARK

* detect `x-ignore-model`
* collect platform-owned models

#### Phase 2 — LOCAL FILTER

* remove ignored models from current processing

#### Phase 3 — GLOBAL REMOVE

* remove models from entire generation graph

---

### Design Outcome

> Models can exist in OpenAPI but are not generated

This enables:

* reference integrity
* zero duplication
* consistent contract reuse

---

## 6. Template System (`api_wrapper.mustache`)

Core template structure:

```
public class {{classname}} extends ServiceResponse<...>
```

### Behavior

* wraps generated models
* injects generics
* binds to contract types

---

### Container-Aware Generation

Supports:

```
Page<T> vs T
```

via:

* `x-data-container`
* `x-data-item`

---

### Design Principle

> Generate structure, reuse behavior

---

## 7. Parent POM as Build Orchestrator

The parent POM wires the entire system.

User action:

```
<parent>
  openapi-generics-java-codegen-parent
</parent>
```

### Result

* custom generator is injected
* templates are patched and applied
* contract mappings are configured

No manual wiring required.

---

## 8. Build-Time Template Pipeline

### Step 1 — Extract upstream template

```
openapi-generator → model.mustache
```

### Step 2 — Patch

* inject `api_wrapper` hook
* enforce wrapper structure

### Step 3 — Overlay

* add custom templates (`api_wrapper.mustache`)

### Step 4 — Execute generator

---

### Safety

* patch validation enforced
* build fails if upstream template structure changes

---

## 9. Contract Mapping Strategy

Mappings are explicit:

```
ServiceResponse → openapi-generics-contract
Meta → openapi-generics-contract
Page → openapi-generics-contract
```

### Mechanism

* `importMappings`
* controlled template usage

---

### Result

Generated code:

* references contract classes
* does NOT regenerate them

---

## 10. Determinism Guarantees

The system guarantees:

* same OpenAPI → same output
* no runtime variation
* no implicit behavior

### Mechanisms

* controlled pipeline execution
* fixed template structure
* explicit mapping rules

---

## 11. Usage Model (Consumer Perspective)

### Minimal Setup

1. Inherit parent POM
2. Configure OpenAPI Generator plugin
3. Provide OpenAPI spec

---

### What the User Gets

* contract-aligned client
* generics-safe wrappers
* no duplicated models

---

## 12. Design Trade-offs

### Limited Flexibility

* only supported shapes are allowed
* prevents ambiguity

---

### Template Patching

* depends on upstream structure
* mitigated via fail-fast checks

---

### No Core Model Generation

* requires contract dependency
* eliminates duplication

---

## 13. Failure Philosophy

System behavior:

* invalid structure → fail build
* missing contract → fail build

No:

* silent fallback
* partial generation

### Principle

> Incorrect output is worse than no output

---

## 14. Mental Model

Think of the system as:

> A deterministic compiler from OpenAPI projection to contract-aligned Java client

Not:

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

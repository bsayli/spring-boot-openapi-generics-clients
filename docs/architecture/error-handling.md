---
title: ProblemDetail-Centric Error Handling Strategy
nav_exclude: true
---

# ProblemDetail-Centric Error Handling Strategy

## Table of Contents

1. [Overview](#overview)
2. [Core Principles](#core-principles)
3. [Architectural Model](#architectural-model)
4. [Key Design Decisions](#key-design-decisions)
5. [Trade-offs](#trade-offs)
6. [When This Approach Fits Best](#when-this-approach-fits-best)
7. [Summary](#summary)

## Overview

This document defines a **runtime-first error handling strategy** based on **RFC 9457 Problem Details** as the **single canonical error model**.

The goal is simple:

> Define errors once at runtime, propagate them consistently, and avoid re-defining them in schemas or generated code.

The design explicitly separates concerns:

* **Runtime truth** → backend error model
* **Specification** → OpenAPI (success only)
* **Client interpretation** → language-specific adapters

This prevents duplication, eliminates drift, and keeps error semantics stable across services and clients.

---

## Core Principles

### 1. Single Source of Truth (Runtime First)

All errors are represented using Spring's native `ProblemDetail`.

```text
Error = ProblemDetail (canonical runtime model)
```

* No alternative DTOs
* No parallel error representations
* No OpenAPI-generated error classes

This guarantees:

* deterministic behavior
* zero duplication
* no divergence between runtime and clients

---

### 2. ProblemDetail + Extensions (Domain Semantics)

Standard RFC 9457 fields:

* `type`
* `title`
* `status`
* `detail`
* `instance`

Domain semantics are expressed via extensions:

* `errorCode`
* `extensions.errors[]` (structured error details)

```json
{
  "type": "urn:customer-service:problem:validation-failed",
  "title": "Validation Failed",
  "status": 400,
  "detail": "Request validation failed",
  "errorCode": "VALIDATION_FAILED",
  "extensions": {
    "errors": [
      {
        "code": "INVALID_EMAIL",
        "message": "Email format is invalid",
        "field": "email"
      }
    ]
  }
}
```

These extensions carry the **actual business error contract**.

---

### 3. OpenAPI is NOT the Error Authority

OpenAPI intentionally describes **success responses only**.

```text
OpenAPI → success contract
Error → runtime protocol (ProblemDetail)
```

Rationale:

* avoids duplicate schema definitions
* prevents spec/runtime divergence
* eliminates generated DTO conflicts

---

### 4. Client Responsibility is Split by Concern

#### Backend (Java)

* fully typed error handling
* centralized adapter layer
* `ProblemDetail → ApiProblemException`

Capabilities:

* fallback handling (non-JSON, empty body, unparsable payloads)
* structured extraction (`errorCode`, `ErrorItem`)
* domain-level exception mapping

---

#### Frontend (TypeScript / Web Clients)

Frontend clients **do not rely on OpenAPI for error typing**.

Instead, a lightweight interpretation layer is used.

##### Type Definition

```ts
export type ProblemDetail = {
  type?: string
  title?: string
  status?: number
  detail?: string
  instance?: string
  errorCode?: string
  extensions?: {
    errors?: ErrorItem[]
  }
}

export type ErrorItem = {
  code?: string
  message?: string
  field?: string
  resource?: string
  id?: string
}
```

##### Parser

```ts
export function parseProblem(error: any): ProblemDetail | null {
  const data = error?.response?.data
  if (!data || typeof data !== "object") return null

  return {
    type: data.type,
    title: data.title,
    status: data.status,
    detail: data.detail,
    instance: data.instance,
    errorCode: data.errorCode,
    extensions: data.extensions
  }
}
```

This enables:

* type-safe error handling
* zero coupling to OpenAPI
* alignment with backend runtime behavior

---

## Architectural Model

```text
Backend
  ↓
ProblemDetail (runtime truth)

OpenAPI
  ↓
Success client (generated)

Client Adapter Layer
  ↓
Error interpretation
```

Each layer has a single responsibility and does not redefine the others.

---

## Key Design Decisions

### No Error Schema in OpenAPI

Rejected because it:

* duplicates the runtime model
* introduces drift
* creates conflicting generated classes
* breaks canonical error ownership

---

### No Generated Error DTOs

Generated error models are intentionally avoided.

Instead:

* runtime payload is parsed directly
* adapters interpret it per language

---

### Protocol over Schema

Errors are treated as **protocol-level constructs**, not schema entities.

```text
Error = protocol (ProblemDetail)
Success = schema (OpenAPI)
```

---

## Trade-offs

### Advantages

* single canonical error model
* no duplication or drift
* clear separation of concerns
* strong backend guarantees
* flexible multi-language support

---

### Costs

* OpenAPI does not describe errors
* frontend requires a small parsing layer
* consumers must understand ProblemDetail semantics

---

## When This Approach Fits Best

* microservice ecosystems with shared error semantics
* platform-oriented architectures
* systems prioritizing correctness over convenience
* Spring-based services using ProblemDetail

---

## Summary

This strategy adopts a **runtime-first, protocol-driven error model** while keeping OpenAPI focused on success contracts.

It avoids duplication, preserves a single source of truth, and enables consistent behavior across services and clients.

> Errors are defined once, transported as-is, and interpreted where needed.

The result is a system that is **deterministic, explicit, and evolvable**.

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

This document defines the architectural approach for error handling across services using **RFC 9457 Problem Details** as the single canonical error model.

The design intentionally separates:

* Runtime truth (backend error model)
* Specification (OpenAPI)
* Client interpretation (language-specific adapters)

This avoids duplication, drift, and ambiguity while preserving flexibility across heterogeneous clients.

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

This ensures:

* Determinism
* Zero duplication
* No drift between spec and runtime

---

### 2. ProblemDetail + Extensions (Domain Semantics)

Standard RFC 9457 fields:

* `type`
* `title`
* `status`
* `detail`
* `instance`

Custom domain semantics are expressed via:

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

These extensions represent the **actual business error contract**.

---

### 3. OpenAPI is NOT the Error Authority

OpenAPI is intentionally limited to **success responses only**.

```text
OpenAPI → success contract only
Error → runtime protocol (ProblemDetail)
```

Rationale:

* Avoid duplicate schema definitions
* Prevent divergence between spec and runtime
* Eliminate generated DTO conflicts (e.g. multiple ProblemDetail classes)

---

### 4. Client Responsibility is Split by Concern

#### Backend (Java)

* Fully typed error handling
* Centralized adapter layer
* `ProblemDetail → ApiProblemException`

Capabilities:

* Fallback handling (non-JSON, empty body, unparsable payloads)
* Structured extraction (`errorCode`, `ErrorItem`)
* Domain-level exception mapping

---

#### Frontend (TypeScript / Web Clients)

Frontend clients do **not rely on OpenAPI for error typing**.

Instead, a lightweight interpretation layer is provided.

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

* Type-safe error handling
* Zero coupling to OpenAPI
* Alignment with backend runtime behavior

---

## Architectural Model

```text
Backend
  ↓
ProblemDetail (truth)

OpenAPI
  ↓
Success client (generated)

Custom TS Layer
  ↓
Error understanding
```

---

## Key Design Decisions

### No Error Schema in OpenAPI

Rejected because:

* Causes duplication
* Leads to drift
* Produces conflicting generated classes
* Breaks canonical model

---

### No Generated Error DTOs

All generated error models are avoided.

Instead:

* Runtime model is parsed directly
* Language-specific adapters interpret it

---

### Protocol over Schema

Errors are treated as **protocol-level constructs**, not schema-defined entities.

```text
Error = protocol (ProblemDetail)
Success = schema (OpenAPI)
```

---

## Trade-offs

### Advantages

* Single canonical error model
* No duplication or drift
* Clean separation of concerns
* Strong backend guarantees
* Flexible multi-language support

---

### Costs

* OpenAPI does not fully describe errors
* Frontend requires minimal parsing layer
* Consumers must be aware of ProblemDetail semantics

---

## When This Approach Fits Best

* Microservice ecosystems with strong backend contracts
* Platform-oriented architectures
* Teams prioritizing correctness over convenience
* Systems using Spring ProblemDetail natively

---

## Summary

This strategy embraces a **runtime-first, protocol-driven error model** while keeping OpenAPI minimal and focused.

It deliberately avoids duplication and treats error handling as a cross-cutting concern handled via lightweight client adapters rather than schema expansion.

The result is a clean, scalable, and deterministic system that remains flexible across languages and client types.

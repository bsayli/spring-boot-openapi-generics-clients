# OpenAPI Generics Platform — Architecture Decisions (0.8.x)

## Table of Contents

1. [Vision](#vision)
2. [High-Level Platform Structure](#highlevel-platform-structure)
3. [Core Architectural Principle](#core-architectural-principle)
4. [Supported Contract Shapes (Explicit Scope)](#supported-contract-shapes-explicit-scope)

5. [Client Platform Design](#client-platform-design)
    - [openapi-generics-client-parent](#openapigenericsclientparent)
    - [openapi-generics-java-engine](#openapigenericsjavaengine)
    - [openapi-generics-client-starter](#openapigenericsclientstarter)

6. [Server Platform Design](#server-platform-design)
    - [openapi-generics-server-starter](#openapigenericsserverstarter)

7. [Configuration Philosophy](#configuration-philosophy)
8. [Generator Orchestration Strategy](#generator-orchestration-strategy)
9. [Deterministic Wrapper Semantics](#deterministic-wrapper-semantics)
10. [Developer Experience Goal](#developer-experience-goal)
11. [Adoption Strategy Principle](#adoption-strategy-principle)
12. [Long-Term Evolution Axis](#longterm-evolution-axis)
13. [Final Architectural Identity](#final-architectural-identity)

## Vision

Provide a deterministic, contract‑aware OpenAPI generation and client consumption platform
for Spring Boot microservice ecosystems.

Goal:

* eliminate copy‑paste DTO envelopes
* guarantee schema stability
* hide template complexity from end users
* enable thin generated wrappers
* keep generator orchestration external and transparent

This platform is NOT a generic Java generics resolver.
It is an **OpenAPI contract determinism layer**.

---

## High‑Level Platform Structure

```
spring-boot-openapi-generics-clients
│
├── openapi-generics-client-parent
├── openapi-generics-java-engine
├── openapi-generics-client-starter
├── openapi-generics-server-starter
├── api-contract
├── samples
│   ├── producer-sample
│   └── client-sample
└── pom.xml (aggregator)
```

---

## Core Architectural Principle

The platform introduces **contract‑aware response envelope semantics** across:

* Spring MVC controllers
* Published OpenAPI specification
* OpenAPI Generator templates
* Generated client wrappers

This ensures that:

* envelope duplication is eliminated
* schema composition remains deterministic
* generated clients stay minimal and stable

---

## Supported Contract Shapes (Explicit Scope)

The platform intentionally supports only a minimal deterministic set:

* `ServiceResponse<T>`
* `ServiceResponse<Page<T>>`

Out of scope (delegated to default Springdoc / OpenAPI behaviour):

* `ServiceResponse<List<T>>`
* `ServiceResponse<Map<K,V>>`
* nested arbitrary generics
* polymorphic deep containers

This boundary is a **deliberate architecture contract**.

Expanding it increases:

* generator complexity
* template fragility
* support surface
* cognitive load for adopters

---

## Client Platform Design

### openapi‑generics‑client‑parent

Purpose:

* lifecycle orchestration defaults
* pluginManagement
* version alignment
* generator hygiene conventions
* ignore patterns standardisation

End users SHOULD inherit from this parent.

---

### openapi‑generics‑java‑engine

Purpose:

* semantic template overlays
* api_wrapper.mustache
* generator import mapping strategy
* language‑specific patch signatures
* default generator tuning

This module is **pure capability pack**, not orchestration.

---

### openapi‑generics‑client‑starter

Purpose:

* expose canonical contract classes transitively
* simplify dependency onboarding
* hide DTO envelope duplication

Contains:

* api‑contract dependency
* future runtime helpers (optional)

Does NOT:

* run generator
* patch templates
* control build lifecycle

---

## Server Platform Design

### openapi‑generics‑server‑starter

Purpose:

Provide automatic OpenAPI contract publication enrichment.

Responsibilities:

* register canonical envelope schemas
* detect supported response shapes
* compose deterministic wrapper schemas
* attach vendor extensions for client templates

Contains Spring Boot auto‑configuration for:

* ResponseTypeIntrospector
* AutoWrapperSchemaCustomizer
* SwaggerResponseCustomizer
* ApiResponseSchemaFactory
* OpenApiSchemas

Design rule:

Server starter affects **specification layer only**, not runtime JSON payloads.

---

## Configuration Philosophy

Platform must be **zero‑configuration by default**.

Optional advanced property:

* extra annotation injection for generated client wrappers

This is:

* DX convenience
* not part of core contract

HTTP policy configuration (media types, error exposure etc.)
MUST remain application‑level responsibility.

---

## Generator Orchestration Strategy

Chosen model:

### SAFE MODEL — Pre‑Generator Lifecycle Preparation

Platform prepares environment but does not control OpenAPI Generator plugin.

User still declares:

* openapi‑generator‑maven‑plugin

Platform ensures:

* effective template directory
* semantic overlay consistency
* deterministic schema contract

This avoids:

* tight coupling to generator evolution
* lifecycle ownership conflicts
* plugin maintenance burden

---

## Deterministic Wrapper Semantics

Wrapper schemas are composed using OpenAPI `allOf`:

* base canonical envelope reference
* overlay data property reference

Vendor extensions used:

* x‑api‑wrapper
* x‑api‑wrapper‑datatype
* x‑data‑container
* x‑data‑item

These are **internal signalling primitives** between:

server publication → generator templates → generated client code

---

## Developer Experience Goal

### Server side

User adds only:

```
openapi‑generics‑server‑starter
```

Then simply returns:

```
ServiceResponse<CustomerDto>
```

Canonical deterministic OpenAPI appears automatically.

No customizers.
No schema factories.
No vendor extension awareness.

---

### Client side

User inherits parent and runs generator normally.

No template patch awareness required.
No envelope DTO duplication.

Generated wrappers stay thin.

---

## Adoption Strategy Principle

Architecture must be:

* invisible
* deterministic
* boring to use
* powerful internally

Users should not feel they are adopting a framework.
They should feel **their OpenAPI just became cleaner**.

---

## Long‑Term Evolution Axis

Potential future capabilities:

* reactive response support
* spec stability validator
* contract lint rules
* Gradle support layer
* generator drift detection
* multi‑language engines

These must remain **opt‑in layers**, never core complexity.

---

## Final Architectural Identity

This platform is:

> Contract‑aware OpenAPI publication and consumption infrastructure

It is NOT:

* a template hack collection
* a generator fork
* a Java generics experimentation lab

It is an **architecture product**.

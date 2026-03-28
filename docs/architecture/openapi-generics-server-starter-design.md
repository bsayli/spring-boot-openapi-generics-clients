# openapi-generics-server-starter — Architectural Design Rules

This document defines the **foundational design rules and evolution constraints** for the
`openapi-generics-server-starter` module.

The intent is to ensure long‑term ecosystem stability, predictable dependency behavior,
and clear mental models for adopters.

These rules are binding for the 0.8.x design phase and beyond unless explicitly revised.

---

## 1. Architectural Role

`openapi-generics-server-starter` is a **framework integration module**.

It is NOT:

* a domain contract
* a transport library
* a generator engine
* a runtime envelope implementation

It IS:

> A Spring Boot auto‑configuration layer that enriches the published OpenAPI document
> so that client generation can remain deterministic and contract‑aligned.

Primary responsibilities:

* register canonical response envelope schemas
* detect supported generic response shapes
* publish vendor extensions required by client templates
* integrate with Springdoc lifecycle

Non‑responsibilities:

* HTTP runtime behavior
* controller advice
* serialization customization
* validation enforcement
* pagination logic
* error handling

---

## 2. Contract Boundary

The module operates strictly **at specification level**.

It must never:

* alter runtime payload structure
* wrap controller responses
* introduce transport‑layer coupling

All runtime semantics belong to:

* `api-contract`
* application services
* framework infrastructure

This guarantees:

* server/client contract stability
* generator determinism
* minimal cognitive load for adopters

---

## 3. Supported Response Shapes (Explicit Scope)

The starter must only provide deterministic schema enrichment for:

* `ServiceResponse<T>`
* `ServiceResponse<Page<T>>`

All other shapes are intentionally **out of scope**, including:

* nested generics
* collections as root payload containers
* maps or polymorphic wrappers

This constrained scope ensures:

* stable OpenAPI component naming
* generator‑safe evolution
* predictable client typing

---

## 4. Versioning Strategy (Framework Alignment)

The starter follows the rule:

> **Starter major version = Spring Boot major compatibility line**

Example roadmap model:

* `openapi-generics-server-starter 1.x` → Spring Boot 3.x
* `openapi-generics-server-starter 2.x` → Spring Boot 4.x

Rationale:

* binary compatibility clarity
* simplified user mental model
* reduced dependency conflict risk
* alignment with common Spring ecosystem practices

Minor versions evolve feature scope.
Patch versions fix defects.

---

## 5. Dependency Governance Rules

### 5.1 Framework Dependencies

The starter MUST NOT pin versions for:

* Spring Boot starters
* Springdoc OpenAPI
* Swagger Core
* Jackson runtime modules

Instead, it must rely on:

* user BOM
* Spring Boot dependency management
* platform parent POM

This enables:

* corporate stack alignment
* security upgrade flexibility
* reduced dependency hell

### 5.2 Minimum Compatibility Declaration

Documentation must state:

* minimum supported Spring Boot line
* minimum supported Springdoc line

Example:

> Compatible with Spring Boot 3.4+ and Springdoc 2.8+

The codebase must avoid relying on:

* undocumented internal APIs
* unstable framework extension points

---

## 6. Auto‑Configuration Philosophy

Auto‑configuration must be:

* conditional
* narrowly scoped
* fail‑safe

Guidelines:

* use `@ConditionalOnClass` for Springdoc presence
* use `@ConditionalOnWebApplication`
* avoid aggressive bean overriding
* prefer additive customization

Startup failures due to missing optional infrastructure
must be avoided.

---

## 7. OpenAPI Schema Publication Guarantees

The starter guarantees:

* canonical base envelope schemas always registered
* deterministic wrapper schema naming
* stable vendor extension signaling

The starter does NOT guarantee:

* generator output structure
* client code style
* endpoint documentation completeness

These belong to generator templates and service design.

---

## 8. Package Structure Stability

Public extension points must be clearly separated from:

* internal introspection logic
* schema factory utilities
* vendor extension constants

Recommended logical grouping:

* `autoconfigure`
* `customizer`
* `introspection`
* `schema`
* `internal`

Only explicitly documented packages are considered
**public API surface**.

---

## 9. Binary Compatibility Expectations

Within the same major line:

* public configuration classes must remain binary compatible
* vendor extension keys must remain stable
* schema naming rules must not change

Breaking changes require:

* major version increment
* migration note publication

---

## 10. Evolution Principles

Future enhancements must follow:

* additive schema capabilities
* optional feature toggles
* backward‑compatible auto‑configuration

Avoid:

* expanding generic shape guarantees prematurely
* embedding generator assumptions into server layer
* coupling starter to specific serialization engines

---

## 11. Relationship to api-contract

`api-contract` is:

* framework‑agnostic
* long‑lived
* domain semantic authority

`openapi-generics-server-starter` is:

* framework adapter
* evolution‑driven
* replaceable

This separation is intentional and must be preserved.

---

## 12. Design North Star

The module exists to enable a single architectural outcome:

> Deterministic, reusable response envelope semantics
> across server publication and generated client ecosystems.

All implementation decisions should be evaluated
against this outcome.

If a feature does not strengthen determinism,
contract clarity, or ecosystem stability,
it should not be included.

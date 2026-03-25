---
layout: default
title: Client-Side Build Setup
parent: Client-Side Adoption
nav_order: 1
---

# Client‑Side Build Setup — Deterministic Model Rendering Governance

This guide explains how to configure a **client module build** that produces
**contract‑bound, generics‑aware OpenAPI clients** while maintaining
**deterministic model rendering behaviour** and avoiding duplication of
shared response contracts.

Unlike generic OpenAPI client tutorials, this document focuses on the
**architectural control surface** of client generation.

The build does not attempt to customize the entire generator template set.
Instead, it introduces a **bounded governance interception point** at the
model rendering stage.

This approach ensures that:

* canonical response envelopes remain a **single source of truth**
* wrapper typing is enforced **semantically rather than heuristically**
* generator upgrades remain **fail‑fast and observable**
* client regeneration stays **stable across contract evolution**

---

## Contract usage

Client generation assumes the following architectural conventions:

* Successful responses use the shared contract provided by
  **`io.github.bsayli:api-contract`**, specifically **`ServiceResponse<T>`**.
* Nested generics are treated as contract‑aware **only for**
  **`ServiceResponse<Page<T>>`**.
* All other generic compositions follow OpenAPI Generator default behaviour.

The client build therefore focuses on **preserving contract semantics**
rather than exhaustively modelling every theoretical generic combination.

---

## Architectural build pipeline

The generation pipeline is intentionally minimal and governance‑oriented.

At a high level it:

1. extracts the upstream **model rendering template** as a deterministic baseline
2. applies a **fail‑fast semantic patch** to intercept wrapper rendering
3. overlays **minimal partial templates** for contract binding
4. generates client sources using the effective rendering configuration
5. compiles generated sources as part of the standard Maven lifecycle

This bounded control model avoids full template forks while still ensuring
that **contract semantics cannot silently drift**.

---

## 📑 Table of Contents

* [1) Maven Properties](#1-maven-properties)
* [2) Core Dependencies](#2-core-dependencies)
* [3) Maven Plugins — Governance Pipeline](#3-maven-plugins--governance-pipeline)
* [4) Why This Pipeline Exists](#4-why-this-pipeline-exists)
* [5) Mustache Contract Binding Responsibilities](#5-mustache-contract-binding-responsibilities)
* [6) Verification](#6-verification)

---

## 1) Maven Properties

Version pinning is required for **reproducible model rendering** and predictable
contract‑bound client generation.

```xml
<properties>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
  <java.version>21</java.version>

  <spring-boot.version>3.5.12</spring-boot.version>
  <openapi.generator.version>7.21.0</openapi.generator.version>

  <jakarta.validation.version>3.1.1</jakarta.validation.version>
  <jakarta.annotation-api.version>3.0.0</jakarta.annotation-api.version>
  <httpclient5.version>5.5.2</httpclient5.version>

  <maven.compiler.plugin.version>3.15.0</maven.compiler.plugin.version>
  <maven.resources.plugin.version>3.5.0</maven.resources.plugin.version>
  <maven.dependency.plugin.version>3.9.0</maven.dependency.plugin.version>
  <spotless-maven-plugin.version>3.3.0</spotless-maven-plugin.version>
  <build.helper.plugin.version>3.6.1</build.helper.plugin.version>

  <openapi.templates.effective>${project.build.directory}/effective-templates</openapi.templates.effective>

  <api-contract.version>0.7.7</api-contract.version>
</properties>
```

---

## 2) Core Dependencies

The client module depends directly on the **shared canonical contract**.

```xml
<dependencies>

  <dependency>
    <groupId>io.github.bsayli</groupId>
    <artifactId>api-contract</artifactId>
    <version>${api-contract.version}</version>
  </dependency>

  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>${spring-boot.version}</version>
    <scope>provided</scope>
  </dependency>

  <dependency>
    <groupId>jakarta.validation</groupId>
    <artifactId>jakarta.validation-api</artifactId>
    <version>${jakarta.validation.version}</version>
    <scope>provided</scope>
  </dependency>

  <dependency>
    <groupId>org.apache.httpcomponents.client5</groupId>
    <artifactId>httpclient5</artifactId>
    <version>${httpclient5.version}</version>
  </dependency>

</dependencies>
```

---

## 3) Maven Plugins — Governance Pipeline

This build intentionally governs **only the model rendering surface**.

### Stage 1 — Extract upstream model template

The upstream `model.mustache` template is unpacked to provide a
**stable interception baseline**.

### Stage 2 — Apply deterministic semantic patch

A controlled regex patch introduces a wrapper‑aware rendering branch.

The build **fails fast** if the upstream template structure changes.

### Stage 3 — Overlay local contract‑binding partials

Minimal Mustache partials implement wrapper inheritance binding
against the shared contract.

### Stage 4 — Generate client sources

OpenAPI Generator runs using the **effective template directory**.

### Stage 5 — Compile generated sources

Generated sources are attached to the Maven lifecycle using
`build-helper-maven-plugin`.

---

## 4) Why This Pipeline Exists

This governance model is designed to achieve:

* **Template minimalism** — no full generator fork
* **Upgrade safety** — fail‑fast detection of upstream changes
* **Semantic stability** — wrapper logic remains contract‑driven
* **Operational predictability** — deterministic regeneration

The pipeline therefore treats client generation as an
**architecture enforcement activity**, not a convenience build step.

---

## 5) Mustache Contract Binding Responsibilities

Mustache overlays are responsible only for **binding generated wrappers
onto canonical contract types**.

They must:

* extend `ServiceResponse<T>` from `api-contract`
* extend `ServiceResponse<Page<T>>` for pagination wrappers
* avoid redefining envelope fields or paging metadata

Vendor extensions emitted by the server contract guide this binding.

---

## 6) Verification

After configuring the build, verify determinism by running:

```bash
mvn -q clean install
```

Confirm that:

* generated wrapper models extend canonical contract classes
* no duplicate envelope or paging DTOs are generated
* build fails if upstream generator template structure changes

With this setup, client generation remains **contract‑driven, deterministic,
and regeneration‑safe** across evolving API specifications.

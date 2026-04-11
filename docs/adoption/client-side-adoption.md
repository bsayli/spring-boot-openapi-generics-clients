---
layout: default
title: Client-Side Adoption
parent: Adoption Guides
nav_order: 2
---

# Client-Side Adoption — Contract-First Client Integration

> Generate a Java client that **preserves contract semantics exactly as published** — no duplication, no reinterpretation, no drift.

This is **not a typical OpenAPI client setup**.

It defines a **controlled build-time system** where:

* OpenAPI is treated as input (not authority)
* the contract is preserved (not regenerated)
* and the output is deterministic across environments

This guide defines the **correct client-side integration model** for the platform.

It focuses on three things only:

* consuming OpenAPI as input
* executing a controlled build pipeline
* using the generated client safely

Everything else is intentionally handled by the platform.

---

## 📑 Table of Contents

* [⚡ 60-second quick start](#-60-second-quick-start)
* [🎯 What the client actually does](#-what-the-client-actually-does)
* [📥 Input: OpenAPI (not your contract)](#-input-openapi-not-your-contract)
* [📦 Minimal setup](#-minimal-setup)
* [🏗 Build pipeline (what really happens)](#-build-pipeline-what-really-happens)
* [🧠 Output: what gets generated](#-output-what-gets-generated)
* [🚀 Usage: how the client enters your system](#-usage-how-the-client-enters-your-system)
* [🧱 Adapter boundary (strongly recommended)](#-adapter-boundary-strongly-recommended)
* [🔍 Quick verification](#-quick-verification)
* [⚠️ Error handling](#-error-handling)
* [🧠 Mental model](#-mental-model)
* [🧾 Summary](#-summary)

---

## ⚡ 60-second quick start

You want:

* a type-safe client
* zero duplicated models
* preserved `ServiceResponse<T>` semantics

Do this:

### 1) Inherit the parent

```xml
<parent>
  <groupId>io.github.blueprintplatform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
</parent>
```

### 2) Provide OpenAPI

```text
/v3/api-docs.yaml
```

### 3) Build

```bash
mvn clean install
```

That’s it.

---

## 🎯 What the client actually does

The client has **one responsibility**:

> Convert an OpenAPI document into a **contract-aligned Java client** without redefining anything.

It does **not**:

* design models
* interpret business semantics
* introduce abstractions

It only executes a deterministic transformation:

```text
OpenAPI → deterministic Java client
```

---

## 📥 Input: OpenAPI (not your contract)

Client generation always starts from an **existing OpenAPI document**.

```bash
curl http://localhost:8084/.../v3/api-docs.yaml -o api.yaml
```

Critical distinction:

> OpenAPI is **input metadata**, not the contract itself.

Implication:

* structure comes from OpenAPI
* semantics come from shared contract types

---

## 📦 Minimal setup

You provide exactly two inputs. Everything else is handled by the platform.

---

### 1. Build-time orchestration (MANDATORY)

```xml
<parent>
  <groupId>io.github.blueprintplatform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>0.8.3</version>
</parent>
```

This is the **entry point of the system**.

It provides everything required for generation:

* generator binding (`java-generics-contract`)
* template pipeline (extract → patch → overlay)
* contract-aware import mappings
* deterministic execution model
* generated sources registration

You do NOT add or configure internal dependencies. The parent already wires the system.

This includes the contract dependency.

If it is needed, it is already managed by the parent/BOM — not by you.

---

### 2. OpenAPI Generator plugin (USER INPUT ONLY)

Provide:

* input OpenAPI spec
* desired HTTP client (`library`)
* package structure

Expand the example below if you need a full configuration.

<details>
<summary>Example configuration</summary>

```xml
<plugin>
  <groupId>org.openapitools</groupId>
  <artifactId>openapi-generator-maven-plugin</artifactId>

  <executions>
    <execution>
      <id>generate-client</id>
      <phase>generate-sources</phase>
      <goals>
        <goal>generate</goal>
      </goals>

      <configuration>

        <inputSpec>${project.basedir}/src/main/resources/your-api-docs.yaml</inputSpec>

        <library>your-library-choice</library>

        <apiPackage>your.api.package</apiPackage>
        <modelPackage>your.model.package</modelPackage>
        <invokerPackage>your.invoker.package</invokerPackage>

        <configOptions>
          <useSpringBoot3>true</useSpringBoot3>
          <serializationLibrary>your-choice</serializationLibrary>
          <openApiNullable>false</openApiNullable>
        </configOptions>

      </configuration>

    </execution>
  </executions>
</plugin>
```

</details>

---

What you control here:

* input OpenAPI spec
* HTTP client (`library`)
* package structure
* serialization strategy

What you do NOT control:

* generator internals
* template system
* contract mappings

---

## 🏗 Build pipeline (what really happens)

This system is **not configuration-driven**.
It is a **controlled execution pipeline**.

Full pipeline:

```text
OpenAPI spec (input)
   ↓
Parent POM (orchestration)
   ↓
Template extraction (upstream)
   ↓
Template patch (api_wrapper injection)
   ↓
Template overlay (custom templates)
   ↓
Custom generator (java-generics-contract)
   ↓
Generated sources (contract-aligned)
```

> Each step is fixed and ordered. No user-defined hooks exist in this pipeline.

---

### What the platform enforces

The build guarantees:

* contract models are NOT generated
* wrapper classes are generated deterministically
* generics are preserved (`ServiceResponse<T>`, `Page<T>`)
* OpenAPI is interpreted — not materialized as independent models

---

### Generated sources integration

Generated code is automatically:

* written to `target/generated-sources/openapi`
* added to the Maven compilation lifecycle

No manual configuration is required.

---

## 🧠 Configuration boundaries

The system is intentionally split into **two control zones**.

---

### User-controlled (safe)

```xml
<inputSpec>...</inputSpec>
<library>...</library>
<apiPackage>...</apiPackage>
<modelPackage>...</modelPackage>
<invokerPackage>...</invokerPackage>
<openapi-generator.version>...</openapi-generator.version>
```

These control:

* input specification
* HTTP transport layer
* package structure
* generator version

---

### Platform-controlled (DO NOT override)

The parent already provides:

* generator name (`java-generics-contract`)
* template directory
* import mappings
* template patching pipeline
* model suppression rules

These ensure:

* contract preservation (`ServiceResponse<T>`, `Page<T>`)
* deterministic wrapper generation
* zero duplication of platform models

---

### Critical rule

> If you override platform-controlled settings, you leave the contract-safe execution path.

---

## 🚫 What users should NOT do

Do NOT:

* add internal platform dependencies manually
* override templates
* change generator name
* modify import mappings
* inject custom model logic

Reason:

> The system is intentionally controlled to guarantee determinism and contract alignment.


## 🧠 Output: what gets generated

From OpenAPI schema:

```text
ServiceResponseCustomerDto
```

Generated code:

```java
class ServiceResponseCustomerDto extends ServiceResponse<CustomerDto>
```

Properties:

* thin wrappers only
* no duplicated envelopes
* direct reuse of contract types

> The wrapper exists only to rebind generics — it does not introduce new structure.

Implication:

* no additional fields are created
* no behavior is added
* no contract semantics are modified

The generated layer is purely structural — it restores type information that OpenAPI cannot represent directly.

---

## 🚀 Usage: how the client enters your system

Generated sources:

```text
target/generated-sources/openapi
```

Usage:

```java
ServiceResponse<CustomerDto>
```

At this point:

No additional mapping layer is required for correctness.

* type system is preserved
* contract is intact
* client is aligned with producer

---

## 🧱 Adapter boundary (strongly recommended)

Do not expose generated APIs directly.

Define a boundary:

```java
public interface CustomerClient {
  ServiceResponse<CustomerDto> getCustomer(Long id);
}
```

Purpose:

* isolate generation details
* protect domain logic
* enable safe evolution

---

## 🔍 Quick verification

After generation:

* wrappers extend contract types
* no duplicate envelope classes exist
* generics are preserved

If true:

```text
Client is correctly aligned
```

---

## ⚠️ Error handling

Errors are not generated models.

They follow a runtime protocol:

```text
ProblemDetail (RFC 9457)
```

Example:

```java
try {
  client.call();
} catch (ApiProblemException ex) {
  var pd = ex.getProblem();
}
```

---

## 🧠 Mental model

Think of the client as:

> A deterministic build-time compiler
> that maps OpenAPI → contract-aligned Java code

Not:

* a DTO generator
* a modeling tool

---

## 🧾 Summary

```text
Input   = OpenAPI
Process = controlled build pipeline
Output  = thin wrappers over contract
```

The system works because:

* contract is never regenerated
* generation is deterministic
* boundaries are strictly enforced

---

🛡 MIT License

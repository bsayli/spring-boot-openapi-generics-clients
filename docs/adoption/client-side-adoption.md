---
layout: default
title: Client-Side Adoption
parent: Adoption Guides
nav_order: 2
---

# Client-Side Adoption — Contract-First Client Integration

> A **minimal, deterministic guide** for generating and using a client that stays fully aligned with the server-side contract.

---

## 📑 Table of Contents

* [⚡ 60-second quick start](#-60-second-quick-start)
* [🎯 What the client is responsible for](#-what-the-client-is-responsible-for)
* [🧩 The only rule that matters](#-the-only-rule-that-matters)
* [📦 Minimal setup](#-minimal-setup)
* [🚀 Generate the client](#-generate-the-client)
* [🧠 What gets generated](#-what-gets-generated)
* [⚠️ Rules (do NOT break these)](#-rules-do-not-break-these)
* [🔍 Quick verification](#-quick-verification)
* [🧱 Adapter boundary (recommended)](#-adapter-boundary-recommended)
* [⚠️ Error handling](#-error-handling)
* [🧠 Mental model](#-mental-model)
* [🧾 Summary](#-summary)

---

## ⚡ 60-second quick start

You want:

* type-safe client
* no duplicated models
* preserved `ServiceResponse<T>`

Do this:

### 1) Use codegen parent

```xml
<parent>
  <groupId>io.github.bsayli</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
</parent>
```

### 2) Point to OpenAPI

```text
/v3/api-docs.yaml
```

### 3) Build

```bash
mvn clean install
```

Done.

---

## 🎯 What the client is responsible for

The client has one job:

> Interpret the OpenAPI projection WITHOUT redefining the contract.

It does NOT:

* redefine response models
* generate its own envelopes
* modify semantics

It only:

```text
OpenAPI → contract-aligned client
```

---

## 🧩 The only rule that matters

The envelope is shared and fixed:

```text
ServiceResponse<T>
ServiceResponse<Page<T>>
```

The client MUST reuse it — not regenerate it.

---

## 📦 Minimal setup

You need:

* generated client (via parent)
* shared contract dependency

```xml
<dependency>
  <groupId>io.github.bsayli</groupId>
  <artifactId>api-contract</artifactId>
</dependency>
```

---

## 🚀 Generate the client

### Step 1 — Get OpenAPI

```bash
curl http://localhost:8084/.../v3/api-docs.yaml -o api.yaml
```

### Step 2 — Build

```bash
mvn clean install
```

### Step 3 — Use generated sources

```text
target/generated-sources/openapi
```

---

## 🧠 What gets generated

From OpenAPI schema:

```text
ServiceResponseCustomerDto
```

Client generates:

```java
class ServiceResponseCustomerDto extends ServiceResponse<CustomerDto>
```

Important:

* no envelope duplication
* only thin wrappers
* contract types reused

---

## ⚠️ Rules (do NOT break these)

### 1. Do NOT generate contract models

Never allow generation of:

```text
ServiceResponse
Page
Meta
Sort
```

---

### 2. Do NOT redefine the envelope

❌ Wrong:

```text
ApiResponse
BaseResponse
CustomWrapper
```

---

### 3. Only thin wrappers are allowed

✔ Correct:

```java
class ServiceResponseX extends ServiceResponse<X>
```

No fields. No logic.

---

### 4. Do NOT modify generated code

Generated code is:

```text
replaceable
```

Always treat it as build output.

---

## 🔍 Quick verification

After generation, check:

✔ Wrapper extends contract
✔ No duplicated envelope classes
✔ Pagination preserved

If true:

```text
Client is correctly aligned with server
```

---

## 🧱 Adapter boundary (recommended)

Never expose generated APIs directly.

Define an adapter:

```java
public interface CustomerClient {
  ServiceResponse<CustomerDto> getCustomer(Long id);
}
```

Why:

* isolates generator changes
* keeps domain clean

---

## ⚠️ Error handling

Errors are NOT generated models.

They are:

```text
ProblemDetail (RFC 9457)
```

Handle via:

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

> A compiler from OpenAPI → contract-bound code

NOT:

* a model generator
* a DTO factory

---

## 🧾 Summary

If you remember only this:

```text
Do not generate the contract
Only generate thin wrappers
Use the shared types
```

Everything else is handled by the platform.

---

🛡 MIT License

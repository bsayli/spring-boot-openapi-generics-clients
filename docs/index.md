---
layout: default
title: Home
nav_order: 1
---

# OpenAPI Generics — Keep Your API Contract Intact End-to-End

> Define your API once in Java.  
> Preserve it across OpenAPI and generated clients — without duplication or drift.

---

## Table of Contents

- [Why this exists](#why-this-exists)
- [What you actually do](#what-you-actually-do)
- [Quick Start](#-quick-start)
    - [1. Server (producer)](#1-server-producer)
    - [2. Client (consumer)](#2-client-consumer)
- [Result](#result)
- [Compatibility Matrix](#-compatibility-matrix)
- [Proof — Generated Client (Before vs After)](#proof--generated-client-before-vs-after)
    - [Before (default OpenAPI behavior)](#before-default-openapi-behavior)
    - [After (contract-aligned generation)](#after-contract-aligned-generation)
- [What changed](#what-changed)
- [What is actually generated](#what-is-actually-generated)
- [How you actually use it](#how-you-actually-use-it)
- [What this gives you](#what-this-gives-you)
- [Why this matters](#why-this-matters)
- [Mental model](#mental-model)
- [Next steps](#next-steps)
- [References & External Links](#-references--external-links)
- [Final note](#final-note)

---

## Why this exists

In most OpenAPI-based workflows:

* generics are flattened or lost
* response envelopes are regenerated per endpoint
* clients gradually drift from server-side contracts

Over time, this creates a gap between what your API **defines** and what your clients **consume**.

The result is not an immediate failure — but a slow erosion of contract integrity.

This platform removes that entire class of problems.

> Your Java contract remains the single source of truth — across all layers.

---

## What you actually do

You don’t configure OpenAPI.  
You don’t maintain templates.  
You don’t fight generator behavior.

You only do two things:

1. return your contract from controllers
2. generate clients from OpenAPI

That’s it.

The platform handles projection, generation, and contract alignment automatically.

---

## ⚡ Quick Start

### 1. Server (producer)

Add the dependency:

```xml
<dependency>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-server-starter</artifactId>
  <version>0.8.2</version>
</dependency>
```

Return your contract:

```java
ServiceResponse<CustomerDto>
```

---

### 2. Client (consumer)

Inherit the parent:

```xml
<parent>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>0.8.2</version>
</parent>
```

Generate the client:

```bash
mvn clean install
```

---

## Result

```java
ServiceResponse<CustomerDto>
```

The exact same contract type flows from server to client.

* no duplicated models
* generics preserved end-to-end
* contract types reused (not regenerated)

---

## 🔧 Compatibility Matrix

### Runtime (Server)

| Component           | Supported Versions        |
|--------------------|--------------------------|
| Java               | 17+                      |
| Spring Boot        | 3.4.x, 3.5.x             |
| springdoc-openapi  | 2.8.x (WebMvc starter)   |

### Build-time (Client Generation)

| Component           | Supported Versions |
|--------------------|-------------------|
| OpenAPI Generator  | 7.x               |

---

## Proof — Generated Client (Before vs After)

### Before (default OpenAPI behavior)

<p align="center">
  <img src="https://raw.githubusercontent.com/blueprint-platform/openapi-generics/main/docs/images/proof/generated-client-wrapper-before.png" width="700"/>
</p>


* duplicated envelope per endpoint
* generics flattened or lost
* unstable and verbose model graph

---

### After (contract-aligned generation)

<p align="center">
  <img src="https://raw.githubusercontent.com/blueprint-platform/openapi-generics/main/docs/images/proof/generated-client-wrapper-after.png" width="700"/>
</p>

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {}
```

* no envelope duplication
* generics preserved end-to-end
* contract types reused directly

---


## What changed

Instead of generating new models from OpenAPI:

* the contract is preserved
* wrappers are generated as thin type bindings
* the client reuses existing domain semantics

Result:

```text
Java Contract (SSOT)
        ↓
OpenAPI (projection, not authority)
        ↓
Generator (deterministic reconstruction)
        ↓
Client (canonical contract types)
```

No reinterpretation.
No duplication.
No drift.

---

## What is actually generated

The client does **not** recreate your models.

Instead, it generates **thin wrapper classes** that bind OpenAPI responses back to your canonical contract.

Example:

```java
public class ServiceResponseCustomerDto extends ServiceResponse<CustomerDto> {
}
```

```java
public class ServiceResponsePageCustomerDto extends ServiceResponse<Page<CustomerDto>> {
}
```

Key properties:

* no envelope duplication
* no structural redefinition
* no generic type loss

These classes exist only to bridge OpenAPI → Java type system.

---

## How you actually use it

You never interact with generated wrappers directly.

Instead, you define an adapter layer:

```java
public interface CustomerClientAdapter {

  ServiceResponse<CustomerDto> createCustomer(CustomerCreateRequest request);

  ServiceResponse<CustomerDto> getCustomer(Integer customerId);

  ServiceResponse<Page<CustomerDto>> getCustomers();

}
```

Implementation delegates to generated API:

```java
@Service
public class CustomerClientAdapterImpl implements CustomerClientAdapter {

  private final CustomerControllerApi api;

  public CustomerClientAdapterImpl(CustomerControllerApi api) {
    this.api = api;
  }

  @Override
  public ServiceResponse<CustomerDto> getCustomer(Integer customerId) {
    return api.getCustomer(customerId);
  }

  @Override
  public ServiceResponse<Page<CustomerDto>> getCustomers() {
    return api.getCustomers(null, null, 0, 5, "customerId", "ASC");
  }
}
```

---

## What this gives you

At usage level, your application only sees:

```java
ServiceResponse<CustomerDto>
ServiceResponse<Page<CustomerDto>>
```

Not:

* generated wrapper classes
* duplicated DTO hierarchies
* OpenAPI-specific models

No translation layer. No reinterpretation. No drift.

---

## Why this matters

Traditional OpenAPI generation produces:

* duplicated response envelopes
* flattened generics
* unstable model graphs

This approach guarantees:

* a single contract shared across all layers
* stable and predictable client generation
* zero drift between server and client semantics

---

## Mental model

Think of generated classes as:

> thin type adapters — not models

They exist because OpenAPI cannot express Java generics — not because your model requires them.

They simply reconnect OpenAPI output back to your **existing contract**, without redefining it.

Your system always operates on:

```text
ServiceResponse<T>
```

Everything else is just infrastructure.

---

## Next steps

* [Server Adoption](adoption/server-side-adoption.md)
* [Client Adoption](adoption/client-side-adoption.md)

---

## 🔗 References & External Links

* 🌐 **GitHub Repository** — [openapi-generics](https://github.com/blueprint-platform/openapi-generics)
* 📘 **Medium** — [We Made OpenAPI Generator Think in Generics](https://medium.com/@baris.sayli/type-safe-generic-api-responses-with-spring-boot-3-4-openapi-generator-and-custom-templates-ccd93405fb04)

---


## Final note

If the contract stays consistent, everything stays consistent.

This system works by keeping that boundary intact.

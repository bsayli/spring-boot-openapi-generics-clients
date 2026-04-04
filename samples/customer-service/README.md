# customer-service

> **Reference implementation: exposing a Spring Boot API that produces a clean, deterministic OpenAPI for contract-aligned, generics-aware clients**

[![Java 21](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![Springdoc](https://img.shields.io/badge/Springdoc-2.8.x-brightgreen)](https://springdoc.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](../../LICENSE)

---

## 📑 Table of Contents

* [🚀 Start here (what you actually want)](#-start-here-what-you-actually-want)
* [⚠️ Rules (do NOT break these)](#-rules-do-not-break-these)

  * [1. Only constrain the envelope — payload is yours](#1-only-constrain-the-envelope--payload-is-yours)
  * [2. Do NOT replace the envelope (this is the only restriction)](#2-do-not-replace-the-envelope-this-is-the-only-restriction)
  * [3. Do NOT wrap errors](#3-do-not-wrap-errors)
  * [4. Do NOT customize OpenAPI manually](#4-do-not-customize-openapi-manually)
* [🧠 What is happening under the hood (short version)](#-what-is-happening-under-the-hood-short-version)
* [🔄 Full pipeline (important)](#-full-pipeline-important)
* [🔗 Related Modules](#-related-modules)
* [🧪 Verify quickly](#-verify-quickly)
* [🌐 OpenAPI endpoints](#-openapi-endpoints)
* [🚫 What this project is NOT](#-what-this-project-is-not)
* [🛡️ License](#-license)

---

## 🚀 Start here (what you actually want)

You have a Spring Boot service.

You want a **reliable downstream outcome**:

* OpenAPI is **clean and stable** (no schema noise)
* Client generation is **type-safe** (no envelope duplication)
* `ServiceResponse<T>` is **preserved end-to-end** (not flattened)

Do this:

### 1. Add ONE dependency

```xml
<dependency>
  <groupId>io.github.blueprintplatform</groupId>
  <artifactId>openapi-generics-server-starter</artifactId>
</dependency>
```

That’s it.

> You write your controller contract. The starter ensures a deterministic OpenAPI projection.

---

## ⚠️ Rules (do NOT break these)

### 1. Only constrain the envelope — payload is yours

You must use **only these envelope shapes**:

```text
ServiceResponse<T>
ServiceResponse<Page<T>>
```

**Important:**

* `T` is completely free
* It represents YOUR domain model
* It can be anything: DTO, record, response object, etc.

Examples (all valid):

```text
ServiceResponse<CustomerDto>
ServiceResponse<OrderDto>
ServiceResponse<AnythingYouDefine>
ServiceResponse<Page<AnythingYouDefine>>
```

👉 The platform constrains only the **outer contract (ServiceResponse / Page)**
👉 It does NOT constrain your domain

---

### 2. Do NOT replace the envelope (this is the only restriction)

What is forbidden is NOT your DTO naming.

What is forbidden is replacing the **envelope abstraction itself**.

❌ Wrong (custom envelope types):

```text
CustomerResponse
PagedCustomerResponse
ApiResponse
BaseResponse
```

These break:

* contract symmetry
* OpenAPI determinism
* client generation

---

### ✅ What is absolutely fine (and expected)

You can define ANY domain models:

```text
CustomerDto
CustomerResponse   ← perfectly fine as a DOMAIN object
OrderResult
Anything
```

And use them like:

```text
ServiceResponse<CustomerResponse>
```

✔ Correct
✔ Expected usage

---

### 🔑 Mental model (critical)

```text
YOU own:        T (your domain / DTOs)
PLATFORM owns:  envelope (ServiceResponse, Page)
```

If you don’t touch the envelope:

> You are free to design your API however you want

---

### 3. Do NOT wrap errors

Errors must be:

```text
ProblemDetail (RFC 9457)
```

Never:

```text
ServiceResponse<Error>
```

---

### 4. Do NOT customize OpenAPI manually

No annotations.
No schema hacks.
No manual overrides.

Everything is handled by the starter.

---

## 🧠 What is happening under the hood (short version)

```text
Controller (your contract)
   ↓
ServiceResponse<T>
   ↓
openapi-generics-server-starter
   ↓
Deterministic OpenAPI (+ vendor extensions)
```

Key point:

> OpenAPI is a projection of your contract — not a place to define it.

---

## 🔄 Full pipeline (important)

```text
THIS MODULE (producer)
   ↓
OpenAPI spec (projection)
   ↓
openapi-generics-java-codegen-parent (build-time orchestration)
   ↓
Generated client (contract-aligned)
```

This project exists to guarantee the **first step is correct**.

If this step is correct:

```text
Server → OpenAPI → Client stays consistent
```

---

## 🔗 Related Modules

* **[openapi-generics-contract](../../openapi-generics-contract/README.md)**
  Canonical contract definitions (`ServiceResponse<T>`, `Page<T>`).

* **[openapi-generics-server-starter](../../openapi-generics-server-starter/README.md)**
  Projection layer used in this module.

* **[openapi-generics-java-codegen-parent](../../openapi-generics-java-codegen-parent/README.md)**
  Client generation orchestration.

* **[customer-service-client](../customer-service-client/README.md)**
  Consumer example showing how the generated client is used.

---

## 🧪 Verify quickly

```bash
curl http://localhost:8084/customer-service/v1/customers/1
```

Expected:

```json
{
  "data": {
    "customerId": 1,
    "name": "Jane Doe",
    "email": "jane@example.com"
  },
  "meta": {
    "serverTime": "...",
    "sort": []
  }
}
```

If this shape is correct:

```text
Contract → OpenAPI → Client will be correct
```

---

## 🌐 OpenAPI endpoints

* Swagger UI
  [http://localhost:8084/customer-service/swagger-ui/index.html](http://localhost:8084/customer-service/swagger-ui/index.html)

* OpenAPI YAML
  [http://localhost:8084/customer-service/v3/api-docs.yaml](http://localhost:8084/customer-service/v3/api-docs.yaml)

---

## 🚫 What this project is NOT

* not a framework
* not a reusable starter template
* not a production system

It is only:

> A minimal, correct reference for contract-first API exposure

---

## 🛡️ License

MIT License

---

**Maintained by:**
**Barış Saylı**
[GitHub](https://github.com/bsayli) · [Medium](https://medium.com/@baris.sayli) · [LinkedIn](https://www.linkedin.com/in/bsayli)

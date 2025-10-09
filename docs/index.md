---
layout: default
title: Home
nav_order: 1
canonical_url: https://medium.com/@baris.sayli/type-safe-generic-api-responses-with-spring-boot-3-4-openapi-generator-and-custom-templates-ccd93405fb04
---

# Spring Boot OpenAPI Generics Clients

> A production-grade blueprint for generics-aware API clients built with Spring Boot and OpenAPI Generator.

Welcome! 👋

This project demonstrates a **modern, generics-aware OpenAPI client generation pattern** for Spring Boot 3.4+, featuring
the unified `{ data, meta }` response model and full **nested generic support** — from `ServiceResponse<T>` to
`ServiceResponse<Page<T>>`.

---

## 💡 Overview

Using Springdoc on the backend and OpenAPI Generator 7.16.0 on the client side, this setup enables seamless code
generation where all responses are **type-safe**, **clean**, and **boilerplate-free**.

```java
public class ServiceResponseCustomerDto
        extends ServiceClientResponse<CustomerDto> {
}
```

Each generated client wrapper now automatically supports nested generic envelopes such as:

```java
ServiceClientResponse<Page<CustomerDto>>
```

---

## ✅ Key Features

* **Unified response model:** `{ data, meta }` replaces legacy status/message/errors structure.
* **Nested generics support:** Handles both `ServiceResponse<T>` and `ServiceResponse<Page<T>>`.
* **RFC 7807 compliant errors:** All non-2xx responses are mapped into `ProblemDetail` and thrown as
  `ClientProblemException`.
* **Generics-aware OpenAPI Generator overlay:** Mustache templates produce thin, type-safe wrappers.
* **Simple integration:** Works with any Spring Boot service exposing `/v3/api-docs.yaml`.

---

## 🧩 Architecture

```
[customer-service]  →  publishes OpenAPI spec (/v3/api-docs.yaml)
         │
         └──►  [customer-service-client]  →  generates thin wrappers extending ServiceClientResponse<T>
                       │
                       └──►  used by consumer microservices via adapters
```

---

## 🚀 Quick Start

```bash
# Run the backend
cd customer-service && mvn spring-boot:run

# Generate the OpenAPI client
cd ../customer-service-client && mvn clean install
```

Generated wrappers appear under:

`target/generated-sources/openapi/src/gen/java`

Each class extends `ServiceClientResponse<T>` and is compatible with the `{ data, meta }` response structure.

---

## 🧱 Example Response

```json
{
  "data": {
    "customerId": 1,
    "name": "Jane Doe",
    "email": "jane@example.com"
  },
  "meta": {
    "serverTime": "2025-01-01T12:34:56Z",
    "sort": []
  }
}
```

Client usage:

```java
ServiceClientResponse<CustomerDto> response = api.getCustomer(1);
CustomerDto dto = response.getData();
Instant serverTime = response.getMeta().serverTime();
```

---

## ⚙️ Toolchain

| Component             | Version | Purpose                          |
|-----------------------|---------|----------------------------------|
| **Java**              | 21      | Language baseline                |
| **Spring Boot**       | 3.4.10  | REST + OpenAPI provider          |
| **Springdoc**         | 2.8.13  | OpenAPI 3.1 integration          |
| **OpenAPI Generator** | 7.16.0  | Generics-aware client generation |
| **HttpClient5**       | 5.5     | Production-grade HTTP backend    |

---

## 📚 Learn More

* [Server-Side Adoption](adoption/server-side-adoption.md)
* [Client-Side Adoption](adoption/client-side-adoption.md)

---

## 🔗 References & External Links

<div class="callout learn-more">
  <ul>
    <li>🌐 <a href="https://github.com/bsayli/spring-boot-openapi-generics-clients" target="_blank" rel="noopener">GitHub Repository</a></li>
    <li>📘 <a href="https://medium.com/@baris.sayli/type-safe-generic-api-responses-with-spring-boot-3-4-openapi-generator-and-custom-templates-ccd93405fb04" target="_blank" rel="noopener">Medium — We Made OpenAPI Generator Think in Generics</a></li>
    <li>💬 <a href="https://dev.to/barissayli/spring-boot-openapi-generator-type-safe-generic-api-clients-without-boilerplate-3a8f" target="_blank" rel="noopener">Dev.to — We Made OpenAPI Generator Think in Generics</a></li>
  </ul>
</div>

---

✅ With this setup, you get **end-to-end generics awareness**, clean `{ data, meta }` responses, nested generic wrappers,
and unified error handling — all generated automatically.

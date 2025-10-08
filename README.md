# Spring Boot + OpenAPI Generator — End-to-End Generics-Aware API Clients

[![Build](https://github.com/bsayli/spring-boot-openapi-generics-clients/actions/workflows/build.yml/badge.svg)](https://github.com/bsayli/spring-boot-openapi-generics-clients/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/bsayli/spring-boot-openapi-generics-clients?logo=github\&label=release)](https://github.com/bsayli/spring-boot-openapi-generics-clients/releases/latest)
[![codecov](https://codecov.io/gh/bsayli/spring-boot-openapi-generics-clients/branch/main/graph/badge.svg)](https://codecov.io/gh/bsayli/spring-boot-openapi-generics-clients)
[![Java](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.10-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![OpenAPI Generator](https://img.shields.io/badge/OpenAPI%20Generator-7.16.0-blue?logo=openapiinitiative)](https://openapi-generator.tech/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

<p align="center">
  <img src="docs/images/openapi-generics-cover.png" alt="OpenAPI Generics Cover" width="720"/>
  <br/>
  <em><strong>End-to-end generics-aware OpenAPI clients</strong> — unified <code>{ data, meta }</code> responses without boilerplate.</em>
</p>

**Modern, type-safe OpenAPI client generation** — powered by **Spring Boot 3.4**, **Java 21**, and **OpenAPI Generator 7.16.0**.
This repository demonstrates a production-grade architecture where backend and client are fully aligned through generics, enabling nested generic envelopes (`ServiceResponse<Page<T>>`) and [**RFC 9457 — Problem Details for HTTP APIs**](https://www.rfc-editor.org/rfc/rfc9457)-based error handling.

> 🧠 **RFC 9457 vs RFC 7807**
> RFC 9457 supersedes 7807 and standardizes `application/problem+json` / `application/problem+xml` for HTTP APIs.
> Spring Framework 6+ implements this via the built-in `ProblemDetail` class, enabling consistent error serialization across server and client.

---

## 📑 Table of Contents

* 📦 [Modules](#-modules)
* 🚀 [Problem & Motivation](#-problem--motivation)
* 💡 [Solution Overview](#-solution-overview)
* ⚙️ [Architecture Overview](#-architecture-overview)
* ⚡ [Quick Start](#-quick-start)
* 🔄 [Generated Wrappers — Before & After](#-generated-wrappers--before--after)
* 🧱 [Example Responses](#-example-responses)
* 🧩 [Tech Stack](#-tech-stack)
* ✅ [Key Features](#-key-features)
* ✨ [Usage Example](#-usage-example)
* 📘 [Adoption Guides](#-adoption-guides)
* 🔗 [References & External Links](#-references--external-links)

> *A clean architecture pattern for building generics-aware OpenAPI clients that stay fully type-safe, consistent, and boilerplate-free.*

---

## 📦 Modules

* [**customer-service**](customer-service/README.md) — sample backend exposing `/v3/api-docs.yaml` via Springdoc
* [**customer-service-client**](customer-service-client/README.md) — generated OpenAPI client with generics-aware wrappers

---

## 🚀 Problem & Motivation

OpenAPI Generator, by default, does not handle **generic response types**.

When backend APIs wrap payloads in `ServiceResponse<T>` (e.g., the unified `{ data, meta }` envelope), the generator produces **duplicated models per endpoint** instead of a single reusable generic base.

This results in:

* ❌ Dozens of almost-identical response classes
* ❌ Higher maintenance overhead
* ❌ Harder to evolve a single envelope contract across services

```java
// Default OpenAPI output (before)
class CreateCustomerResponse { CustomerDto data; Meta meta; }
class UpdateCustomerResponse { CustomerDto data; Meta meta; }
// ... dozens of duplicates
```

---

## 💡 Solution Overview

This project provides a **full-stack pattern** aligning Spring Boot services and OpenAPI clients through automatic schema introspection and template overlay.

### 🖥️ Server-Side (Producer)

A `Springdoc` customizer inspects controller return types such as:

```java
ResponseEntity<ServiceResponse<CustomerDto>>
ResponseEntity<ServiceResponse<Page<CustomerDto>>>
```

and enriches the generated OpenAPI schema with vendor extensions:

**Single type (`ServiceResponse<T>`):**

```yaml
x-api-wrapper: true
x-api-wrapper-datatype: CustomerDto
```

**Nested generics (`ServiceResponse<Page<T>>`):**

```yaml
x-api-wrapper: true
x-data-container: Page
x-data-item: CustomerDto
```

These extensions make the OpenAPI spec *aware* of generic and nested structures — no manual annotations required.

### 💻 Client-Side (Consumer)

Custom Mustache overlays redefine OpenAPI templates to generate **thin, type-safe wrappers** extending the reusable base `ServiceClientResponse<T>`.

**Generated output:**

```java
// Single
data class ServiceResponseCustomerDto
    extends ServiceClientResponse<CustomerDto> {}

// Paged
data class ServiceResponsePageCustomerDto
    extends ServiceClientResponse<Page<CustomerDto>> {}
```

✅ Supports **nested generics** like `ServiceClientResponse<Page<CustomerDto>>`
✅ Automatically maps error responses into **RFC 9457 Problem Details**

---

## ⚙️ Architecture Overview

<p align="center">
  <img src="docs/images/architectural-diagram.png" alt="OpenAPI Generics Architecture" width="900"/>
  <br/>
  <em>End-to-end generics-aware architecture: from Spring Boot producer to OpenAPI client consumer.</em>
</p>

| Layer                 | Description                                                                                           |
| --------------------- | ----------------------------------------------------------------------------------------------------- |
| **Server (Producer)** | Publishes an **OpenAPI 3.1-compliant** spec via Springdoc 2.8.13 with auto-registered wrapper schemas |
| **Client (Consumer)** | Uses **OpenAPI Generator 7.16.0** with Mustache overlays for generics support                         |
| **Envelope Model**    | Unified `{ data, meta }` response structure                                                           |
| **Error Handling**    | **RFC 9457-compliant Problem Details** decoded into `ClientProblemException`                          |
| **Nested Generics**   | Full support for `ServiceResponse<Page<T>>`                                                           |

---

## ⚡ Quick Start

```bash
# Run backend service
cd customer-service && mvn spring-boot:run

# Generate and build client
cd ../customer-service-client && mvn clean install
```

Generated wrappers appear under:

```
target/generated-sources/openapi/src/gen/java
```

Each wrapper extends `ServiceClientResponse<T>` and aligns with the unified `{ data, meta }` envelope.

Now you can test end-to-end type-safe responses via the generated client — validating both single and paged envelopes.

---

## 🔄 Generated Wrappers — Before & After

**Before (duplicated full models):**

<p align="center">
  <img src="docs/images/generated-client-wrapper-before.png" alt="Generated client before generics support" width="800"/>
  <br/>
  <em>Each endpoint generated its own response class, duplicating <code>data</code> and <code>meta</code> fields.</em>
</p>

**After (thin generic wrapper):**

<p align="center">
  <img src="docs/images/generated-client-wrapper-after.png" alt="Generated client after generics support" width="800"/>
  <br/>
  <em>Each endpoint now extends the reusable <code>ServiceClientResponse&lt;Page&lt;T&gt;&gt;</code> base, eliminating boilerplate and preserving type safety.</em>
</p>

---

## 🧱 Example Responses

Unified envelope structure applies to both single and paged results.

### 🧩 Single Item Example (`ServiceClientResponse<CustomerDto>`)

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

### 📄 Paged Example (`ServiceClientResponse<Page<CustomerDto>>`)

```json
{
  "data": {
    "content": [
      { "customerId": 1, "name": "Jane Doe", "email": "jane@example.com" },
      { "customerId": 2, "name": "John Smith", "email": "john@example.com" }
    ],
    "page": 0,
    "size": 5,
    "totalElements": 37,
    "totalPages": 8,
    "hasNext": true,
    "hasPrev": false
  },
  "meta": {
    "serverTime": "2025-01-01T12:34:56Z",
    "sort": [ { "field": "CUSTOMER_ID", "direction": "ASC" } ]
  }
}
```

> **Content-Type:**  `application/json` (success)
> **Content-Type:**  `application/problem+json` (error — RFC 9457)

### Client Usage

```java
ServiceClientResponse<Page<CustomerDto>> resp =
    customerClientAdapter.getCustomers("Jane", null, 0, 5, SortField.CUSTOMER_ID, SortDirection.ASC);

Page<CustomerDto> page = resp.getData();
for (CustomerDto c : page.content()) {
    // ...
}
```

---

## 🧩 Tech Stack

| Component             | Version | Purpose                               |
| --------------------- | ------- | ------------------------------------- |
| **Java**              | 21      | Language baseline                     |
| **Spring Boot**       | 3.4.10  | REST + OpenAPI provider               |
| **Springdoc**         | 2.8.13  | OpenAPI 3.1 integration               |
| **OpenAPI Generator** | 7.16.0  | Generics-aware code generation        |
| **HttpClient5**       | 5.5     | Pooled, production-ready HTTP backend |

---

## ✅ Key Features

* 🔹 Unified `{ data, meta }` response model
* 🔹 Nested generics support — `ServiceResponse<Page<T>>`
* 🔹 **RFC 9457-compliant Problem Details** (`application/problem+json`)
* 🔹 Mustache overlays for thin wrapper generation
* 🔹 Full alignment between producer and consumer
* 🔹 Zero boilerplate — clean, evolvable, and type-safe

---

## ✨ Usage Example

```java
public interface CustomerClientAdapter {
    ServiceClientResponse<CustomerDto> createCustomer(CustomerCreateRequest request);
    ServiceClientResponse<CustomerDto> getCustomer(Integer customerId);
    ServiceClientResponse<Page<CustomerDto>> getCustomers();
}
```

A stable adapter contract hides generated artifacts while preserving strong typing and client independence.

---

## 📘 Adoption Guides

See integration details under [`docs/adoption`](docs/adoption):

* [Server-Side Adoption](docs/adoption/server-side-adoption.md)
* [Client-Side Adoption](docs/adoption/client-side-adoption.md)

---

## 🔗 References & External Links

* 🌐 [GitHub Repository](https://github.com/bsayli/spring-boot-openapi-generics-clients)
* 📰 [Medium — We Made OpenAPI Generator Think in Generics](https://medium.com/@baris.sayli/type-safe-generic-api-responses-with-spring-boot-3-4-openapi-generator-and-custom-templates-ccd93405fb04)
* 💬 [Dev.to — We Made OpenAPI Generator Think in Generics](https://dev.to/barissayli/spring-boot-openapi-generator-type-safe-generic-api-clients-without-boilerplate-3a8f)
* 📘 [RFC 9457 — Problem Details for HTTP APIs](https://www.rfc-editor.org/rfc/rfc9457)

---

## 🛡 License

Licensed under **MIT** — see [LICENSE](LICENSE).

---

## 💬 Feedback

If you spot an error or have suggestions, open an issue or join the discussion — contributions are welcome.
💭 [Start a discussion →](https://github.com/bsayli/spring-boot-openapi-generics-clients/discussions)

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!
Feel free to [open an issue](https://github.com/bsayli/spring-boot-openapi-generics-clients/issues) or submit a PR.

---

## ⭐ Support

If you found this project helpful, please give it a ⭐ on GitHub — it helps others discover it.

---

**Barış Saylı**
[GitHub](https://github.com/bsayli) · [Medium](https://medium.com/@baris.sayli) · [LinkedIn](https://www.linkedin.com/in/bsayli)

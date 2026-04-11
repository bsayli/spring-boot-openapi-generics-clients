# customer-service

> **Reference implementation: exposing a Spring Boot API that produces a clean, deterministic OpenAPI for contract-aligned, generics-aware clients**

[![Java 21](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.13-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![Springdoc](https://img.shields.io/badge/Springdoc-2.8.16-brightgreen)](https://springdoc.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](../../LICENSE)

---

## 📑 Table of Contents

* [🚀 Start here (what you actually want)](#-start-here-what-you-actually-want)
* [🔗 Related Modules](#-related-modules)
* [🧪 Verify quickly](#-verify-quickly)
* [🌐 OpenAPI endpoints](#-openapi-endpoints)
* [🛡️ License](#-license)

---

> This is a minimal reference implementation.  
> See the [Adoption Guides](../../docs/adoption/server-side-adoption.md) for rules, constraints, and architecture.

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

### 2. Do NOT customize OpenAPI manually

* No annotations
* No schema hacks
* No manual overrides

Everything is handled by the starter.

---

## 🔗 Related Modules

* **[Contract](../../openapi-generics-contract/README.md)**
* **[Server Starter](../../openapi-generics-server-starter/README.md)**
* **[Client Codegen](../../openapi-generics-java-codegen-parent/README.md)**
* **[Client Sample](../customer-service-client/README.md)**

---

## 🧪 Verify quickly

```bash
curl http://localhost:8085/customer-service/customers/1
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
  [http://localhost:8085/customer-service/swagger-ui/index.html](http://localhost:8085/customer-service/swagger-ui/index.html)

* OpenAPI YAML
  [http://localhost:8085/customer-service/v3/api-docs.yaml](http://localhost:8085/customer-service/v3/api-docs.yaml)

---

## 🛡️ License

MIT License

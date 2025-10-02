---
layout: default
title: Home
nav_order: 1
---

# Spring Boot OpenAPI Generics Clients

Welcome! 👋

This project demonstrates how to extend **OpenAPI Generator** with **generics-aware wrappers**, avoiding duplicated
response models and keeping client code clean and type-safe.

---

## 🚩 Problem

By default, **OpenAPI Generator does not support generics**. When backend teams use a generic response wrapper like
`ServiceResponse<T>`, the generator produces **one full wrapper per endpoint**, duplicating fields such as `status`,
`message`, and `errors`.

This causes:

* ❌ Dozens of nearly identical classes
* ❌ High maintenance overhead
* ❌ Boilerplate code scattered across clients

---

## 💡 Solution

This project shows how to:

* Mark wrapper schemas in OpenAPI using a small **Springdoc customizer**.
* Provide a **tiny Mustache template overlay** so the generator emits **thin shells** extending a reusable generic base.
* Preserve **compile-time type safety** while removing repetitive wrappers.

Result: Instead of duplicating fields, the generator creates wrappers like:

```java
public class ServiceResponseCustomerDto
        extends ServiceClientResponse<CustomerDto> {
}
```

---

## ✅ Benefits

* Strong typing without boilerplate
* A single generic base (`ServiceClientResponse<T>`) for all responses
* Easier maintenance — update the base once, all clients benefit
* Clean, consistent contracts across microservices

---

## 📘 Adoption Guides

Choose one of the following to integrate this pattern into your own project:

* [Server-Side Adoption](adoption/server-side-adoption.md)
* [Client-Side Adoption](adoption/client-side-adoption.md)

---

## 🚀 Quick Start

```bash
# Run the sample server
cd customer-service && mvn spring-boot:run

# Generate and build the client
cd ../customer-service-client && mvn clean install
```

Generated wrappers can be found under:

`target/generated-sources/openapi/src/gen/java`

---

## 📖 More Docs

[![View on GitHub](https://img.shields.io/badge/GitHub-View%20Repo-blue?logo=github)](https://github.com/bsayli/spring-boot-openapi-generics-clients)

See the full [README on GitHub](https://github.com/bsayli/spring-boot-openapi-generics-clients#readme).

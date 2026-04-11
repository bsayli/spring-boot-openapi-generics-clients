# customer-service-client

> **Reference integration: generating and using a contract-aligned, generics-aware OpenAPI client in a Spring Boot application**

[![Java 21](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.13-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![OpenAPI Generator](https://img.shields.io/badge/OpenAPI%20Generator-7.21.0-blue?logo=openapiinitiative)](https://openapi-generator.tech/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](../../LICENSE)

---

## 📑 Table of Contents

* 🚀 [TL;DR (Start Here)](#-tldr-start-here)
* 🧩 [Adapter Pattern](#-adapter-pattern-recommended)
* 🌐 [HTTP Client Setup](#-http-client-setup-production-ready)
* ⚖️ [Error Handling](#-error-handling-model)
* 🔗 [Related Modules](#-related-modules)
* 🧪 [Testing](#-testing)
* 🛡️ [License](#-license)

---

> This is a minimal reference implementation.  
> See the [Adoption Guides](../../docs/adoption/client-side-adoption.md) for rules, constraints, and architecture.

## 🚀 TL;DR (Start Here)

### 1. Inherit the parent

```xml
<parent>
  <groupId>io.github.blueprintplatform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>0.8.3</version>
</parent>
```

### 2. Provide OpenAPI

```text
src/main/resources/your-api-docs.yaml
```

### 3. Build

```bash
mvn clean install
```

---

### Notes

* The parent provides generator, templates, and contract mappings
* You only supply input (OpenAPI) and structure (packages, client choice)
* Generated code is written to `target/generated-sources/openapi`

---

## 🧩 Adapter Pattern (Recommended)

Generated code is replaceable. Your application should not be.

Introduce a thin adapter as a stability boundary:

```java
public interface CustomerClientAdapter {
  ServiceResponse<CustomerDto> getCustomer(Integer id);
}
```

```java
@Service
public class CustomerClientAdapterImpl implements CustomerClientAdapter {

  private final CustomerControllerApi api;

  public CustomerClientAdapterImpl(CustomerControllerApi api) {
    this.api = api;
  }

  @Override
  public ServiceResponse<CustomerDto> getCustomer(Integer id) {
    return api.getCustomer(id);
  }
}
```

This ensures:

* generated code stays isolated
* contract types (`ServiceResponse<T>`) flow unchanged
* client regeneration is safe

---

## 🌐 HTTP Client Setup (Production Ready)

This module demonstrates:

* Apache HttpClient 5
* connection pooling
* timeouts
* explicit behavior (no hidden retries)

You may simplify or replace this depending on your environment.

---

## ⚖️ Error Handling Model

Errors follow a runtime protocol:

```text
ProblemDetail (RFC 9457)
```

Behavior:

* parsed into structured objects
* surfaced via `ApiProblemException`

Fallbacks handled:

* empty response
* invalid JSON
* unexpected formats

---

## 🔗 Related Modules

* **[Contract](../../openapi-generics-contract/README.md)**
* **[Server Starter](../../openapi-generics-server-starter/README.md)**
* **[Client Codegen](../../openapi-generics-java-codegen-parent/README.md)**
* **[Server Sample](../customer-service/README.md)**

---

## 🧪 Testing

```bash
mvn verify
```

---

## 🛡️ License

MIT License

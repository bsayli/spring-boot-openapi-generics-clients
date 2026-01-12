# customer-service

[![Java](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![Build](https://github.com/bsayli/spring-boot-openapi-generics-clients/actions/workflows/build.yml/badge.svg)](https://github.com/bsayli/spring-boot-openapi-generics-clients/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](../LICENSE)

---

## 📑 Table of Contents

- 🎯 [Purpose](#-purpose)
- 📊 [Architecture at a Glance](#-architecture-at-a-glance)
- 🛠 [Tech Stack](#-tech-stack)
- 🚀 [How to Run](#-how-to-run)
  - ✅ [Recommended (from repo root)](#-recommended-from-repo-root)
  - ⚙️ [Alternative (module-only, if api-contract is already installed)](#-alternative-module-only-if-api-contract-is-already-installed)
  - 🐳 [Run with Docker](#-run-with-docker)
- 🧪 [Verify with a Simple Request](#-verify-with-a-simple-request)
- 📙 [CRUD Endpoints](#-crud-endpoints)
- 🔗 [OpenAPI Endpoints](#-openapi-endpoints)
- ⚠️ [Error Response (RFC 9457)](#-error-response-rfc-9457)
- 🧪 [Testing](#-testing)
- 🖖 [Notes](#-notes)
- 📦 [Related Module](#-related-module)
- 💬 [Feedback](#-feedback)
- 🤝 [Contributing](#-contributing)
- 🛡 [License](#-license)

---

## 🎯 Purpose

`customer-service` provides a **minimal yet production-grade backend** exposing CRUD endpoints for customers. It acts as the **OpenAPI producer** that defines the canonical contract consumed by the generated client module.

This module is intentionally contract-driven: response models are sourced from the shared **`api-contract`** artifact (`ServiceResponse`, `Meta`, `Page`, `Sort`, ...), ensuring server and client speak the same language.

**Key responsibilities:**

* Publishes an OpenAPI spec (`/v3/api-docs.yaml`) enriched with **vendor extensions** required for deterministic wrapper typing.
* Feeds the [`customer-service-client`](../customer-service-client/README.md) module for type-safe, boilerplate-free client generation.
* Demonstrates **automatic schema registration** and **wrapper introspection** via custom Springdoc customizers.

### Contract rules (non-negotiable)

* The canonical success envelope is **`ServiceResponse<T>`**.
* Nested generics are supported **only** for **`ServiceResponse<Page<T>>`**.

  * For any other generic type (`List<T>`, `Map<K,V>`, `Foo<Bar>`), generics are **ignored** in schema naming: only the **raw type name** is used.

---

## 📊 Architecture at a Glance

```
[customer-service]  ── publishes ──>  /v3/api-docs.yaml (OpenAPI 3.1 + x-api-wrapper + optional Page hints)
        │
        └─ consumed by OpenAPI Generator (+ custom Mustache overlays)
                 │
                 └─> [customer-service-client]  (type-safe wrappers)
                          │
                          └─ used by consumer microservices
```

### Explanation

* **customer-service** auto-registers wrapper schemas by scanning controller methods and extracting the `T` inside `ServiceResponse<T>`.

  * `AutoWrapperSchemaCustomizer` registers composed schemas for each discovered `T`.
  * `ResponseTypeIntrospector` enforces the **Page-only nested generics rule**.

* The OpenAPI document is enriched with vendor extensions:

| Extension key                           | Example value                | Purpose                                       |
| --------------------------------------- | ---------------------------- | --------------------------------------------- |
| `x-api-wrapper`                         | `true`                       | Marks the schema as a response wrapper        |
| `x-api-wrapper-datatype`                | `CustomerDto`                | Specifies the `T` in `ServiceResponse<T>`     |
| `x-data-container`                      | `Page`                       | Present **only when** `T` is `Page<…>`        |
| `x-data-item`                           | `CustomerDto`                | Present **only when** `T` is `Page<…>`        |
| `x-class-extra-annotation` *(optional)* | `@JsonIgnoreProperties(...)` | Optional annotation hint for generated models |

* These hints allow the OpenAPI Generator to produce nested generic clients such as:

```java
public class ServiceResponsePageCustomerDto extends ServiceResponse<Page<CustomerDto>> {}
```

* **customer-service-client** uses custom templates to emit **thin wrappers** extending the base `ServiceResponse<T>` without duplicating model definitions.

---

## 🛠 Tech Stack

* **Java 21**

* **Spring Boot 3.5.9**

  * spring-boot-starter-web
  * spring-boot-starter-validation
  * spring-boot-starter-test (test scope)

* **OpenAPI / Swagger**

  * springdoc-openapi-starter-webmvc-ui (2.8.15)

* **Build & Tools**

  * Maven 3.9+
  * JaCoCo, Surefire, Failsafe

---

## 🚀 How to Run

This service depends on the shared **`api-contract`** module. The easiest and safest way to run it locally is via the **root aggregator build**, which guarantees all modules are compiled and resolved correctly.

> ⚠️ Running `customer-service` in isolation may fail if `api-contract` is not already installed in your local Maven repository.

---

### ✅ Recommended (from repo root)

```bash
# Build all modules (api-contract, customer-service, client)
mvn -q clean package

# Run the service
java -jar customer-service/target/customer-service-*.jar
```

This ensures:

* `api-contract` is built and available
* no missing dependencies at runtime
* consistent behavior with CI

The service starts at:

```
http://localhost:8084/customer-service
```

---

### ⚙️ Alternative (module-only, if api-contract is already installed)

Use this **only if** you have already run a full build once or installed `api-contract` locally:

```bash
cd customer-service
mvn clean package
java -jar target/customer-service-*.jar
```

---

### 🐳 Run with Docker

> `customer-service` depends on the shared `api-contract` module.
> The Docker build uses the **repository root** as the build context to ensure all modules are compiled deterministically.

---

#### Using Docker Compose (recommended)

Run from the `customer-service` directory:

```bash
cd customer-service
docker compose up --build -d
```

Stop and clean up:

```bash
docker compose down
```

This setup:

* Uses the repository root (`..`) as the build context
* Builds `api-contract` and `customer-service` together
* Produces a self-contained runtime image for `customer-service`

---

#### Using plain Docker (manual)

Run the build from the **repository root** so the build context includes all required modules:

```bash
docker build -t customer-service:latest \
  -f customer-service/Dockerfile \
  .
```

Run the container:

```bash
docker run --rm -p 8084:8084 \
  -e APP_PORT=8084 \
  -e SPRING_PROFILES_ACTIVE=local \
  -e JAVA_OPTS= \
  customer-service:latest
```

---

## Notes

* You **do not** need to run `mvn package` locally when using Docker — the multi-stage Dockerfile handles the build.

* The Dockerfile intentionally runs Maven with:

  ```bash
  mvn -q -DskipTests -ntp clean package -pl customer-service -am
  ```

  This guarantees:

  * `api-contract` is built first
  * `customer-service` is packaged with correct dependencies
  * No reliance on local Maven state

* Port `8084` is exposed by default and matches the local JVM setup.

---

If Docker works but local JVM does not, the issue is almost always a missing `api-contract` build — use the **root aggregator build** for local runs.


---

## 🧪 Verify with a Simple Request

### Create Customer (example)

```bash
curl -X POST "http://localhost:8084/customer-service/v1/customers" \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane Doe","email":"jane@example.com"}'
```

### Expected Response

All successful responses are wrapped in the **canonical contract**:

```java
ServiceResponse<CustomerDto>
```

Example JSON:

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

---

## 🧠 Why This Matters

* The runtime behavior exactly matches what clients generate against.
* `{ data, meta }` is **not a demo wrapper** — it is the enforced contract.
* This is the same shape consumed by `customer-service-client`.

If this response looks correct, your **end‑to‑end contract** is working as designed.

---

## 📙 CRUD Endpoints

All success responses are wrapped in `ServiceResponse<...>`.

| Method | Path                         | Description              | Returns (`data`)         |
| ------ | ---------------------------- | ------------------------ | ------------------------ |
| POST   | `/v1/customers`              | Create new customer      | `CustomerDto`            |
| GET    | `/v1/customers/{customerId}` | Get single customer      | `CustomerDto`            |
| GET    | `/v1/customers`              | List customers (paged)   | `Page<CustomerDto>`      |
| PUT    | `/v1/customers/{customerId}` | Update existing customer | `CustomerDto`            |
| DELETE | `/v1/customers/{customerId}` | Delete customer          | `CustomerDeleteResponse` |

**Base URL:** `/customer-service` (configured in `application.yml`)

---

## 🔗 OpenAPI Endpoints

* Swagger UI → `http://localhost:8084/customer-service/swagger-ui/index.html`
* OpenAPI JSON → `http://localhost:8084/customer-service/v3/api-docs`
* OpenAPI YAML → `http://localhost:8084/customer-service/v3/api-docs.yaml`

> The YAML/JSON spec above is the **canonical contract** consumed by `customer-service-client`.

### Example Wrapper Snippet (Page-only nested generics)

```yaml
ServiceResponsePageCustomerDto:
  allOf:
    - $ref: "#/components/schemas/ServiceResponse"
    - type: object
      properties:
        data:
          $ref: "#/components/schemas/PageCustomerDto"
  x-api-wrapper: true
  x-api-wrapper-datatype: PageCustomerDto
  x-data-container: Page
  x-data-item: CustomerDto
```

`x-data-container` and `x-data-item` are emitted **only** when `data` is a `Page<...>`.

---

## ⚠️ Error Response (RFC 9457)

If a resource is missing:

```bash
curl -X GET "http://localhost:8084/customer-service/v1/customers/999"
```

**Response:**

```json
{
  "type": "https://example.com/problems/not-found",
  "title": "Resource not found",
  "status": 404,
  "detail": "Requested resource was not found.",
  "instance": "/customer-service/v1/customers/999",
  "errorCode": "NOT_FOUND",
  "extensions": {
    "errors": [
      {
        "code": "NOT_FOUND",
        "message": "Customer not found: 999",
        "field": null,
        "resource": "Customer",
        "id": null
      }
    ]
  }
}
```

> Content-Type: `application/problem+json` — compliant with **RFC 9457** (*obsoletes RFC 7807*).
> Spring’s `ProblemDetail` maps directly to this structure.

---

## 🧪 Testing

```bash
cd customer-service
mvn verify
```

---

## 🖖 Notes

* Uses shared response models from **`io.github.bsayli:api-contract`**.
* Demonstrates **`ServiceResponse<T>`** and **`ServiceResponse<Page<T>>`** (Page-only nested generics rule).
* Adds wrapper typing hints via vendor extensions (`x-api-wrapper`, `x-api-wrapper-datatype`).
* Adds Page container hints **only** for `Page<T>` (`x-data-container`, `x-data-item`).
* Implements **RFC 9457-compliant Problem Details** (`application/problem+json`) responses.
* Provides **unit and integration tests** for controller and OpenAPI customization layers.
* Supports optional annotation injection for generated wrappers via `app.openapi.wrapper.class-extra-annotation`.

---

## 📦 Related Module

This service is the API producer for the generated client:

* [customer-service-client](../customer-service-client/README.md) — Java client generated from this service's OpenAPI spec, supporting Page-aware wrappers and RFC 9457 problem decoding.

---

## 💬 Feedback

If you spot an error or have suggestions, open an issue or join the discussion — contributions are welcome.
💭 [Start a discussion →](https://github.com/bsayli/spring-boot-openapi-generics-clients/discussions)

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!
Feel free to [open an issue](https://github.com/bsayli/spring-boot-openapi-generics-clients/issues) or submit a PR.

---

## 🛡 License

This repository is licensed under **MIT** (root `LICENSE`). Submodules inherit the license.

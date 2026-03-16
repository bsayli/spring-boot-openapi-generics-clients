# customer-service

[![Java](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.11-green?logo=springboot)](https://spring.io/projects/spring-boot)
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

### Contract semantics

This module follows a **clear and intentionally limited contract scope** to keep the published OpenAPI specification and client generation predictable.

* Successful responses use the shared envelope **`ServiceResponse<T>`**, sourced from the common `api-contract` module.

* Nested generics are treated as contract-aware **only** for:

  ```java
  ServiceResponse<Page<T>>
  ```

  This reflects the common pagination use case and allows stable schema naming.

* For all other generic shapes (`List<T>`, `Map<K,V>`, `Foo<Bar>`, etc.), generic information is **not interpreted** during schema naming.
  In those cases, the OpenAPI output follows the default behavior and uses the **raw container type** only.

This scoped approach ensures that both the server-published contract and the generated client remain consistent, understandable, and evolvable over time.

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
  * `ResponseTypeIntrospector` **identifies and marks only Page-based nested generics as contract-aware**.

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

* **Spring Boot 3.5.11**

  * spring-boot-starter-web
  * spring-boot-starter-validation
  * spring-boot-starter-test (test scope)

* **OpenAPI / Swagger**

  * springdoc-openapi-starter-webmvc-ui (2.8.16)

* **Build & Tools**

  * Maven 3.9+
  * JaCoCo, Surefire, Failsafe

---

## 🚀 How to Run

`customer-service` depends on the shared **`api-contract`** library, which is now **published to Maven Central**.
This means the service can be built and run **independently**, without requiring a full repository build.

---

### ✅ Recommended (module‑only)

Build and run the service directly from its module directory:

```bash
cd customer-service
mvn clean package
java -jar target/customer-service-*.jar
```

This approach:

* Resolves `api-contract` automatically from Maven Central
* Matches real‑world consumer usage (service as a standalone artifact)
* Keeps local development workflow simple and fast

The service starts at:

```
http://localhost:8084/customer-service
```

---

### 🧪 Optional (full repository build)

If you want to verify the **entire multi‑module repository** (for example before pushing changes or running CI‑like validation), you can still build from the root:

```bash
mvn -q clean package
java -jar customer-service/target/customer-service-*.jar
```

This is useful for:

* validating cross‑module compatibility
* running the full test suite
* ensuring deterministic CI parity

However, it is **not required** for normal local development.

---

### 🐳 Run with Docker

The Docker image builds and runs **only the `customer-service` module**.
Dependencies such as `api-contract` are resolved from Maven Central during the image build.

#### Using Docker Compose (recommended)

```bash
cd customer-service
docker compose up --build -d
```

Stop and clean up:

```bash
docker compose down
```

This setup:

* Builds a self‑contained runtime image for `customer-service`
* Does not require building other modules locally
* Mirrors a realistic deployment pipeline

---

#### Using plain Docker (manual)

Build the image from the repository root (or any context that contains the module sources):

```bash
docker build -t customer-service:latest \
  -f customer-service/Dockerfile \
  .
```

Run the container:

```bash
docker run --rm -p 8084:8084 \
  -e SPRING_PROFILES_ACTIVE=local \
  customer-service:latest
```

---

## Notes

* `api-contract` is resolved as a **regular external dependency**.
* No local Maven installation or multi‑module pre‑build is required.
* Docker builds perform a clean Maven package step inside the container.
* Port `8084` is exposed by default and matches the local JVM runtime configuration.

If Docker runs successfully but a local JVM run fails, the issue is most likely unrelated to dependency resolution (for example environment variables or local configuration differences).

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

* The runtime response shape exactly matches what clients are generated against.
* `{ data, meta }` is **not a demo or sample wrapper** — it is the canonical response contract.
* The same contract is reused directly by `customer-service-client`.

If this response shape looks correct, it means the **end-to-end contract flow** —
from server, to OpenAPI specification, to generated client — is working as intended.

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

* **Swagger UI**  
  👉 [http://localhost:8084/customer-service/swagger-ui/index.html](http://localhost:8084/customer-service/swagger-ui/index.html)

* **OpenAPI JSON**  
  👉 [http://localhost:8084/customer-service/v3/api-docs](http://localhost:8084/customer-service/v3/api-docs)

* **OpenAPI YAML**  
  👉 [http://localhost:8084/customer-service/v3/api-docs.yaml](http://localhost:8084/customer-service/v3/api-docs.yaml)

> The YAML/JSON specification above is the **canonical contract** consumed by  
> **`customer-service-client`**.

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
  "type": "urn:customer-service:problem:not-found",
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
        "resource": "Customer"
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
* Demonstrates **`ServiceResponse<T>`** and the paged variant **`ServiceResponse<Page<T>>`**.
* Adds wrapper typing hints via vendor extensions (`x-api-wrapper`, `x-api-wrapper-datatype`).
* Adds pagination container hints **only when** `Page<T>` is involved (`x-data-container`, `x-data-item`).
* Implements **RFC 9457–compliant Problem Details** (`application/problem+json`) responses.
* Provides **unit and integration tests** for controller logic and OpenAPI customization.
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

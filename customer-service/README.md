# customer-service

[![Java](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.10-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](../LICENSE)
[![Build](https://github.com/bsayli/spring-boot-openapi-generics-clients/actions/workflows/build.yml/badge.svg)](https://github.com/bsayli/spring-boot-openapi-generics-clients/actions/workflows/build.yml)

---

## 🎯 Purpose

`customer-service` provides a **minimal yet production-grade backend** exposing CRUD endpoints for customers. Its
primary role is to act as the **OpenAPI producer** that defines the canonical contract consumed by the generated client
module.

**Key responsibilities:**

* Publishes OpenAPI spec (`/v3/api-docs.yaml`) enriched with **vendor extensions** for generics and nested wrappers.
* Feeds the [`customer-service-client`](../customer-service-client/README.md) module for type-safe, boilerplate-free
  client generation.
* Demonstrates **automatic schema registration** and **generic wrapper introspection** via custom Springdoc extensions.

This module serves as the **server-side anchor**: the reference point for generics-aware OpenAPI code generation.

---

## 📊 Architecture at a Glance

```
[customer-service]  ── publishes ──>  /v3/api-docs.yaml (OpenAPI 3.1 contract with x-api-wrapper & x-data-container)
        │
        └─ consumed by OpenAPI Generator (+ custom Mustache overlays)
                 │
                 └─> [customer-service-client]  (type-safe wrappers)
                          │
                          └─ used by consumer microservices
```

### Explanation

* **customer-service** auto-registers `ServiceResponse<T>` and `ServiceResponse<Page<T>>` schemas through the
  `AutoWrapperSchemaCustomizer` and `ResponseTypeIntrospector`.

* The OpenAPI document is enriched with vendor extensions:

    * `x-api-wrapper: true`
    * `x-api-wrapper-datatype: <T>`
    * `x-data-container: <Container>` (e.g., `Page`)
    * `x-data-item: <Item>` (e.g., `CustomerDto`)

* These hints allow the OpenAPI Generator to produce nested generic clients such as:

  ```java
  class CustomerListResponse extends ServiceClientResponse<Page<CustomerDto>> {}
  ```

* **customer-service-client** uses custom templates to emit **thin wrappers** extending the base
  `ServiceClientResponse<T>` without repeating model definitions.

---

## 🛠 Tech Stack

* **Java 21**
* **Spring Boot 3.4.10**

    * spring-boot-starter-web
    * spring-boot-starter-validation
    * spring-boot-starter-test (test scope)
* **OpenAPI / Swagger**

    * springdoc-openapi-starter-webmvc-ui (2.8.13)
* **Build & Tools**

    * Maven 3.9+
    * JaCoCo, Surefire, Failsafe for test & coverage

---

## 🚀 How to Run (Local JVM)

```bash
cd customer-service
mvn clean package
java -jar target/customer-service-*.jar
```

Service runs at: `http://localhost:8084/customer-service`

### Create Customer (example)

```bash
curl -X POST "http://localhost:8084/customer-service/v1/customers" \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane Doe","email":"jane@example.com"}'
```

**Expected response (wrapped in `ServiceResponse<CustomerDto>`):**

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

## 📙 CRUD Endpoints

| Method | Path                         | Description         | Returns                  |
|--------|------------------------------|---------------------|--------------------------|
| POST   | `/v1/customers`              | Create new customer | `CustomerDto`            |
| GET    | `/v1/customers/{customerId}` | Get single customer | `CustomerDto`            |
| GET    | `/v1/customers`              | List all customers  | `Page<CustomerDto>`      |
| PUT    | `/v1/customers/{customerId}` | Update customer     | `CustomerDto`            |
| DELETE | `/v1/customers/{customerId}` | Delete customer     | `CustomerDeleteResponse` |

**Base URL:** `/customer-service` (defined in `application.yml`)

### Example Response: Get Customer

```json
{
  "data": {
    "customerId": 1,
    "name": "Jane Doe",
    "email": "jane@example.com"
  },
  "meta": {
    "serverTime": "2025-01-01T12:34:56Z"
  }
}
```

### Example Response: List Customers (Page-aware)

```json
{
  "data": {
    "content": [
      {
        "customerId": 1,
        "name": "Jane Doe",
        "email": "jane@example.com"
      },
      {
        "customerId": 2,
        "name": "John Smith",
        "email": "john@example.com"
      }
    ],
    "page": 0,
    "size": 5,
    "totalElements": 2
  },
  "meta": {
    "serverTime": "2025-01-01T12:35:00Z",
    "sort": [
      {
        "field": "customerId",
        "direction": "asc"
      }
    ]
  }
}
```

---

## 🔗 OpenAPI Endpoints

* Swagger UI → `http://localhost:8084/customer-service/swagger-ui/index.html`
* OpenAPI JSON → `http://localhost:8084/customer-service/v3/api-docs`
* OpenAPI YAML → `http://localhost:8084/customer-service/v3/api-docs.yaml`

🤙 The YAML/JSON spec above is the **canonical contract** consumed by `customer-service-client`.

🔙 In this repository, the spec is also stored under the client module: `src/main/resources/customer-api-docs.yaml`

---

### Example Wrapper Snippet

The generated OpenAPI YAML (`/v3/api-docs.yaml`) includes wrapper schemas
with vendor extensions marking nested generic response envelopes:

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

These fields are added automatically by the `AutoWrapperSchemaCustomizer` and `ResponseTypeIntrospector`, allowing the
client generator to produce nested generics like:

```java
ServiceClientResponse<Page<CustomerDto>>
```

---

## ⚠️ Error Response Example

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

---

## 🐳 Run with Docker

Build and run container:

```bash
cd customer-service
mvn -q -DskipTests package
```

```bash
docker build -t customer-service:latest .
docker run --rm -p 8084:8084 \
  -e APP_PORT=8084 \
  -e SPRING_PROFILES_ACTIVE=local \
  customer-service:latest
```

### Using Docker Compose

```bash
cd customer-service
docker compose up --build -d
```

```bash
docker compose down
```

---

## 🥺 Testing

Run unit and integration tests:

```bash
cd customer-service
mvn test
```

---

## 🖖 Notes

* Demonstrates **generic `ServiceResponse<T>`** and nested `ServiceResponse<Page<T>>` patterns.
* Acts as the **API producer** for the generated client.
* Uses **Swagger customizers** (`AutoWrapperSchemaCustomizer`, `GlobalErrorResponsesCustomizer`).
* Auto-registers wrapper schemas and adds container/item hints via vendor extensions.
* Implements **RFC 7807-compliant ProblemDetail** responses.
* Provides **unit and integration tests** for controller and error handler layers.
* Supports **annotation injection** for generated wrappers via `app.openapi.wrapper.class-extra-annotation`.

---

## 📦 Related Module

This service is the API producer for the generated client:

* [customer-service-client](../customer-service-client/README.md) — Java client generated from this service's OpenAPI
  spec, supporting nested generic wrappers and problem decoding.

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

# customer-service

[![Java](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.10-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](../LICENSE)
[![Build](https://github.com/bsayli/spring-boot-openapi-generics-clients/actions/workflows/build.yml/badge.svg)](https://github.com/bsayli/spring-boot-openapi-generics-clients/actions/workflows/build.yml)

---

## 🎯 Purpose

`customer-service` provides a **minimal but complete backend** that exposes CRUD endpoints for customers. Its primary
role in this repository is:

* To **serve as the API producer** that publishes an OpenAPI spec (`/v3/api-docs.yaml`).
* To **feed the `customer-service-client` module**, where the spec is consumed and turned into a type-safe client with
  generics-aware wrappers.
* To demonstrate how **Swagger customizers** can teach OpenAPI about generic and nested wrappers so that the generated
  client stays clean and DRY.

Think of this module as the **server-side anchor**: without it, the client module would have nothing to generate
against.

---

## 📊 Architecture at a Glance

```
[customer-service]  ── publishes ──>  /v3/api-docs.yaml (OpenAPI contract with x-api-wrapper & x-data-container)
        │
        └─ consumed by OpenAPI Generator (+ generics-aware templates)
                 │
                 └─> [customer-service-client]  (type-safe wrappers)
                          │
                          └─ used by consumer apps (your services)
```

### Explanation

* **customer-service** exposes an **enhanced OpenAPI contract** at `/v3/api-docs.yaml` (and Swagger UI).
  It auto-registers wrapper schemas (`ServiceResponse<T>`) using `OpenApiCustomizer` and `ResponseTypeIntrospector`,
  enriching the spec with vendor extensions:

    * `x-api-wrapper: true`
    * `x-api-wrapper-datatype: <T>`
    * `x-data-container: <Container>` (e.g. `Page`)
    * `x-data-item: <Item>` (e.g. `CustomerDto`)

* These allow OpenAPI Generator to produce **nested generic clients** such as:

  ```java
  class CustomerListResponse extends ServiceClientResponse<Page<CustomerDto>> {}
  ```

* **customer-service-client** uses custom Mustache templates that recognize these vendor extensions and generate thin,
  DRY wrappers without repeating boilerplate model definitions.

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

**Base URL Note:** All endpoints are prefixed with `/customer-service` as defined in `application.yml`.

Example full URL for listing customers:

```
http://localhost:8084/customer-service/v1/customers
```

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

🤙 The YAML/JSON spec above is the **contract** that the client module (`customer-service-client`) consumes when
generating code.

🔙 For clarity, in this repository it is saved under the client module as: `src/main/resources/customer-api-docs.yaml`

---

### Example Wrapper Snippet

The generated OpenAPI YAML (`/v3/api-docs.yaml`) includes wrapper schemas
with vendor extensions that mark nested generic response envelopes:

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

🔙 These `x-api-wrapper`, `x-data-container`, and `x-data-item` fields are added automatically by the
`AutoWrapperSchemaCustomizer` and `ResponseTypeIntrospector`, allowing the client generator to produce nested generics
like `ServiceClientResponse<Page<CustomerDto>>`.

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

## 📖 Notes

* Demonstrates **generic `ServiceResponse<T>`** and nested `ServiceResponse<Page<T>>` pattern.
* Acts as the **API producer** for the generated client.
* Uses **Swagger customizers** (`AutoWrapperSchemaCustomizer`, `GlobalErrorResponsesCustomizer`) to mark wrappers for
  OpenAPI.
* Auto-registers **wrapper schemas** and adds container/item hints via vendor extensions.
* OpenAPI spec (`/v3/api-docs.yaml`) is the input for client generation.
* Includes **exception handling** with detailed `ProblemDetail` responses.
* Provides **unit tests** for controller and error handler layers.
* Supports **optional annotation injection** on generated wrappers via `app.openapi.wrapper.class-extra-annotation`.

---

## 📦 Related Module

This service is the API producer for the generated client:

* [customer-service-client](../customer-service-client/README.md) — Java client generated from this service's OpenAPI
  spec, supporting nested generic wrappers and problem decoding.

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!
Feel free to [open an issue](../../issues) or submit a PR.

---

## 🛡 License

This repository is licensed under **MIT** (root `LICENSE`). Submodules inherit the license.

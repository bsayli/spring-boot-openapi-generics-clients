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
* To demonstrate how **Swagger customizers** can teach OpenAPI about generic wrappers so that the generated client stays
  clean and DRY.

Think of this module as the **server-side anchor**: without it, the client module would have nothing to generate
against.

---

## 📊 Architecture at a Glance

```
Client
   │
   ▼
Customer Service
   │
   ▼
OpenAPI Spec (YAML/JSON)
   │
   ▼
Customer Service Client
```

This module defines the contract; the client module consumes it.

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

**Expected response (shape):**

```json
{
  "status": 201,
  "message": "CREATED",
  "data": {
    "customer": {
      "customerId": 1,
      "name": "Jane Doe",
      "email": "jane@example.com"
    },
    "createdAt": "2025-01-01T12:34:56Z"
  },
  "errors": []
}
```

---

## 📚 CRUD Endpoints

| Method | Path                         | Description         | Returns                  |
|--------|------------------------------|---------------------|--------------------------|
| POST   | `/v1/customers`              | Create new customer | `CustomerCreateResponse` |
| GET    | `/v1/customers/{customerId}` | Get single customer | `CustomerDto`            |
| GET    | `/v1/customers`              | List all customers  | `CustomerListResponse`   |
| PUT    | `/v1/customers/{customerId}` | Update customer     | `CustomerUpdateResponse` |
| DELETE | `/v1/customers/{customerId}` | Delete customer     | `CustomerDeleteResponse` |

**Base URL Note:** All endpoints are prefixed with `/customer-service` as defined in `application.yml`.

Example full URL for listing customers:

```
http://localhost:8084/customer-service/v1/customers
```

### Example Response: Get Customer

```json
{
  "status": 200,
  "message": "OK",
  "data": {
    "customerId": 1,
    "name": "Jane Doe",
    "email": "jane@example.com"
  },
  "errors": []
}
```

### Example Response: List Customers

```json
{
  "status": 200,
  "message": "OK",
  "data": [
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
  "errors": []
}
```

---

## 🔗 OpenAPI Endpoints

* Swagger UI → `http://localhost:8084/customer-service/swagger-ui/index.html`
* OpenAPI JSON → `http://localhost:8084/customer-service/v3/api-docs`
* OpenAPI YAML → `http://localhost:8084/customer-service/v3/api-docs.yaml`

➡️ The YAML/JSON spec above is the **contract** that the client module (`customer-service-client`) consumes when
generating code.

---

## ⚠️ Error Response Example

If a resource is missing:

```bash
curl -X GET "http://localhost:8084/customer-service/v1/customers/999"
```

**Response:**

```json
{
  "status": 404,
  "message": "NOT_FOUND",
  "data": {
    "code": "NOT_FOUND",
    "message": "Customer not found: 999",
    "timestamp": "2025-01-01T12:45:00Z",
    "violations": []
  },
  "errors": []
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

## 🧪 Testing

Run unit and integration tests:

```bash
cd customer-service
mvn test
```

---

## 📖 Notes

* Demonstrates **generic `ServiceResponse<T>`** pattern.
* Acts as the **API producer** for the generated client.
* Uses **Swagger customizers** to mark wrappers for OpenAPI.
* Auto-registers **wrapper schemas** in OpenAPI using `OpenApiCustomizer` and `ResponseTypeIntrospector` (adds
  `x-api-wrapper` vendor extensions).
* OpenAPI spec (`/v3/api-docs.yaml`) is the input for client generation.
* Includes **exception handling via `CustomerControllerAdvice`**.
* Provides **unit tests** for both controller and service layers.
* Profiles: `local` (default) and `dev` available — can be extended per environment.
* Focused on clarity and minimal setup.
* Optional: You can attach extra annotations (e.g., Jackson) to generated wrapper classes by setting  
  `app.openapi.wrapper.class-extra-annotation` in `application.yml`.  
  See [customer-service-client README](../customer-service-client/README.md#-optional-extra-class-annotations) for
  details.

---

## 📦 Related Module

This service is the API producer for the generated client:

* [customer-service-client](../customer-service-client/README.md) — Java client generated from this service's OpenAPI
  spec.

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!
Feel free to [open an issue](../../issues) or submit a PR.

---

## 🛡 License

MIT

# customer-service

Sample Spring Boot 3.4 microservice demonstrating **type-safe generic API responses** with OpenAPI.

This module is part of the parent repo: `spring-boot-openapi-generics-clients`.

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

---

## 🔗 OpenAPI Endpoints

* Swagger UI → `http://localhost:8084/customer-service/swagger-ui/index.html`
* OpenAPI JSON → `http://localhost:8084/customer-service/v3/api-docs`
* OpenAPI YAML → `http://localhost:8084/customer-service/v3/api-docs.yaml`

➡️ The YAML/JSON spec above is what the client module (`customer-service-client`) consumes when generating code.

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

## 📖 Notes

* Demonstrates **generic `ServiceResponse<T>`** pattern.
* Uses **Swagger customizers** to teach OpenAPI about generic wrappers.
* OpenAPI spec (`/v3/api-docs.yaml`) is the source for client generation.
* Includes **exception handling via `CustomerControllerAdvice`**.
* Provides **unit tests** for both controller and service layers.
* Focused on clarity and minimal setup for demo purposes.

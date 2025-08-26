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

Service runs at: `http://localhost:8084/customer`

### Create Customer (example)

```bash
curl -X POST "http://localhost:8084/customer/v1/customers" \
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
      "id": 1,
      "name": "Jane Doe",
      "email": "jane@example.com"
    },
    "createdAt": "2025-01-01T12:34:56Z"
  },
  "errors": []
}
```

---

## 🔗 OpenAPI Endpoints

* Swagger UI → `http://localhost:8084/customer/swagger-ui/index.html`
* OpenAPI JSON → `http://localhost:8084/customer/v3/api-docs`
* OpenAPI YAML → `http://localhost:8084/customer/v3/api-docs.yaml`

➡️ The YAML/JSON spec above is what the client module (`customer-service-client`) consumes when generating code.

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

* Demonstrates **generic `ApiResponse<T>`** pattern.
* Uses **Swagger customizers** to teach OpenAPI about generic wrappers.
* OpenAPI spec (`/v3/api-docs.yaml`) is the source for client generation.
* Focused on clarity and minimal setup for demo purposes.

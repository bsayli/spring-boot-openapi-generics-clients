---

## 🚀 Start here (what you actually want)

You have a Spring Boot service.

You want this outcome:

* OpenAPI is clean and stable
* Client generation is type-safe
* `ServiceResponse<T>` is preserved (not flattened)

Do this:

### 1. Add ONE dependency

```xml
<dependency>
  <groupId>io.github.bsayli</groupId>
  <artifactId>openapi-generics-server-starter</artifactId>
</dependency>
```

That’s it.

---

## ⚠️ Rules (do NOT break these)

### 1. Only constrain the envelope — payload is yours

You must use **only these envelope shapes**:

```text
ServiceResponse<T>
ServiceResponse<Page<T>>
```

**Important:**

* `T` is completely free
* It represents YOUR domain model
* It can be anything: DTO, record, response object, etc.

Examples (all valid):

```text
ServiceResponse<CustomerDto>
ServiceResponse<OrderDto>
ServiceResponse<AnythingYouDefine>
ServiceResponse<Page<AnythingYouDefine>>
```

👉 The platform only cares about the **outer contract (ServiceResponse / Page)**
👉 It does NOT care what `T` is

---

### 2. Do NOT replace the envelope (this is the only restriction)

What is forbidden is NOT your DTO naming.

What is forbidden is replacing the **envelope abstraction itself**.

❌ Wrong (custom envelope types):

```text
CustomerResponse
PagedCustomerResponse
ApiResponse
BaseResponse
```

These break:

* contract symmetry
* OpenAPI determinism
* client generation

---

### ✅ What is absolutely fine (and expected)

You can define ANY domain models:

```text
CustomerDto
CustomerResponse   ← perfectly fine as a DOMAIN object
OrderResult
Anything
```

And use them like:

```text
ServiceResponse<CustomerResponse>
```

✔ No problem
✔ This is correct usage

---

### 🔑 Mental model (critical)

```text
YOU own:        T (your domain / DTOs)
PLATFORM owns:  envelope (ServiceResponse, Page)
```

If you don’t touch the envelope:

> You are free to design your API however you want

---

### 3. Do NOT wrap errors

Errors must be:

```text
ProblemDetail (RFC 9457)
```

Never:

```text
ServiceResponse<Error>
```

---

### 4. Do NOT customize OpenAPI manually

No annotations.
No schema hacks.
No manual overrides.

Everything is handled by the starter.

---

## 🧠 What is happening under the hood (short version)

```text
Controller
   ↓
ServiceResponse<T>
   ↓
openapi-generics-server-starter
   ↓
Deterministic OpenAPI (+ vendor extensions)
```

This is what enables correct client generation.

---

## 🔄 Full pipeline (important)

```text
THIS MODULE (producer)
   ↓
OpenAPI spec
   ↓
openapi-generics-java-codegen-parent
   ↓
Generated client
```

---

## 🧪 Verify quickly

```bash
curl http://localhost:8084/customer-service/v1/customers/1
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

If this shape is correct → everything downstream will work.

---

## 🌐 OpenAPI endpoints

* Swagger UI
  [http://localhost:8084/customer-service/swagger-ui/index.html](http://localhost:8084/customer-service/swagger-ui/index.html)

* OpenAPI YAML
  [http://localhost:8084/customer-service/v3/api-docs.yaml](http://localhost:8084/customer-service/v3/api-docs.yaml)

---

## 🚫 What this project is NOT

* not a framework
* not a reusable starter template
* not a production system

It is only:

> A minimal, correct reference for contract-first API exposure

---

## 🛡️ License

MIT License

# spring-boot-openapi-generics-clients

**Type-safe client generation with Spring Boot & OpenAPI using generics.**
This repository demonstrates how to teach OpenAPI Generator to work with generics in order to avoid boilerplate, reduce duplicated wrappers, and keep client code clean.

---

## 🚀 Problem Statement

Most backend teams standardize responses with a generic wrapper like `ApiResponse<T>`.
However, **OpenAPI Generator does not natively support generics** — instead, it generates one wrapper per endpoint (duplicating fields like `status`, `message`, and `errors`).
This creates:

* ❌ Dozens of almost-identical classes
* ❌ High maintenance overhead
* ❌ No single place to evolve the response envelope

---

## 💡 Solution

This project shows how to:

* Customize **Springdoc** to mark wrapper schemas in OpenAPI
* Add a **tiny Mustache partial** to make the generator emit thin shells extending a reusable generic base
* Keep **compile-time type safety** without repetitive mappers

---

## 📂 Project Structure

```text
spring-boot-openapi-generics-clients/
 ├── customer-service/          # Sample Spring Boot microservice (API producer)
 ├── customer-service-client/   # Generated client using custom templates
 └── README.md
```

---

## 🧩 Key Features

* ✅ **Generic base model**: `ApiClientResponse<T>`
* ✅ **Thin wrappers**: endpoint-specific shells extending the base
* ✅ **Strong typing preserved**: `getData()` returns the exact payload type
* ✅ **No duplicated fields** across wrappers
* ✅ Easy to maintain and evolve

---

## 🔧 How to Run

1. **Start the sample service**

   ```bash
   cd customer-service
   mvn spring-boot:run
   ```

2. **Generate the client**

   ```bash
   cd customer-service-client
   mvn clean install
   ```

3. **Use the generated API**

   ```java
   ApiClientResponse<CustomerCreateResponse> response =
       customerControllerApi.create(request);
   ```

---

## 📖 Related Article

This repository is based on my article:
👉 [Type-Safe Generic API Responses with Spring Boot 3.4, OpenAPI Generator, and Custom Templates](https://medium.com/@baris.sayli/type-safe-generic-api-responses-with-spring-boot-3-4-openapi-generator-and-custom-templates-ccd93405fb04)

---

## 🛡 License

MIT

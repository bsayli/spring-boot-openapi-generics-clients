# spring-boot-openapi-generics-clients

[![Build](https://github.com/bsayli/spring-boot-openapi-generics-clients/actions/workflows/build.yml/badge.svg)](https://github.com/bsayli/spring-boot-openapi-generics-clients/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/bsayli/spring-boot-openapi-generics-clients?logo=github&label=release)](https://github.com/bsayli/spring-boot-openapi-generics-clients/releases/latest)
[![codecov](https://codecov.io/gh/bsayli/spring-boot-openapi-generics-clients/branch/main/graph/badge.svg)](https://codecov.io/gh/bsayli/spring-boot-openapi-generics-clients)
[![Java](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.10-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![OpenAPI Generator](https://img.shields.io/badge/OpenAPI%20Generator-7.16.0-blue?logo=openapiinitiative)](https://openapi-generator.tech/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

<p align="center">
  <img src="docs/images/social-preview.png" alt="Social preview" width="720"/>
  <br/>
  <em>Type-safe API responses without boilerplate — powered by Spring Boot & OpenAPI Generator</em>
</p>

**Type-safe client generation with Spring Boot & OpenAPI using generics.**
This repository demonstrates how to extend OpenAPI Generator to work with generics in order to avoid boilerplate, reduce
duplicated wrappers, and keep client code clean.

---

## 📑 Table of Contents

- 📦 [Modules](#-modules-in-this-repository)
- 🛠 [Compatibility Matrix](#-compatibility-matrix)
- 🚀 [Problem Statement](#-problem-statement)
- 💡 [Solution](#-solution)
- ⚡  [Quick Start](#-quick-start)
- 🧩 [Tech Stack](#-tech-stack--features)
- ✅ [Key Features](#-key-features)
- ✨ [Usage Example](#-usage-example-adapter-interface)
- 📦 [Related Modules](#-related-modules-quick-view)
- 📘 [Adoption Guides](#-adoption-guides)
- 🔗 [References & Links](#-references--links)

### 📦 Modules in this Repository

This repository consists of two main modules:

- [**customer-service**](customer-service/README.md) — Sample API producer (Spring Boot microservice + OpenAPI spec)
- [**customer-service-client**](customer-service-client/README.md) — Generated Java client (generics support via custom
  templates)

---

### 🔧 Compatibility Matrix

| Component               | Version |
|-------------------------|---------|
| **Java**                | 21      |
| **Spring Boot**         | 3.4.10  |
| **Springdoc OpenAPI**   | 2.8.13  |
| **OpenAPI Generator**   | 7.16.0  |
| **Apache HttpClient 5** | 5.5     |

---

## 🚀 Problem Statement

Most backend teams standardize responses with a generic wrapper like `ServiceResponse<T>`.
However, **OpenAPI Generator does not natively support generics** — instead, it generates one wrapper per endpoint (
duplicating fields like `status`, `message`, and `errors`).

This creates:

* ❌ Dozens of almost-identical classes
* ❌ High maintenance overhead
* ❌ No single place to evolve the response envelope

---

## 💡 Solution

This project shows how to:

* Customize **Springdoc** to mark wrapper schemas in OpenAPI
* Add a **tiny Mustache partial** so the generator emits thin shells extending a reusable generic base
* Keep **compile-time type safety** without repetitive mappers

---

### How it works (under the hood)

At generation time, the reference service **auto-registers** wrapper schemas in the OpenAPI doc:

* A Spring `OpenApiCustomizer` scans controller return types and unwraps `ResponseEntity`, `CompletionStage`, Reactor (
  `Mono`/`Flux`), etc. until it reaches `ServiceResponse<T>`.
* For every discovered `T`, it adds a `ServiceResponse{T}` schema that composes the base envelope + the concrete `data`
  type, and marks it with vendor extensions:

    * `x-api-wrapper: true`
    * `x-api-wrapper-datatype: <T>`

The Java client then uses a tiny Mustache override to render **thin shells** for those marked schemas:

```mustache
// api_wrapper.mustache
import {{commonPackage}}.ServiceClientResponse;

public class {{classname}}
    extends ServiceClientResponse<{{vendorExtensions.x-api-wrapper-datatype}}> {
}
```

This is what turns e.g. `ServiceResponseCustomerCreateResponse` into:

```java
public class ServiceResponseCustomerCreateResponse
        extends ServiceClientResponse<CustomerCreateResponse> {
}
```

---

## ⚡ Quick Start

Run the reference service:

```bash
cd customer-service
mvn spring-boot:run
```

Generate and build the client:

```bash
cd customer-service-client
mvn clean install
```

Use the generated API:

```java
ServiceClientResponse<CustomerCreateResponse> response =
        customerControllerApi.createCustomer(request);
```

### 🖼 Swagger Screenshot

Here’s what the `create customer` endpoint looks like in Swagger UI after running the service:

![Customer create example](docs/images/swagger-customer-create.png)

### 🖼 Generated Client Wrapper

Comparison of how OpenAPI Generator outputs looked **before** vs **after** adding the generics-aware wrapper:

**Before (duplicated full model):**

![Generated client (before)](docs/images/generated-client-wrapper-before.png)

**After (thin generic wrapper):**

![Generated client (after)](docs/images/generated-client-wrapper-after.png)

---

## ✅ Verify in 60 Seconds

1. Clone this repo
2. Run `mvn clean install -q`
3. Open `customer-service-client/target/generated-sources/...`
4. See the generated wrappers → they now extend a **generic base class** instead of duplicating fields.

You don’t need to write a single line of code — the generator does the work.

---

## 🛠 Tech Stack & Features

* 🚀 **Java 21** — modern language features
* 🍃 **Spring Boot 3.4.10** — microservice foundation
* 📖 **Springdoc OpenAPI** — API documentation
* 🔧 **OpenAPI Generator 7.x** — client code generation
* 🧩 **Custom Mustache templates** — generics-aware wrappers
* 🧪 **JUnit 5 + MockWebServer** — integration testing
* 🌐 **Apache HttpClient 5** — connection pooling & timeouts

---

## 📦 Next Steps: Dependency-based Adoption

The long-term goal is to publish the core pieces as standalone modules, so that any project using
a generic response type like `ServiceResponse<T>` can enable the same behavior with **just one dependency**:

- `io.github.bsayli:openapi-generics-autoreg` → **server-side**: automatically registers wrapper schemas in the OpenAPI
  spec.
- `io.github.bsayli:openapi-generics-templates` → **client-side**: plugs into OpenAPI Generator for thin, type-safe
  wrappers.

This will let teams adopt **generics-aware OpenAPI support** without copying customizers or Mustache templates —
just by adding a Maven/Gradle dependency.

---

## 📂 Project Structure

```text
spring-boot-openapi-generics-clients/
 ├── customer-service/          # Sample Spring Boot microservice (API producer)
 ├── customer-service-client/   # Generated client using custom templates
 └── README.md                  # Root documentation
```

---

## 🧩 Key Features

* ✅ **Generic base model**: `ServiceClientResponse<T>`
* ✅ **Thin wrappers**: endpoint-specific shells extending the base
* ✅ **Strong typing preserved**: `getData()` returns the exact payload type
* ✅ **No duplicated fields** across wrappers
* ✅ Easy to maintain and evolve

---

### ✨ Usage Example: Adapter Interface

Sometimes you don’t want to expose all the thin wrappers directly.
A simple adapter interface can consolidate them into clean, type-safe methods:

```java
public interface CustomerClientAdapter {
    ServiceClientResponse<CustomerCreateResponse> createCustomer(CustomerCreateRequest request);

    ServiceClientResponse<CustomerDto> getCustomer(Integer customerId);

    ServiceClientResponse<CustomerListResponse> getCustomers();

    ServiceClientResponse<CustomerUpdateResponse> updateCustomer(
            Integer customerId, CustomerUpdateRequest request);

    ServiceClientResponse<CustomerDeleteResponse> deleteCustomer(Integer customerId);
}
```

---

## 🔍 Why This Matters

Without generics support, OpenAPI client generation creates bloated and repetitive code.
By applying this approach:

* Development teams **save time** maintaining response models
* Client libraries become **cleaner and smaller**
* Easier for **new developers** to understand the contract
* Code stays **future-proof** when envelope fields evolve

---

## 💼 Use Cases

This pattern is useful when:

* You have **multiple microservices** with a shared response structure
* You need to **evolve response envelopes** without breaking dozens of generated classes
* You want **type safety** in generated clients but without boilerplate

---

## 🔧 How to Run

1. **Start the reference service**

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
   ServiceClientResponse<CustomerCreateResponse> response =
       customerControllerApi.createCustomer(request);
   ```

---

## 👤 Who Should Use This?

* Backend developers maintaining multiple microservices
* API platform teams standardizing response envelopes
* Teams already invested in OpenAPI Generator looking to reduce boilerplate

---

## ⚠️ Why Not Use It?

This project may not be the right fit if:

* Your APIs do **not** use a common response wrapper
* You are fine with duplicated wrapper models
* You don’t generate client code from OpenAPI specs

---

## 📂 References & Links

- 📘 [Medium Article — Type-Safe Generic API Responses](https://medium.com/@baris.sayli/type-safe-generic-api-responses-with-spring-boot-3-4-openapi-generator-and-custom-templates-ccd93405fb04)
- 🌐 [GitHub Pages (Adoption Guides)](https://bsayli.github.io/spring-boot-openapi-generics-clients/)

---

## 🛡 License

This repository is licensed under **MIT** (see [LICENSE](LICENSE)). Submodules inherit the license.

---

✅ **Note:** CLI examples should always be provided **on a single line**.
If parameters include spaces or special characters, wrap them in quotes `"..."`.

---

## 💬 Feedback

If you spot any mistakes in this README or have questions about the project, feel free to open an issue or start a
discussion. I’m happy to improve the documentation and clarify concepts further!

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!  
Feel free to [open an issue](../../issues) or submit a PR.

---

## ⭐ Support

If you found this project useful, please consider giving it a star ⭐ on GitHub — it helps others discover it too!

---

## 📦 Related Modules (Quick View)

| Module                         | Description                                 | Docs                                        |
|--------------------------------|---------------------------------------------|---------------------------------------------|
| 🟢 **customer-service**        | Spring Boot sample API (producer)           | [README](customer-service/README.md)        |
| 🔵 **customer-service-client** | Generated Java client with generics support | [README](customer-service-client/README.md) |

---

## 📘 Adoption Guides

Looking to integrate this approach into your own project?  
See the detailed guides under [`docs/adoption`](docs/adoption):

- [Server-Side Adoption](docs/adoption/server-side-adoption.md)
- [Client-Side Adoption](docs/adoption/client-side-adoption.md)
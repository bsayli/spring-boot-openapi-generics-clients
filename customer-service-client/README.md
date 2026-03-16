# customer-service-client

[![Java 21](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.11-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![OpenAPI Generator](https://img.shields.io/badge/OpenAPI%20Generator-7.20.0-blue?logo=openapiinitiative)](https://openapi-generator.tech/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](../LICENSE)

---

## 📑 Table of Contents

- 🎯 [Purpose](#-purpose)
- 🔧 [TL;DR — Generate in 1 Minute](#-tldr--generate-in-1-minute)
   - ✅ [What this gives you](#-what-this-gives-you)
- ✅ [What You Get](#-what-you-get)
- 🧩 [How Thin Wrappers Are Produced](#-how-thin-wrappers-are-produced)
   - [Vendor Extensions Used](#vendor-extensions-used)
   - [Template (excerpt)](#template-excerpt)
   - [Generated Examples](#generated-examples)
- 📦 [Core Concepts](#-core-concepts)
   - [Canonical Response Contract](#canonical-response-contract)
   - [Page Semantics](#page-semantics)
- ⚠️ [Error Handling (RFC 9457)](#-error-handling-rfc-9457)
- ⚙️ [Spring Configuration (Production-Ready)](#-spring-configuration-production-ready)
   - [Defaults](#defaults)
- 🧠 [Adapter Pattern](#-adapter-pattern)
- 🧹 [Ignoring Redundant Generated DTOs](#-ignoring-redundant-generated-dtos)
- 📘 [Summary](#-summary)
- 🛡 [License](#-license)

---

## 🎯 Purpose

`customer-service-client` is a **thin, type-safe Java client** generated from the OpenAPI contract exposed by
[`customer-service`](../customer-service/README.md).

It demonstrates how a client can:

* Reuse the **same canonical response contract** (`ServiceResponse<T>`) as the server
  via the shared **`api-contract`** module.
* Preserve **generic typing** — including **Page-only nested generics** — without duplicating models.
* Decode non-2xx responses into **RFC 9457** `ProblemDetail` objects and surface them as domain-appropriate exceptions.

This module intentionally contains **no business logic** and **no duplicated contracts**.
It exists solely to adapt the generated OpenAPI client into a clean, Spring-friendly API.

---

## 🔧 TL;DR — Generate in 1 Minute

> ℹ️ **Dependency note**
> The shared `api-contract` library is published to Maven Central (`io.github.bsayli:api-contract:0.7.7`).
> Client generation and compilation therefore do not require a root-level bootstrap build.
> You can work with the modules independently, as in a typical multi-repository setup.
>

---

```bash
# 1) Start the customer-service (API producer)
cd customer-service
mvn -q spring-boot:run
```

```bash
# 2) Pull the OpenAPI spec into the client module (in another shell)
cd ../customer-service-client
curl -s http://localhost:8084/customer-service/v3/api-docs.yaml \
  -o src/main/resources/customer-api-docs.yaml
```

```bash
# 3) Generate & compile the client
mvn -q clean install
```

*Generated sources → `target/generated-sources/openapi/src/gen/java`*

ℹ️ Generated sources are automatically added to compilation via
`build-helper-maven-plugin`.

---

### ✅ What this gives you

* A running **customer-service** exposing an OpenAPI 3.1 spec
* A regenerated **customer-service-client** using the shared `api-contract`
* Thin, generics‑aware wrappers (`ServiceResponse<T>`, `ServiceResponse<Page<T>>`)
* No duplicated envelopes, no manual wiring


---

## ✅ What You Get

* **OpenAPI Generator 7.20.0** + **Spring `RestClient`**-based Java client.
* Canonical response envelope: **`ServiceResponse<T>`** (from `api-contract`).
* **Page-only nested generics** support: `ServiceResponse<Page<CustomerDto>>`.
* **RFC 9457 Problem decoding** surfaced as `ApiProblemException`.
* Production-ready HTTP setup (HttpClient5 pooling, timeouts, retries disabled).
* Adapter pattern shielding the rest of your application from generated code.

---

## 🧩 How Thin Wrappers Are Produced

The OpenAPI spec published by `customer-service` is enriched with vendor extensions that describe
**wrapper semantics**, not implementation details.

### Vendor Extensions Used

```
- x-api-wrapper: true
- x-api-wrapper-datatype: CustomerDto
- x-data-container: Page            # only for Page<T>
- x-data-item: CustomerDto           # only for Page<T>
- x-class-extra-annotation: (optional)
```

These hints allow a minimal Mustache overlay to generate **thin wrapper classes** that extend the
canonical contract type.

### Template (excerpt)

```mustache
import {{apiContractEnvelope}}.ServiceResponse;
{{#vendorExtensions.x-data-container}}
import {{apiContractPage}}.{{vendorExtensions.x-data-container}};
{{/vendorExtensions.x-data-container}}

{{#vendorExtensions.x-class-extra-annotation}}
{{{vendorExtensions.x-class-extra-annotation}}}
{{/vendorExtensions.x-class-extra-annotation}}
public class {{classname}} extends ServiceResponse<
{{#vendorExtensions.x-data-container}}
{{vendorExtensions.x-data-container}}<{{vendorExtensions.x-data-item}}>
{{/vendorExtensions.x-data-container}}
{{^vendorExtensions.x-data-container}}
{{vendorExtensions.x-api-wrapper-datatype}}
{{/vendorExtensions.x-data-container}}
> {
}
```

### Generated Examples

* `ServiceResponseCustomerDto`

  ```java
  extends ServiceResponse<CustomerDto>
  ```

* `ServiceResponsePageCustomerDto`

  ```java
  extends ServiceResponse<Page<CustomerDto>>
  ```

No response envelope is duplicated; everything delegates to `ServiceResponse<T>`.

---

## 📦 Core Concepts

### Canonical Response Contract

All successful responses use a **single, shared response contract**:

```java
ServiceResponse<T>
```

This contract is:

* Defined in **`io.github.bsayli:api-contract`**
* Reused **unchanged** by both server and client
* The sole owner of the `{ data, meta }` response shape

As a result, the response envelope is **never generated**, duplicated, or redefined by tooling.
It exists once, as an explicit contract, and is referenced everywhere else.

---

### Page Semantics

This setup **explicitly supports exactly one nested generic shape**:

```java
ServiceResponse<Page<T>>
```

This is the **only** nested generic treated as *contract-aware*.

What this means in practice:

* Pagination semantics are preserved end‑to‑end
* Schema names remain deterministic
* Generated clients bind generics without duplicating envelope fields

---

### Everything Else (Intentionally Out of Scope)

All other generic compositions — including but not limited to:

* `ServiceResponse<List<T>>`
* `ServiceResponse<Map<K,V>>`
* `ServiceResponse<Foo<Bar>>`

are **intentionally left to OpenAPI Generator's default behavior**.

They are:

* **Not** treated as contract‑aware
* **Not** given special schema naming rules
* **Not** enriched with wrapper‑specific vendor extensions

This is a deliberate design boundary.

By limiting guarantees to:

* `ServiceResponse<T>`
* `ServiceResponse<Page<T>>`

the contract remains:

* predictable
* generator‑safe
* stable over time

> The goal is not to support every possible generic shape —
> but to make the **supported ones boringly reliable**.

---

## ⚠️ Error Handling (RFC 9457)

Non-2xx responses are decoded into RFC 9457 `ProblemDetail` objects and thrown as
`ApiProblemException`.

```java
try {
    customerClientAdapter.getCustomer(999);
} catch (ApiProblemException ex) {
    ProblemDetail pd = ex.getProblem();

    log.warn(
        "Customer API error [status={}, code={}, title={}, detail={}]",
        ex.getStatus(),
        pd.getErrorCode(),
        pd.getTitle(),
        pd.getDetail()
    );
}
```

### Why an Exception?

* Keeps generated APIs clean (no `Either`, no wrapper leakage).
* Aligns with Spring’s error-handling model.
* Preserves the full RFC 9457 payload for diagnostics and logging.

---

## ⚙️ Spring Configuration (Production-Ready)

```java
@Configuration
public class CustomerApiClientConfig {

  @Bean
  RestClientCustomizer problemDetailStatusHandler(ObjectMapper om) {
    return builder ->
            builder.defaultStatusHandler(
                    HttpStatusCode::isError,
                    (request, response) -> {
                      ProblemDetail pd = ProblemDetailSupport.extract(om, response);
                      throw new ApiProblemException(pd, response.getStatusCode().value());
                    });
  }

  @Bean(destroyMethod = "close")
  CloseableHttpClient customerHttpClient(
          @Value("${customer.api.max-connections-total:64}") int maxTotal,
          @Value("${customer.api.max-connections-per-route:16}") int maxPerRoute) {

    var connectionManager =
            PoolingHttpClientConnectionManagerBuilder.create()
                    .setMaxConnTotal(maxTotal)
                    .setMaxConnPerRoute(maxPerRoute)
                    .build();

    return HttpClients.custom()
            .setConnectionManager(connectionManager)
            .evictExpiredConnections()
            .evictIdleConnections(TimeValue.ofSeconds(30))
            .setUserAgent("customer-service-client")
            .disableAutomaticRetries()
            .build();
  }
}
```

### Defaults

* Max connections (total / per-route): `64 / 16`
* Connect timeout: `10s`
* Read timeout: `15s`
* Retries: **disabled** (safe default)

---

## 🧠 Adapter Pattern

Generated APIs are never consumed directly by application code.
Instead, they are wrapped by a **stable adapter interface**.

```java
@Service
public class CustomerClientAdapterImpl implements CustomerClientAdapter {

  private final CustomerControllerApi api;

  public CustomerClientAdapterImpl(CustomerControllerApi api) {
    this.api = api;
  }

  @Override
  public ServiceResponse<Page<CustomerDto>> getCustomers(...) {
    return api.getCustomers(...);
  }
}
```

### Why This Matters

* Generated code can be regenerated or replaced at any time.
* Callers depend only on **your adapter API**, not OpenAPI internals.
* Enables painless evolution of templates and generator versions.

---

## 🧹 Ignoring Redundant Generated DTOs

The following patterns in `.openapi-generator-ignore` prevent duplication of
contracts already provided by `api-contract`:

```bash
**/src/gen/java/**/generated/dto/Page*.java
**/src/gen/java/**/generated/dto/ServiceResponse.java
**/src/gen/java/**/generated/dto/ServiceResponseVoid.java
**/src/gen/java/**/generated/dto/Meta.java
**/src/gen/java/**/generated/dto/Sort.java
```

This guarantees **one contract, one source of truth**.

---

## 📘 Summary

* **Single response contract**: `ServiceResponse<T>`
* **Explicit generic scope**: nested generics are supported only for `Page<T>`
* **No duplication** between server and client
* **RFC 9457-first** error handling
* Generated code isolated behind adapters

This client is not a demo — it is a **reference implementation** for
contract-driven, generics-aware OpenAPI client generation.

---

## 🛡 License

Licensed under the **MIT License** (see root `LICENSE`).
All submodules inherit this license.

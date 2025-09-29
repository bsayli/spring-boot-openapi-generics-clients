# customer-service-client

[![Java 21](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.10-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![OpenAPI Generator](https://img.shields.io/badge/OpenAPI%20Generator-7.16.0-blue?logo=openapiinitiative)](https://openapi-generator.tech/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](../LICENSE)

Generated Java client for the **customer-service**, showcasing **type-safe generic responses** with OpenAPI + a
custom Mustache template (wrapping payloads in a reusable `ServiceClientResponse<T>`).

This module demonstrates how to evolve OpenAPI Generator with minimal customization to support generic response
envelopes — avoiding duplicated wrappers and preserving strong typing.

---

## 🔧 TL;DR: Generate in 1 minute

```bash
# 1) Start the customer service server (in another shell)
cd customer-service && mvn -q spring-boot:run

# 2) Pull the OpenAPI spec into the client module
cd ../customer-service-client
curl -s http://localhost:8084/customer-service/v3/api-docs.yaml \
  -o src/main/resources/customer-api-docs.yaml

# 3) Generate & compile the client
mvn -q clean install
```

Generated sources → `target/generated-sources/openapi/src/gen/java`

---

## ✅ What You Get

* Generated code using **OpenAPI Generator** (`restclient` with Spring Framework `RestClient`).
* A reusable generic base: `io.github.bsayli.openapi.client.common.ServiceClientResponse<T>`.
* Thin wrappers per endpoint (e.g. `ServiceResponseCustomerCreateResponse`, `ServiceResponseCustomerUpdateResponse`).
* Spring Boot configuration to auto-expose the client as beans.
* Focused integration tests using **OkHttp MockWebServer** covering all CRUD endpoints.

---

## 🚀 Quick Pipeline (3 Steps)

1. **Run the sample service**

```bash
cd customer-service
mvn spring-boot:run
# Service base URL: http://localhost:8084/customer-service
```

2. **Pull the OpenAPI spec into this module**

```bash
cd customer-service-client
curl -s http://localhost:8084/customer-service/v3/api-docs.yaml \
  -o src/main/resources/customer-api-docs.yaml
```

3. **Generate & build the client**

```bash
mvn clean install
```

### What got generated?

Look for these classes under `target/generated-sources/openapi/src/gen/java`:

* `io.github.bsayli.openapi.client.generated.dto.ServiceResponseCustomerCreateResponse`
* `...ServiceResponseCustomerUpdateResponse`, etc.

Each is a **thin shell** extending `ServiceClientResponse<PayloadType>`.

---

## 🚫 Not a Published Library (Re-generate in your project)

This module is a **reference demo**, not a published library.
To apply the same approach in your own project:

1. Generate your own OpenAPI spec (`/v3/api-docs.yaml`).
2. Copy the two Mustache templates (`api_wrapper.mustache`, `model.mustache`) into your project.
3. Run OpenAPI Generator with your spec + templates → you’ll get type-safe wrappers.

> ⚠️ **Do not add `customer-service-client` as a Maven/Gradle dependency in your project.**
> Instead, re-generate your own client using **your service’s OpenAPI spec** and the provided Mustache templates.

---

## 📦 Prerequisites

Before generating or using the client, make sure you have:

* **Java 21** or newer
* **Maven 3.9+** (or Gradle 8+ if you adapt the build)
* A running instance of the `customer-service` exposing its OpenAPI spec

---

## 🎯 Scope & Non-Goals

This module focuses on **generics-aware client generation** only. Specifically, it demonstrates:

* Marking wrapper schemas via OpenAPI **vendor extensions** (e.g., `x-api-wrapper`, `x-api-wrapper-datatype`)
* A tiny **Mustache overlay** that emits **thin wrapper classes** extending a reusable `ServiceClientResponse<T>`
* How those wrappers enable **compile-time type safety** in consumer code (see the adapter example)

**Out of scope (non-goals):**

* Runtime concerns such as error handling strategies for non-2xx responses, retries, logging, metrics, circuit-breaking
* Business validation, pagination conventions, or API design guidelines
* Authentication/authorization configuration
* Packaging and publishing this client as a reusable library

If you need those capabilities, add them in your host application or platform code. This repo is intentionally minimal
to keep the focus on the **wrapper generation pattern**.

---

## 🔄 How thin wrappers are produced (end-to-end flow)

```
Controller returns `ServiceResponse<T>`
        │
        ▼
Springdoc `OpenApiCustomizer` discovers `T` and marks wrapper schemas
(vendor extensions: `x-api-wrapper: true`, `x-api-wrapper-datatype: <T>`)
        │
        ▼
OpenAPI spec (YAML/JSON) contains `ServiceResponse{T}` schemas with vendor extensions
        │
        ▼
OpenAPI Generator runs with a tiny Mustache overlay
(`api_wrapper.mustache`, `model.mustache`)
        │
        ▼
Generated Java classes:
- Thin wrappers like `ServiceResponseCustomerCreateResponse`
  extending `ServiceClientResponse<CustomerCreateResponse>`
- No duplicated envelope fields
        │
        ▼
Consumer code (e.g., adapter) gets compile-time type safety
```

**Key files**

* Templates: `src/main/resources/openapi-templates/api_wrapper.mustache`, `.../model.mustache`
* Generated output: `target/generated-sources/openapi/src/gen/java`
* Packages (from `pom.xml`): `apiPackage`, `modelPackage`, `invokerPackage`

---

## 🧰 Troubleshooting (quick)

* **No thin wrappers generated?**
  Check your spec contains vendor extensions on wrapper schemas (look for `x-api-wrapper: true`).
  Also verify the generator uses your overlay via `<templateDirectory>`.

* **Wrong packages or missing classes?**
  Ensure `apiPackage`, `modelPackage`, and `invokerPackage` in the plugin configuration match what you expect.
  Delete `target/` and re-run: `mvn clean install`.

* **Spec is stale?**
  Re-pull it:

  ```bash
  curl -s http://localhost:8084/customer-service/v3/api-docs.yaml \
    -o src/main/resources/customer-api-docs.yaml
  mvn -q clean install
  ```

* **Validation/annotations not found at runtime?**
  Some dependencies (e.g., `spring-web`, `jakarta.*`) are marked **provided**.
  Your host app must supply them on the classpath.

* **Base URL not applied?**
  If you use the Spring configuration, set `customer.api.base-url` correctly and ensure the `RestClient` bean is
  created.

---

## 🧩 Using the Client

### Option A — Spring Configuration (recommended)

```java

@Configuration
public class CustomerApiClientConfig {

    @Bean
    public RestClient customerRestClient(RestClient.Builder builder,
                                         @Value("${customer.api.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean
    public io.github.bsayli.openapi.client.generated.invoker.ApiClient customerApiClient(
            RestClient customerRestClient,
            @Value("${customer.api.base-url}") String baseUrl) {
        return new io.github.bsayli.openapi.client.generated.invoker.ApiClient(customerRestClient)
                .setBasePath(baseUrl);
    }

    @Bean
    public io.github.bsayli.openapi.client.generated.api.CustomerControllerApi customerControllerApi(
            io.github.bsayli.openapi.client.generated.invoker.ApiClient apiClient) {
        return new io.github.bsayli.openapi.client.generated.api.CustomerControllerApi(apiClient);
    }
}
```

**application.properties:**

```properties
customer.api.base-url=http://localhost:8084/customer-service
```

**Usage example:**

```java

@Autowired
private io.github.bsayli.openapi.client.generated.api.CustomerControllerApi customerApi;

public void createCustomer() {
    var req = new io.github.bsayli.openapi.client.generated.dto.CustomerCreateRequest()
            .name("Jane Doe")
            .email("jane@example.com");

    var resp = customerApi.createCustomer(req); // ServiceResponseCustomerCreateResponse

    System.out.println(resp.getStatus());                       // 201
    System.out.println(resp.getData().getCustomer().getName()); // "Jane Doe"
}
```

> Tip — The return type is strongly typed: `ServiceClientResponse<CustomerCreateResponse>`.
> You can safely navigate `resp.getData().getCustomer()` without casting.
> Handle non-2xx via Spring exceptions (e.g., `HttpClientErrorException`) as usual.

---

### Option A.2 — Alternative with HttpClient5 (connection pooling)

If you want more control (connection pooling, timeouts, etc.), you can wire the client with **Apache HttpClient5**:

```java

@Configuration
public class CustomerApiClientConfig {

    @Bean(destroyMethod = "close")
    CloseableHttpClient customerHttpClient(
            @Value("${customer.api.max-connections-total:64}") int maxTotal,
            @Value("${customer.api.max-connections-per-route:16}") int maxPerRoute) {

        var cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(maxTotal)
                .setMaxConnPerRoute(maxPerRoute)
                .build();

        return HttpClients.custom()
                .setConnectionManager(cm)
                .evictExpiredConnections()
                .evictIdleConnections(org.apache.hc.core5.util.TimeValue.ofSeconds(30))
                .setUserAgent("customer-service-client")
                .disableAutomaticRetries()
                .build();
    }

    @Bean
    HttpComponentsClientHttpRequestFactory customerRequestFactory(
            CloseableHttpClient customerHttpClient,
            @Value("${customer.api.connect-timeout-seconds:10}") long connect,
            @Value("${customer.api.connection-request-timeout-seconds:10}") long connReq,
            @Value("${customer.api.read-timeout-seconds:15}") long read) {

        var f = new HttpComponentsClientHttpRequestFactory(customerHttpClient);
        f.setConnectTimeout(Duration.ofSeconds(connect));
        f.setConnectionRequestTimeout(Duration.ofSeconds(connReq));
        f.setReadTimeout(Duration.ofSeconds(read));
        return f;
    }

    @Bean
    RestClient customerRestClient(RestClient.Builder builder,
                                  HttpComponentsClientHttpRequestFactory rf) {
        return builder.requestFactory(rf).build();
    }

    @Bean
    ApiClient customerApiClient(RestClient customerRestClient,
                                @Value("${customer.api.base-url}") String baseUrl) {
        return new ApiClient(customerRestClient).setBasePath(baseUrl);
    }

    @Bean
    CustomerControllerApi customerControllerApi(ApiClient apiClient) {
        return new CustomerControllerApi(apiClient);
    }
}
```

---

### Option B — Manual Wiring (no Spring context)

```java
var rest = RestClient.builder().baseUrl("http://localhost:8084/customer-service").build();
var apiClient = new io.github.bsayli.openapi.client.generated.invoker.ApiClient(rest)
        .setBasePath("http://localhost:8084/customer-service");
var customerApi = new io.github.bsayli.openapi.client.generated.api.CustomerControllerApi(apiClient);
```

---

## 🧩 Adapter Pattern Example

For larger applications, encapsulate the generated API in an adapter:

```java
package io.github.bsayli.openapi.client.adapter.impl;

import io.github.bsayli.openapi.client.adapter.CustomerClientAdapter;
import io.github.bsayli.openapi.client.common.ServiceClientResponse;
import io.github.bsayli.openapi.client.generated.api.CustomerControllerApi;
import io.github.bsayli.openapi.client.generated.dto.*;
import org.springframework.stereotype.Service;

@Service
public class CustomerClientAdapterImpl implements CustomerClientAdapter {

    private final CustomerControllerApi customerControllerApi;

    public CustomerClientAdapterImpl(CustomerControllerApi customerControllerApi) {
        this.customerControllerApi = customerControllerApi;
    }

    @Override
    public ServiceClientResponse<CustomerCreateResponse> createCustomer(CustomerCreateRequest request) {
        return customerControllerApi.createCustomer(request);
    }

    @Override
    public ServiceClientResponse<CustomerDto> getCustomer(Integer customerId) {
        return customerControllerApi.getCustomer(customerId);
    }

    @Override
    public ServiceClientResponse<CustomerListResponse> getCustomers() {
        return customerControllerApi.getCustomers();
    }

    @Override
    public ServiceClientResponse<CustomerUpdateResponse> updateCustomer(Integer customerId, CustomerUpdateRequest request) {
        return customerControllerApi.updateCustomer(customerId, request);
    }

    @Override
    public ServiceClientResponse<CustomerDeleteResponse> deleteCustomer(Integer customerId) {
        return customerControllerApi.deleteCustomer(customerId);
    }
}
```

This ensures:

* Generated code stays isolated.
* Business code depends only on the adapter interface.
* Naming conventions are consistent with the service (createCustomer, getCustomer, getCustomers, updateCustomer,
  deleteCustomer).

---

## 🧩 How the Generics Work

The template at `src/main/resources/openapi-templates/api_wrapper.mustache` emits wrappers like:

```java
import io.github.bsayli.openapi.client.common.ServiceClientResponse;

// e.g., ServiceResponseCustomerCreateResponse
public class ServiceResponseCustomerCreateResponse
        extends ServiceClientResponse<CustomerCreateResponse> {
}
```

Only this Mustache partial is customized. All other models use stock templates.

### Template overlay (Mustache)

This module overlays **two** tiny Mustache files on top of the stock Java generator:

* `src/main/resources/openapi-templates/api_wrapper.mustache`
* `src/main/resources/openapi-templates/model.mustache`

At build time, the Maven `maven-dependency-plugin` unpacks the upstream templates and the
`maven-resources-plugin` overlays the two local files. That’s what enables thin generic wrappers.

**Disable templates (optional):**
set `<templateDirectory>` to a non-existent path or comment the overlay steps in `pom.xml`
to compare stock output vs generic wrappers.

---

## 🧪 Tests

Integration test with MockWebServer:

```bash
mvn -q -DskipITs=false test
```

It enqueues responses for **all CRUD operations** and asserts correct mapping into the respective wrappers (e.g.
`ServiceResponseCustomerCreateResponse`, `ServiceResponseCustomerUpdateResponse`).

---

## 📚 Notes

* Dependencies like `spring-web`, `spring-context`, `jackson-*`, `jakarta.*` are marked **provided**; your host app
  supplies them.
* Generator options: Spring 6 `RestClient`, Jakarta EE, Jackson, Java 21.
* OpenAPI spec lives at: `src/main/resources/customer-api-docs.yaml`.
  If you re-run the server and want an updated client, re-pull the spec:

  ```bash
  curl -s http://localhost:8084/customer-service/v3/api-docs.yaml \
    -o src/main/resources/customer-api-docs.yaml
  mvn -q clean install
  ```

---

## 🛡 License

This repository is licensed under **MIT** (root `LICENSE`). Submodules inherit the license.

### Packaging note (optional)

This module is **reference-oriented**. If you want to publish it as a reusable library later:

* remove `provided` scopes and pin minimal runtime deps,
* add a semantic version and release process (e.g., GitHub Release + `mvn deploy` to Maven Central),
* keep the Mustache overlay in-repo for transparent builds.

---

## 📦 Related Module

This client is generated from the OpenAPI spec exposed by:

* [customer-service](../customer-service/README.md) — Sample Spring Boot microservice (API producer).

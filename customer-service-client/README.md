# customer-service-client

[![Java 21](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.11-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![OpenAPI Generator](https://img.shields.io/badge/OpenAPI%20Generator-7.17.0-blue?logo=openapiinitiative)](https://openapi-generator.tech/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](../LICENSE)

Generated Java client for the **customer-service**, showcasing **type‑safe generic responses** and **nested generics** with a minimal OpenAPI Generator Mustache overlay.
Successful responses are wrapped in a reusable envelope `ServiceClientResponse<T>`, while non‑2xx responses are decoded into **RFC 9457** (the successor to RFC 7807) `ProblemDetail` objects via a custom exception.

---

## 🔧 TL;DR — Generate in 1 Minute

```bash
# 1) Start the customer-service (in another shell)
cd customer-service && mvn -q spring-boot:run

# 2) Pull the OpenAPI spec into the client module
cd ../customer-service-client
curl -s http://localhost:8084/customer-service/v3/api-docs.yaml \
  -o src/main/resources/customer-api-docs.yaml

# 3) Generate & compile the client
mvn -q clean install
```

*Generated sources → `target/generated-sources/openapi/src/gen/java`*
ℹ️ **Multi-module builds:** Generated code path is automatically compiled via `build-helper-maven-plugin`.

---

## ✅ What You Get

* **OpenAPI Generator 7.17.0** + **Spring `RestClient`**-based Java client.
* Reusable generic base: `ServiceClientResponse<T>` containing `{ data, meta }`.
* **Nested generics** support: `ServiceClientResponse<Page<CustomerDto>>`.
* **RFC 9457 Problem decoding** via `ClientProblemException`.
* **Production-ready HTTP config:** pooled HttpClient5 + `RestClientCustomizer`.
* Adapter pattern for clean, type-safe service integration.

---

## 🧩 How the Thin Wrappers Are Produced

Vendor extensions mark wrappers in the OpenAPI spec; the Mustache overlay generates **thin wrappers** extending the shared generic base.

**Vendor extensions used:**

```
- x-api-wrapper: true
- x-api-wrapper-datatype: CustomerDto
- x-data-container: Page
- x-data-item: CustomerDto
- x-class-extra-annotation: (optional)
```

**Template snippet:**

```mustache
public class {{classname}} extends ServiceClientResponse<
  {{#vendorExtensions.x-data-container}}
    {{vendorExtensions.x-data-container}}<{{vendorExtensions.x-data-item}}>
  {{/vendorExtensions.x-data-container}}
  {{^vendorExtensions.x-data-container}}
    {{vendorExtensions.x-api-wrapper-datatype}}
  {{/vendorExtensions.x-data-container}}
> {}
```

**Generated examples:**

* `ServiceResponseCustomerDto` → `extends ServiceClientResponse<CustomerDto>`
* `ServiceResponsePageCustomerDto` → `extends ServiceClientResponse<Page<CustomerDto>>`

---

## 📦 Core Components

```java
public class ServiceClientResponse<T> {
    private T data;
    private ClientMeta meta;
}

public record ClientMeta(Instant serverTime, List<ClientSort> sort) {}

public record Page<T>(List<T> content, int page, int size,
                      long totalElements, int totalPages,
                      boolean hasNext, boolean hasPrev) {}
```

**Problem Exception (RFC 9457):**

```java
public class ClientProblemException extends RuntimeException {
    private final transient ProblemDetail problem;
    private final int status;
}
```

---

## ⚙️ Spring Configuration (Production-Ready)

```java
@Configuration
public class CustomerApiClientConfig {

   @Bean
   RestClientCustomizer problemDetailStatusHandler(ObjectMapper om) {
      return builder -> builder.defaultStatusHandler(
              HttpStatusCode::isError,
              (request, response) -> {
                 ProblemDetail pd = ProblemDetailSupport.extract(om, response);
                 throw new ClientProblemException(pd, response.getStatusCode().value());
              });
   }

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
    RestClient customerRestClient(RestClient.Builder builder,
                                  HttpComponentsClientHttpRequestFactory rf,
                                  List<RestClientCustomizer> customizers) {
        builder.requestFactory(rf);
        if (customizers != null) customizers.forEach(c -> c.customize(builder));
        return builder.build();
    }
}
```

**application.properties:**

```properties
customer.api.base-url=http://localhost:8084/customer-service
customer.api.max-connections-total=64
customer.api.max-connections-per-route=16
customer.api.connect-timeout-seconds=10
customer.api.connection-request-timeout-seconds=10
customer.api.read-timeout-seconds=15
```

**Defaults:**

* Pool size → total: 64, per-route: 16
* Timeouts → connect: 10s, read: 15s, connection-request: 10s
* Retries → disabled (safe default for non-idempotent ops)

---

## 🧠 Adapter Pattern Example

```java
@Service
public class CustomerClientAdapterImpl implements CustomerClientAdapter {
    private final CustomerControllerApi api;

    public CustomerClientAdapterImpl(CustomerControllerApi api) {
        this.api = api;
    }

    @Override
    public ServiceClientResponse<Page<CustomerDto>> getCustomers(
            String name, String email, Integer page, Integer size,
            ClientSortField sortBy, ClientSortDirection direction) {
        return api.getCustomers(
                name, email, page, size,
                sortBy != null ? sortBy.value() : ClientSortField.CUSTOMER_ID.value(),
                direction != null ? direction.value() : ClientSortDirection.ASC.value());
    }
}
```

**Benefits:**

* Generated code remains isolated.
* Business logic depends on stable interfaces only.
* Client evolution never leaks across services.

---

## 🚀 Quick Usage Example

```java
var resp = customerClientAdapter.getCustomer(42);
var dto = resp.getData();
var serverTime = resp.getMeta().serverTime();
```

Error handling (RFC 9457):

```java
try {
    customerClientAdapter.getCustomer(999);
} catch (ClientProblemException ex) {
    var pd = ex.getProblem();
    System.err.println("type   = " + pd.getType());
    System.err.println("title  = " + pd.getTitle());
    System.err.println("detail = " + pd.getDetail());
    System.err.println("status = " + ex.getStatus());
    System.err.println("code   = " + pd.getErrorCode());
}
```

---

## 📘 Adoption Summary

1. Mark wrapper schemas in the OpenAPI spec:

    * `x-api-wrapper: true`
    * `x-api-wrapper-datatype: <T>`
    * (optional) `x-data-container` / `x-data-item`
2. Keep templates under `src/main/resources/openapi-templates/`.
3. Run OpenAPI Generator → wrappers extend `ServiceClientResponse<T>` automatically.

**Example Maven setup (coming soon):**

```xml
<dependency>
  <groupId>io.github.bsayli</groupId>
  <artifactId>openapi-generics-templates</artifactId>
  <version>0.7.0</version>
</dependency>
```

---

## 🧰 Troubleshooting

* **No thin wrappers?** Check vendor extensions + template directory.
* **Nested generics missing?** Ensure `x-data-container` & `x-data-item` exist in spec.
* **ProblemDetail not thrown?** Verify your `RestClientCustomizer`.
* **Page schema missing?** Confirm `components/schemas/Page` defines `content.items.$ref`.
* **Missing deps?** Ensure host app includes `jakarta.validation` & Spring Web.

---

### 🧹 Ignoring Redundant Generated DTOs

The following patterns in [`.openapi-generator-ignore`](.openapi-generator-ignore) prevent redundant DTOs from being regenerated.
These classes already exist in the shared `common` package and are excluded from code generation.

```bash
# --- Custom additions for generated DTO cleanup ---
**/src/gen/java/**/generated/dto/Page*.java
**/src/gen/java/**/generated/dto/ServiceResponse.java
**/src/gen/java/**/generated/dto/ServiceResponseVoid.java
**/src/gen/java/**/generated/dto/Meta.java
**/src/gen/java/**/generated/dto/Sort.java
```

---

## 📚 Notes

* **Toolchain:** Java 21, Spring Boot 3.4.11, OpenAPI Generator 7.17.0
* **Options:** `useSpringBoot3=true`, `useJakartaEe=true`, `serializationLibrary=jackson`, `dateLibrary=java8`
* **Spec file:** `src/main/resources/customer-api-docs.yaml`
* Optional: `x-class-extra-annotation` injects annotations on wrappers.

---

## 📦 Related Module

Generated from the OpenAPI spec exposed by:

* [customer-service](../customer-service/README.md) — Spring Boot microservice acting as the API producer for this client.

---

## 💬 Feedback

Found a bug or have a suggestion? Contributions are welcome — open an issue or join the discussion:
💭 [Start a discussion →](https://github.com/bsayli/spring-boot-openapi-generics-clients/discussions)

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!  
Feel free to [open an issue](https://github.com/bsayli/spring-boot-openapi-generics-clients/issues) or submit a PR.

---

## 🛡 License

Licensed under the **MIT License** (see root `LICENSE`).
All submodules inherit this license.

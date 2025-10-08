# customer-service-client

[![Java 21](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.10-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![OpenAPI Generator](https://img.shields.io/badge/OpenAPI%20Generator-7.16.0-blue?logo=openapiinitiative)](https://openapi-generator.tech/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](../LICENSE)

Generated Java client for the **customer-service**, showcasing **type‑safe generic responses** and **nested generics**
with a minimal OpenAPI Generator Mustache overlay. The client maps successful responses into a reusable envelope
`ServiceClientResponse<T>` and decodes non‑2xx responses into RFC 7807-compliant `ProblemDetail` via a custom exception.

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

> ℹ️ **Multi-module builds:** If your project is multi-module, ensure the generated path is compiled. (Handled via
> `build-helper-maven-plugin` in this repo.)

---

## ✅ What You Get

* Java client using **OpenAPI Generator 7.16.0** with the **Spring `RestClient`** library.
* A reusable generic base: `io.github.bsayli.openapi.client.common.ServiceClientResponse<T>` containing `data` and
  `meta`.
* **Nested generics support**: wrappers such as `ServiceClientResponse<Page<CustomerDto>>`.
* **RFC 7807 Problem decoding** via `ClientProblemException`.
* **Spring Boot configuration** for pooled HttpClient5 + `RestClientCustomizer` for error handling.
* Adapter pattern for clean, type-safe service integration.

---

## 🧠 How the Thin Wrappers Are Produced

Server marks wrapper schemas with vendor extensions. The Mustache overlay generates thin wrappers extending the generic
base.

```mustache
{{! Generics-aware thin wrapper }}
import {{commonPackage}}.ServiceClientResponse;
{{#vendorExtensions.x-data-container}}
import {{commonPackage}}.{{vendorExtensions.x-data-container}};
{{/vendorExtensions.x-data-container}}

{{#vendorExtensions.x-class-extra-annotation}}
{{{vendorExtensions.x-class-extra-annotation}}}
{{/vendorExtensions.x-class-extra-annotation}}
public class {{classname}} extends ServiceClientResponse<
  {{#vendorExtensions.x-data-container}}
    {{vendorExtensions.x-data-container}}<{{vendorExtensions.x-data-item}}>
  {{/vendorExtensions.x-data-container}}
  {{^vendorExtensions.x-data-container}}
    {{vendorExtensions.x-api-wrapper-datatype}}
  {{/vendorExtensions.x-data-container}}
> {}
```

**Example outputs:**

* `ServiceResponseCustomerDto` → `extends ServiceClientResponse<CustomerDto>`
* `ServiceResponsePageCustomerDto` → `extends ServiceClientResponse<Page<CustomerDto>>`

---

## 📦 Core Components

**Envelope and Meta:**

```java
public class ServiceClientResponse<T> {
    private T data;
    private ClientMeta meta;
}

public record ClientMeta(Instant serverTime, List<ClientSort> sort) {
}

public record Page<T>(List<T> content, int page, int size,
                      long totalElements, int totalPages,
                      boolean hasNext, boolean hasPrev) {
}
```

**Problem Exception:**

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
                    ProblemDetail pd = null;
                    try (var is = response.getBody()) {
                        pd = om.readValue(is, ProblemDetail.class);
                    } catch (Exception ignore) {
                    }
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

---

## 🧩 Adapter Pattern Example

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
            SortField sortBy, SortDirection direction) {
        return api.getCustomers(
                name, email, page, size,
                sortBy != null ? sortBy.value() : SortField.CUSTOMER_ID.value(),
                direction != null ? direction.value() : SortDirection.ASC.value());
    }
}
```

**Benefits:**

* Generated code stays isolated.
* Business logic depends only on stable interfaces.
* Client evolution never leaks across services.

---

## 🚀 Quick Usage Example

```java
var resp = customerClientAdapter.getCustomer(42);
var dto = resp.getData();
var serverTime = resp.getMeta().serverTime();
```

Error handling:

```java
try{
        customerClientAdapter.getCustomer(999);
}catch(
ClientProblemException ex){
var pd = ex.getProblem();
// pd.getTitle(), pd.getDetail(), pd.getErrorCode(), etc.
}
```

---

## 📘 Adoption Summary

1. Mark wrapper schemas in the OpenAPI spec:

    * `x-api-wrapper: true`
    * `x-api-wrapper-datatype: <T>`
    * Optionally `x-data-container` and `x-data-item`
2. Keep templates under `src/main/resources/openapi-templates/`.
3. Run OpenAPI Generator → wrappers extend `ServiceClientResponse<T>` automatically.

For detailed steps, see [`../docs/adoption`](../docs/adoption).

---

## 🧰 Troubleshooting

* **No thin wrappers?** Check vendor extensions + template directory.
* **Nested generics missing?** Ensure `x-data-container` and `x-data-item` exist.
* **ProblemDetail not thrown?** Verify your `RestClientCustomizer`.
* **Provided deps:** Ensure your host app includes `jakarta.validation` & Spring Web.

---

## 📚 Notes

* **Toolchain:** Java 21, OpenAPI Generator 7.16.0
* **Generator options:** `useSpringBoot3=true`, `useJakartaEe=true`, `serializationLibrary=jackson`, `dateLibrary=java8`
* **OpenAPI spec:** `src/main/resources/customer-api-docs.yaml`
* Optional `x-class-extra-annotation` adds annotations on generated wrappers.

---

## 📦 Related Module

Generated from the OpenAPI spec exposed by:

* [customer-service](../customer-service/README.md) — Spring Boot microservice acting as the API producer for this generated client.

---

## 💬 Feedback

If you spot an error or have suggestions, open an issue or join the discussion — contributions are welcome.
💭 [Start a discussion →](https://github.com/bsayli/spring-boot-openapi-generics-clients/discussions)

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!
Feel free to [open an issue](https://github.com/bsayli/spring-boot-openapi-generics-clients/issues) or submit a PR.

---

## 🛡 License

This repository is licensed under **MIT** (root `LICENSE`). Submodules inherit the license.

# customer-service-client

[![Java 21](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.10-green?logo=springboot)](https://spring.io/projects/spring-boot)
[![OpenAPI Generator](https://img.shields.io/badge/OpenAPI%20Generator-7.16.0-blue?logo=openapiinitiative)](https://openapi-generator.tech/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](../LICENSE)

Generated Java client for the **customer-service**, showcasing **type‑safe generic responses** and **nested generics**
with a tiny OpenAPI Generator template overlay. The client maps successful responses to a reusable envelope
`ServiceClientResponse<T>` and decodes non‑2xx responses into RFC7807 `ProblemDetail` with a custom exception.

---

## 🔧 TL;DR — Generate in 1 minute

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

> ℹ️ **Multi-module builds:** If your project is multi-module, ensure the generated path is compiled (already wired via
`build-helper-maven-plugin` in this repo).

---

## ✅ What You Get

* Java client using **OpenAPI Generator** (`java` + `restclient` library).
* A reusable generic base: `io.github.bsayli.openapi.client.common.ServiceClientResponse<T>` with **`data`** and *
  *`meta`**.
* **Nested generics awareness**: wrappers can be `ServiceClientResponse<Page<Item>>` when server marks container/item.
* **Problem decoding**: non‑2xx responses parsed into generated `ProblemDetail` and thrown as `ClientProblemException`.
* Spring configuration that wires a pooled **HttpClient5** + `RestClientCustomizer` for status handling.
* An adapter layer that exposes clean, stable methods for your services.

---

## 🧠 How the thin wrappers are produced

Server marks wrapper schemas with vendor extensions. The Mustache overlay renders **thin shells** extending the generic
base.

```mustache
{{! --- Generics-aware thin wrapper --- }}
{{! If x-data-container/x-data-item exist, render ServiceClientResponse<Container<Item>> }}
{{! Otherwise fall back to ServiceClientResponse<x-api-wrapper-datatype> }}

import {{commonPackage}}.ServiceClientResponse;
{{#vendorExtensions.x-data-container}}
import {{commonPackage}}.{{vendorExtensions.x-data-container}};
{{/vendorExtensions.x-data-container}}

{{#vendorExtensions.x-class-extra-annotation}}
{{{vendorExtensions.x-class-extra-annotation}}}
{{/vendorExtensions.x-class-extra-annotation}}
public class {{classname}}
    extends ServiceClientResponse<
      {{#vendorExtensions.x-data-container}}
        {{vendorExtensions.x-data-container}}<{{vendorExtensions.x-data-item}}>
      {{/vendorExtensions.x-data-container}}
      {{^vendorExtensions.x-data-container}}
        {{vendorExtensions.x-api-wrapper-datatype}}
      {{/vendorExtensions.x-data-container}}
    > {
}
```

Typical outputs under `generated-sources`:

* `...ServiceResponseCustomerDto` → `extends ServiceClientResponse<CustomerDto>`
* `...ServiceResponsePageCustomerDto` → `extends ServiceClientResponse<Page<CustomerDto>>`

---

## 📦 Client Building Blocks (in this module)

**Envelope & meta**

```java
// io.github.bsayli.openapi.client.common.ServiceClientResponse
public class ServiceClientResponse<T> {
    private T data;
    private ClientMeta meta; // serverTime, sort ...
    // getters/setters/equals/hashCode/toString
}

// io.github.bsayli.openapi.client.common.ClientMeta
public record ClientMeta(java.time.Instant serverTime,
                         java.util.List<io.github.bsayli.openapi.client.common.sort.ClientSort> sort) {
}

// io.github.bsayli.openapi.client.common.Page
public record Page<T>(java.util.List<T> content, int page, int size,
                      long totalElements, int totalPages, boolean hasNext, boolean hasPrev) {
}
```

**Problem decoding**

```java
// io.github.bsayli.openapi.client.common.error.ClientProblemException
public class ClientProblemException extends RuntimeException {
    private final transient io.github.bsayli.openapi.client.generated.dto.ProblemDetail problem;
    private final int status;
    // message includes title/detail/errorCode if present
}
```

---

## ⚙️ Spring Wiring (production-friendly)

The client registers a pooled HttpClient5 and a `RestClientCustomizer` that turns non‑2xx responses into
`ClientProblemException`.

```java

@Configuration
public class CustomerApiClientConfig {
    @Bean
    RestClientCustomizer problemDetailStatusHandler(ObjectMapper om) {
        return builder -> builder.defaultStatusHandler(
                HttpStatusCode::isError,
                (request, response) -> {
                    io.github.bsayli.openapi.client.generated.dto.ProblemDetail pd = null;
                    try (var is = response.getBody()) {
                        pd = om.readValue(is, io.github.bsayli.openapi.client.generated.dto.ProblemDetail.class);
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
                .setMaxConnTotal(maxTotal).setMaxConnPerRoute(maxPerRoute).build();
        return HttpClients.custom().setConnectionManager(cm)
                .evictExpiredConnections()
                .evictIdleConnections(org.apache.hc.core5.util.TimeValue.ofSeconds(30))
                .setUserAgent("customer-service-client").disableAutomaticRetries().build();
    }

    @Bean
    HttpComponentsClientHttpRequestFactory customerRequestFactory(
            CloseableHttpClient http,
            @Value("${customer.api.connect-timeout-seconds:10}") long connect,
            @Value("${customer.api.connection-request-timeout-seconds:10}") long connReq,
            @Value("${customer.api.read-timeout-seconds:15}") long read) {
        var f = new HttpComponentsClientHttpRequestFactory(http);
        f.setConnectTimeout(Duration.ofSeconds(connect));
        f.setConnectionRequestTimeout(Duration.ofSeconds(connReq));
        f.setReadTimeout(Duration.ofSeconds(read));
        return f;
    }

    @Bean
    RestClient customerRestClient(RestClient.Builder b,
                                  HttpComponentsClientHttpRequestFactory rf,
                                  java.util.List<RestClientCustomizer> customizers) {
        b.requestFactory(rf);
        if (customizers != null) customizers.forEach(c -> c.customize(b));
        return b.build();
    }

    @Bean
    ApiClient customerApiClient(RestClient rest, @Value("${customer.api.base-url}") String baseUrl) {
        return new ApiClient(rest).setBasePath(baseUrl);
    }

    @Bean
    CustomerControllerApi customerControllerApi(ApiClient apiClient) {
        return new CustomerControllerApi(apiClient);
    }
}
```

**application.properties**

```properties
# Base URL of customer-service
customer.api.base-url=http://localhost:8084/customer-service
# HttpClient5 pool & timeouts
customer.api.max-connections-total=64
customer.api.max-connections-per-route=16
customer.api.connect-timeout-seconds=10
customer.api.connection-request-timeout-seconds=10
customer.api.read-timeout-seconds=15
```

---

## 🧩 Adapter Pattern (recommended)

Encapsulate the generated API behind your own adapter so the rest of your code does not depend on generated classes.

```java

@Service
public class CustomerClientAdapterImpl implements CustomerClientAdapter {
    private final CustomerControllerApi api;

    public CustomerClientAdapterImpl(CustomerControllerApi api) {
        this.api = api;
    }

    @Override
    public ServiceClientResponse<CustomerDto> createCustomer(CustomerCreateRequest req) {
        return api.createCustomer(req);
    }

    @Override
    public ServiceClientResponse<CustomerDto> getCustomer(Integer customerId) {
        return api.getCustomer(customerId);
    }

    @Override
    public ServiceClientResponse<Page<CustomerDto>> getCustomers() {
        return getCustomers(null, null, 0, 5, SortField.CUSTOMER_ID, SortDirection.ASC);
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

    @Override
    public ServiceClientResponse<CustomerDto> updateCustomer(Integer id, CustomerUpdateRequest req) {
        return api.updateCustomer(id, req);
    }

    @Override
    public ServiceClientResponse<CustomerDeleteResponse> deleteCustomer(Integer id) {
        return api.deleteCustomer(id);
    }
}
```

> Returns now follow the simplified server contract: **create/update/get → `CustomerDto`**, list → `Page<CustomerDto>`;
> delete → `CustomerDeleteResponse`.

---

## 🚀 Quick usage example

```java
var response = customerClientAdapter.getCustomer(42);
var dto = response.getData();
var serverTime = response.getMeta().serverTime();
```

For listing with paging and sorting:

```java
var list = customerClientAdapter.getCustomers("Jane", null, 0, 5, SortField.CUSTOMER_ID, SortDirection.ASC);
var page = list.getData(); // Page<CustomerDto>
for(
var c :page.

content()){ /* ... */ }
```

Error handling example:

```java
try{
        customerClientAdapter.getCustomer(999);
}catch(
ClientProblemException ex){
var pd = ex.getProblem();
// pd.getTitle(), pd.getDetail(), pd.getStatus(), pd.getErrorCode(), pd.getExtensions()...
}
```

---

## 📘 Adoption Guide (summary)

1. Ensure your server marks wrapper schemas with:

    * `x-api-wrapper: true`
    * `x-api-wrapper-datatype: <T>`
    * *(optional nested)* `x-data-container: <Container>`, `x-data-item: <Item>`
2. Keep the Mustache overlay in `src/main/resources/openapi-templates/` (see snippet above).
3. Run the OpenAPI Generator during build; thin wrappers will extend the generic base accordingly.

For complete, step-by-step guides see [`../docs/adoption`](../docs/adoption).

---

## 🧰 Troubleshooting

* **No thin wrappers generated?** Check that your spec carries the vendor extensions and your `<templateDirectory>`
  points to the **effective** templates.
* **Nested generics not applied?** Verify `x-data-container` and `x-data-item` are present on the composed schema (
  `ServiceResponsePageCustomerDto`, etc.).
* **`ProblemDetail` not thrown?** Ensure the `RestClientCustomizer` is registered and not overridden later.
* **Provided deps**: Generated code uses `jakarta.validation` & Spring types; your host app must provide them if you
  depend on the generated sources directly.

---

## 📚 Notes

* **Generator & toolchain**

    * Java 21, OpenAPI Generator 7.16.0
    * Options: `useSpringBoot3=true`, `useJakartaEe=true`, `serializationLibrary=jackson`, `dateLibrary=java8`,
      `useBeanValidation=true`
* **OpenAPI spec location**: `src/main/resources/customer-api-docs.yaml`
* **Optional**: `x-class-extra-annotation` can inject annotations on wrapper classes (advanced).

---

## 📦 Related Module

Generated from the OpenAPI spec exposed by:

* [`customer-service`](../customer-service/README.md) — sample Spring Boot microservice (API producer).

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!
Feel free to [open an issue](../../issues) or submit a PR.

---

## 🛡 License

This repository is licensed under **MIT** (root `LICENSE`). Submodules inherit the license.

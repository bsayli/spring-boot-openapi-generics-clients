---
layout: default
title: Client-Side Adoption
parent: Adoption Guides
nav_order: 2
---

# Client‑Side Integration Guide

This guide describes how to integrate the **generics‑aware OpenAPI client** into your own project, aligned with the
new `{ data, meta }` response structure and RFC 9457 `ProblemDetail` error model introduced in the updated
`customer-service` / `customer-service-client` architecture.

---

## 🎯 Goals

* Generate thin, **type‑safe wrappers** extending `ServiceClientResponse<T>` instead of duplicating envelopes.
* Support **nested generics** such as `ServiceClientResponse<Page<CustomerDto>>`.
* Decode non‑2xx responses into **RFC 9457 ProblemDetail** and raise `ClientProblemException`.
* Allow seamless injection into Spring Boot apps using a pooled `RestClient`.

---

## ⚙️ What You Get

* **Generated wrappers** per endpoint (e.g., `ServiceResponseCustomerDto`) extending `ServiceClientResponse<T>`.
* **Strong typing** for `.getData()` and `.getMeta()`.
* **Problem‑aware exception** decoding (`ClientProblemException`).
* **Spring configuration** using `RestClientCustomizer` + Apache HttpClient5.

---

## 🧱 Prerequisites

* Java 21+
* Maven 3.9+
* A running OpenAPI provider exposing `/v3/api-docs.yaml` (from your server‑side service)

---

## 🚀 Generate the Client

1. **Download the OpenAPI spec:**

   ```bash
   curl -s http://localhost:8084/customer-service/v3/api-docs.yaml \
     -o src/main/resources/customer-api-docs.yaml
   ```

2. **Build the client:**

   ```bash
   mvn clean install
   ```

3. **Inspect generated output:**

    * `target/generated-sources/openapi/src/gen/java`
    * Look for classes like `ServiceResponseCustomerDto` extending `ServiceClientResponse<CustomerDto>`

---

## 🧩 Core Classes (Shared Base)

Copy these into your client module under `openapi/client/common`:

### `ServiceClientResponse.java`

```java
package

<your.base>.openapi.client.common;

import java.util.Objects;

public class ServiceClientResponse<T> {
    private T data;
    private ClientMeta meta;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ClientMeta getMeta() {
        return meta;
    }

    public void setMeta(ClientMeta meta) {
        this.meta = meta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceClientResponse<?> that)) return false;
        return Objects.equals(data, that.data) && Objects.equals(meta, that.meta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, meta);
    }

    @Override
    public String toString() {
        return "ServiceClientResponse{" + "data=" + data + ", meta=" + meta + '}';
    }
}
```

### `ClientMeta.java`

```java
package

<your.base>.openapi.client.common;

import java.time.Instant;
import java.util.List;

import <your.base>.openapi.client.common.sort.ClientSort;

public record ClientMeta(Instant serverTime, List<ClientSort> sort) {
}
```

### `Page.java`

```java
package

<your.base>.openapi.client.common;

import java.util.List;

public record Page<T>(List<T> content, int page, int size, long totalElements,
                      int totalPages, boolean hasNext, boolean hasPrev) {
}
```

### `ClientProblemException.java`

```java
package

<your.base>.openapi.client.common.error;

import io.github.bsayli.openapi.client.generated.dto.ProblemDetail;

public class ClientProblemException extends RuntimeException {
    private final transient ProblemDetail problem;
    private final int status;

    public ClientProblemException(ProblemDetail problem, int status) {
        super(problem != null ? problem.getTitle() + ": " + problem.getDetail() : "HTTP " + status);
        this.problem = problem;
        this.status = status;
    }

    public ProblemDetail getProblem() {
        return problem;
    }

    public int getStatus() {
        return status;
    }
}
```

---

## 🧰 Mustache Template Overlay

Place templates under `src/main/resources/openapi-templates/`.

### `api_wrapper.mustache`

{% raw %}

```mustache
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

{% endraw %}

This ensures wrappers extend the generic base, including nested containers.

---

## 🧩 Adapter Pattern (Recommended)

Encapsulate generated APIs behind your own adapter interface.

```java
package

<your.base>.openapi.client.adapter;

import <your.base>.openapi.client.common.ServiceClientResponse;
import <your.base>.openapi.client.common.Page;
import <your.base>.openapi.client.generated.api.YourControllerApi;
import <your.base>.openapi.client.generated.dto .*;

public interface YourClientAdapter {
    ServiceClientResponse<YourDto> getYourEntity(Integer id);

    ServiceClientResponse<Page<YourDto>> listEntities();

    ServiceClientResponse<YourDto> createEntity(YourCreateRequest req);
}
```

Then implement it using the generated API:

```java

@Service
public class YourClientAdapterImpl implements YourClientAdapter {
    private final YourControllerApi api;

    public YourClientAdapterImpl(YourControllerApi api) {
        this.api = api;
    }

    @Override
    public ServiceClientResponse<YourDto> getYourEntity(Integer id) {
        return api.getYourEntity(id);
    }

    @Override
    public ServiceClientResponse<Page<YourDto>> listEntities() {
        return api.getEntities();
    }

    @Override
    public ServiceClientResponse<YourDto> createEntity(YourCreateRequest req) {
        return api.createEntity(req);
    }
}
```

---

## 🧠 Spring Boot Configuration

Production‑ready configuration using pooled Apache HttpClient5 and `RestClientCustomizer`.

```java

@Configuration
public class YourApiClientConfig {

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
    CloseableHttpClient httpClient(@Value("${your.api.max-connections-total:64}") int total,
                                   @Value("${your.api.max-connections-per-route:16}") int perRoute) {
        var cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(total).setMaxConnPerRoute(perRoute).build();
        return HttpClients.custom()
                .setConnectionManager(cm)
                .evictExpiredConnections()
                .evictIdleConnections(org.apache.hc.core5.util.TimeValue.ofSeconds(30))
                .disableAutomaticRetries()
                .setUserAgent("your-service-client")
                .build();
    }

    @Bean
    HttpComponentsClientHttpRequestFactory requestFactory(CloseableHttpClient http,
                                                          @Value("${your.api.connect-timeout-seconds:10}") long c,
                                                          @Value("${your.api.connection-request-timeout-seconds:10}") long r,
                                                          @Value("${your.api.read-timeout-seconds:15}") long t) {
        var f = new HttpComponentsClientHttpRequestFactory(http);
        f.setConnectTimeout(Duration.ofSeconds(c));
        f.setConnectionRequestTimeout(Duration.ofSeconds(r));
        f.setReadTimeout(Duration.ofSeconds(t));
        return f;
    }

    @Bean
    RestClient yourRestClient(RestClient.Builder builder, HttpComponentsClientHttpRequestFactory rf,
                              List<RestClientCustomizer> customizers) {
        builder.requestFactory(rf);
        if (customizers != null) customizers.forEach(c -> c.customize(builder));
        return builder.build();
    }

    @Bean
    ApiClient yourApiClient(RestClient rest, @Value("${your.api.base-url}") String baseUrl) {
        return new ApiClient(rest).setBasePath(baseUrl);
    }

    @Bean
    YourControllerApi yourControllerApi(ApiClient apiClient) {
        return new YourControllerApi(apiClient);
    }
}
```

**application.properties**

```properties
your.api.base-url=http://localhost:8084/your-service
your.api.max-connections-total=64
your.api.max-connections-per-route=16
your.api.connect-timeout-seconds=10
your.api.connection-request-timeout-seconds=10
your.api.read-timeout-seconds=15
```

---

## 🧪 Example Usage

```java
var response = yourClientAdapter.getYourEntity(42);
var dto = response.getData();
var serverTime = response.getMeta().serverTime();
```

Error handling:

```java
try{
        yourClientAdapter.getYourEntity(999);
}catch(
ClientProblemException ex){
var pd = ex.getProblem();
    System.err.

println(pd.getTitle() +": "+pd.

getDetail());
        }
```

---

## 🧭 Folder Structure (Suggested)

```
your-service-client/
 ├─ src/main/java/<your/base>/openapi/client/common/
 │   ├─ ServiceClientResponse.java
 │   ├─ ClientMeta.java
 │   ├─ Page.java
 │   └─ error/ClientProblemException.java
 ├─ src/main/java/<your/base>/openapi/client/adapter/
 │   ├─ YourClientAdapter.java
 │   └─ YourClientAdapterImpl.java
 ├─ src/main/resources/openapi-templates/
 │   └─ api_wrapper.mustache
 ├─ src/main/resources/customer-api-docs.yaml
 └─ pom.xml
```

---

## ✅ Key Points

* Mirrors `{ data, meta }` structure from the server side.
* Fully supports nested generics and vendor extensions (`x-data-container`, `x-data-item`).
* Decodes non‑2xx responses into `ProblemDetail` and throws `ClientProblemException`.
* Modular design: adapters hide generated artifacts from your domain logic.

Your client is now **fully aligned** with the new generics‑aware and ProblemDetail‑compatible architecture.

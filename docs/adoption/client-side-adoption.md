---
layout: default
title: Client-Side Adoption
parent: Adoption Guides
nav_order: 2
---

# Client-Side Integration Guide

This document describes how to integrate the **generics-aware OpenAPI client** into your own microservice project, based
on the patterns demonstrated in the `customer-service-client` module.

The purpose is to generate **type-safe clients** that extend a reusable generic base (`ServiceClientResponse<T>`)
instead of duplicating response envelopes.

---

## 1) What You Get

* Thin wrappers per endpoint (`ServiceResponseYourDto`), each extending `ServiceClientResponse<T>`.
* Strong typing for `getData()` without boilerplate or casting.
* A reusable adapter interface to keep generated code isolated.
* Optional Spring Boot configuration to auto-wire the client beans.

---

## 2) Prerequisites

* Java 21+
* Maven 3.9+ (or Gradle 8+ if adapted)
* Running service exposing `/v3/api-docs.yaml`

---

## 3) Steps to Generate

1. **Pull the OpenAPI spec** from your service:

   ```bash
   curl -s http://localhost:8084/your-service/v3/api-docs.yaml \
     -o src/main/resources/your-api-docs.yaml
   ```

2. **Run Maven build** in the client module:

   ```bash
   mvn clean install
   ```

3. **Inspect generated code**:

    * `target/generated-sources/openapi/src/gen/java`
    * Look for classes like `ServiceResponseYourDto` extending `ServiceClientResponse<YourDto>`

---

## 4) Core Classes to Copy

Ensure you copy the following **shared classes** into your client project:

**`common/ServiceClientResponse.java`**

```java
package

<your.base>.openapi.client.common;

import java.util.List;
import java.util.Objects;

public class ServiceClientResponse<T> {
    private Integer status;
    private String message;
    private List<ClientErrorDetail> errors;
    private T data;
    // getters, setters, equals, hashCode, toString
}
```

**`common/ClientErrorDetail.java`**

```java
package <your.base>.openapi.client.common;

public record ClientErrorDetail(String errorCode, String message) {
}
```

These are referenced by the Mustache templates and must exist in your client project.

---

## 5) Mustache Templates

Place the following templates under:

```
src/main/resources/openapi-templates/
```

**`api_wrapper.mustache`**

```mustache
import {{commonPackage}}.ServiceClientResponse;

{{#vendorExtensions.x-class-extra-annotation}}
{{{vendorExtensions.x-class-extra-annotation}}}
{{/vendorExtensions.x-class-extra-annotation}}
public class {{classname}}
    extends ServiceClientResponse<{{vendorExtensions.x-api-wrapper-datatype}}> {
}
```

**`model.mustache`** (partial overlay to delegate wrapper classes to `api_wrapper.mustache`).

These ensure generated wrappers extend the generic base instead of duplicating fields.

---

## 6) Adapter Pattern

Encapsulate generated APIs in an adapter interface:

```java
package

<your.base>.openapi.client.adapter;

import <your.base>.openapi.client.common.ServiceClientResponse;
import <your.base>.openapi.client.generated.dto .*;

public interface YourClientAdapter {
    ServiceClientResponse<YourDto> getYourEntity(Integer id);

    ServiceClientResponse<YourCreateResponse> createYourEntity(YourCreateRequest request);
}
```

This shields your business code from generated artifacts and provides a stable contract.

---

## 7) Spring Boot Configuration (Optional)

Example auto-wiring configuration:

```java

@Configuration
public class YourApiClientConfig {

    @Bean
    RestClient yourRestClient(RestClient.Builder builder,
                              @Value("${your.api.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean
    ApiClient yourApiClient(RestClient yourRestClient,
                            @Value("${your.api.base-url}") String baseUrl) {
        return new ApiClient(yourRestClient).setBasePath(baseUrl);
    }

    @Bean
    YourControllerApi yourControllerApi(ApiClient yourApiClient) {
        return new YourControllerApi(yourApiClient);
    }
}
```

**application.properties:**

```properties
your.api.base-url=http://localhost:8084/your-service
```

---

## 8) Usage Example

```java

@Autowired
private YourControllerApi yourApi;

public void demo() {
    var req = new YourCreateRequest().name("Alice");
    var resp = yourApi.createYourEntity(req);
    System.out.println(resp.getStatus());
    System.out.println(resp.getData().getName());
}
```

---

## 9) Notes

* Dependencies like `spring-web`, `jakarta.*` are often marked **provided** — your host app must supply them.
* Re-run `curl` + `mvn clean install` whenever your service’s OpenAPI spec changes.
* Optional vendor extension `x-class-extra-annotation` can add annotations (e.g., Jackson or Lombok) on generated
  wrappers.

---

## 10) Folder Structure (Suggested)

```
your-service-client/
 ├─ src/main/java/<your/base>/openapi/client/common/
 │   ├─ ServiceClientResponse.java
 │   └─ ClientErrorDetail.java
 ├─ src/main/java/<your/base>/openapi/client/adapter/
 │   └─ YourClientAdapter.java
 ├─ src/main/resources/openapi-templates/
 │   ├─ api_wrapper.mustache
 │   └─ model.mustache
 ├─ src/main/resources/your-api-docs.yaml
 └─ pom.xml
```

---

## 11) Maven Setup

See [Client-Side Adoption (POM Setup)](client-side-adoption-pom.md) for the full `pom.xml` configuration, including
required plugins (`maven-dependency-plugin`, `maven-resources-plugin`, `openapi-generator-maven-plugin`, etc.)
and dependency declarations.

---

✅ With this setup, your client project generates **type-safe wrappers** that align with `ServiceResponse<T>` from the
server side, without any boilerplate duplication.

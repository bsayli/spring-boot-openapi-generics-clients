---

layout: default
title: Server-Side Adoption (Simplified)
parent: Adoption Guides
nav_order: 1
------------

# Server-Side Adoption — Spring Boot + Springdoc

**Goal:** Integrate a minimal, production-ready setup into your Spring MVC service so it returns unified `{ data, meta }` envelopes, automatically registers generic wrappers in OpenAPI, and enables thin client generation via `ServiceClientResponse<T>`.

> Scope: Spring MVC (WebMVC) + Springdoc (no WebFlux).

---

## 1️⃣ Overview

Your service will:

* Return success bodies like:

```json
{
  "data": { /* T */ },
  "meta": { "serverTime": "2025-01-01T12:34:56Z", "sort": [] }
}
```

* Expose Swagger UI and `/v3/api-docs(.yaml)` including:

    * Base `ServiceResponse`
    * Composed wrappers for each DTO (`ServiceResponseCustomerDto`, etc.)
    * Vendor extensions: `x-api-wrapper`, `x-api-wrapper-datatype`, *(optionally)* `x-data-container`, `x-data-item`

---

## 2️⃣ Dependencies (pom.xml)

```xml
<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.13</version>
  </dependency>
</dependencies>
```

> ✅ Ensure `common.openapi` packages are inside your application's scan base package.

---

## 3️⃣ Core Response Envelope

Include your unified response primitives under `common/api/response/`.

**`ServiceResponse.java`**

```java
package <your.base>.common.api.response;

public record ServiceResponse<T>(T data, Meta meta) {

    public static <T> ServiceResponse<T> ok(T data) {
        return new ServiceResponse<>(data, Meta.now());
    }

    public static <T> ServiceResponse<T> ok(T data, Meta meta) {
        return new ServiceResponse<>(data, meta != null ? meta : Meta.now());
    }
}
```

**`Meta.java`**

```java
package <your.base>.common.api.response;

import java.time.Instant;
import java.util.List;

public record Meta(Instant serverTime, List<Sort> sort) {

    public static Meta now() {
        return new Meta(Instant.now(), List.of());
    }
    
    public static Meta now(List<Sort> sort) {
        return new Meta(Instant.now(), sort == null ? List.of() : List.copyOf(sort));
    }
}
```

These define the `{ data, meta }` envelope shared across all controllers.

---

## 4️⃣ OpenAPI Schema Setup

Define and register your reusable OpenAPI schema components directly in your service. Below are the key files and minimal inline examples — each followed by a link to its full source.

**`OpenApiSchemas.java`** — centralizes all schema names and vendor extension keys.

```java
package <your.base>.common.openapi;

public final class OpenApiSchemas {

  public static final String PROP_DATA = "data";
  public static final String PROP_META = "meta";

  public static final String SCHEMA_SERVICE_RESPONSE = "ServiceResponse";
  public static final String SCHEMA_SERVICE_RESPONSE_VOID = "ServiceResponseVoid";
  public static final String SCHEMA_META = "Meta";

  public static final String EXT_API_WRAPPER = "x-api-wrapper";
  public static final String EXT_API_WRAPPER_DATATYPE = "x-api-wrapper-datatype";
  public static final String EXT_DATA_CONTAINER = "x-data-container";
  public static final String EXT_DATA_ITEM = "x-data-item";

  private OpenApiSchemas() {}
}
```

➡️ [View full source →](snippets/OpenApiSchemas.java)

---

**`SwaggerResponseCustomizer.java`** — registers base envelope schemas (`ServiceResponse`, `Meta`, etc.).

```java
@Configuration
public class SwaggerResponseCustomizer {

  @Bean
  public OpenApiCustomizer responseEnvelopeSchemas() {
    return openApi -> {
      var schemas = openApi.getComponents().getSchemas();

      schemas.computeIfAbsent("ServiceResponse", k -> new ObjectSchema()
        .addProperty("data", new Schema<>())
        .addProperty("meta", new Schema<>().$ref("#/components/schemas/Meta")));

      schemas.computeIfAbsent("Meta", k -> new ObjectSchema()
        .addProperty("serverTime", new StringSchema().format("date-time"))
        .addProperty("sort", new ArraySchema().items(new ObjectSchema())));
    };
  }
}
```

➡️ [View full source →](snippets/SwaggerResponseCustomizer.java)

---

**`ApiResponseSchemaFactory.java`** — composes a new wrapper schema per DTO and enriches it with vendor extensions.

```java
public final class ApiResponseSchemaFactory {

  public static Schema<?> createComposedWrapper(String dataRef) {
    var schema = new ComposedSchema();
    schema.setAllOf(List.of(
      new Schema<>().$ref("#/components/schemas/ServiceResponse"),
      new ObjectSchema().addProperty("data", new Schema<>().$ref("#/components/schemas/" + dataRef))
    ));
    schema.addExtension("x-api-wrapper", true);
    schema.addExtension("x-api-wrapper-datatype", dataRef);
    return schema;
  }
}
```

➡️ [View full source →](snippets/ApiResponseSchemaFactory.java)

---

## 5️⃣ Auto‑Registration Logic

Add dynamic schema registration so OpenAPI automatically composes wrappers for all controllers returning `ServiceResponse<T>`.

**`ResponseTypeIntrospector.java`** — unwraps controller return types to detect `ServiceResponse<T>`.

```java
package <your.base>.common.openapi.introspector;

import <your.base>.common.api.response.ServiceResponse;
import java.lang.reflect.Method;
import java.util.Optional;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

@Component
public final class ResponseTypeIntrospector {

  public Optional<String> extractDataRefName(Method method) {
    if (method == null) return Optional.empty();
    ResolvableType type = ResolvableType.forMethodReturnType(method);

    if (!ServiceResponse.class.isAssignableFrom(type.resolve())) return Optional.empty();
    if (!type.hasGenerics()) return Optional.empty();

    Class<?> dataClass = type.getGeneric(0).resolve();
    return Optional.ofNullable(dataClass).map(Class::getSimpleName);
  }
}
```

➡️ [View full source →](snippets/ResponseTypeIntrospector.java)

---

**`AutoWrapperSchemaCustomizer.java`** — scans controllers and dynamically registers composed wrapper schemas for each detected DTO.

```java
@Configuration
public class AutoWrapperSchemaCustomizer {

  private final Set<String> dataRefs;
  private final ResponseTypeIntrospector introspector;

  public AutoWrapperSchemaCustomizer(ListableBeanFactory beans, ResponseTypeIntrospector introspector) {
    this.introspector = introspector;
    this.dataRefs = beans.getBeansOfType(RequestMappingHandlerMapping.class).values().stream()
        .flatMap(rmh -> rmh.getHandlerMethods().values().stream())
        .map(HandlerMethod::getMethod)
        .map(introspector::extractDataRefName)
        .flatMap(Optional::stream)
        .collect(Collectors.toSet());
  }

  @Bean
  public OpenApiCustomizer autoResponseWrappers() {
    return openApi -> dataRefs.forEach(ref -> {
      openApi.getComponents().addSchemas(
        "ServiceResponse" + ref,
        ApiResponseSchemaFactory.createComposedWrapper(ref)
      );
    });
  }
}
```

➡️ [View full source →](snippets/AutoWrapperSchemaCustomizer.java)

---

## 6️⃣ Global Problem Responses (RFC 7807)

Add automatic `ProblemDetail` registration and standard error responses for all operations.

**`GlobalErrorResponsesCustomizer.java`** — auto-registers `ProblemDetail` schema and attaches default responses (400, 404, 405, 500).

```java
@Configuration
public class GlobalErrorResponsesCustomizer {

  @Bean
  OpenApiCustomizer addDefaultProblemResponses() {
    return openApi -> openApi.getPaths().forEach((path, item) ->
      item.readOperations().forEach(op -> {
        var problem = new Schema<>().$ref("#/components/schemas/ProblemDetail");
        var content = new Content().addMediaType("application/problem+json", new MediaType().schema(problem));
        op.getResponses().addApiResponse("400", new ApiResponse().description("Bad Request").content(content));
        op.getResponses().addApiResponse("404", new ApiResponse().description("Not Found").content(content));
        op.getResponses().addApiResponse("405", new ApiResponse().description("Method Not Allowed").content(content));
        op.getResponses().addApiResponse("500", new ApiResponse().description("Internal Server Error").content(content));
      })
    );
  }
}
```

➡️ [View full source →](snippets/GlobalErrorResponsesCustomizer.java)

> Ensures your API spec always includes standardized problem responses without extra boilerplate.

---

### Optional: Problem extensions (RFC7807)

Some projects enrich `ProblemDetail` with structured error data inside `extensions.errors`.
These simple records provide a reusable base for that purpose.

**`ErrorItem.java`**

```java
package <your.base>.common.api.response.error;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorItem(String code, String message, String field, String resource, String id) {}
```

**`ProblemExtensions.java`**

```java
package <your.base>.common.api.response.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemExtensions(List<ErrorItem> errors) {
  public static ProblemExtensions ofErrors(List<ErrorItem> errors) {
    return new ProblemExtensions(errors);
  }
}
```

> Usage example: in a `@RestControllerAdvice`,
> `pd.setProperty("extensions", ProblemExtensions.ofErrors(List.of(...)))`
> and optionally `pd.setProperty("errorCode", "VALIDATION_FAILED")`.

---

## 7️⃣ Example Controller

```java
@RestController
@RequestMapping("/v1/customers")
class CustomerController {
  private final CustomerService service;

  @GetMapping("/{id}")
  ResponseEntity<ServiceResponse<CustomerDto>> get(@PathVariable int id) {
    return ResponseEntity.ok(ServiceResponse.ok(service.getCustomer(id)));
  }
}
```

---

## 8️⃣ Verification

Run your service and verify:

1. Swagger UI → `http://localhost:8084/your-service/swagger-ui/index.html`
2. OpenAPI JSON → `http://localhost:8084/your-service/v3/api-docs`

Confirm these:

* `ServiceResponse` base schema exists.
* Composed schemas appear: `ServiceResponseCustomerDto`, etc.
* Vendor extensions (`x-api-wrapper`, `x-api-wrapper-datatype`, ...) are present.

---

## 9️⃣ Troubleshooting

| Problem              | Likely Cause                                   |
| -------------------- | ---------------------------------------------- |
| No composed wrappers | Controller doesn’t return `ServiceResponse<T>` |
| Missing Meta         | Schema not registered or excluded from scan    |
| `$ref` mismatch      | DTO class name differs from schema reference   |

---

## 📁 Folder Map (Minimal)

```
src/main/java/<your/base>/
  common/api/response/
    Meta.java
    ServiceResponse.java
  common/openapi/
    OpenApiSchemas.java
    SwaggerResponseCustomizer.java
    ApiResponseSchemaFactory.java
    ResponseTypeIntrospector.java
    GlobalErrorResponsesCustomizer.java
    autoreg/
      AutoWrapperSchemaCustomizer.java
  api/controller/
    YourControllers...
```

---

✅ Your service now exposes a **generics‑aware**, `ProblemDetail`‑compliant OpenAPI 3.1 spec — ready for thin, type‑safe client generation.

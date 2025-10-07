---

layout: default
title: Server-Side Adoption (Updated)
parent: Adoption Guides
nav_order: 1
------------

# Adopt the server‑side pieces in your own Spring Boot service (MVC + Springdoc)

**Purpose (updated):** Copy the minimal set of classes/config from `customer-service` into your own microservice so that:

* Controllers return **`ServiceResponse<T>` *with* `{ data, meta }`** (no `status/message/errors` in success bodies).
* Errors are emitted as **RFC7807 `ProblemDetail`**.
* The OpenAPI spec **auto-registers wrapper schemas** for each concrete `T` and enriches them with vendor extensions,
  including nested container hints: `x-api-wrapper`, `x-api-wrapper-datatype`, `x-data-container`, `x-data-item`.
* A client module can generate **thin, generics-aware wrappers** like `ServiceClientResponse<Page<CustomerDto>>`.

> Scope: Spring MVC (WebMVC) + Springdoc. No WebFlux.

---

## 1) Result overview

After this guide, your service will:

* Expose endpoints returning `ServiceResponse<T>` where **success bodies** look like:

  ```json
  {
    "data": { /* T */ },
    "meta": { "serverTime": "2025-01-01T12:34:56Z", "sort": [] }
  }
  ```

* Publish Swagger UI and `/v3/api-docs(.yaml)` with **composed wrapper schemas** for every `T`.

* Include vendor extensions on those composed schemas:

    * `x-api-wrapper: true`
    * `x-api-wrapper-datatype: <T>`
    * `x-data-container: <Container>` (e.g., `Page`)
    * `x-data-item: <Item>` (e.g., `CustomerDto`)

* Emit non‑2xx responses as **`ProblemDetail`** (RFC7807) instead of custom error envelopes.

---

## 2) Add dependencies (pom.xml)

```xml
<dependencies>
  <!-- Web + Bean Validation -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>

  <!-- Springdoc (OpenAPI 3.1 + Swagger UI) -->
  <dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.13</version>
  </dependency>

  <!-- optional: configuration processor for metadata hints -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
    <optional>true</optional>
  </dependency>

  <!-- test (optional) -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
  </dependency>
</dependencies>
```

> **Component scan:** Ensure all `common.openapi` configuration classes are under your main application’s scan base
> package. If not, adjust `@SpringBootApplication(scanBasePackages = { ... })` accordingly.

---

## 3) Create the success response envelope & primitives

**`common/api/response/ServiceResponse.java`** (success bodies only → `{ data, meta }`)

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

**`common/api/response/Meta.java`**

```java
package <your.base>.common.api.response;

import java.time.Instant;
import java.util.List;

public record Meta(Instant serverTime, List<Sort> sort) {
  public static Meta now() { return new Meta(null, Instant.now(), List.of()); }
  public static Meta now(List<Sort> sort) { return new Meta(null, Instant.now(), sort == null ? List.of() : List.copyOf(sort)); }
}
```

**`common/api/response/Sort.java`** (example shape)

```java
package <your.base>.common.api.response;

public record Sort(SortField field, SortDirection direction) {}
```

**`common/api/response/SortField.java` / `SortDirection.java`**

```java
public enum SortField { CUSTOMER_ID, NAME, EMAIL }
public enum SortDirection { ASC, DESC }
```

**`common/api/response/Page.java`** (generic container for lists/pagination)

```java
package <your.base>.common.api.response;

import java.util.List;

public record Page<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext,
    boolean hasPrev) {

  public static <T> Page<T> of(List<T> content, int page, int size, long totalElements) {
    List<T> safe = content == null ? List.of() : List.copyOf(content);
    int totalPages = (int) Math.ceil((double) totalElements / Math.max(1, size));
    boolean hasNext = page + 1 < totalPages;
    boolean hasPrev = page > 0;
    return new Page<>(safe, page, size, totalElements, totalPages, hasNext, hasPrev);
  }
}
```

> ❗ Error responses are **not** wrapped in `ServiceResponse`. Use **`ProblemDetail`** (RFC7807) via a
> `@RestControllerAdvice`.

---

## 4) OpenAPI base envelope + vendor extensions (updated)

Create the following under `common/openapi/`.

**`OpenApiSchemas.java`** (updated keys + nested/container hints)

```java
package <your.base>.common.openapi;

import <your.base>.common.api.response.ServiceResponse;

public final class OpenApiSchemas {
  // Property keys
  public static final String PROP_DATA = "data";
  public static final String PROP_META = "meta";

  // Base envelope schema names
  public static final String SCHEMA_SERVICE_RESPONSE = ServiceResponse.class.getSimpleName();
  public static final String SCHEMA_SERVICE_RESPONSE_VOID = SCHEMA_SERVICE_RESPONSE + "Void";

  // Other shared schemas (optional but recommended)
  public static final String SCHEMA_META = "Meta";
  public static final String SCHEMA_SORT = "Sort";
  public static final String SCHEMA_PROBLEM_DETAIL = "ProblemDetail";

  // Vendor extensions
  public static final String EXT_API_WRAPPER = "x-api-wrapper";
  public static final String EXT_API_WRAPPER_DATATYPE = "x-api-wrapper-datatype";
  public static final String EXT_CLASS_EXTRA_ANNOTATION = "x-class-extra-annotation";

  // Nested/container awareness
  public static final String EXT_DATA_CONTAINER = "x-data-container"; // e.g. "Page"
  public static final String EXT_DATA_ITEM = "x-data-item";           // e.g. "CustomerDto"

  private OpenApiSchemas() {}
}
```

**`SwaggerResponseCustomizer.java`** — define the **base** `ServiceResponse` once (with `data` & `meta`).

```java
package <your.base>.common.openapi;

import static <your.base>.common.openapi.OpenApiSchemas.*;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerResponseCustomizer {
  @Bean
  public OpenApiCustomizer responseEnvelopeSchemas() {
    return openApi -> {
      var schemas = openApi.getComponents().getSchemas();
      if (!schemas.containsKey(SCHEMA_SERVICE_RESPONSE)) {
        var metaRef = new Schema<>().$ref("#/components/schemas/" + SCHEMA_META);
        schemas.put(
          SCHEMA_SERVICE_RESPONSE,
          new ObjectSchema()
            .addProperty(PROP_DATA, new Schema<>())
            .addProperty(PROP_META, metaRef)
        );
      }
      if (!schemas.containsKey(SCHEMA_SERVICE_RESPONSE_VOID)) {
        var metaRef = new Schema<>().$ref("#/components/schemas/" + SCHEMA_META);
        schemas.put(
          SCHEMA_SERVICE_RESPONSE_VOID,
          new ObjectSchema()
            .addProperty(PROP_DATA, new ObjectSchema())
            .addProperty(PROP_META, metaRef)
        );
      }
      // Ensure Meta exists (simplified; you can generate from class as well)
      schemas.computeIfAbsent(SCHEMA_META, k -> new ObjectSchema()
        .addProperty("serverTime", new Schema<>().type("string").format("date-time"))
        .addProperty("sort", new Schema<>().type("array"))
      );
    };
  }
}
```

**`ApiResponseSchemaFactory.java`** — compose a wrapper per `T` and attach vendor extensions.

```java
package <your.base>.common.openapi;

import static <your.base>.common.openapi.OpenApiSchemas.*;

import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;

public final class ApiResponseSchemaFactory {
  private ApiResponseSchemaFactory() {}

  public static Schema<?> createComposedWrapper(String dataRefName) {
    return createComposedWrapper(dataRefName, null);
  }

  public static Schema<?> createComposedWrapper(String dataRefName, String classExtraAnnotation) {
    var schema = new ComposedSchema();
    schema.setAllOf(List.of(
      new Schema<>().$ref("#/components/schemas/" + SCHEMA_SERVICE_RESPONSE),
      new ObjectSchema().addProperty(PROP_DATA, new Schema<>().$ref("#/components/schemas/" + dataRefName))
    ));
    schema.addExtension(EXT_API_WRAPPER, true);
    schema.addExtension(EXT_API_WRAPPER_DATATYPE, dataRefName);
    if (classExtraAnnotation != null && !classExtraAnnotation.isBlank()) {
      schema.addExtension(EXT_CLASS_EXTRA_ANNOTATION, classExtraAnnotation);
    }
    return schema;
  }
}
```

**`introspector/ResponseTypeIntrospector.java`** — unwrap return types until `ServiceResponse<T>`.

```java
package <your.base>.common.openapi.introspector;

import <your.base>.common.api.response.ServiceResponse;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncTask;

@Component
public final class ResponseTypeIntrospector {
  private static final Logger log = LoggerFactory.getLogger(ResponseTypeIntrospector.class);
  private static final int MAX_UNWRAP_DEPTH = 8;
  private static final Set<String> REACTOR = Set.of("reactor.core.publisher.Mono", "reactor.core.publisher.Flux");

  public Optional<String> extractDataRefName(Method method) {
    if (method == null) return Optional.empty();
    ResolvableType type = ResolvableType.forMethodReturnType(method);
    type = unwrapToServiceResponse(type);
    Class<?> raw = type.resolve();
    if (raw == null || !ServiceResponse.class.isAssignableFrom(raw) || !type.hasGenerics()) return Optional.empty();
    Class<?> dataClass = type.getGeneric(0).resolve();
    return Optional.ofNullable(dataClass).map(Class::getSimpleName);
  }

  private ResolvableType unwrapToServiceResponse(ResolvableType type) {
    for (int i=0; i<MAX_UNWRAP_DEPTH; i++) {
      Class<?> raw = type.resolve();
      if (raw == null || ServiceResponse.class.isAssignableFrom(raw)) return type;
      ResolvableType next = nextLayer(type, raw);
      if (next == null) return type;
      type = next;
    }
    return type;
  }

  private ResolvableType nextLayer(ResolvableType cur, Class<?> raw) {
    if (ResponseEntity.class.isAssignableFrom(raw)) return cur.getGeneric(0);
    if (CompletionStage.class.isAssignableFrom(raw) || Future.class.isAssignableFrom(raw)) return cur.getGeneric(0);
    if (DeferredResult.class.isAssignableFrom(raw) || WebAsyncTask.class.isAssignableFrom(raw)) return cur.getGeneric(0);
    if (REACTOR.contains(raw.getName())) return cur.getGeneric(0);
    return null;
  }
}
```

**`autoreg/AutoWrapperSchemaCustomizer.java`** — register composed wrappers and add nested container hints.

```java
package <your.base>.common.openapi.autoreg;

import <your.base>.common.openapi.ApiResponseSchemaFactory;
import <your.base>.common.openapi.OpenApiSchemas;
import <your.base>.common.openapi.introspector.ResponseTypeIntrospector;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.JsonSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.*;
import java.util.stream.Collectors;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class AutoWrapperSchemaCustomizer {
  private static final String SCHEMA_REF_PREFIX = "#/components/schemas/";
  private static final String CONTENT = "content";

  private final Set<String> dataRefs;
  private final String classExtraAnnotation;
  private final Set<String> genericContainers;

  public AutoWrapperSchemaCustomizer(
      ListableBeanFactory beanFactory,
      ResponseTypeIntrospector introspector,
      @Value("${app.openapi.wrapper.class-extra-annotation:}") String classExtraAnnotation,
      @Value("${app.openapi.wrapper.generic-containers:Page}") String genericContainersProp) {

    this.dataRefs = beanFactory.getBeansOfType(RequestMappingHandlerMapping.class).values().stream()
      .flatMap(rmh -> rmh.getHandlerMethods().values().stream())
      .map(HandlerMethod::getMethod)
      .map(introspector::extractDataRefName)
      .flatMap(Optional::stream)
      .collect(Collectors.toCollection(LinkedHashSet::new));

    this.classExtraAnnotation = (classExtraAnnotation == null || classExtraAnnotation.isBlank()) ? null : classExtraAnnotation;

    this.genericContainers = Arrays.stream(genericContainersProp.split(","))
      .map(String::trim).filter(s -> !s.isEmpty())
      .collect(Collectors.toUnmodifiableSet());
  }

  @Bean
  public OpenApiCustomizer autoResponseWrappers() {
    return openApi -> dataRefs.forEach(ref -> {
      String wrapperName = OpenApiSchemas.SCHEMA_SERVICE_RESPONSE + ref;
      openApi.getComponents().addSchemas(wrapperName, ApiResponseSchemaFactory.createComposedWrapper(ref, classExtraAnnotation));
      enrichWrapperExtensions(openApi, wrapperName, ref);
    });
  }

  private void enrichWrapperExtensions(OpenAPI openApi, String wrapperName, String dataRefName) {
    String container = matchContainer(dataRefName);
    if (container == null) return;

    Map<String, Schema> schemas = openApi.getComponents() != null ? openApi.getComponents().getSchemas() : null;
    if (schemas == null) return;

    Schema<?> raw = schemas.get(dataRefName);
    Schema<?> containerSchema = resolveObjectLikeSchema(schemas, raw, new LinkedHashSet<>());
    if (containerSchema == null) return;

    String itemName = extractItemNameFromSchema(containerSchema);
    if (itemName == null) return;

    Schema<?> wrapper = schemas.get(wrapperName);
    if (wrapper == null) return;

    wrapper.addExtension(OpenApiSchemas.EXT_DATA_CONTAINER, container);
    wrapper.addExtension(OpenApiSchemas.EXT_DATA_ITEM, itemName);
  }

  private Schema<?> resolveObjectLikeSchema(Map<String, Schema> schemas, Schema<?> schema, Set<String> visited) {
    if (schema == null) return null;
    Schema<?> cur = derefIfNeeded(schemas, schema, visited);
    if (cur == null) return null;

    if (isObjectLike(cur)) return cur;

    if (cur instanceof ComposedSchema cs && cs.getAllOf() != null) {
      for (Schema<?> s : cs.getAllOf()) {
        Schema<?> resolved = resolveObjectLikeSchema(schemas, s, visited);
        if (resolved != null) return resolved;
      }
    }
    return null;
  }

  private boolean isObjectLike(Schema<?> s) {
    return (s instanceof ObjectSchema) || "object".equals(s.getType()) || (s.getProperties() != null && !s.getProperties().isEmpty());
  }

  private Schema<?> derefIfNeeded(Map<String, Schema> schemas, Schema<?> s, Set<String> visited) {
    if (s == null) return null;
    String ref = s.get$ref();
    if (ref == null || !ref.startsWith(SCHEMA_REF_PREFIX)) return s;
    String name = ref.substring(SCHEMA_REF_PREFIX.length());
    if (!visited.add(name)) return null; // cycle guard
    return schemas.get(name);
  }

  private String extractItemNameFromSchema(Schema<?> containerSchema) {
    Map<String, Schema> props = containerSchema.getProperties();
    if (props == null) return null;

    Schema<?> content = props.get(CONTENT);
    if (content == null) return null;

    Schema<?> items = null;
    if (content instanceof ArraySchema arr) items = arr.getItems();
    else if ("array".equals(content.getType())) items = content.getItems();
    else if (content instanceof JsonSchema js && js.getTypes() != null && js.getTypes().contains("array")) items = js.getItems();

    if (items == null) return null;
    String itemRef = items.get$ref();
    if (itemRef == null || !itemRef.startsWith(SCHEMA_REF_PREFIX)) return null;
    return itemRef.substring(SCHEMA_REF_PREFIX.length());
  }

  private String matchContainer(String dataRefName) {
    return genericContainers.stream().filter(dataRefName::startsWith).findFirst().orElse(null);
  }
}
```

**`OpenApiConfig.java`** — optional: set title/version/server URL.

```java
package <your.base>.common.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  @Value("${app.openapi.version:${project.version:unknown}}")
  private String version;
  @Value("${app.openapi.base-url:}")
  private String baseUrl;

  @Bean
  public OpenAPI serviceOpenAPI() {
    var openapi = new OpenAPI().info(new Info().title("Your Service API").version(version).description("Generic responses via OpenAPI"));
    if (baseUrl != null && !baseUrl.isBlank()) openapi.addServersItem(new Server().url(baseUrl).description("Local service URL"));
    return openapi;
  }
}
```

---

## 5) Application configuration (application.yml)

```yaml
server:
  port: 8084
  servlet:
    context-path: /your-service

spring:
  application:
    name: your-service
  profiles:
    active: local

app:
  openapi:
    version: @project.version@
    base-url: "http://localhost:${server.port}${server.servlet.context-path:}"
    # wrapper:
    #   class-extra-annotation: "@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)"
    #   generic-containers: "Page"  # comma-separated if you add more

springdoc:
  default-consumes-media-type: application/json
  default-produces-media-type: application/json
```

---

## 6) Return `ServiceResponse<T>` from controllers (examples)

```java
@RestController
@RequestMapping(value = "/v1/customers", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
class CustomerController {
  private final CustomerService customerService;
  public CustomerController(CustomerService customerService) { this.customerService = customerService; }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ServiceResponse<CustomerDto>> createCustomer(@Valid @RequestBody CustomerCreateRequest request) {
    CustomerDto created = customerService.createCustomer(request);
    URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.customerId()).toUri();
    return ResponseEntity.created(location).body(ServiceResponse.ok(created));
  }

  @GetMapping("/{customerId}")
  public ResponseEntity<ServiceResponse<CustomerDto>> getCustomer(@PathVariable @Min(1) Integer customerId) {
    CustomerDto dto = customerService.getCustomer(customerId);
    return ResponseEntity.ok(ServiceResponse.ok(dto));
  }

  @GetMapping
  public ResponseEntity<ServiceResponse<Page<CustomerDto>>> getCustomers(
      @ModelAttribute CustomerSearchCriteria criteria,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "5") @Min(1) @Max(10) int size,
      @RequestParam(defaultValue = "customerId") SortField sortBy,
      @RequestParam(defaultValue = "asc") SortDirection direction) {
    var paged = customerService.getCustomers(criteria, page, size, sortBy, direction);
    var meta = Meta.now(List.of(new Sort(sortBy, direction)));
    return ResponseEntity.ok(ServiceResponse.ok(paged, meta));
  }
}
```

> **Errors:** use `@RestControllerAdvice` to convert validation and domain exceptions to **`ProblemDetail`**.

---

## 7) Run & verify

1. Start your service and open:

    * Swagger UI → `http://localhost:8084/your-service/swagger-ui/index.html`
    * OpenAPI JSON → `http://localhost:8084/your-service/v3/api-docs`
    * OpenAPI YAML → `http://localhost:8084/your-service/v3/api-docs.yaml`
2. In **Schemas**, confirm you see:

    * `ServiceResponse` (with `data`, `meta`)
    * `Meta`, `Sort` (and your DTOs)
    * `ServiceResponse<YourDto>` composed wrappers **with** vendor extensions (`x-api-wrapper`, `x-api-wrapper-datatype`, optional `x-class-extra-annotation`).
3. For paged endpoints, wrappers for `Page<YourDto>` should include `x-data-container: Page` and `x-data-item: YourDto`.

---

## 8) What the client generator relies on

* Base `ServiceResponse` schema (`data`, `meta`).
* Composed wrapper schema per `T` with extensions:

    * `x-api-wrapper: true`
    * `x-api-wrapper-datatype: <T>`
    * *(optional)* `x-class-extra-annotation`
* **If `T` is a container** (e.g., `Page<CustomerDto>`), also include:

    * `x-data-container: Page`
    * `x-data-item: CustomerDto`

These allow a Mustache overlay to emit thin classes that extend `ServiceClientResponse<T>` (and nested generics like `ServiceClientResponse<Page<CustomerDto>>`).

---

## 9) Common pitfalls (updated)

* **Still returning `status/message/errors` in success bodies** → migrate to `{ data, meta }`.
* **No composed wrappers** → ensure controller methods actually return `ServiceResponse<T>` and that `AutoWrapperSchemaCustomizer` is loaded.
* **Missing Meta schema** → include it (see `SwaggerResponseCustomizer`) or let Springdoc derive it from your `Meta` record.
* **Wrong `$ref` names** → by default Springdoc uses simple class names; if you customize schema names, adapt the introspector.
* **Paged detection fails** → check `app.openapi.wrapper.generic-containers` includes your container name (default: `Page`).

---

## 10) Suggested folder map

```
src/main/java/<your/base>/
  common/api/response/
    Meta.java
    Page.java
    Sort.java
    SortDirection.java
    SortField.java
    ServiceResponse.java
  common/openapi/
    OpenApiSchemas.java
    SwaggerResponseCustomizer.java
    ApiResponseSchemaFactory.java
    OpenApiConfig.java
    introspector/
      ResponseTypeIntrospector.java
    autoreg/
      AutoWrapperSchemaCustomizer.java
  api/controller/
    YourControllers...
  api/error/ (optional)
    YourProblemDetailHandlers...
```

That’s it — your service now publishes a **generics‑aware**, `ProblemDetail`‑friendly OpenAPI spec ready for client generation.

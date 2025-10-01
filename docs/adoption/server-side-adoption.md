---
layout: default
title: Server-Side Adoption
parent: Adoption Guides
nav_order: 1
---

# Adopt the server‑side pieces in your own Spring Boot service (MVC + Springdoc)

**Purpose:** Copy the minimal set of classes/config from `customer-service` into your own microservice so that:

* You return **`ServiceResponse<T>`** from controllers.
* The OpenAPI spec **auto-registers wrapper schemas** (e.g., `ServiceResponseCustomerDto`) with vendor extensions.
* A client module can later generate **thin, generics-aware wrappers**.

> Scope: Spring MVC (WebMVC) + Springdoc. No WebFlux.

---

## 1) Result overview

After this guide, your service will:

* Expose CRUD (or your own endpoints) returning `ServiceResponse<T>`.
* Publish Swagger UI and `/v3/api-docs(.yaml)` with **composed wrapper schemas**.
* Include vendor extensions:

    * `x-api-wrapper: true`
    * `x-api-wrapper-datatype: <YourDto>`
    * *(optional)* `x-class-extra-annotation: "@..."`

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

Add your usual build plugins (compiler, surefire/failsafe, jacoco) as you prefer.

> **Note:** Make sure all `common.openapi` configuration classes are inside your main
> Spring Boot application’s component scan (same base package or a sub-package).
> If you place them elsewhere, adjust `@SpringBootApplication(scanBasePackages=...)`
> to include their package.

---

## 3) Create the generic response envelope

**`common/api/response/ServiceResponse.java`**

```java
package

<your.base>.common.api.response;

import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;

public record ServiceResponse<T>(int status, String message, T data, List<ErrorDetail> errors) {
    public static <T> ServiceResponse<T> ok(T data) {
        return new ServiceResponse<>(HttpStatus.OK.value(), "OK", data, Collections.emptyList());
    }

    public static <T> ServiceResponse<T> of(HttpStatus status, String message, T data) {
        return new ServiceResponse<>(status.value(), message, data, Collections.emptyList());
    }

    public static <T> ServiceResponse<T> error(HttpStatus status, String message) {
        return new ServiceResponse<>(status.value(), message, null, Collections.emptyList());
    }

    public static <T> ServiceResponse<T> error(HttpStatus status, String message, List<ErrorDetail> errors) {
        return new ServiceResponse<>(status.value(), message, null, errors != null ? errors : Collections.emptyList());
    }
}
```

**`common/api/response/ErrorDetail.java`**

```java
package <your.base>.common.api.response;

public record ErrorDetail(String errorCode, String message) {
}
```

> **Note:** Ensure ServiceResponse and ErrorDetail are in a package visible to both controllers and OpenAPI config (
> e.g., common.api.response).
> If you place them in a different package, make sure springdoc picks them up in schema generation.

> You can keep your existing error model; only the field names `status`, `message`, `data`, `errors` are used by the
> OpenAPI base envelope below.

---

## 4) OpenAPI base envelope + vendor extensions

Create the following under `common/openapi/`.

**`OpenApiSchemas.java`**

```java
package

<your.base>.common.openapi;

import <your.base>.common.api.response.ServiceResponse;

public final class OpenApiSchemas {
    // Base envelope schema names
    public static final String SCHEMA_SERVICE_RESPONSE = ServiceResponse.class.getSimpleName();
    public static final String SCHEMA_SERVICE_RESPONSE_VOID = SCHEMA_SERVICE_RESPONSE + "Void";

    // Common property keys
    public static final String PROP_STATUS = "status";
    public static final String PROP_MESSAGE = "message";
    public static final String PROP_ERRORS = "errors";
    public static final String PROP_ERROR_CODE = "errorCode";
    public static final String PROP_DATA = "data";

    // Vendor extension keys
    public static final String EXT_API_WRAPPER = "x-api-wrapper";
    public static final String EXT_API_WRAPPER_DATATYPE = "x-api-wrapper-datatype";
    public static final String EXT_CLASS_EXTRA_ANNOTATION = "x-class-extra-annotation";

    private OpenApiSchemas() {
    }
}
```

**`SwaggerResponseCustomizer.java`** — defines the *base* `ServiceResponse` envelope as a schema once.

```java
package <your.base>.common.openapi;

import static <your.base>.common.openapi.OpenApiSchemas.*;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerResponseCustomizer {

  @Bean
  public OpenApiCustomizer responseEnvelopeSchemas() {
    return openApi -> {
      if (!openApi.getComponents().getSchemas().containsKey(SCHEMA_SERVICE_RESPONSE)) {
        openApi.getComponents().addSchemas(
          SCHEMA_SERVICE_RESPONSE,
          new ObjectSchema()
            .addProperty(PROP_STATUS, new IntegerSchema().format("int32"))
            .addProperty(PROP_MESSAGE, new StringSchema())
            .addProperty(PROP_ERRORS, new ArraySchema().items(
                new ObjectSchema()
                  .addProperty(PROP_ERROR_CODE, new StringSchema())
                  .addProperty(PROP_MESSAGE, new StringSchema()))));
      }
      if (!openApi.getComponents().getSchemas().containsKey(SCHEMA_SERVICE_RESPONSE_VOID)) {
        openApi.getComponents().addSchemas(
          SCHEMA_SERVICE_RESPONSE_VOID,
          new ObjectSchema()
            .addProperty(PROP_STATUS, new IntegerSchema().format("int32"))
            .addProperty(PROP_MESSAGE, new StringSchema())
            .addProperty(PROP_DATA, new ObjectSchema())
            .addProperty(PROP_ERRORS, new ArraySchema().items(
                new ObjectSchema()
                  .addProperty(PROP_ERROR_CODE, new StringSchema())
                  .addProperty(PROP_MESSAGE, new StringSchema()))));
      }
    };
  }
}
```

**`ApiResponseSchemaFactory.java`** — builds a *composed* wrapper per concrete `T` (e.g., `CustomerDto`).

```java
package

<your.base>.common.openapi;

import static <your.base>.common.openapi.OpenApiSchemas.*;

import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;

public final class ApiResponseSchemaFactory {
    private ApiResponseSchemaFactory() {
    }

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

**`ResponseTypeIntrospector.java`** — unwraps return types until it finds `ServiceResponse<T>` and extracts `T`.

```java
package <your.base>.common.openapi.introspector;

import <your.base>.common.api.response.ServiceResponse;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncTask;

@Component
public final class ResponseTypeIntrospector {
  private static final Logger log = LoggerFactory.getLogger(ResponseTypeIntrospector.class);
  private static final int MAX_UNWRAP_DEPTH = 8;
  private static final Set<String> REACTOR_WRAPPERS = Set.of("reactor.core.publisher.Mono", "reactor.core.publisher.Flux");

  public Optional<String> extractDataRefName(Method method) {
    if (method == null) return Optional.empty();
    ResolvableType type = ResolvableType.forMethodReturnType(method);
    type = unwrapToServiceResponse(type);

    Class<?> raw = type.resolve();
    if (raw == null || !ServiceResponse.class.isAssignableFrom(raw)) return Optional.empty();
    if (!type.hasGenerics()) return Optional.empty();

    Class<?> dataClass = type.getGeneric(0).resolve();
    Optional<String> ref = Optional.ofNullable(dataClass).map(Class::getSimpleName);

    if (log.isDebugEnabled()) {
      log.debug("Introspected method [{}]: wrapper [{}], data [{}]", method.toGenericString(), raw.getSimpleName(), ref.orElse("<none>"));
    }
    return ref;
  }

  private ResolvableType unwrapToServiceResponse(ResolvableType type) {
    for (int guard = 0; guard < MAX_UNWRAP_DEPTH; guard++) {
      Class<?> raw = type.resolve();
      if (raw == null || ServiceResponse.class.isAssignableFrom(raw)) return type;
      ResolvableType next = nextLayer(type, raw);
      if (next == null) return type;
      type = next;
    }
    return type;
  }

  private ResolvableType nextLayer(ResolvableType current, Class<?> raw) {
    return switch (raw) {
      case Class<?> c when ResponseEntity.class.isAssignableFrom(c) -> current.getGeneric(0);
      case Class<?> c when CompletionStage.class.isAssignableFrom(c) || Future.class.isAssignableFrom(c) -> current.getGeneric(0);
      case Class<?> c when DeferredResult.class.isAssignableFrom(c) || WebAsyncTask.class.isAssignableFrom(c) -> current.getGeneric(0);
      case Class<?> c when REACTOR_WRAPPERS.contains(c.getName()) -> current.getGeneric(0);
      default -> null;
    };
  }
}
```

**`autoreg/AutoWrapperSchemaCustomizer.java`** — collects all controller return types and registers composed schemas.

```java
package <your.base>.common.openapi.autoreg;

import <your.base>.common.openapi.ApiResponseSchemaFactory;
import <your.base>.common.openapi.OpenApiSchemas;
import <your.base>.common.openapi.introspector.ResponseTypeIntrospector;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class AutoWrapperSchemaCustomizer {
  private final Set<String> dataRefs;
  private final String classExtraAnnotation;

  public AutoWrapperSchemaCustomizer(
      ListableBeanFactory beanFactory,
      ResponseTypeIntrospector introspector,
      @Value("${app.openapi.wrapper.class-extra-annotation:}") String classExtraAnnotation) {

    Set<String> refs = new LinkedHashSet<>();
    beanFactory.getBeansOfType(RequestMappingHandlerMapping.class).values()
      .forEach(rmh -> rmh.getHandlerMethods().values().stream()
        .map(HandlerMethod::getMethod)
        .forEach(m -> introspector.extractDataRefName(m).ifPresent(refs::add)));

    this.dataRefs = Collections.unmodifiableSet(refs);
    this.classExtraAnnotation = (classExtraAnnotation == null || classExtraAnnotation.isBlank()) ? null : classExtraAnnotation;
  }

  @Bean
  public OpenApiCustomizer autoResponseWrappers() {
    return openApi -> dataRefs.forEach(ref -> {
      String name = OpenApiSchemas.SCHEMA_SERVICE_RESPONSE + ref;
      openApi.getComponents().addSchemas(name, ApiResponseSchemaFactory.createComposedWrapper(ref, classExtraAnnotation));
    });
  }
}
```

**`OpenApiConfig.java`** — optional, for title/version/server URL.

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
    if (baseUrl != null && !baseUrl.isBlank()) {
      openapi.addServersItem(new Server().url(baseUrl).description("Local service URL"));
    }
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

springdoc:
  default-consumes-media-type: application/json
  default-produces-media-type: application/json
```

* Set `base-url` if you want Swagger UI to show the server URL.
* Uncomment `class-extra-annotation` only if you want to push an extra annotation to generated wrapper classes.

---

## 6) Return `ServiceResponse<T>` from controllers

Example controller method (update to your domain):

```java
@RestController
@RequestMapping(value = "/v1/customers", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
class CustomerController {
  private final CustomerService customerService;
  public CustomerController(CustomerService customerService) { this.customerService = customerService; }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<ServiceResponse<CustomerCreateResponse>> createCustomer(@Valid @RequestBody CustomerCreateRequest request) {
    CustomerDto created = customerService.createCustomer(request);
    CustomerCreateResponse body = new CustomerCreateResponse(created, Instant.now());
    URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.customerId()).toUri();
    return ResponseEntity.created(location).body(ServiceResponse.of(HttpStatus.CREATED, "CREATED", body));
  }

  @GetMapping("/{customerId}")
  public ResponseEntity<ServiceResponse<CustomerDto>> getCustomer(@PathVariable @Min(1) Integer customerId) {
    CustomerDto dto = customerService.getCustomer(customerId);
    return ResponseEntity.ok(ServiceResponse.ok(dto));
  }
}
```

> You can wrap *any* DTO type the same way. The auto‑registration picks up all controller methods that return
`ServiceResponse<T>` (including `ResponseEntity<ServiceResponse<T>>`, `CompletionStage<...>`, etc.).

---

## 7) Run & verify

1. Start your service and open:

    * Swagger UI → `http://localhost:8084/your-service/swagger-ui/index.html`
    * OpenAPI JSON → `http://localhost:8084/your-service/v3/api-docs`
    * OpenAPI YAML → `http://localhost:8084/your-service/v3/api-docs.yaml`
2. In the **Schemas** section, confirm you see:

    * `ServiceResponse`
    * `ServiceResponseVoid`
    * `ServiceResponse<YourDto>` generated as **composed** schemas with `x-api-wrapper` vendor extensions.

---

## 8) What the client generator relies on

* `ServiceResponse` base envelope schema (added by `SwaggerResponseCustomizer`).
* A composed schema per `T` with extensions:

    * `x-api-wrapper: true`
    * `x-api-wrapper-datatype: <YourDtoName>`
    * *(optional)* `x-class-extra-annotation`
* These let the client module generate **thin wrappers** that extend `ServiceClientResponse<T>`.

---

## 9) Common pitfalls

* **No composed wrappers appear:** ensure your controllers actually return `ServiceResponse<T>` and that
  `AutoWrapperSchemaCustomizer` is loaded (it’s a `@Configuration`).
* **Wrong `data` `$ref`:** the DTO class name must match the schema name Springdoc emits (usually the simple type name).
  If you use custom schema names, adapt `extractDataRefName` to your naming.
* **Profiles/paths:** if you change `context-path` or port, also update `app.openapi.base-url`.
* **Extra annotations:** if you don’t need additional annotations on generated client wrappers, keep
  `class-extra-annotation` **unset**.

---

## 10) Minimal folder map (suggested)

```
src/main/java/<your/base>/
  common/api/response/
    ErrorDetail.java
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
```

That’s it — your service now publishes a spec that is **generics-aware** and ready for client generation.

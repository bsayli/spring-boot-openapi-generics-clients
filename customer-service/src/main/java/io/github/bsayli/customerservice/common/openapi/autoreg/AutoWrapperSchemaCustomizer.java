package io.github.bsayli.customerservice.common.openapi.autoreg;

import io.github.bsayli.apicontract.paging.Page;
import io.github.bsayli.customerservice.common.openapi.ApiResponseSchemaFactory;
import io.github.bsayli.customerservice.common.openapi.OpenApiSchemas;
import io.github.bsayli.customerservice.common.openapi.introspector.ResponseTypeIntrospector;
import io.swagger.v3.oas.models.Components;
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

/**
 * Registers composed OpenAPI wrapper schemas for contract-aware responses and enriches them with
 * vendor extensions used by client-side templates.
 *
 * <p>This customizer runs during OpenAPI generation (Springdoc). It does not change runtime
 * behavior and does not affect the JSON payload. Its sole responsibility is to make the published
 * specification explicit and deterministic for a small, explicitly supported set of response
 * shapes.
 *
 * <h2>How it works</h2>
 *
 * <ol>
 *   <li>Collects data schema references from controller methods via {@link
 *       ResponseTypeIntrospector}.
 *   <li>For each detected reference (e.g. {@code CustomerDto} or {@code PageCustomerDto}),
 *       registers a composed schema named {@code ServiceResponse{RefName}} (for example, {@code
 *       ServiceResponseCustomerDto}).
 *   <li>Adds vendor extensions that allow client-side templates to emit thin wrapper classes
 *       instead of duplicating response envelope fields.
 * </ol>
 *
 * <h2>Vendor extensions</h2>
 *
 * <ul>
 *   <li>{@code x-api-wrapper: true} marks the schema as a <em>contract-aware wrapper</em> intended
 *       for client-side template selection.
 *   <li>{@code x-api-wrapper-datatype: &lt;T&gt;} points to the underlying data schema name.
 *   <li>{@code x-data-container} and {@code x-data-item} are added <b>only</b> for {@code
 *       Page&lt;T&gt;} to preserve pagination semantics.
 * </ul>
 *
 * <p>Collection types such as {@code List&lt;T&gt;} or {@code Map&lt;K,V&gt;} are intentionally not
 * overridden here and remain on Springdoc / OpenAPI Generator defaults. Only {@code
 * ServiceResponse&lt;T&gt;} and {@code ServiceResponse&lt;Page&lt;T&gt;&gt;} are treated as
 * contract-aware for wrapper schema registration.
 */
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
      @Value("${app.openapi.wrapper.class-extra-annotation:}") String classExtraAnnotation) {

    this.dataRefs =
        beanFactory.getBeansOfType(RequestMappingHandlerMapping.class).values().stream()
            .flatMap(rmh -> rmh.getHandlerMethods().values().stream())
            .map(HandlerMethod::getMethod)
            .map(introspector::extractDataRefName)
            .flatMap(Optional::stream)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    this.classExtraAnnotation =
        (classExtraAnnotation == null || classExtraAnnotation.isBlank())
            ? null
            : classExtraAnnotation;

    this.genericContainers = Set.of(Page.class.getSimpleName());
  }

  @Bean
  public OpenApiCustomizer autoResponseWrappers() {
    return openApi -> {
      if (openApi.getComponents() == null) {
        openApi.setComponents(new Components());
      }
      if (openApi.getComponents().getSchemas() == null) {
        openApi.getComponents().setSchemas(new LinkedHashMap<>());
      }

      Map<String, Schema> schemas = openApi.getComponents().getSchemas();

      dataRefs.forEach(
          ref -> {
            // Guard: only wrap schemas that are already materialized by Springdoc.
            // We never invent component schemas here — this keeps the OpenAPI output deterministic
            // and prevents accidental drift between Java types and published contracts.
            if (!schemas.containsKey(ref)) {
              return; // skip
            }

            String wrapperName = OpenApiSchemas.SCHEMA_SERVICE_RESPONSE + ref;

            schemas.put(
                wrapperName,
                ApiResponseSchemaFactory.createComposedWrapper(ref, classExtraAnnotation));

            enrichWrapperExtensions(openApi, wrapperName, ref);
          });
    };
  }

  private void enrichWrapperExtensions(OpenAPI openApi, String wrapperName, String dataRefName) {
    String container = matchContainer(dataRefName);
    if (container == null) return;

    Map<String, Schema> schemas =
        (openApi.getComponents() != null) ? openApi.getComponents().getSchemas() : null;
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

  private Schema<?> resolveObjectLikeSchema(
      Map<String, Schema> schemas, Schema<?> schema, Set<String> visited) {
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
    return (s instanceof ObjectSchema)
        || "object".equals(s.getType())
        || (s.getProperties() != null && !s.getProperties().isEmpty());
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
    if (content instanceof ArraySchema arr) {
      items = arr.getItems();
    } else if ("array".equals(content.getType())) {
      items = content.getItems();
    } else if (content instanceof JsonSchema js
        && js.getTypes() != null
        && js.getTypes().contains("array")) {
      items = js.getItems();
    }
    if (items == null) return null;

    String itemRef = items.get$ref();
    if (itemRef == null || !itemRef.startsWith(SCHEMA_REF_PREFIX)) return null;

    return itemRef.substring(SCHEMA_REF_PREFIX.length());
  }

  private String matchContainer(String dataRefName) {
    return genericContainers.stream().filter(dataRefName::startsWith).findFirst().orElse(null);
  }
}

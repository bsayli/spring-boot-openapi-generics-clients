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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Registers contract-aware wrapper schemas in the published OpenAPI document.
 *
 * <p>This customizer makes Springdoc output explicit for the response shapes intentionally
 * supported by this setup. It does not change runtime payloads. Its role is limited to OpenAPI
 * enrichment so client generation can remain deterministic and reuse the shared canonical contract.
 *
 * <p>Supported wrapper registration scope:
 *
 * <ul>
 *   <li>{@code ServiceResponse<T>}
 *   <li>{@code ServiceResponse<Page<T>>}
 * </ul>
 *
 * <p>Other shapes such as {@code ServiceResponse<List<T>>}, {@code ServiceResponse<Map<K,V>>}, or
 * arbitrary nested generics are intentionally left on Springdoc and OpenAPI Generator defaults.
 *
 * <p>For each detected response data type, this customizer registers a composed wrapper schema
 * named {@code ServiceResponse{RefName}} and enriches it with vendor extensions used by client
 * templates:
 *
 * <ul>
 *   <li>{@code x-api-wrapper}
 *   <li>{@code x-api-wrapper-datatype}
 *   <li>{@code x-data-container} and {@code x-data-item} for {@code Page<T>} only
 * </ul>
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

  /**
   * Adds composed wrapper schemas for the contract-aware response types detected from controller
   * methods.
   *
   * <p>Wrapper schemas are created only when the underlying data schema already exists in
   * components. This keeps the published specification aligned with Springdoc materialization and
   * avoids inventing synthetic component schemas.
   */
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
            if (!schemas.containsKey(ref)) {
              return;
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
        openApi.getComponents() != null ? openApi.getComponents().getSchemas() : null;
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

    Schema<?> current = derefIfNeeded(schemas, schema, visited);
    if (current == null) return null;

    if (isObjectLike(current)) return current;

    if (current instanceof ComposedSchema composed && composed.getAllOf() != null) {
      for (Schema<?> candidate : composed.getAllOf()) {
        Schema<?> resolved = resolveObjectLikeSchema(schemas, candidate, visited);
        if (resolved != null) return resolved;
      }
    }

    return null;
  }

  private boolean isObjectLike(Schema<?> schema) {
    return schema instanceof ObjectSchema
        || "object".equals(schema.getType())
        || (schema.getProperties() != null && !schema.getProperties().isEmpty());
  }

  private Schema<?> derefIfNeeded(
      Map<String, Schema> schemas, Schema<?> schema, Set<String> visited) {
    if (schema == null) return null;

    String ref = schema.get$ref();
    if (ref == null || !ref.startsWith(SCHEMA_REF_PREFIX)) {
      return schema;
    }

    String name = ref.substring(SCHEMA_REF_PREFIX.length());
    if (!visited.add(name)) {
      return null;
    }

    return schemas.get(name);
  }

  private String extractItemNameFromSchema(Schema<?> containerSchema) {
    Map<String, Schema> properties = containerSchema.getProperties();
    if (properties == null) return null;

    Schema<?> content = properties.get(CONTENT);
    if (content == null) return null;

    Schema<?> items = null;
    if (content instanceof ArraySchema arraySchema) {
      items = arraySchema.getItems();
    } else if ("array".equals(content.getType())) {
      items = content.getItems();
    } else if (content instanceof JsonSchema jsonSchema
        && jsonSchema.getTypes() != null
        && jsonSchema.getTypes().contains("array")) {
      items = jsonSchema.getItems();
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
package io.github.bsayli.customerservice.common.openapi.autoreg;

import io.github.bsayli.customerservice.common.openapi.ApiResponseSchemaFactory;
import io.github.bsayli.customerservice.common.openapi.OpenApiSchemas;
import io.github.bsayli.customerservice.common.openapi.introspector.ResponseTypeIntrospector;
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

    this.genericContainers =
        Arrays.stream(genericContainersProp.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toUnmodifiableSet());
  }

  @Bean
  public OpenApiCustomizer autoResponseWrappers() {
    return openApi ->
        dataRefs.forEach(
            ref -> {
              String wrapperName = OpenApiSchemas.SCHEMA_SERVICE_RESPONSE + ref;
              openApi
                  .getComponents()
                  .addSchemas(
                      wrapperName,
                      ApiResponseSchemaFactory.createComposedWrapper(ref, classExtraAnnotation));
              enrichWrapperExtensions(openApi, wrapperName, ref);
            });
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

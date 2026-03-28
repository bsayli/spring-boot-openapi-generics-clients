package io.github.bsayli.openapi.generics.server.core.customizer;

import io.github.bsayli.openapi.generics.server.core.schema.contract.PropertyNames;
import io.github.bsayli.openapi.generics.server.core.schema.contract.SchemaNames;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.*;
import java.util.LinkedHashMap;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;

/**
 * Registers canonical base schemas required by the contract-aware response model.
 *
 * <p>This customizer defines the foundational envelope structures used by all generated wrapper
 * schemas:
 *
 * <ul>
 *   <li>{@code Sort}
 *   <li>{@code Meta}
 *   <li>{@code ServiceResponse}
 *   <li>{@code ServiceResponseVoid}
 * </ul>
 *
 * <p>These schemas form the base layer upon which all {@code ServiceResponse<T>} wrappers are
 * composed.
 *
 * <p><b>Important:</b>
 *
 * <ul>
 *   <li>Schema names are derived from {@link SchemaNames}
 *   <li>Property names are derived from {@link PropertyNames}
 *   <li>No hardcoded contract strings are allowed
 * </ul>
 *
 * <p>This customizer is:
 *
 * <ul>
 *   <li><b>Idempotent</b> → safe to run multiple times
 *   <li><b>Non-invasive</b> → does not override existing schemas
 *   <li><b>Contract-aligned</b> → reflects API contract as-is
 * </ul>
 *
 * <p>This class does not affect runtime behavior — only OpenAPI output.
 */
public class BaseSchemaCustomizer implements OpenApiCustomizer {

  private static final String COMPONENTS_SCHEMAS = "#/components/schemas/";

  @Override
  public void customise(io.swagger.v3.oas.models.OpenAPI openApi) {

    ensureComponents(openApi);

    var schemas = openApi.getComponents().getSchemas();

    registerSort(schemas);
    registerMeta(schemas);
    registerServiceResponse(schemas);
    registerServiceResponseVoid(schemas);
  }

  private void ensureComponents(io.swagger.v3.oas.models.OpenAPI openApi) {
    if (openApi.getComponents() == null) {
      openApi.setComponents(new Components());
    }

    if (openApi.getComponents().getSchemas() == null) {
      openApi.getComponents().setSchemas(new LinkedHashMap<>());
    }
  }

  private void registerSort(java.util.Map<String, Schema> schemas) {
    if (!schemas.containsKey(SchemaNames.SORT)) {
      schemas.put(
          SchemaNames.SORT,
          new ObjectSchema()
              .addProperty("field", new StringSchema())
              .addProperty("direction", new StringSchema()._enum(List.of("asc", "desc"))));
    }
  }

  private void registerMeta(java.util.Map<String, Schema> schemas) {
    if (!schemas.containsKey(SchemaNames.META)) {
      schemas.put(
          SchemaNames.META,
          new ObjectSchema()
              .addProperty("serverTime", new StringSchema().format("date-time"))
              .addProperty(
                  "sort", new ArraySchema().items(new Schema<>().$ref(ref(SchemaNames.SORT)))));
    }
  }

  private void registerServiceResponse(java.util.Map<String, Schema> schemas) {
    if (!schemas.containsKey(SchemaNames.SERVICE_RESPONSE)) {
      schemas.put(
          SchemaNames.SERVICE_RESPONSE,
          new ObjectSchema()
              .addProperty(PropertyNames.DATA, new ObjectSchema())
              .addProperty(PropertyNames.META, new Schema<>().$ref(ref(SchemaNames.META))));
    }
  }

  private void registerServiceResponseVoid(java.util.Map<String, Schema> schemas) {
    if (!schemas.containsKey(SchemaNames.SERVICE_RESPONSE_VOID)) {
      schemas.put(
          SchemaNames.SERVICE_RESPONSE_VOID,
          new ObjectSchema()
              .addProperty(PropertyNames.DATA, new ObjectSchema())
              .addProperty(PropertyNames.META, new Schema<>().$ref(ref(SchemaNames.META))));
    }
  }

  private String ref(String schemaName) {
    return COMPONENTS_SCHEMAS + schemaName;
  }
}

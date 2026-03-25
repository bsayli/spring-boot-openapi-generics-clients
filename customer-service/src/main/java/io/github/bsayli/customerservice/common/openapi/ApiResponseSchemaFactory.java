package io.github.bsayli.customerservice.common.openapi;

import static io.github.bsayli.customerservice.common.openapi.OpenApiSchemas.*;

import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;

/**
 * Factory responsible for creating contract-aware OpenAPI wrapper schemas for {@code
 * ServiceResponse<T>} shapes.
 *
 * <p>This factory does <b>not</b> change runtime serialization or controller behavior. Its sole
 * responsibility is to shape the published OpenAPI specification so that:
 *
 * <ul>
 *   <li>Response envelopes remain explicit and deterministic in the spec
 *   <li>Client generators can emit thin wrapper classes instead of duplicating envelope fields
 *   <li>The canonical {@code ServiceResponse} contract is preserved across server and client code
 * </ul>
 *
 * <p>The produced schema is a composed schema using {@code allOf}:
 *
 * <ul>
 *   <li>Base reference → {@code ServiceResponse}
 *   <li>Overlay object → overrides the {@code data} property reference
 * </ul>
 *
 * <p>Additionally, vendor extensions are attached to support generics-aware client templates:
 *
 * <ul>
 *   <li>{@code x-api-wrapper} marks the schema as a contract wrapper
 *   <li>{@code x-api-wrapper-datatype} carries the underlying payload schema name
 *   <li>{@code x-class-extra-annotation} optionally injects additional annotations into generated
 *       clients
 * </ul>
 *
 * <p>This mechanism is intentionally limited to explicitly supported response shapes and is part of
 * the architecture contract between:
 *
 * <ul>
 *   <li>Spring MVC controllers
 *   <li>OpenAPI publication
 *   <li>OpenAPI Generator client templates
 * </ul>
 */
public final class ApiResponseSchemaFactory {

  private ApiResponseSchemaFactory() {}

  public static Schema<?> createComposedWrapper(String dataRefName) {
    return createComposedWrapper(dataRefName, null);
  }

  public static Schema<?> createComposedWrapper(String dataRefName, String classExtraAnnotation) {
    var schema = new ComposedSchema();
    schema.setAllOf(
        List.of(
            new Schema<>().$ref("#/components/schemas/" + SCHEMA_SERVICE_RESPONSE),
            new ObjectSchema()
                .addProperty(
                    PROP_DATA, new Schema<>().$ref("#/components/schemas/" + dataRefName))));

    schema.addExtension(EXT_API_WRAPPER, true);
    schema.addExtension(EXT_API_WRAPPER_DATATYPE, dataRefName);

    if (classExtraAnnotation != null && !classExtraAnnotation.isBlank()) {
      schema.addExtension(EXT_CLASS_EXTRA_ANNOTATION, classExtraAnnotation);
    }
    return schema;
  }
}

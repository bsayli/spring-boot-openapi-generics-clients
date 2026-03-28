package io.github.bsayli.openapi.generics.server.core.schema;

import io.github.bsayli.openapi.generics.server.core.schema.contract.PropertyNames;
import io.github.bsayli.openapi.generics.server.core.schema.contract.SchemaNames;
import io.github.bsayli.openapi.generics.server.core.schema.contract.VendorExtensions;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;

/**
 * Factory responsible for creating contract-aware OpenAPI wrapper schemas for {@code
 * ServiceResponse<T>} based response types.
 *
 * <p>This factory defines how a concrete response shape such as:
 *
 * <pre>
 *   ServiceResponse&lt;CustomerDto&gt;
 *   ServiceResponse&lt;Page&lt;CustomerDto&gt;&gt;
 * </pre>
 *
 * is represented in the OpenAPI specification.
 *
 * <p><b>Key design principle:</b>
 *
 * <ul>
 *   <li>Do not duplicate envelope structure
 *   <li>Reuse canonical {@code ServiceResponse} schema
 *   <li>Override only the {@code data} property via composition
 * </ul>
 *
 * <p>The generated schema uses {@code allOf} composition:
 *
 * <ul>
 *   <li>Base reference → {@code ServiceResponse}
 *   <li>Overlay object → redefines {@code data} field with concrete type
 * </ul>
 *
 * <p>Example output:
 *
 * <pre>
 * ServiceResponseCustomerDto:
 *   allOf:
 *     - $ref: "#/components/schemas/ServiceResponse"
 *     - type: object
 *       properties:
 *         data:
 *           $ref: "#/components/schemas/CustomerDto"
 * </pre>
 *
 * <p><b>Vendor extensions:</b>
 *
 * <ul>
 *   <li>{@code x-api-wrapper} → marks schema as contract wrapper
 *   <li>{@code x-api-wrapper-datatype} → underlying data type name
 *   <li>{@code x-class-extra-annotation} → optional generator hint
 * </ul>
 *
 * <p><b>Important:</b>
 *
 * <ul>
 *   <li>This class is purely specification-level
 *   <li>Does not affect runtime serialization
 *   <li>Does not inspect schema graph (handled by enricher)
 * </ul>
 *
 * <p>This factory is intentionally minimal and deterministic. All conditional logic (e.g. container
 * awareness) is handled externally.
 */
public final class ServiceResponseSchemaFactory {

  private static final String SCHEMA_PREFIX = "#/components/schemas/";

  private ServiceResponseSchemaFactory() {}

  /** Creates a wrapper schema without additional class annotations. */
  public static Schema<?> createComposedWrapper(String dataRefName) {
    return createComposedWrapper(dataRefName, null);
  }

  /**
   * Creates a composed OpenAPI schema representing {@code ServiceResponse<T>}.
   *
   * <p>The resulting schema:
   *
   * <ul>
   *   <li>Reuses the canonical {@code ServiceResponse} base schema
   *   <li>Overrides only the {@code data} field with {@code dataRefName}
   *   <li>Attaches vendor extensions for client generation
   * </ul>
   *
   * @param dataRefName name of the concrete data schema (must exist in components)
   * @param classExtraAnnotation optional annotation hint for generated clients
   * @return composed OpenAPI schema
   */
  public static Schema<?> createComposedWrapper(String dataRefName, String classExtraAnnotation) {

    String dataRef = buildRef(dataRefName);

    var schema = new ComposedSchema();

    schema.setAllOf(
        List.of(
            new Schema<>().$ref(buildRef(SchemaNames.SERVICE_RESPONSE)),
            new ObjectSchema().addProperty(PropertyNames.DATA, new Schema<>().$ref(dataRef))));

    // ---- vendor extensions (contract metadata)
    schema.addExtension(VendorExtensions.API_WRAPPER, Boolean.TRUE);
    schema.addExtension(VendorExtensions.API_WRAPPER_DATATYPE, dataRefName);

    if (hasText(classExtraAnnotation)) {
      schema.addExtension(VendorExtensions.CLASS_EXTRA_ANNOTATION, classExtraAnnotation);
    }

    return schema;
  }

  /** Builds a safe OpenAPI $ref string. */
  private static String buildRef(String schemaName) {
    return SCHEMA_PREFIX + schemaName;
  }

  /** Lightweight null/blank check without Spring dependency. */
  private static boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}

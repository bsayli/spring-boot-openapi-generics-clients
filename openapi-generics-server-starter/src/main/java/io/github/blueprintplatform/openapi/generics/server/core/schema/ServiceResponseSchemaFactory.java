package io.github.blueprintplatform.openapi.generics.server.core.schema;

import io.github.blueprintplatform.openapi.generics.server.core.schema.contract.PropertyNames;
import io.github.blueprintplatform.openapi.generics.server.core.schema.contract.SchemaNames;
import io.github.blueprintplatform.openapi.generics.server.core.schema.contract.VendorExtensions;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;

/**
 * Factory responsible for creating contract-aware OpenAPI wrapper schemas for {@code
 * ServiceResponse<T>} based response types.
 *
 * <p>This factory is the <b>single authoritative source</b> of wrapper schema structure.
 *
 * <h2>Key Responsibilities</h2>
 *
 * <ul>
 *   <li>Create wrapper schemas using {@code allOf} composition
 *   <li>Attach required vendor extensions for contract semantics
 * </ul>
 *
 * <h2>Design Principles</h2>
 *
 * <ul>
 *   <li><b>Single source of truth</b> → wrapper structure is defined only here
 *   <li><b>Deterministic</b> → same input produces identical schema
 *   <li><b>No duplication</b> → reuses canonical {@code ServiceResponse}
 *   <li><b>No patching</b> → schemas are always created, never modified
 * </ul>
 *
 * <h2>Composition Model</h2>
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
 * <h2>Vendor Extensions</h2>
 *
 * <ul>
 *   <li>{@code x-api-wrapper} → marks schema as wrapper
 *   <li>{@code x-api-wrapper-datatype} → underlying data type
 *   <li>{@code x-class-extra-annotation} → optional generator hint
 * </ul>
 *
 * <h2>Important</h2>
 *
 * <ul>
 *   <li>This class is responsible for <b>creation only</b>
 *   <li>It does NOT inspect or modify existing schemas
 *   <li>It assumes full ownership of wrapper schema structure
 * </ul>
 *
 * <h2>Architectural Note</h2>
 *
 * <p>This factory operates under a strict rule:
 *
 * <pre>
 * Existing schemas are non-authoritative and must be replaced, not patched.
 * </pre>
 *
 * <p>This aligns with the pipeline design:
 *
 * <ul>
 *   <li>No merge
 *   <li>No partial fixes
 *   <li>Only deterministic reconstruction
 * </ul>
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
   *   <li>Reuses the canonical {@code ServiceResponse} base schema</li>
   *   <li>Overrides only the {@code data} field with {@code dataRefName}</li>
   *   <li>Attaches required vendor extensions</li>
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
                    new ObjectSchema()
                            .addProperty(PropertyNames.DATA, new Schema<>().$ref(dataRef))
            )
    );

    // extensions are part of CREATION → authoritative
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
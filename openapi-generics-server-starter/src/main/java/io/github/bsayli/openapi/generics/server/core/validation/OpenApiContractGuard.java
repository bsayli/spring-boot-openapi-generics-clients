package io.github.bsayli.openapi.generics.server.core.validation;

import static io.github.bsayli.openapi.generics.server.core.schema.contract.PropertyNames.DATA;
import static io.github.bsayli.openapi.generics.server.core.schema.contract.SchemaNames.*;
import static io.github.bsayli.openapi.generics.server.core.schema.contract.VendorExtensions.*;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Map;

/**
 * Fail-fast validator ensuring that generated OpenAPI output adheres to
 * the generics-aware response contract.
 *
 * <p>This component represents the <b>final validation stage</b> of the
 * OpenAPI processing pipeline.
 *
 * <h2>Responsibilities</h2>
 *
 * <ul>
 *   <li>Validate presence of required base schemas</li>
 *   <li>Validate structural correctness of wrapper schemas</li>
 *   <li>Ensure required vendor extensions are present</li>
 * </ul>
 *
 * <h2>Pipeline Role</h2>
 *
 * <p>This class is invoked by {@code OpenApiPipelineOrchestrator}
 * after all schema generation and enrichment steps are completed.
 *
 * <h2>Design Principles</h2>
 *
 * <ul>
 *   <li><b>Fail-fast</b> → throws exception on any contract violation</li>
 *   <li><b>Minimal</b> → validates only critical invariants</li>
 *   <li><b>Deterministic</b> → no heuristics or reflection</li>
 * </ul>
 *
 * <h2>Non-goals</h2>
 *
 * <ul>
 *   <li>No deep schema graph validation</li>
 *   <li>No attempt to fix invalid schemas</li>
 * </ul>
 *
 * <p>This class is framework-independent and operates purely on the OpenAPI model.
 */
public class OpenApiContractGuard {

  /**
   * Executes validation on the given OpenAPI document.
   *
   * @param openApi OpenAPI document
   */
  public void validate(OpenAPI openApi) {

    Map<String, Schema> schemas = getSchemas(openApi);

    validateBaseSchemas(schemas);
    validateWrapperSchemas(schemas);
  }

  // -------------------------------------------------------------------------
  // Base schema validation
  // -------------------------------------------------------------------------

  private void validateBaseSchemas(Map<String, Schema> schemas) {

    requireSchema(schemas, SERVICE_RESPONSE);
    requireSchema(schemas, SERVICE_RESPONSE_VOID);
    requireSchema(schemas, META);
    requireSchema(schemas, SORT);
  }

  private void requireSchema(Map<String, Schema> schemas, String name) {
    if (!schemas.containsKey(name)) {
      throw new IllegalStateException(
              "Missing required OpenAPI schema: '" + name + "'");
    }
  }

  // -------------------------------------------------------------------------
  // Wrapper validation
  // -------------------------------------------------------------------------

  private void validateWrapperSchemas(Map<String, Schema> schemas) {

    schemas.forEach((name, schema) -> {

      if (!isWrapper(schema)) {
        return;
      }

      validateWrapperExtensions(name, schema);
      validateWrapperStructure(name, schema);
    });
  }

  private boolean isWrapper(Schema<?> schema) {
    return schema.getExtensions() != null
            && Boolean.TRUE.equals(schema.getExtensions().get(API_WRAPPER));
  }

  private void validateWrapperExtensions(String name, Schema<?> schema) {

    Object dataType = schema.getExtensions().get(API_WRAPPER_DATATYPE);

    if (dataType == null) {
      throw new IllegalStateException(
              "Wrapper schema '" + name +
                      "' is missing required extension: " + API_WRAPPER_DATATYPE);
    }
  }

  private void validateWrapperStructure(String name, Schema<?> schema) {

    if (schema.getAllOf() == null || schema.getAllOf().isEmpty()) {
      throw new IllegalStateException(
              "Wrapper schema '" + name + "' must use allOf composition");
    }

    boolean hasDataProperty =
            schema.getAllOf().stream()
                    .filter(s -> s.getProperties() != null)
                    .anyMatch(s -> s.getProperties().containsKey(DATA));

    if (!hasDataProperty) {
      throw new IllegalStateException(
              "Wrapper schema '" + name +
                      "' must define '" + DATA + "' property");
    }
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private Map<String, Schema> getSchemas(OpenAPI openApi) {

    if (openApi.getComponents() == null
            || openApi.getComponents().getSchemas() == null) {

      throw new IllegalStateException(
              "OpenAPI components.schemas is missing");
    }

    return openApi.getComponents().getSchemas();
  }
}
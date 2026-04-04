package io.github.blueprintplatform.openapi.generics.server.core.validation;

import static io.github.blueprintplatform.openapi.generics.server.core.schema.contract.PropertyNames.DATA;
import static io.github.blueprintplatform.openapi.generics.server.core.schema.contract.SchemaNames.*;
import static io.github.blueprintplatform.openapi.generics.server.core.schema.contract.VendorExtensions.*;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static final Logger log = LoggerFactory.getLogger(OpenApiContractGuard.class);

  /**
   * Executes validation on the given OpenAPI document.
   *
   * @param openApi OpenAPI document
   */
  public void validate(OpenAPI openApi) {
    log.debug("OpenAPI contract validation started");

    Map<String, Schema> schemas = getSchemas(openApi);

    validateBaseSchemas(schemas);
    validateWrapperSchemas(schemas);

    log.debug("OpenAPI contract validation completed successfully");
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
      log.error("Missing required base schema '{}'", name);
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
      log.error("Wrapper '{}' missing required extension '{}'", name, API_WRAPPER_DATATYPE);
      throw new IllegalStateException(
              "Wrapper schema '" + name +
                      "' is missing required extension: " + API_WRAPPER_DATATYPE);
    }
  }

  private void validateWrapperStructure(String name, Schema<?> schema) {

    if (schema.getAllOf() == null || schema.getAllOf().isEmpty()) {
      log.error("Wrapper '{}' has invalid structure: missing allOf composition", name);
      throw new IllegalStateException(
              "Wrapper schema '" + name + "' must use allOf composition");
    }

    boolean hasDataProperty =
            schema.getAllOf().stream()
                    .filter(s -> s.getProperties() != null)
                    .anyMatch(s -> s.getProperties().containsKey(DATA));

    if (!hasDataProperty) {
      log.error("Wrapper '{}' missing required property '{}'", name, DATA);
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

      log.error("OpenAPI validation failed: components.schemas is missing");

      throw new IllegalStateException(
              "OpenAPI components.schemas is missing");
    }

    return openApi.getComponents().getSchemas();
  }
}
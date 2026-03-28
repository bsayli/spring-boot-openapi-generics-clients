package io.github.bsayli.openapi.generics.server.core.validation;

import static io.github.bsayli.openapi.generics.server.core.schema.contract.PropertyNames.DATA;
import static io.github.bsayli.openapi.generics.server.core.schema.contract.SchemaNames.*;
import static io.github.bsayli.openapi.generics.server.core.schema.contract.VendorExtensions.*;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;

/**
 * Fail-fast validator ensuring that generated OpenAPI output adheres to the generics-aware response
 * contract.
 *
 * <p>This guard performs a minimal but critical validation pass after all schema customizers have
 * been applied.
 *
 * <h2>Purpose</h2>
 *
 * <ul>
 *   <li>Detect contract violations early (startup time)
 *   <li>Prevent silent schema corruption
 *   <li>Protect client generation integrity
 * </ul>
 *
 * <h2>Validation scope</h2>
 *
 * <ul>
 *   <li>Presence of base schemas (ServiceResponse, Meta, Sort)
 *   <li>Wrapper schemas must declare required vendor extensions
 *   <li>Wrapper schemas must contain {@code data} property
 * </ul>
 *
 * <h2>Design principles</h2>
 *
 * <ul>
 *   <li><b>Fail-fast</b> → throws exception on violation
 *   <li><b>Minimal</b> → validates only critical invariants
 *   <li><b>Deterministic</b> → no heuristics or reflection
 * </ul>
 *
 * <h2>Non-goals</h2>
 *
 * <ul>
 *   <li>No deep schema graph validation
 *   <li>No attempt to fix invalid schemas
 * </ul>
 *
 * <p>This class is intended for controlled environments where contract integrity is more important
 * than leniency.
 */
public class OpenApiContractGuard implements OpenApiCustomizer {

  @Override
  public void customise(OpenAPI openApi) {

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
      throw new IllegalStateException("Missing required OpenAPI schema: '" + name + "'");
    }
  }

  // -------------------------------------------------------------------------
  // Wrapper validation
  // -------------------------------------------------------------------------

  private void validateWrapperSchemas(Map<String, Schema> schemas) {

    schemas.forEach(
        (name, schema) -> {
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
          "Wrapper schema '" + name + "' is missing required extension: " + API_WRAPPER_DATATYPE);
    }
  }

  private void validateWrapperStructure(String name, Schema<?> schema) {

    if (schema.getAllOf() == null || schema.getAllOf().isEmpty()) {
      throw new IllegalStateException("Wrapper schema '" + name + "' must use allOf composition");
    }

    boolean hasDataProperty =
        schema.getAllOf().stream()
            .filter(s -> s.getProperties() != null)
            .anyMatch(s -> s.getProperties().containsKey(DATA));

    if (!hasDataProperty) {
      throw new IllegalStateException(
          "Wrapper schema '" + name + "' must define '" + DATA + "' property");
    }
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private Map<String, Schema> getSchemas(OpenAPI openApi) {

    if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {

      throw new IllegalStateException("OpenAPI components.schemas is missing");
    }

    return openApi.getComponents().getSchemas();
  }
}

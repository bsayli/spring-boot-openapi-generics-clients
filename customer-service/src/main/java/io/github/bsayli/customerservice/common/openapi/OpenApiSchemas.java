package io.github.bsayli.customerservice.common.openapi;

import io.github.bsayli.apicontract.envelope.ServiceResponse;

/**
 * Central registry of canonical OpenAPI schema names and vendor-extension keys used by the
 * contract-aware response envelope model.
 *
 * <p>This class defines the shared vocabulary between:
 *
 * <ul>
 *   <li>Spring MVC controllers publishing {@code ServiceResponse<T>}
 *   <li>Springdoc OpenAPI customizers enriching the generated specification
 *   <li>OpenAPI Generator client templates producing thin wrapper classes
 * </ul>
 *
 * <p>The constants declared here ensure that schema composition, vendor-extension signaling, and
 * client code generation remain deterministic and aligned across server and client modules.
 *
 * <p>These constants are intentionally centralized to avoid schema-name drift and vendor-extension
 * inconsistencies across modules that participate in the response-envelope contract.
 *
 * <p>This class does not affect runtime serialization or HTTP behavior. Its purpose is purely
 * specification-level consistency.
 */
public final class OpenApiSchemas {

  // ---- Common property keys
  public static final String PROP_DATA = "data";
  public static final String PROP_META = "meta";

  // ---- Base envelopes
  public static final String SCHEMA_SERVICE_RESPONSE = ServiceResponse.class.getSimpleName();
  public static final String SCHEMA_SERVICE_RESPONSE_VOID = SCHEMA_SERVICE_RESPONSE + "Void";

  // ---- Other shared schemas
  public static final String SCHEMA_META = "Meta";
  public static final String SCHEMA_SORT = "Sort";

  // ---- Vendor extensions
  public static final String EXT_API_WRAPPER = "x-api-wrapper";
  public static final String EXT_API_WRAPPER_DATATYPE = "x-api-wrapper-datatype";
  public static final String EXT_CLASS_EXTRA_ANNOTATION = "x-class-extra-annotation";

  // ---- Vendor extensions (nested/container awareness)
  public static final String EXT_DATA_CONTAINER = "x-data-container"; // e.g. "Page"
  public static final String EXT_DATA_ITEM = "x-data-item"; // e.g. "CustomerDto"

  private OpenApiSchemas() {}
}

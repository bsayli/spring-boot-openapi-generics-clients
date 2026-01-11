package io.github.bsayli.customerservice.common.openapi;

import io.github.bsayli.apicontract.envelope.ServiceResponse;

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
  public static final String SCHEMA_PROBLEM_DETAIL = "ProblemDetail";

  // ---- Vendor extensions
  public static final String EXT_API_WRAPPER = "x-api-wrapper";
  public static final String EXT_API_WRAPPER_DATATYPE = "x-api-wrapper-datatype";
  public static final String EXT_CLASS_EXTRA_ANNOTATION = "x-class-extra-annotation";

  // ---- Vendor extensions (nested/container awareness)
  public static final String EXT_DATA_CONTAINER = "x-data-container"; // e.g. "Page"
  public static final String EXT_DATA_ITEM = "x-data-item"; // e.g. "CustomerDto"

  private OpenApiSchemas() {}
}

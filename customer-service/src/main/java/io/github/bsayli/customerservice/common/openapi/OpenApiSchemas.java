package io.github.bsayli.customerservice.common.openapi;

import io.github.bsayli.customerservice.common.api.response.ServiceResponse;

public final class OpenApiSchemas {
  // Common property keys
  public static final String PROP_STATUS = "status";
  public static final String PROP_MESSAGE = "message";
  public static final String PROP_ERRORS = "errors";
  public static final String PROP_ERROR_CODE = "errorCode";
  public static final String PROP_DATA = "data";

  // Base envelopes
  public static final String SCHEMA_SERVICE_RESPONSE = ServiceResponse.class.getSimpleName();
  public static final String SCHEMA_SERVICE_RESPONSE_VOID = SCHEMA_SERVICE_RESPONSE + "Void";

  // Vendor extensions
  public static final String EXT_API_WRAPPER = "x-api-wrapper";
  public static final String EXT_API_WRAPPER_DATATYPE = "x-api-wrapper-datatype";

  private OpenApiSchemas() {}
}

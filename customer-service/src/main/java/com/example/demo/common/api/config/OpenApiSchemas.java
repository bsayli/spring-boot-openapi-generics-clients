package com.example.demo.common.api.config;

public final class OpenApiSchemas {
    private OpenApiSchemas() {}

    // Common property keys
    public static final String PROP_STATUS = "status";
    public static final String PROP_MESSAGE = "message";
    public static final String PROP_ERRORS = "errors";
    public static final String PROP_ERROR_CODE = "errorCode";
    public static final String PROP_DATA = "data";

    // Base envelopes
    public static final String SCHEMA_API_RESPONSE = "ApiResponse";
    public static final String SCHEMA_API_RESPONSE_VOID = "ApiResponseVoid";

    // Vendor extensions
    public static final String EXT_API_WRAPPER = "x-api-wrapper";
    public static final String EXT_API_WRAPPER_DATATYPE = "x-api-wrapper-datatype";
}
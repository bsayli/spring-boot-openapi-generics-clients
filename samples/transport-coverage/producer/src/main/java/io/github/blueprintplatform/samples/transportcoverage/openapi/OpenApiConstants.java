package io.github.blueprintplatform.samples.transportcoverage.openapi;

public final class OpenApiConstants {
    public static final String TITLE = "OpenAPI Generics Transport Coverage API";
    public static final String DESCRIPTION =
            """
                    Transport compatibility sample for OpenAPI Generics.
                    
                    Verifies that standard OpenAPI Generator Java RestClient transport behavior coexists with
                    contract-aware ServiceResponse<T> reconstruction for multipart/form-data,
                    application/x-www-form-urlencoded, and application/octet-stream operations.
                    """;
    public static final String SERVER_DESCRIPTION = "Local transport coverage producer";

    private OpenApiConstants() {
    }
}

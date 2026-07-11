package io.github.blueprintplatform.samples.typecoverage.openapi;

public final class OpenApiConstants {

  public static final String TITLE = "ServiceResponse Type Coverage API";

  public static final String DESCRIPTION =
          """
                  Type coverage sample for the built-in ServiceResponse<T> contract.
                  
                  Verifies OpenAPI projection, generated client reconstruction, and runtime
                  deserialization for:
                  
                  - ServiceResponse<T>
                  - ServiceResponse<List<T>>
                  - ServiceResponse<Set<T>>
                  - ServiceResponse<Page<T>>
                  - ServiceResponse<Window<T>>
                  - ServiceResponse<Batch<T>>
                  
                  Covers scalar, value, enum, DTO, list, set, paged, cursor-based window,
                  and batch-oriented payload types.
                  
                  Demonstrates the built-in ServiceResponse<T> envelope with both
                  platform-provided and application-owned generic containers.
                  """;

  public static final String SERVER_DESCRIPTION = "Local ServiceResponse type coverage producer";

  private OpenApiConstants() {}
}
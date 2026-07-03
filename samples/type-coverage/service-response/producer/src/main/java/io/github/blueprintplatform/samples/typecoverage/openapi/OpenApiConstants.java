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

          Covers scalar, value, enum, DTO, list, set, and paged payload types.
          """;

  public static final String SERVER_DESCRIPTION = "Local ServiceResponse type coverage producer";

  private OpenApiConstants() {}
}
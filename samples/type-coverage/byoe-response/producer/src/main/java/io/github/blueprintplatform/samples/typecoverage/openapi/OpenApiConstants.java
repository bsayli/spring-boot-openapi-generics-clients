package io.github.blueprintplatform.samples.typecoverage.openapi;

public final class OpenApiConstants {

  public static final String TITLE = "BYOE Response Type Coverage API";

  public static final String DESCRIPTION =
      """
          Type coverage sample for user-owned response envelopes.

          Verifies OpenAPI projection, generated client reconstruction, and runtime
          deserialization for:

          - ApiResponse<T>
          - ApiResponse<List<T>>
          - ApiResponse<Set<T>>
          - ApiResponse<Page<T>>
          - ApiResponse<Paging<T>>
          - ApiResponse<Window<T>>

          Demonstrates Bring Your Own Envelope (BYOE) with built-in and
          application-defined generic containers.
          """;

  public static final String SERVER_DESCRIPTION = "Local BYOE response type coverage producer";

  private OpenApiConstants() {}
}
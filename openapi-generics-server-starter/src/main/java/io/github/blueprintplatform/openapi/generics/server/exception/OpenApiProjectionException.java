package io.github.blueprintplatform.openapi.generics.server.exception;

/**
 * Raised when OpenAPI projection cannot be completed because the expected schema structure cannot
 * be produced or resolved.
 *
 * <p>Typical failures include missing projected wrapper schemas or inconsistent schema state
 * encountered while enriching the generated OpenAPI document.
 */
public final class OpenApiProjectionException extends OpenApiGenericsServerException {

  public OpenApiProjectionException(String message) {
    super(message);
  }

  public OpenApiProjectionException(String message, Throwable cause) {
    super(message, cause);
  }
}

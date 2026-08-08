package io.github.blueprintplatform.openapi.generics.server.exception;

/**
 * Raised when OpenAPI Generics server configuration cannot be resolved into a valid runtime
 * contract.
 *
 * <p>Typical failures include invalid envelope or container type declarations, missing configured
 * classes, or unsupported configured contract shapes.
 */
public final class OpenApiGenericsConfigurationException extends OpenApiGenericsServerException {

  public OpenApiGenericsConfigurationException(String message) {
    super(message);
  }

  public OpenApiGenericsConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }
}

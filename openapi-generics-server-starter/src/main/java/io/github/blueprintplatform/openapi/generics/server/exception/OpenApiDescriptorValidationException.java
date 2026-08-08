package io.github.blueprintplatform.openapi.generics.server.exception;

/**
 * Raised when an internal OpenAPI Generics descriptor violates the invariants required by the
 * server projection model.
 *
 * <p>Typical failures include missing descriptor attributes or inconsistent container shape
 * metadata that would make deterministic projection impossible.
 */
public final class OpenApiDescriptorValidationException extends OpenApiGenericsServerException {

  public OpenApiDescriptorValidationException(String message) {
    super(message);
  }

  public OpenApiDescriptorValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}

package io.github.blueprintplatform.openapi.generics.server.exception;

/**
 * Base exception for failures raised by the OpenAPI Generics server starter.
 *
 * <p>Represents platform-specific failures that occur while resolving configuration, projecting
 * generic response contracts into OpenAPI, or validating the generated OpenAPI contract.
 */
public abstract class OpenApiGenericsServerException extends RuntimeException {

  protected OpenApiGenericsServerException(String message) {
    super(message);
  }

  protected OpenApiGenericsServerException(String message, Throwable cause) {
    super(message, cause);
  }
}

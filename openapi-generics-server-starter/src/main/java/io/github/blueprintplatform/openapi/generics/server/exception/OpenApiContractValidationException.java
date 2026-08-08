package io.github.blueprintplatform.openapi.generics.server.exception;

/**
 * Raised when the generated OpenAPI document violates the contract expected by OpenAPI Generics.
 *
 * <p>Typical failures include missing wrapper schemas, missing or inconsistent vendor extensions,
 * and required wrapper properties that are not present in the projected OpenAPI contract.
 */
public final class OpenApiContractValidationException extends OpenApiGenericsServerException {

  public OpenApiContractValidationException(String message) {
    super(message);
  }

  public OpenApiContractValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}

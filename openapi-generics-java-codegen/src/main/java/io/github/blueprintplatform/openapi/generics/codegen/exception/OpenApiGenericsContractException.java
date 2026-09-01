package io.github.blueprintplatform.openapi.generics.codegen.exception;

/**
 * Raised when OpenAPI Generics metadata in an input document cannot be reconstructed into a valid
 * Java generic contract.
 */
public final class OpenApiGenericsContractException extends RuntimeException {

  public OpenApiGenericsContractException(String message) {
    super(message);
  }
}

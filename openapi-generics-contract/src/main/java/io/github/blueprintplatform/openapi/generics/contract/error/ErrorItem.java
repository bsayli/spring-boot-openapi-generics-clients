package io.github.blueprintplatform.openapi.generics.contract.error;

/**
 * Structured error detail item used inside problem extensions.
 *
 * @param code machine-readable error code
 * @param message human-readable error message
 * @param field related field name, if applicable
 * @param resource related resource name, if applicable
 * @param id related resource identifier, if applicable
 */
public record ErrorItem(String code, String message, String field, String resource, String id) {}
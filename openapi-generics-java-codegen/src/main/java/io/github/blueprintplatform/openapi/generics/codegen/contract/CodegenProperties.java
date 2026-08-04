package io.github.blueprintplatform.openapi.generics.codegen.contract;

/**
 * Additional property keys supported by the generics-aware Java code generator.
 *
 * <p>Envelope identity is derived directly from canonical OpenAPI wrapper metadata and is therefore
 * not configured through codegen properties.
 */
public final class CodegenProperties {

  public static final String RESPONSE_CONTRACT_PREFIX = "openapi-generics.response-contract.";

  public static final String DATA_CONTAINER_PREFIX = "openapi-generics.data-container.";

  private CodegenProperties() {}
}

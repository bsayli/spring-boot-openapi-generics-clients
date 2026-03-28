package io.github.bsayli.openapi.generics.server.core.schema.contract;

/**
 * Canonical vendor extension keys used by the generics-aware OpenAPI contract.
 *
 * <p>These extensions define a <b>flat semantic layer</b> on top of OpenAPI, enabling client
 * generators to reconstruct higher-level abstractions such as:
 *
 * <ul>
 *   <li>{@code ServiceResponse<T>} (wrapper semantics)
 *   <li>{@code Page<T>} (container semantics)
 * </ul>
 *
 * <h2>Structural model</h2>
 *
 * <ul>
 *   <li>All extensions live in a <b>flat namespace</b> inside OpenAPI schemas
 *   <li>There is no hierarchy or grouping at the specification level
 *   <li>This class reflects that reality
 * </ul>
 *
 * <h2>Ownership model</h2>
 *
 * <ul>
 *   <li>This library is the <b>sole owner</b> of these extensions
 *   <li>They are NOT part of the OpenAPI specification
 *   <li>They form a custom DSL for code generation
 * </ul>
 *
 * <h2>Design principles</h2>
 *
 * <ul>
 *   <li><b>Flat over hierarchical</b> → aligned with OpenAPI model
 *   <li><b>Deterministic</b> → same input produces same extensions
 *   <li><b>Single source of truth</b> → no duplication across classes
 * </ul>
 *
 * <h2>Stability</h2>
 *
 * <ul>
 *   <li>Changing any key is a <b>breaking change</b>
 *   <li>Must be versioned carefully
 * </ul>
 *
 * <h2>Example</h2>
 *
 * <pre>
 * ServiceResponsePageCustomerDto:
 *   x-api-wrapper: true
 *   x-api-wrapper-datatype: PageCustomerDto
 *   x-data-container: Page
 *   x-data-item: CustomerDto
 * </pre>
 *
 * <p>This class contains no behavior and serves purely as a centralized vocabulary.
 */
public final class VendorExtensions {

  // -------------------------------------------------------------------------
  // Wrapper semantics
  // -------------------------------------------------------------------------

  /** Marks a schema as a contract-aware wrapper. */
  public static final String API_WRAPPER = "x-api-wrapper";

  /** Carries the underlying data type of the wrapper. */
  public static final String API_WRAPPER_DATATYPE = "x-api-wrapper-datatype";

  /** Optional extension to inject additional annotations into generated models. */
  public static final String CLASS_EXTRA_ANNOTATION = "x-class-extra-annotation";

  // -------------------------------------------------------------------------
  // Container semantics
  // -------------------------------------------------------------------------

  /** Indicates the container type of the response payload. */
  public static final String DATA_CONTAINER = "x-data-container";

  /** Indicates the item type contained within the container. */
  public static final String DATA_ITEM = "x-data-item";

  private VendorExtensions() {}
}

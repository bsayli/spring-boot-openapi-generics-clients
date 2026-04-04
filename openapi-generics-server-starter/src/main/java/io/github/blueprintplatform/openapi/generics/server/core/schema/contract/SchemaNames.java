package io.github.blueprintplatform.openapi.generics.server.core.schema.contract;

import io.github.blueprintplatform.openapi.generics.contract.envelope.Meta;
import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Sort;

/**
 * Canonical OpenAPI schema names derived directly from API contract types.
 *
 * <p>This class defines the <b>schema identity vocabulary</b> used in:
 *
 * <ul>
 *   <li>Published OpenAPI documents
 *   <li>Schema references ($ref)
 *   <li>Generated client models
 * </ul>
 *
 * <h2>Authority model</h2>
 *
 * <ul>
 *   <li><b>API contract is the single source of truth</b>
 *   <li>Schema names are <b>derived from contract classes</b>
 *   <li>No hardcoded or duplicated schema identity is allowed
 * </ul>
 *
 * <p>This eliminates dual ownership between OpenAPI and contract layers.
 *
 * <h2>Implications</h2>
 *
 * <ul>
 *   <li>Renaming a contract class (e.g. {@code Meta → ResponseMeta}) will directly change the
 *       OpenAPI schema name
 *   <li>This is considered a <b>breaking API change</b>
 *   <li>Client regeneration is expected and required after such changes
 * </ul>
 *
 * <h2>Design principle</h2>
 *
 * <ul>
 *   <li>Correctness and single source of truth are prioritized over backward compatibility
 *   <li>OpenAPI acts as a <b>projection of the contract</b>, not an authority
 * </ul>
 *
 * <p>This class contains no behavior and serves purely as a centralized vocabulary.
 */
public final class SchemaNames {

  /**
   * Base schema name for the canonical response envelope.
   *
   * <p>Derived from {@link ServiceResponse}.
   */
  public static final String SERVICE_RESPONSE = ServiceResponse.class.getSimpleName();

  /**
   * Schema name for void response envelope.
   *
   * <p>Represents {@code ServiceResponse<Void>}.
   */
  public static final String SERVICE_RESPONSE_VOID = SERVICE_RESPONSE + "Void";

  /**
   * Schema name for response metadata.
   *
   * <p>Derived from {@link Meta}.
   */
  public static final String META = Meta.class.getSimpleName();

  /**
   * Schema name for sorting descriptor.
   *
   * <p>Derived from {@link Sort}.
   */
  public static final String SORT = Sort.class.getSimpleName();

  private SchemaNames() {}
}

package io.github.blueprintplatform.openapi.generics.server.core.schema.contract;

/**
 * Canonical JSON property names for the response envelope.
 *
 * <p>These names are part of the <b>external JSON contract</b> and must remain stable.
 *
 * <h2>Design principle</h2>
 *
 * <ul>
 *   <li>Property names represent <b>semantic meaning</b>, not Java type names
 *   <li>They are intentionally <b>explicit</b> and not reflection-derived
 * </ul>
 *
 * <h2>Stability</h2>
 *
 * <ul>
 *   <li>Changing these values is a <b>breaking API change</b>
 *   <li>Client regeneration is required
 * </ul>
 */
public final class PropertyNames {

  /** JSON field for response payload */
  public static final String DATA = "data";

  /** JSON field for response metadata */
  public static final String META = "meta";

  private PropertyNames() {}
}

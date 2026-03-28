package io.github.bsayli.openapi.generics.server.core.schema;

import io.github.bsayli.apicontract.paging.Page;
import io.github.bsayli.openapi.generics.server.core.schema.contract.VendorExtensions;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.*;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Enriches generated wrapper schemas with vendor extensions for container-aware response types such
 * as {@code Page<T>}.
 *
 * <p>This component analyzes already-generated OpenAPI schemas and extracts structural metadata
 * required by client generators.
 *
 * <p>Specifically, it adds:
 *
 * <ul>
 *   <li>{@code x-data-container} → container type (e.g. "Page")
 *   <li>{@code x-data-item} → inner item type (e.g. "CustomerDto")
 * </ul>
 *
 * <p><b>Extensibility:</b>
 *
 * <ul>
 *   <li>Supported container types are configurable via {@code supportedContainers}
 *   <li>Defaults to {@code Page}
 *   <li>New containers (e.g. Slice, Window, Chunk) can be added without modifying logic
 * </ul>
 *
 * <p><b>Design constraints:</b>
 *
 * <ul>
 *   <li>No reflection-based coupling
 *   <li>No runtime dependency on container types
 *   <li>Works purely on OpenAPI schema graph
 * </ul>
 *
 * <p><b>Detection strategy:</b>
 *
 * <ul>
 *   <li>Container is inferred from schema name prefix (e.g. {@code PageCustomerDto})
 *   <li>Only explicitly configured container names are considered valid
 *   <li>False positives (e.g. {@code PagedCustomerDto}) are avoided via strict prefix matching
 * </ul>
 *
 * <p>This class is framework-independent and side-effect free except for schema enrichment.
 */
public class WrapperSchemaEnricher {

  private static final String DEFAULT_CONTAINER = Page.class.getSimpleName();
  private static final String SCHEMA_REF_PREFIX = "#/components/schemas/";
  private static final String CONTENT = "content";

  private final Set<String> supportedContainers;

  /** Creates enricher with default container support ({@code Page}). */
  public WrapperSchemaEnricher() {
    this(Set.of(DEFAULT_CONTAINER));
  }

  /**
   * Creates enricher with custom container support.
   *
   * @param supportedContainers set of container type prefixes (e.g. "Page", "Slice")
   */
  public WrapperSchemaEnricher(Set<String> supportedContainers) {
    this.supportedContainers =
        (supportedContainers == null || supportedContainers.isEmpty())
            ? Set.of(DEFAULT_CONTAINER)
            : Set.copyOf(supportedContainers);
  }

  /**
   * Enriches a wrapper schema if the underlying data type represents a supported container (e.g.
   * {@code Page<T>}).
   *
   * @param openApi OpenAPI document
   * @param wrapperName generated wrapper schema name
   * @param dataRefName underlying data schema name
   */
  public void enrich(OpenAPI openApi, String wrapperName, String dataRefName) {

    if (openApi == null || dataRefName == null || wrapperName == null) {
      return;
    }

    Map<String, Schema> schemas =
        openApi.getComponents() != null ? openApi.getComponents().getSchemas() : null;

    if (schemas == null || schemas.isEmpty()) {
      return;
    }

    String container = matchContainer(dataRefName);
    if (container == null) {
      return;
    }

    if (!schemas.containsKey(dataRefName) || !schemas.containsKey(wrapperName)) {
      return;
    }

    Schema<?> raw = schemas.get(dataRefName);
    Schema<?> containerSchema = resolveObjectLikeSchema(schemas, raw, new LinkedHashSet<>());

    if (containerSchema == null) {
      return;
    }

    String itemName = extractItemNameFromSchema(containerSchema);
    if (itemName == null) {
      return;
    }

    Schema<?> wrapper = schemas.get(wrapperName);
    if (wrapper == null) {
      return;
    }

    wrapper.addExtension(VendorExtensions.DATA_CONTAINER, container);
    wrapper.addExtension(VendorExtensions.DATA_ITEM, itemName);
  }

  /**
   * Matches container prefix from configured container set.
   *
   * <p>Example:
   *
   * <pre>
   * PageCustomerDto → Page
   * SliceCustomerDto → Slice (if configured)
   * </pre>
   *
   * <p>Strict prefix rule prevents false positives:
   *
   * <pre>
   * PagedCustomerDto → NO MATCH
   * </pre>
   */
  private String matchContainer(String dataRefName) {
    for (String container : supportedContainers) {
      if (isStrictContainerMatch(dataRefName, container)) {
        return container;
      }
    }
    return null;
  }

  /** Ensures container match is exact prefix and not a partial word match. */
  private boolean isStrictContainerMatch(String name, String container) {
    if (!name.startsWith(container)) {
      return false;
    }

    if (name.length() == container.length()) {
      return true;
    }

    char next = name.charAt(container.length());
    return Character.isUpperCase(next);
  }

  /** Resolves schema into object-like structure via: - $ref resolution - allOf traversal */
  private Schema<?> resolveObjectLikeSchema(
      Map<String, Schema> schemas, Schema<?> schema, Set<String> visited) {
    if (schema == null) return null;

    Schema<?> current = derefIfNeeded(schemas, schema, visited);
    if (current == null) return null;

    if (isObjectLike(current)) return current;

    if (current instanceof ComposedSchema composed && composed.getAllOf() != null) {
      for (Schema<?> candidate : composed.getAllOf()) {
        Schema<?> resolved = resolveObjectLikeSchema(schemas, candidate, visited);
        if (resolved != null) return resolved;
      }
    }

    return null;
  }

  private boolean isObjectLike(Schema<?> schema) {
    return schema instanceof ObjectSchema
        || "object".equals(schema.getType())
        || (schema.getProperties() != null && !schema.getProperties().isEmpty());
  }

  private Schema<?> derefIfNeeded(
      Map<String, Schema> schemas, Schema<?> schema, Set<String> visited) {
    String ref = schema.get$ref();
    if (ref == null || !ref.startsWith(SCHEMA_REF_PREFIX)) {
      return schema;
    }

    String name = ref.substring(SCHEMA_REF_PREFIX.length());
    if (!visited.add(name)) {
      return null;
    }

    return schemas.get(name);
  }

  /**
   * Extracts item type from container schema.
   *
   * <p>Expected structure:
   *
   * <pre>
   * content:
   *   type: array
   *   items:
   *     $ref: "#/components/schemas/CustomerDto"
   * </pre>
   */
  private String extractItemNameFromSchema(Schema<?> containerSchema) {

    Map<String, Schema> properties = containerSchema.getProperties();
    if (properties == null) return null;

    Schema<?> content = properties.get(CONTENT);
    if (content == null) return null;

    Schema<?> items = null;

    if (content instanceof ArraySchema arraySchema) {
      items = arraySchema.getItems();
    } else if ("array".equals(content.getType())) {
      items = content.getItems();
    } else if (content instanceof JsonSchema jsonSchema
        && jsonSchema.getTypes() != null
        && jsonSchema.getTypes().contains("array")) {
      items = jsonSchema.getItems();
    }

    if (items == null) return null;

    String itemRef = items.get$ref();
    if (itemRef == null || !itemRef.startsWith(SCHEMA_REF_PREFIX)) {
      return null;
    }

    return itemRef.substring(SCHEMA_REF_PREFIX.length());
  }
}

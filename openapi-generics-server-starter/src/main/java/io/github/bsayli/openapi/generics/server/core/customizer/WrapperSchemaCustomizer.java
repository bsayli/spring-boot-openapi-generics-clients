package io.github.bsayli.openapi.generics.server.core.customizer;

import io.github.bsayli.openapi.generics.server.core.introspection.ResponseTypeDiscoveryStrategy;
import io.github.bsayli.openapi.generics.server.core.introspection.ResponseTypeIntrospector;
import io.github.bsayli.openapi.generics.server.core.schema.ServiceResponseSchemaFactory;
import io.github.bsayli.openapi.generics.server.core.schema.WrapperSchemaEnricher;
import io.github.bsayli.openapi.generics.server.core.schema.contract.SchemaNames;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springdoc.core.customizers.OpenApiCustomizer;

/**
 * OpenAPI customizer responsible for registering contract-aware wrapper schemas for {@code
 * ServiceResponse<T>} based responses.
 *
 * <p>This class is the orchestration layer of the generics-aware OpenAPI pipeline. It coordinates:
 *
 * <ul>
 *   <li><b>Discovery</b> → {@link ResponseTypeDiscoveryStrategy}
 *   <li><b>Introspection</b> → {@link ResponseTypeIntrospector}
 *   <li><b>Schema creation</b> → {@link ServiceResponseSchemaFactory}
 *   <li><b>Schema enrichment</b> → {@link WrapperSchemaEnricher}
 * </ul>
 *
 * <p><b>Processing flow:</b>
 *
 * <ol>
 *   <li>Discover response return types from the application
 *   <li>Extract contract-aware data references (e.g. CustomerDto, PageCustomerDto)
 *   <li>Create composed wrapper schemas (ServiceResponse{Ref})
 *   <li>Register schemas in a conflict-safe (idempotent) manner
 *   <li>Enrich wrapper schemas with container metadata if applicable
 * </ol>
 *
 * <p><b>Idempotency & conflict safety:</b>
 *
 * <ul>
 *   <li>If schema does not exist → it is registered
 *   <li>If schema exists and is equivalent → ignored
 *   <li>If schema exists but differs → fails fast (configuration error)
 * </ul>
 *
 * <p>This guarantees deterministic OpenAPI output even in multi-customizer environments.
 *
 * <p>This class is intentionally:
 *
 * <ul>
 *   <li>Framework-independent
 *   <li>Stateless
 *   <li>Focused on orchestration (no deep schema logic)
 * </ul>
 */
public class WrapperSchemaCustomizer implements OpenApiCustomizer {

  private final ResponseTypeDiscoveryStrategy discoveryStrategy;
  private final ResponseTypeIntrospector introspector;
  private final WrapperSchemaEnricher enricher;
  private final String classExtraAnnotation;

  public WrapperSchemaCustomizer(
      ResponseTypeDiscoveryStrategy discoveryStrategy,
      ResponseTypeIntrospector introspector,
      WrapperSchemaEnricher enricher,
      String classExtraAnnotation) {
    this.discoveryStrategy = discoveryStrategy;
    this.introspector = introspector;
    this.enricher = enricher;
    this.classExtraAnnotation =
        (classExtraAnnotation == null || classExtraAnnotation.isBlank())
            ? null
            : classExtraAnnotation;
  }

  @Override
  public void customise(OpenAPI openApi) {

    ensureComponents(openApi);

    Map<String, Schema> schemas = openApi.getComponents().getSchemas();

    Set<String> discoveredRefs = collectDiscoveredRefs();

    discoveredRefs.forEach(
        ref -> {

          // only proceed if base schema already exists
          if (!schemas.containsKey(ref)) {
            return;
          }

          String wrapperName = SchemaNames.SERVICE_RESPONSE + ref;

          Schema<?> wrapperSchema =
              ServiceResponseSchemaFactory.createComposedWrapper(ref, classExtraAnnotation);

          registerSchemaSafely(schemas, wrapperName, wrapperSchema);

          // enrich (e.g. Page<T>)
          enricher.enrich(openApi, wrapperName, ref);
        });
  }

  /**
   * Registers a schema in a conflict-safe and idempotent manner.
   *
   * <p>Behavior:
   *
   * <ul>
   *   <li>If schema does not exist → registers it
   *   <li>If schema exists and is equivalent → no-op
   *   <li>If schema exists but differs → throws exception
   * </ul>
   *
   * <p>This prevents silent overwrites when multiple customizers attempt to register the same
   * schema.
   */
  private void registerSchemaSafely(Map<String, Schema> schemas, String name, Schema<?> newSchema) {
    Schema<?> existing = schemas.get(name);

    if (existing == null) {
      schemas.put(name, newSchema);
      return;
    }

    if (schemasEquivalent(existing, newSchema)) {
      return;
    }

    throw new IllegalStateException(
        "OpenAPI schema conflict detected for '"
            + name
            + "'. "
            + "Multiple components attempted to register different schemas for the same name.");
  }

  /**
   * Lightweight structural equivalence check for schemas.
   *
   * <p>This intentionally compares only the parts relevant for wrapper schemas:
   *
   * <ul>
   *   <li>$ref
   *   <li>allOf composition
   *   <li>vendor extensions
   * </ul>
   */
  private boolean schemasEquivalent(Schema<?> a, Schema<?> b) {
    if (a == b) return true;
    if (a == null || b == null) return false;

    return Objects.equals(a.get$ref(), b.get$ref())
        && Objects.equals(a.getAllOf(), b.getAllOf())
        && Objects.equals(a.getExtensions(), b.getExtensions());
  }

  /** Collects all contract-aware data reference names discovered from response types. */
  private Set<String> collectDiscoveredRefs() {
    Set<String> discoveredRefs = new LinkedHashSet<>();

    discoveryStrategy
        .discover()
        .forEach(type -> introspector.extractDataRefName(type).ifPresent(discoveredRefs::add));

    return discoveredRefs;
  }

  /** Ensures OpenAPI components and schemas map are initialized. */
  private void ensureComponents(OpenAPI openApi) {
    if (openApi.getComponents() == null) {
      openApi.setComponents(new Components());
    }

    if (openApi.getComponents().getSchemas() == null) {
      openApi.getComponents().setSchemas(new LinkedHashMap<>());
    }
  }
}

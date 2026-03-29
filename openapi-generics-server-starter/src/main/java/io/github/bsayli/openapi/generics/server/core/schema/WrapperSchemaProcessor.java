package io.github.bsayli.openapi.generics.server.core.schema;

import io.github.bsayli.openapi.generics.server.core.schema.contract.SchemaNames;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Map;
import java.util.Objects;

/**
 * Processes contract-aware wrapper schemas for {@code ServiceResponse<T>} structures.
 *
 * <p>This component represents the <b>processing stage</b> of the OpenAPI pipeline responsible for
 * handling wrapper schemas derived from generic response types.
 *
 * <h2>Responsibilities</h2>
 *
 * <ul>
 *   <li><b>Precondition check</b> → ensures referenced schema exists</li>
 *   <li><b>Wrapper creation</b> → builds composed schema via {@link ServiceResponseSchemaFactory}</li>
 *   <li><b>Conflict-safe registration</b> → inserts schema deterministically</li>
 *   <li><b>Enrichment</b> → applies container metadata (e.g. {@code Page<T>})</li>
 * </ul>
 *
 * <h2>Pipeline Role</h2>
 *
 * <p>This class is invoked by the pipeline orchestrator as part of the
 * <b>wrapper processing stage</b>. It does not perform discovery or orchestration.
 *
 * <h2>Design Principles</h2>
 *
 * <ul>
 *   <li><b>Single responsibility</b> → owns wrapper schema lifecycle</li>
 *   <li><b>Deterministic</b> → same input produces identical schema</li>
 *   <li><b>Fail-fast</b> → conflicting schema definitions are rejected</li>
 * </ul>
 *
 * <h2>Important</h2>
 *
 * <ul>
 *   <li>This class contains schema construction and mutation logic</li>
 *   <li>Orchestration must remain outside (see OpenApiPipelineOrchestrator)</li>
 * </ul>
 */
public class WrapperSchemaProcessor {

    private final WrapperSchemaEnricher enricher;
    private final String classExtraAnnotation;

    public WrapperSchemaProcessor(
            WrapperSchemaEnricher enricher,
            String classExtraAnnotation) {
        this.enricher = enricher;
        this.classExtraAnnotation = classExtraAnnotation;
    }

    /**
     * Processes a single contract-aware wrapper schema for the given reference.
     *
     * <p>This method performs the full lifecycle:
     *
     * <ul>
     *   <li>Validates that the base schema exists</li>
     *   <li>Creates the wrapper schema</li>
     *   <li>Registers it in a conflict-safe manner</li>
     *   <li>Applies enrichment (if applicable)</li>
     * </ul>
     *
     * @param openApi OpenAPI document
     * @param ref     referenced schema name (e.g. {@code CustomerDto})
     */
    public void process(OpenAPI openApi, String ref) {

        Map<String, Schema> schemas = openApi.getComponents().getSchemas();

        // Guard → referenced schema must exist
        if (!schemas.containsKey(ref)) {
            return;
        }

        String wrapperName = SchemaNames.SERVICE_RESPONSE + ref;

        Schema<?> wrapperSchema =
                ServiceResponseSchemaFactory.createComposedWrapper(ref, classExtraAnnotation);

        registerSchemaSafely(schemas, wrapperName, wrapperSchema);

        // Enrich (e.g. Page<T>, metadata)
        enricher.enrich(openApi, wrapperName, ref);
    }

    /**
     * Registers schema in a conflict-safe and idempotent way.
     *
     * <p>Behavior:
     *
     * <ul>
     *   <li>If schema does not exist → registers it</li>
     *   <li>If schema exists and is equivalent → no-op</li>
     *   <li>If schema exists but differs → throws exception</li>
     * </ul>
     */
    private void registerSchemaSafely(
            Map<String, Schema> schemas,
            String name,
            Schema<?> newSchema) {

        Schema<?> existing = schemas.get(name);

        if (existing == null) {
            schemas.put(name, newSchema);
            return;
        }

        if (schemasEquivalent(existing, newSchema)) {
            return;
        }

        throw new IllegalStateException(
                "OpenAPI schema conflict detected for '" + name +
                        "'. Multiple components attempted to register different schemas.");
    }

    /**
     * Lightweight structural equivalence check for schemas.
     *
     * <p>Compares only relevant parts for wrapper schemas:
     *
     * <ul>
     *   <li>$ref</li>
     *   <li>allOf composition</li>
     *   <li>vendor extensions</li>
     * </ul>
     */
    private boolean schemasEquivalent(Schema<?> a, Schema<?> b) {
        if (a == b) return true;
        if (a == null || b == null) return false;

        return Objects.equals(a.get$ref(), b.get$ref())
                && Objects.equals(a.getAllOf(), b.getAllOf())
                && Objects.equals(a.getExtensions(), b.getExtensions());
    }
}
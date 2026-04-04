package io.github.blueprintplatform.openapi.generics.server.core.schema;

import io.github.blueprintplatform.openapi.generics.server.core.schema.contract.SchemaNames;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes contract-aware wrapper schemas for {@code ServiceResponse<T>} structures.
 *
 * <p>This component represents the <b>processing stage</b> of the OpenAPI pipeline responsible for
 * handling wrapper schemas derived from generic response types.
 *
 * <h2>Responsibilities</h2>
 *
 * <ul>
 *   <li><b>Authoritative creation</b> → always rebuilds wrapper schema from contract</li>
 *   <li><b>Normalization</b> → replaces any existing schema with contract-compliant version</li>
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
 *   <li><b>Contract-driven</b> → contract is the single source of truth</li>
 *   <li><b>Authoritative overwrite</b> → existing schemas are replaced</li>
 * </ul>
 *
 * <h2>Important</h2>
 *
 * <ul>
 *   <li>Wrapper structure is ALWAYS enforced via factory</li>
 *   <li>No attempt is made to partially fix existing schemas</li>
 *   <li>Vendor extensions are applied during creation (factory responsibility)</li>
 * </ul>
 */
public class WrapperSchemaProcessor {

    private static final Logger log = LoggerFactory.getLogger(WrapperSchemaProcessor.class);

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
     * <p>This method performs the lifecycle:
     *
     * <ul>
     *   <li>Rebuilds wrapper schema from contract (authoritative)</li>
     *   <li>Replaces any existing schema</li>
     *   <li>Applies enrichment (if applicable)</li>
     * </ul>
     *
     * @param openApi OpenAPI document
     * @param ref     referenced schema name (e.g. {@code CustomerDto})
     */
    public void process(OpenAPI openApi, String ref) {

        Map<String, Schema> schemas = openApi.getComponents().getSchemas();

        String wrapperName = SchemaNames.SERVICE_RESPONSE + ref;

        boolean exists = schemas.containsKey(wrapperName);

        Schema<?> wrapper =
                ServiceResponseSchemaFactory.createComposedWrapper(ref, classExtraAnnotation);

        schemas.put(wrapperName, wrapper);

        if (exists) {
            log.debug("Wrapper schema '{}' replaced (normalized)", wrapperName);
        } else {
            log.debug("Wrapper schema '{}' created", wrapperName);
        }

        // Enrich (e.g. Page<T>, metadata)
        enricher.enrich(openApi, wrapperName, ref);
    }
}
package io.github.bsayli.openapi.generics.server.core.pipeline;

import io.github.bsayli.openapi.generics.server.core.introspection.ResponseTypeDiscoveryStrategy;
import io.github.bsayli.openapi.generics.server.core.introspection.ResponseTypeIntrospector;
import io.github.bsayli.openapi.generics.server.core.schema.WrapperSchemaProcessor;
import io.github.bsayli.openapi.generics.server.core.schema.base.BaseSchemaRegistrar;
import io.github.bsayli.openapi.generics.server.core.schema.base.SchemaGenerationControlMarker;
import io.github.bsayli.openapi.generics.server.core.validation.OpenApiContractGuard;
import io.swagger.v3.oas.models.OpenAPI;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central orchestrator for the generics-aware OpenAPI system.
 *
 * <p>This class defines the <b>explicit and deterministic execution flow</b> for transforming
 * runtime API contracts into enriched OpenAPI schemas.
 *
 * <h2>Pipeline Execution</h2>
 *
 * <pre>
 * 1. Base Schema Registration   → ensure canonical envelope schemas exist
 * 2. Discovery                 → collect response types from runtime
 * 3. Introspection             → extract contract-aware type references
 * 4. Wrapper Processing        → generate wrapper schemas (ServiceResponse<T>, etc.)
 * 5. Ignore Marking            → mark infrastructure schemas as non-generatable
 * 6. Validation                → enforce contract correctness (fail-fast)
 * </pre>
 *
 * <h2>Design Guarantees</h2>
 *
 * <ul>
 *   <li><b>Deterministic</b> → same input always produces identical OpenAPI output
 *   <li><b>Single execution path</b> → no distributed lifecycle across beans
 *   <li><b>Separation of concerns</b> → orchestration vs schema logic separation
 *   <li><b>Fail-fast</b> → invalid contract state causes immediate failure
 * </ul>
 *
 * <h2>Pipeline Semantics</h2>
 *
 * <ul>
 *   <li>{@link BaseSchemaRegistrar} → creates canonical base schemas
 *   <li>{@link WrapperSchemaProcessor} → composes generic-aware wrappers
 *   <li>{@link SchemaGenerationControlMarker} → controls generation policy (not structure)
 *   <li>{@link OpenApiContractGuard} → validates final contract integrity
 * </ul>
 *
 * <h2>Important</h2>
 *
 * <ul>
 *   <li>This class coordinates execution, it does not implement schema logic
 *   <li>All schema-related behavior is delegated to dedicated components
 *   <li>Ignore marking is applied <b>after schema creation</b> but <b>before validation</b>
 * </ul>
 */
public class OpenApiPipelineOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(OpenApiPipelineOrchestrator.class);

    /**
     * Internal execution guard to prevent multiple pipeline runs on the same OpenAPI instance.
     *
     * <p>Uses identity-based tracking to avoid leaking runtime state into the OpenAPI model.
     */
    private final Set<OpenAPI> processed =
            Collections.newSetFromMap(new IdentityHashMap<>());

    private final BaseSchemaRegistrar baseSchemaRegistrar;
  private final SchemaGenerationControlMarker schemaGenerationControlMarker;
    private final ResponseTypeDiscoveryStrategy discoveryStrategy;
    private final ResponseTypeIntrospector introspector;
    private final WrapperSchemaProcessor wrapperSchemaProcessor;
    private final OpenApiContractGuard contractGuard;

  public OpenApiPipelineOrchestrator(
      BaseSchemaRegistrar baseSchemaRegistrar,
      SchemaGenerationControlMarker schemaGenerationControlMarker,
      ResponseTypeDiscoveryStrategy discoveryStrategy,
      ResponseTypeIntrospector introspector,
      WrapperSchemaProcessor wrapperSchemaProcessor,
      OpenApiContractGuard contractGuard) {

        this.baseSchemaRegistrar = baseSchemaRegistrar;
    this.schemaGenerationControlMarker = schemaGenerationControlMarker;
        this.discoveryStrategy = discoveryStrategy;
        this.introspector = introspector;
        this.wrapperSchemaProcessor = wrapperSchemaProcessor;
        this.contractGuard = contractGuard;
    }

    /**
     * Executes the full OpenAPI transformation pipeline.
     *
     * @param openApi the OpenAPI document
     */
    public void run(OpenAPI openApi) {

        if (!processed.add(openApi)) {
            log.debug("Pipeline already executed → skipping");
            return;
        }

        log.debug("OpenAPI pipeline started");

        // 1. Base schemas
        baseSchemaRegistrar.register(openApi);

        // 2–3. Discovery + Introspection
        Set<String> refs = discoverRefs();
        log.debug("Discovered {} contract-aware response types", refs.size());

        // 4. Wrapper processing
        refs.forEach(ref -> wrapperSchemaProcessor.process(openApi, ref));
        log.debug("Processed {} wrapper schemas", refs.size());

    // 5. Ignore marking (generation control layer)
    schemaGenerationControlMarker.mark(openApi);
        log.debug("Applied ignore markers to base schemas");

        // 6. Validation
        contractGuard.validate(openApi);

        log.debug("OpenAPI pipeline completed successfully");
    }

    // -------------------------------------------------------------------------
    // Discovery + Introspection
    // -------------------------------------------------------------------------

    /**
     * Discovers and extracts contract-aware schema reference names.
     *
     * <p>This method represents the combined discovery + introspection stage.
     */
    private Set<String> discoverRefs() {
        Set<String> discoveredRefs = new LinkedHashSet<>();

        discoveryStrategy
                .discover()
                .forEach(type ->
                        introspector.extractDataRefName(type)
                                .ifPresent(discoveredRefs::add));

        return discoveredRefs;
    }
}
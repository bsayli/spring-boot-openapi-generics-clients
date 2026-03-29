package io.github.bsayli.openapi.generics.server.core.pipeline;

import io.github.bsayli.openapi.generics.server.core.introspection.ResponseTypeDiscoveryStrategy;
import io.github.bsayli.openapi.generics.server.core.introspection.ResponseTypeIntrospector;
import io.github.bsayli.openapi.generics.server.core.schema.WrapperSchemaProcessor;
import io.github.bsayli.openapi.generics.server.core.schema.base.BaseSchemaRegistrar;
import io.github.bsayli.openapi.generics.server.core.validation.OpenApiContractGuard;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Central orchestrator for the generics-aware OpenAPI system.
 *
 * <p>This class defines the <b>explicit and deterministic execution flow</b>
 * for transforming runtime API contracts into enriched OpenAPI schemas.
 *
 * <h2>Pipeline Execution</h2>
 *
 * <pre>
 * 1. Base Schema Registration   → ensure canonical envelope schemas exist
 * 2. Discovery                 → collect response types from runtime
 * 3. Introspection             → extract contract-aware type references
 * 4. Wrapper Processing        → delegate to WrapperSchemaProcessor
 * 5. Validation                → enforce contract correctness (fail-fast)
 * </pre>
 *
 * <h2>Design Guarantees</h2>
 *
 * <ul>
 *   <li><b>Deterministic</b> → same input always produces identical OpenAPI output</li>
 *   <li><b>Single execution path</b> → no distributed lifecycle across beans</li>
 *   <li><b>Separation of concerns</b> → no schema logic inside orchestrator</li>
 *   <li><b>Fail-fast</b> → invalid contract state causes immediate failure</li>
 * </ul>
 *
 * <h2>Important</h2>
 *
 * <ul>
 *   <li>This class coordinates execution, it does not implement schema logic</li>
 *   <li>All schema-related behavior is delegated to dedicated components</li>
 *   <li>Execution assumes {@link BaseSchemaRegistrar} initializes OpenAPI components</li>
 * </ul>
 */
public class OpenApiPipelineOrchestrator {

    private final BaseSchemaRegistrar baseSchemaRegistrar;
    private final ResponseTypeDiscoveryStrategy discoveryStrategy;
    private final ResponseTypeIntrospector introspector;
    private final WrapperSchemaProcessor wrapperSchemaProcessor;
    private final OpenApiContractGuard contractGuard;

    public OpenApiPipelineOrchestrator(
            BaseSchemaRegistrar baseSchemaRegistrar,
            ResponseTypeDiscoveryStrategy discoveryStrategy,
            ResponseTypeIntrospector introspector,
            WrapperSchemaProcessor wrapperSchemaProcessor,
            OpenApiContractGuard contractGuard) {

        this.baseSchemaRegistrar = baseSchemaRegistrar;
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

        // 1. Base schemas (also ensures components initialization)
        baseSchemaRegistrar.register(openApi);

        // 2–3. Discovery + Introspection
        Set<String> refs = discoverRefs();

        // 4. Wrapper processing (generation + enrichment)
        refs.forEach(ref -> wrapperSchemaProcessor.process(openApi, ref));

        // 5. Validation (fail-fast)
        contractGuard.validate(openApi);
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
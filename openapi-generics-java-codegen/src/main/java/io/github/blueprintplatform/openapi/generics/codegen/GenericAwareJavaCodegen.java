package io.github.blueprintplatform.openapi.generics.codegen;

import io.swagger.v3.oas.models.media.Schema;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.languages.JavaClientCodegen;
import org.openapitools.codegen.model.ModelsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A custom OpenAPI Generator that introduces awareness of platform-level generic models.
 *
 * <p>Models marked with {@code x-ignore-model: true} are:
 *
 * <ul>
 *   <li>Detected during {@link #fromModel(String, Schema)} phase</li>
 *   <li>Filtered out locally in {@link #postProcessModels(ModelsMap)}</li>
 *   <li>Completely removed from the global model graph in {@link #postProcessAllModels(Map)}</li>
 * </ul>
 *
 * <p>This ensures that:
 *
 * <ul>
 *   <li>Platform-owned generic types (e.g. ServiceResponse, Meta, Sort) are not generated</li>
 *   <li>But still usable as referenced types in composed/generated models</li>
 * </ul>
 *
 * <p><b>Design Principle:</b><br>
 * Java contract is the authority, OpenAPI is a projection. This generator enforces that
 * projection must not re-materialize platform-owned types.
 */
public class GenericAwareJavaCodegen extends JavaClientCodegen {

    private static final Logger log =
            LoggerFactory.getLogger(GenericAwareJavaCodegen.class);

    /**
     * Vendor extension key used in OpenAPI schemas to mark models as non-generatable.
     */
    private static final String EXT_IGNORE_MODEL = "x-ignore-model";

    /**
     * Holds model names that should be excluded from generation.
     */
    private final Set<String> ignoredModels = new HashSet<>();

    // ================================
    // PHASE 1 — MARK
    // ================================

    /**
     * Intercepts model creation and marks models that should be ignored.
     *
     * <p>This phase does NOT remove models yet. It only records intent.
     */
    @Override
    public CodegenModel fromModel(String name, Schema model) {

        CodegenModel codegenModel = super.fromModel(name, model);

        Map<String, Object> extensions =
                (model != null) ? model.getExtensions() : null;

        if (isIgnoredModel(extensions)) {
            ignoredModels.add(name);
            log.debug("Marked model as ignored: {}", name);
        }

        if (codegenModel.imports != null && !codegenModel.imports.isEmpty()) {
            codegenModel.imports.removeIf(this::shouldIgnore);
        }

        return codegenModel;
    }

    // ================================
    // PHASE 2 — LOCAL FILTER
    // ================================

    /**
     * Removes ignored models from the current processing batch.
     *
     * <p>This prevents template-level generation for those models.
     */
    @Override
    public ModelsMap postProcessModels(ModelsMap modelsMap) {

        ModelsMap result = super.postProcessModels(modelsMap);

        if (result == null || result.getModels() == null) {
            return result;
        }

        result.getModels().removeIf(modelMap -> {
            CodegenModel model = modelMap.getModel();
            return model != null && shouldIgnore(model.name);
        });

        return result;
    }

    // ================================
    // PHASE 3 — GLOBAL REMOVE (CRITICAL)
    // ================================

    /**
     * Completely removes ignored models from the global model map.
     *
     * <p>This is the critical phase which ensures:
     * <ul>
     *   <li>No file generation</li>
     *   <li>No downstream references treated as generatable models</li>
     * </ul>
     */
    @Override
    public Map<String, ModelsMap> postProcessAllModels(Map<String, ModelsMap> allModels) {

        Map<String, ModelsMap> result = super.postProcessAllModels(allModels);

        Iterator<Map.Entry<String, ModelsMap>> iterator = result.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, ModelsMap> entry = iterator.next();

            if (shouldIgnore(entry.getKey())) {
                log.debug("Removed model from generation graph: {}", entry.getKey());
                iterator.remove();
            }
        }

        return result;
    }

    /**
     * Name of the custom generator.
     */
    @Override
    public String getName() {
        return "java-generics-contract";
    }

    // ================================
    // INTERNAL HELPERS
    // ================================

    private boolean isIgnoredModel(Map<String, Object> extensions) {
        return extensions != null && Boolean.TRUE.equals(extensions.get(EXT_IGNORE_MODEL));
    }

    private boolean shouldIgnore(String modelName) {
        return ignoredModels.contains(modelName);
    }
}
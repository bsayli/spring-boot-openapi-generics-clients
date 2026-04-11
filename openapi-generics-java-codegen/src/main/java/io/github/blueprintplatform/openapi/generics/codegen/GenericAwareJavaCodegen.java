package io.github.blueprintplatform.openapi.generics.codegen;

import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.languages.JavaClientCodegen;
import org.openapitools.codegen.model.ModelsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom Java generator that integrates external contract models and generic response wrappers into
 * OpenAPI generation.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Register externally provided models (contract-first approach)
 *   <li>Exclude those models from generation
 *   <li>Inject required imports into wrapper models via vendor extensions
 *   <li>Keep generated code free of invalid/self imports
 * </ul>
 *
 * <p>Design note: This class only orchestrates the flow. Actual decisions (ignore, import
 * resolution) are delegated to dedicated components.
 */
public class GenericAwareJavaCodegen extends JavaClientCodegen {

  private static final Logger log =
          LoggerFactory.getLogger(GenericAwareJavaCodegen.class);

  private final ExternalModelRegistry registry = new ExternalModelRegistry();
  private final ModelIgnoreDecider ignoreDecider = new ModelIgnoreDecider(registry);
  private final ExternalImportResolver importResolver = new ExternalImportResolver(registry);

  /** Registers external model mappings from additionalProperties. */
  @Override
  public void processOpts() {
    super.processOpts();
    registry.register(additionalProperties);

    log.debug("Generic-aware codegen initialized with external model registry");
  }

  /** Marks models that should be ignored and cleans their imports. */
  @Override
  public CodegenModel fromModel(String name, Schema model) {
    CodegenModel cm = super.fromModel(name, model);

    if (ignoreDecider.shouldIgnore(name, model)) {
      ignoreDecider.markIgnored(name);
    }

    cleanImports(cm);
    return cm;
  }

  /** Removes ignored models and injects external imports into wrapper models. */
  @Override
  public ModelsMap postProcessModels(ModelsMap modelsMap) {
    ModelsMap result = super.postProcessModels(modelsMap);

    if (result == null || result.getModels() == null) {
      return result;
    }

    int before = result.getModels().size();

    result
            .getModels()
            .removeIf(
                    m -> {
                      CodegenModel model = m.getModel();
                      return model != null && ignoreDecider.isIgnored(model.name);
                    });

    int after = result.getModels().size();

    if (before != after) {
      log.debug("Filtered ignored models: {} -> {}", before, after);
    }

    result
            .getModels()
            .forEach(
                    m -> {
                      CodegenModel model = m.getModel();
                      if (model != null) {
                        importResolver.apply(model);
                      }
                    });

    return result;
  }

  /** Ensures ignored models are fully removed from the generation graph. */
  @Override
  public Map<String, ModelsMap> postProcessAllModels(Map<String, ModelsMap> allModels) {
    Map<String, ModelsMap> result = super.postProcessAllModels(allModels);

    int before = result.size();

    result.entrySet().removeIf(e -> ignoreDecider.isIgnored(e.getKey()));

    int after = result.size();

    if (before != after) {
      log.debug("Removed ignored models from global model graph: {} -> {}", before, after);
    }

    return result;
  }

  @Override
  public String getName() {
    return "java-generics-contract";
  }

  /** Removes imports that reference ignored models. */
  private void cleanImports(CodegenModel model) {
    if (model.imports == null || model.imports.isEmpty()) return;
    model.imports.removeIf(ignoreDecider::isIgnored);
  }
}
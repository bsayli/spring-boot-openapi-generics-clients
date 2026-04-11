package io.github.blueprintplatform.openapi.generics.codegen;

import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.languages.JavaClientCodegen;
import org.openapitools.codegen.model.ModelsMap;

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

  private final ExternalModelRegistry registry = new ExternalModelRegistry();
  private final ModelIgnoreDecider ignoreDecider = new ModelIgnoreDecider(registry);
  private final ExternalImportResolver importResolver = new ExternalImportResolver(registry);

  /** Registers external model mappings from additionalProperties. */
  @Override
  public void processOpts() {
    super.processOpts();
    registry.register(additionalProperties);
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

    result
        .getModels()
        .removeIf(
            m -> {
              CodegenModel model = m.getModel();
              return model != null && ignoreDecider.isIgnored(model.name);
            });

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

    result.entrySet().removeIf(e -> ignoreDecider.isIgnored(e.getKey()));
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

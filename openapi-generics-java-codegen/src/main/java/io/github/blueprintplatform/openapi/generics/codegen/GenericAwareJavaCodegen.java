package io.github.blueprintplatform.openapi.generics.codegen;

import io.github.blueprintplatform.openapi.generics.codegen.external.ExternalImportResolver;
import io.github.blueprintplatform.openapi.generics.codegen.external.ExternalModelRegistry;
import io.github.blueprintplatform.openapi.generics.codegen.filtering.ModelIgnoreDecider;
import io.github.blueprintplatform.openapi.generics.codegen.metadata.EnvelopeMetadataResolver;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;
import java.util.Map;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.languages.JavaClientCodegen;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OpenAPI Generator extension that reconstructs generic Java response contracts from canonical
 * OpenAPI vendor metadata.
 *
 * <p>The generator extends the standard Java client generator with support for:
 *
 * <ul>
 *   <li>excluding models marked as externally provided or infrastructure-owned
 *   <li>injecting imports for application-owned contract types
 *   <li>deriving envelope identity from {@code x-api-wrapper-type}
 *   <li>preparing wrapper metadata consumed by the custom Java model templates
 *   <li>removing ignored models and their imports from the generated model graph
 * </ul>
 *
 * <p>Envelope identity is resolved independently for each wrapper schema, allowing a single OpenAPI
 * document to describe multiple envelope contracts without client-side envelope configuration.
 */
public class GenericAwareJavaCodegen extends JavaClientCodegen {

  private static final Logger log = LoggerFactory.getLogger(GenericAwareJavaCodegen.class);

  private final ExternalModelRegistry registry = new ExternalModelRegistry();
  private final ModelIgnoreDecider ignoreDecider = new ModelIgnoreDecider(registry);
  private final ExternalImportResolver importResolver = new ExternalImportResolver(registry);
  private final EnvelopeMetadataResolver envelopeResolver = new EnvelopeMetadataResolver();

  @Override
  public void processOpts() {
    super.processOpts();

    registry.register(additionalProperties);

    log.debug(
        "Generic-aware codegen initialized with external model registry and contract-driven"
            + " metadata");
  }

  @Override
  public CodegenModel fromModel(String name, Schema model) {
    CodegenModel codegenModel = super.fromModel(name, model);

    if (ignoreDecider.shouldIgnore(name, model)) {
      ignoreDecider.markIgnored(name);
    }

    return codegenModel;
  }

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
            modelMap -> {
              CodegenModel model = modelMap.getModel();
              return model != null && ignoreDecider.isIgnored(model.name);
            });

    int after = result.getModels().size();

    if (before != after) {
      log.debug("Filtered ignored models: {} -> {}", before, after);
    }

    result
        .getModels()
        .forEach(
            modelMap -> {
              CodegenModel model = modelMap.getModel();

              if (model != null) {
                importResolver.apply(model);
                envelopeResolver.apply(model);
              }
            });

    return result;
  }

  @Override
  public Map<String, ModelsMap> postProcessAllModels(Map<String, ModelsMap> allModels) {
    int before = allModels.size();

    allModels.entrySet().removeIf(entry -> ignoreDecider.isIgnored(entry.getKey()));

    int after = allModels.size();

    if (before != after) {
      log.debug("Removed ignored models from global model graph: {} -> {}", before, after);
    }

    Map<String, ModelsMap> result = super.postProcessAllModels(allModels);

    result.entrySet().removeIf(entry -> ignoreDecider.isIgnored(entry.getKey()));
    result.values().forEach(this::cleanIgnoredImports);

    return result;
  }

  @Override
  public String getName() {
    return "java-generics-contract";
  }

  private void cleanIgnoredImports(ModelsMap modelsMap) {
    if (modelsMap == null) {
      return;
    }

    removeIgnoredImports(modelsMap.get("imports"));

    if (modelsMap.getModels() == null) {
      return;
    }

    modelsMap.getModels().forEach(this::cleanIgnoredImports);
  }

  private void cleanIgnoredImports(ModelMap modelMap) {
    if (modelMap == null) {
      return;
    }

    removeIgnoredImports(modelMap.get("imports"));

    CodegenModel model = modelMap.getModel();

    if (model == null || model.imports == null || model.imports.isEmpty()) {
      return;
    }

    model.imports.removeIf(this::isIgnoredImportName);
  }

  private void removeIgnoredImports(Object imports) {
    if (imports instanceof List<?> list) {
      list.removeIf(this::isIgnoredImport);
    }
  }

  private boolean isIgnoredImport(Object value) {
    if (value instanceof Map<?, ?> map) {
      Object imported = map.get("import");
      return imported instanceof String text && isIgnoredImportName(text);
    }

    return value instanceof String text && isIgnoredImportName(text);
  }

  private boolean isIgnoredImportName(String imported) {
    int lastDot = imported.lastIndexOf('.');
    String simpleName = lastDot >= 0 ? imported.substring(lastDot + 1) : imported;

    return ignoreDecider.isIgnored(simpleName);
  }
}

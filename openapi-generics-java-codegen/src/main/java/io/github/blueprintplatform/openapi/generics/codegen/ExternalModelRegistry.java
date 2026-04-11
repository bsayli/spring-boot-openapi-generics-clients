package io.github.blueprintplatform.openapi.generics.codegen;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds mappings between OpenAPI model names and external Java types (FQCN).
 *
 * <p>Used to prevent generation of shared contract models and reference them instead.
 *
 * <p>Configuration format:
 *
 * <pre>
 * openapiGenerics.externalModel.CustomerDto=io.example.contract.CustomerDto
 * </pre>
 */
public class ExternalModelRegistry {

  private static final String PREFIX = "openapiGenerics.externalModel.";

  private final Map<String, String> externalModels = new HashMap<>();

  /** Registers external models from generator additionalProperties. */
  public void register(Map<String, Object> additionalProperties) {
    for (Map.Entry<String, Object> e : additionalProperties.entrySet()) {
      if (e.getKey().startsWith(PREFIX)) {
        String modelName = e.getKey().substring(PREFIX.length());
        externalModels.put(modelName, String.valueOf(e.getValue()));
      }
    }
  }

  /**
   * @return true if model is externally provided
   */
  public boolean isExternal(String modelName) {
    return externalModels.containsKey(modelName);
  }

  /**
   * @return fully-qualified class name or null
   */
  public String getFqcn(String modelName) {
    return externalModels.get(modelName);
  }
}

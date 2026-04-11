package io.github.blueprintplatform.openapi.generics.codegen;

import java.util.Map;
import java.util.Optional;
import org.openapitools.codegen.CodegenModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves external model types and injects their imports into vendorExtensions.
 *
 * <p>Used for wrapper models (e.g. ServiceResponse&lt;T&gt;) where T is an external contract type
 * and must be imported instead of generated.
 *
 * <p>Flow:
 *
 * <ul>
 *   <li>Detect wrapper model (x-api-wrapper)
 *   <li>Extract inner type (x-data-item or x-api-wrapper-datatype)
 *   <li>Resolve FQCN via {@link ExternalModelRegistry}
 *   <li>Inject into x-extra-imports for template usage
 * </ul>
 */
public class ExternalImportResolver {

  private static final Logger log = LoggerFactory.getLogger(ExternalImportResolver.class);

  private static final String EXT_API_WRAPPER = "x-api-wrapper";
  private static final String EXT_DATA_ITEM = "x-data-item";
  private static final String EXT_WRAPPER_DATATYPE = "x-api-wrapper-datatype";
  private static final String EXT_EXTRA_IMPORTS = "x-extra-imports";

  private final ExternalModelRegistry registry;

  public ExternalImportResolver(ExternalModelRegistry registry) {
    this.registry = registry;
  }

  /** Injects external import if the model uses an external type. */
  public void apply(CodegenModel model) {
    if (!isWrapperModel(model)) return;

    Map<String, Object> ve = model.getVendorExtensions();
    if (ve == null) return;

    Optional<String> typeOpt =
            extract(ve, EXT_DATA_ITEM).or(() -> extract(ve, EXT_WRAPPER_DATATYPE));

    if (typeOpt.isEmpty()) {
      log.debug("Wrapper model has no resolvable inner type: {}", model.name);
      return;
    }

    String type = typeOpt.get();
    String fqcn = registry.getFqcn(type);

    if (fqcn == null) {
      log.debug("No external mapping found for type: {} (model: {})", type, model.name);
      return;
    }

    ve.put(EXT_EXTRA_IMPORTS, fqcn);

    log.debug("External import applied: {} -> {}", type, fqcn);
  }

  private boolean isWrapperModel(CodegenModel model) {
    Map<String, Object> ve = model.getVendorExtensions();
    return ve != null && Boolean.TRUE.equals(ve.get(EXT_API_WRAPPER));
  }

  private Optional<String> extract(Map<String, Object> ve, String key) {
    Object val = ve.get(key);
    if (val instanceof String s && !s.isBlank()) {
      return Optional.of(s);
    }
    return Optional.empty();
  }
}
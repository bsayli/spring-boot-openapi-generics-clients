package io.github.blueprintplatform.openapi.generics.codegen.external;

import io.github.blueprintplatform.openapi.generics.codegen.contract.CodegenVendorExtensions;
import java.util.Map;
import java.util.Optional;
import org.openapitools.codegen.CodegenModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves imports for application-owned response contract types used by generated wrapper models.
 *
 * <p>The resolver inspects canonical OpenAPI wrapper metadata to determine the effective inner
 * contract type. It first prefers {@code x-data-item} for container-based responses and otherwise
 * falls back to {@code x-api-wrapper-datatype} for direct payload responses.
 *
 * <p>When the resolved type is registered in the {@link ExternalModelRegistry}, its fully qualified
 * Java type is published through {@code x-extra-imports} for consumption by the wrapper template.
 *
 * <p>Models that are not marked as wrapper schemas, do not expose a resolvable inner type, or do
 * not have a matching external contract registration are left unchanged.
 */
public class ExternalImportResolver {

  private static final Logger log = LoggerFactory.getLogger(ExternalImportResolver.class);

  private final ExternalModelRegistry registry;

  public ExternalImportResolver(ExternalModelRegistry registry) {
    this.registry = registry;
  }

  public void apply(CodegenModel model) {
    if (!isWrapperModel(model)) {
      return;
    }

    Map<String, Object> vendorExtensions = model.getVendorExtensions();

    if (vendorExtensions == null) {
      return;
    }

    Optional<String> type =
        extract(vendorExtensions, CodegenVendorExtensions.DATA_ITEM)
            .or(() -> extract(vendorExtensions, CodegenVendorExtensions.API_WRAPPER_DATATYPE));

    if (type.isEmpty()) {
      log.debug("Wrapper model has no resolvable inner type: {}", model.name);
      return;
    }

    String fqcn = registry.getFqcn(type.get());

    if (fqcn == null) {
      log.debug("No external mapping found for type: {} (model: {})", type.get(), model.name);
      return;
    }

    vendorExtensions.put(CodegenVendorExtensions.EXTRA_IMPORTS, fqcn);

    log.debug("External import applied: {} -> {}", type.get(), fqcn);
  }

  private boolean isWrapperModel(CodegenModel model) {
    Map<String, Object> vendorExtensions = model.getVendorExtensions();

    return vendorExtensions != null
        && Boolean.TRUE.equals(vendorExtensions.get(CodegenVendorExtensions.API_WRAPPER));
  }

  private Optional<String> extract(Map<String, Object> vendorExtensions, String key) {
    Object value = vendorExtensions.get(key);

    if (value instanceof String text && !text.isBlank()) {
      return Optional.of(text);
    }

    return Optional.empty();
  }
}

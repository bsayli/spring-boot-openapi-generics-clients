package io.github.blueprintplatform.openapi.generics.codegen.metadata;

import io.github.blueprintplatform.openapi.generics.codegen.contract.CodegenVendorExtensions;
import java.util.Map;
import org.openapitools.codegen.CodegenModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves envelope identity from canonical OpenAPI wrapper metadata and derives the template
 * metadata required for Java wrapper reconstruction.
 */
public class EnvelopeMetadataResolver {

  private static final Logger log = LoggerFactory.getLogger(EnvelopeMetadataResolver.class);

  public void apply(CodegenModel model) {
    if (!isWrapperModel(model)) {
      return;
    }

    Map<String, Object> vendorExtensions = model.getVendorExtensions();

    String envelopeImport = (String) vendorExtensions.get(CodegenVendorExtensions.API_WRAPPER_TYPE);
    String envelopeType = extractSimpleName(envelopeImport);

    vendorExtensions.put(CodegenVendorExtensions.ENVELOPE_IMPORT, envelopeImport);
    vendorExtensions.put(CodegenVendorExtensions.ENVELOPE_TYPE, envelopeType);

    log.debug("Envelope metadata applied to wrapper model: {} -> {}", model.name, envelopeType);
  }

  private boolean isWrapperModel(CodegenModel model) {
    Map<String, Object> vendorExtensions = model.getVendorExtensions();

    return vendorExtensions != null
        && Boolean.TRUE.equals(vendorExtensions.get(CodegenVendorExtensions.API_WRAPPER));
  }

  private String extractSimpleName(String fqcn) {
    int lastDot = fqcn.lastIndexOf('.');

    return lastDot >= 0 ? fqcn.substring(lastDot + 1) : fqcn;
  }
}

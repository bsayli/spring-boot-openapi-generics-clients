package io.github.blueprintplatform.openapi.generics.codegen.validation;

import io.github.blueprintplatform.openapi.generics.codegen.contract.CodegenVendorExtensions;
import io.github.blueprintplatform.openapi.generics.codegen.exception.OpenApiGenericsContractException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.openapitools.codegen.CodegenModel;

/** Validates canonical OpenAPI Generics metadata before wrapper reconstruction begins. */
public final class WrapperMetadataValidator {

  private static final List<String> WRAPPER_DETAIL_KEYS =
          List.of(
                  CodegenVendorExtensions.API_WRAPPER_TYPE,
                  CodegenVendorExtensions.API_WRAPPER_DATATYPE,
                  CodegenVendorExtensions.DATA_CONTAINER,
                  CodegenVendorExtensions.DATA_CONTAINER_TYPE,
                  CodegenVendorExtensions.DATA_ITEM);

  private static final List<String> CONTAINER_METADATA_KEYS =
          List.of(
                  CodegenVendorExtensions.DATA_CONTAINER,
                  CodegenVendorExtensions.DATA_CONTAINER_TYPE,
                  CodegenVendorExtensions.DATA_ITEM);

  public void validate(CodegenModel model) {
    Objects.requireNonNull(model, "model must not be null");

    Map<String, Object> vendorExtensions = model.getVendorExtensions();

    if (vendorExtensions == null || vendorExtensions.isEmpty()) {
      return;
    }

    boolean wrapperMarkerPresent =
            vendorExtensions.containsKey(CodegenVendorExtensions.API_WRAPPER);

    List<String> presentWrapperDetails =
            presentKeys(vendorExtensions, WRAPPER_DETAIL_KEYS);

    if (!wrapperMarkerPresent && presentWrapperDetails.isEmpty()) {
      return;
    }

    if (!wrapperMarkerPresent) {
      throw missingWrapperMarker(model, presentWrapperDetails);
    }

    boolean wrapper =
            requireBoolean(
                    model,
                    vendorExtensions,
                    CodegenVendorExtensions.API_WRAPPER);

    if (!wrapper) {
      if (!presentWrapperDetails.isEmpty()) {
        throw contradictoryWrapperMetadata(model, presentWrapperDetails);
      }

      return;
    }

    requireQualifiedType(
            model,
            vendorExtensions,
            CodegenVendorExtensions.API_WRAPPER_TYPE);

    requireText(
            model,
            vendorExtensions,
            CodegenVendorExtensions.API_WRAPPER_DATATYPE);

    validateContainerMetadata(model, vendorExtensions);
  }

  private void validateContainerMetadata(
          CodegenModel model,
          Map<String, Object> vendorExtensions) {

    List<String> present =
            presentKeys(vendorExtensions, CONTAINER_METADATA_KEYS);

    if (present.isEmpty()) {
      return;
    }

    List<String> missing =
            CONTAINER_METADATA_KEYS.stream()
                    .filter(key -> !vendorExtensions.containsKey(key))
                    .toList();

    if (!missing.isEmpty()) {
      throw incompleteContainerMetadata(
              model,
              missing,
              CONTAINER_METADATA_KEYS);
    }

    requireText(
            model,
            vendorExtensions,
            CodegenVendorExtensions.DATA_CONTAINER);

    requireQualifiedType(
            model,
            vendorExtensions,
            CodegenVendorExtensions.DATA_CONTAINER_TYPE);

    requireText(
            model,
            vendorExtensions,
            CodegenVendorExtensions.DATA_ITEM);
  }

  private boolean requireBoolean(
          CodegenModel model,
          Map<String, Object> vendorExtensions,
          String key) {

    Object value = vendorExtensions.get(key);

    if (!(value instanceof Boolean booleanValue)) {
      throw invalidType(model, key, "Boolean", value);
    }

    return booleanValue;
  }

  private String requireText(
          CodegenModel model,
          Map<String, Object> vendorExtensions,
          String key) {

    if (!vendorExtensions.containsKey(key)) {
      throw missing(model, key);
    }

    Object value = vendorExtensions.get(key);

    if (!(value instanceof String text)) {
      throw invalidType(model, key, "String", value);
    }

    if (text.isBlank()) {
      throw new OpenApiGenericsContractException(
              ("Invalid OpenAPI Generics metadata for wrapper model '%s': "
                      + "vendor extension '%s' must not be blank.")
                      .formatted(modelName(model), key));
    }

    return text;
  }

  private void requireQualifiedType(
          CodegenModel model,
          Map<String, Object> vendorExtensions,
          String key) {

    String value = requireText(model, vendorExtensions, key);

    if (!value.contains(".")) {
      throw new OpenApiGenericsContractException(
              ("Invalid OpenAPI Generics metadata for wrapper model '%s': "
                      + "vendor extension '%s' must contain a fully qualified Java type "
                      + "but was '%s'.")
                      .formatted(modelName(model), key, value));
    }
  }

  private OpenApiGenericsContractException missingWrapperMarker(
          CodegenModel model,
          List<String> detectedMetadata) {

    return new OpenApiGenericsContractException(
            ("Invalid OpenAPI Generics metadata for model '%s': required vendor extension "
                    + "'%s' is missing. Wrapper metadata was detected through: %s.")
                    .formatted(
                            modelName(model),
                            CodegenVendorExtensions.API_WRAPPER,
                            quoteKeys(detectedMetadata)));
  }

  private OpenApiGenericsContractException contradictoryWrapperMetadata(
          CodegenModel model,
          List<String> detectedMetadata) {

    return new OpenApiGenericsContractException(
            ("Invalid OpenAPI Generics metadata for model '%s': vendor extension '%s' "
                    + "is false, but wrapper metadata is also present: %s. "
                    + "Remove the wrapper metadata or set '%s' to true.")
                    .formatted(
                            modelName(model),
                            CodegenVendorExtensions.API_WRAPPER,
                            quoteKeys(detectedMetadata),
                            CodegenVendorExtensions.API_WRAPPER));
  }

  private OpenApiGenericsContractException incompleteContainerMetadata(
          CodegenModel model,
          List<String> missing,
          List<String> required) {

    String missingDescription =
            missing.size() == 1
                    ? "required vendor extension is missing"
                    : "required vendor extensions are missing";

    return new OpenApiGenericsContractException(
            ("Invalid OpenAPI Generics metadata for wrapper model '%s': %s: %s. "
                    + "Container metadata must declare these vendor extensions together: %s.")
                    .formatted(
                            modelName(model),
                            missingDescription,
                            quoteKeys(missing),
                            quoteKeys(required)));
  }

  private OpenApiGenericsContractException missing(
          CodegenModel model,
          String key) {

    return new OpenApiGenericsContractException(
            ("Invalid OpenAPI Generics metadata for wrapper model '%s': "
                    + "required vendor extension '%s' is missing.")
                    .formatted(modelName(model), key));
  }

  private OpenApiGenericsContractException invalidType(
          CodegenModel model,
          String key,
          String expectedType,
          Object value) {

    String actualType =
            value == null
                    ? "null"
                    : value.getClass().getName();

    return new OpenApiGenericsContractException(
            ("Invalid OpenAPI Generics metadata for model '%s': "
                    + "vendor extension '%s' must be a %s but was %s.")
                    .formatted(
                            modelName(model),
                            key,
                            expectedType,
                            actualType));
  }

  private List<String> presentKeys(
          Map<String, Object> vendorExtensions,
          List<String> candidateKeys) {

    return candidateKeys.stream()
            .filter(vendorExtensions::containsKey)
            .toList();
  }

  private String quoteKeys(List<String> keys) {
    return String.join(
            ", ",
            keys.stream()
                    .map(key -> "'" + key + "'")
                    .toList());
  }

  private String modelName(CodegenModel model) {
    return model.name == null || model.name.isBlank()
            ? "<unnamed>"
            : model.name;
  }
}
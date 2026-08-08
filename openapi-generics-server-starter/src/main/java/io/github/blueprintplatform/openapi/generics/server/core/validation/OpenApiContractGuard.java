package io.github.blueprintplatform.openapi.generics.server.core.validation;

import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.VendorExtensions.API_WRAPPER;
import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.VendorExtensions.API_WRAPPER_DATATYPE;
import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.VendorExtensions.DATA_CONTAINER;
import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.VendorExtensions.DATA_ITEM;

import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDescriptor;
import io.github.blueprintplatform.openapi.generics.server.exception.OpenApiContractValidationException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates that generated OpenAPI schemas conform to the contract defined by the response
 * descriptors.
 *
 * <p>Specifically, it verifies:
 *
 * <ul>
 *   <li>Wrapper schema exists for each descriptor
 *   <li>{@code x-api-wrapper=true} is present
 *   <li>{@code x-api-wrapper-datatype} matches the payload type
 *   <li>Wrapper contains the expected payload property
 *   <li>Container metadata ({@code x-data-container}, {@code x-data-item}) is correct when
 *       applicable
 * </ul>
 *
 * <p>This is a fail-fast validation step in the OpenAPI projection pipeline. Any inconsistency
 * results in an exception to prevent invalid client generation.
 */
public class OpenApiContractGuard {

  private static final Logger log = LoggerFactory.getLogger(OpenApiContractGuard.class);

  @SuppressWarnings("rawtypes")
  public void validate(OpenAPI openApi, Set<ResponseTypeDescriptor> descriptors) {
    log.debug("OpenAPI contract validation started");

    Map<String, Schema> schemas = getSchemas(openApi);
    validateDescriptors(schemas, descriptors);

    log.debug("OpenAPI contract validation completed successfully");
  }

  @SuppressWarnings("rawtypes")
  private void validateDescriptors(
      Map<String, Schema> schemas, Set<ResponseTypeDescriptor> descriptors) {
    if (descriptors == null || descriptors.isEmpty()) {
      return;
    }

    for (ResponseTypeDescriptor descriptor : descriptors) {
      validateWrapperSchema(schemas, descriptor);
    }
  }

  @SuppressWarnings("rawtypes")
  private void validateWrapperSchema(
      Map<String, Schema> schemas, ResponseTypeDescriptor descriptor) {

    String wrapperName = descriptor.envelopeType().getSimpleName() + descriptor.dataRefName();
    Schema<?> wrapper = schemas.get(wrapperName);

    if (wrapper == null) {
      failMissingSchema("wrapper", wrapperName);
    }

    validateWrapperExtensions(wrapperName, wrapper, descriptor);
    validateWrapperStructure(wrapperName, wrapper, descriptor);

    if (descriptor.isContainer()) {
      validateContainerExtensions(wrapperName, wrapper, descriptor);
    }
  }

  private void validateWrapperExtensions(
      String wrapperName, Schema<?> wrapper, ResponseTypeDescriptor descriptor) {

    Map<String, Object> extensions = requireExtensions(wrapperName, wrapper, "required extensions");

    requireTrueExtension(wrapperName, extensions, API_WRAPPER);
    requireMatchingExtension(
        wrapperName, extensions, API_WRAPPER_DATATYPE, descriptor.dataRefName());
  }

  private void validateWrapperStructure(
      String wrapperName, Schema<?> wrapper, ResponseTypeDescriptor descriptor) {

    boolean hasPayloadProperty = false;

    if (wrapper.getAllOf() != null && !wrapper.getAllOf().isEmpty()) {
      hasPayloadProperty =
          wrapper.getAllOf().stream()
              .filter(schema -> schema.getProperties() != null)
              .anyMatch(
                  schema -> schema.getProperties().containsKey(descriptor.payloadPropertyName()));
    } else if (wrapper.getProperties() != null) {
      hasPayloadProperty = wrapper.getProperties().containsKey(descriptor.payloadPropertyName());
    }

    if (!hasPayloadProperty) {
      failMissingProperty(wrapperName, descriptor.payloadPropertyName());
    }
  }

  private void validateContainerExtensions(
      String wrapperName, Schema<?> wrapper, ResponseTypeDescriptor descriptor) {

    Map<String, Object> extensions =
        requireExtensions(wrapperName, wrapper, "required container extensions");

    requireMatchingExtension(wrapperName, extensions, DATA_CONTAINER, descriptor.containerName());
    requireMatchingExtension(wrapperName, extensions, DATA_ITEM, descriptor.itemRefName());
  }

  @SuppressWarnings("rawtypes")
  private Map<String, Schema> getSchemas(OpenAPI openApi) {
    if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
      log.error("OpenAPI validation failed: components.schemas is missing");
      throw new OpenApiContractValidationException("OpenAPI components.schemas is missing");
    }

    return openApi.getComponents().getSchemas();
  }

  private Map<String, Object> requireExtensions(
      String wrapperName, Schema<?> wrapper, String detail) {

    Map<String, Object> extensions = wrapper.getExtensions();
    if (extensions == null) {
      failMissingExtensions(wrapperName, detail);
    }

    return extensions;
  }

  private void requireTrueExtension(
      String wrapperName, Map<String, Object> extensions, String extensionName) {

    Object actual = extensions.get(extensionName);
    if (!Boolean.TRUE.equals(actual)) {
      failInvalidExtension(wrapperName, extensionName, Boolean.TRUE, actual);
    }
  }

  private void requireMatchingExtension(
      String wrapperName,
      Map<String, Object> extensions,
      String extensionName,
      Object expectedValue) {

    Object actualValue = extensions.get(extensionName);
    if (!expectedValue.equals(actualValue)) {
      failInvalidExtension(wrapperName, extensionName, expectedValue, actualValue);
    }
  }

  private void failMissingSchema(String schemaType, String schemaName) {
    log.error("Missing {} schema '{}'", schemaType, schemaName);
    throw new OpenApiContractValidationException(
        "Missing required " + schemaType + " schema: '" + schemaName + "'");
  }

  private void failMissingExtensions(String wrapperName, String detail) {
    log.error("Wrapper '{}' missing {}", wrapperName, detail);
    throw new OpenApiContractValidationException(
        "Wrapper schema '" + wrapperName + "' is missing " + detail);
  }

  private void failInvalidExtension(
      String wrapperName, String extensionName, Object expectedValue, Object actualValue) {

    log.error(
        "Wrapper '{}' has invalid '{}': expected '{}', actual '{}'",
        wrapperName,
        extensionName,
        expectedValue,
        actualValue);

    throw new OpenApiContractValidationException(
        "Wrapper schema '" + wrapperName + "' has invalid extension: " + extensionName);
  }

  private void failMissingProperty(String wrapperName, String propertyName) {
    log.error("Wrapper '{}' missing required property '{}'", wrapperName, propertyName);
    throw new OpenApiContractValidationException(
        "Wrapper schema '" + wrapperName + "' must define '" + propertyName + "' property");
  }
}

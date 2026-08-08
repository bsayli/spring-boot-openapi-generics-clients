package io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor;

import io.github.blueprintplatform.openapi.generics.server.exception.OpenApiDescriptorValidationException;

/**
 * Describes a supported generic container contract recognized by the projection pipeline.
 *
 * <p>A container descriptor separates Java identity, OpenAPI schema identity, semantic identity,
 * and schema-shape behavior. This allows built-in containers and configured BYOC containers to pass
 * through the same deterministic introspection and projection pipeline.
 *
 * @param type raw Java container type discovered during introspection
 * @param schemaName canonical schema identifier used during projection
 * @param containerName semantic container identifier exposed through vendor extensions
 * @param shape OpenAPI schema shape of the container
 * @param itemPropertyName JSON property containing the generic item collection for object
 *     containers
 * @param source descriptor source
 * @param matchMode Java type matching policy
 */
public record SupportedContainerDescriptor(
    Class<?> type,
    String schemaName,
    String containerName,
    ContainerShape shape,
    String itemPropertyName,
    ContainerSource source,
    ContainerMatchMode matchMode) {

  public SupportedContainerDescriptor {
    requireNonNull(type, "type");
    requireNonBlank(schemaName, "schemaName");
    requireNonBlank(containerName, "containerName");
    requireNonNull(shape, "shape");
    requireNonNull(source, "source");
    requireNonNull(matchMode, "matchMode");

    validateItemProperty(shape, itemPropertyName);
  }

  public String containerTypeName() {
    return type.getName();
  }

  public boolean matches(Class<?> candidate) {
    if (candidate == null) {
      return false;
    }

    return switch (matchMode) {
      case EXACT -> type.equals(candidate);
      case ASSIGNABLE -> type.isAssignableFrom(candidate);
    };
  }

  private static void validateItemProperty(ContainerShape shape, String itemPropertyName) {

    switch (shape) {
      case OBJECT_WITH_ITEM_ARRAY -> {
        if (itemPropertyName == null || itemPropertyName.isBlank()) {
          fail("itemPropertyName must not be null or blank for object containers");
        }
      }

      case DIRECT_ARRAY -> {
        if (itemPropertyName != null) {
          fail("itemPropertyName must be null for direct array containers");
        }
      }
    }
  }

  private static void requireNonNull(Object value, String propertyName) {
    if (value == null) {
      fail(propertyName + " must not be null");
    }
  }

  private static void requireNonBlank(String value, String propertyName) {
    if (value == null || value.isBlank()) {
      fail(propertyName + " must not be null or blank");
    }
  }

  private static void fail(String message) {
    throw new OpenApiDescriptorValidationException(message);
  }
}

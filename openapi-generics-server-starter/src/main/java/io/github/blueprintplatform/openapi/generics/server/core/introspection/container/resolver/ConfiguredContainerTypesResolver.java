package io.github.blueprintplatform.openapi.generics.server.core.introspection.container.resolver;

import io.github.blueprintplatform.openapi.generics.server.autoconfigure.properties.ContainerProperties;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerMatchMode;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerShape;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerSource;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.SupportedContainerDescriptor;
import io.github.blueprintplatform.openapi.generics.server.exception.OpenApiGenericsConfigurationException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Resolves application-configured generic container contracts into supported container descriptors.
 *
 * <p>Configured containers are validated eagerly so invalid BYOC definitions fail fast during
 * application startup.
 */
public final class ConfiguredContainerTypesResolver {

  public static final String ITEM_PROPERTY = "item-property '";

  public Set<SupportedContainerDescriptor> resolve(List<ContainerProperties> properties) {
    if (properties == null || properties.isEmpty()) {
      return Set.of();
    }

    Set<SupportedContainerDescriptor> containers = new LinkedHashSet<>();

    for (ContainerProperties property : properties) {
      if (property == null) {
        continue;
      }

      Class<?> containerType = resolveContainerClass(property.type());

      validateConcreteContainer(containerType);
      TypeVariable<?> itemTypeParameter = validateSingleTypeParameter(containerType);
      validateItemProperty(containerType, property.itemProperty(), itemTypeParameter);

      String simpleName = containerType.getSimpleName();

      containers.add(
          new SupportedContainerDescriptor(
              containerType,
              simpleName,
              simpleName,
              ContainerShape.OBJECT_WITH_ITEM_ARRAY,
              property.itemProperty(),
              ContainerSource.CONFIGURED,
              ContainerMatchMode.EXACT));
    }

    return Set.copyOf(containers);
  }

  private Class<?> resolveContainerClass(String configuredType) {
    if (configuredType == null || configuredType.isBlank()) {
      throw new OpenApiGenericsConfigurationException("Container type must not be null or blank");
    }

    if (!configuredType.contains(".")) {
      throw new OpenApiGenericsConfigurationException(
          "Invalid container type '"
              + configuredType
              + "'. Expected fully-qualified class name (e.g. com.example.Paging)");
    }

    try {
      return Class.forName(configuredType);
    } catch (ClassNotFoundException e) {
      throw new OpenApiGenericsConfigurationException(
          "Configured container class not found: '"
              + configuredType
              + "'. Ensure the class exists and is on the application classpath.",
          e);
    }
  }

  private void validateConcreteContainer(Class<?> containerType) {
    if (containerType.isInterface()) {
      throw invalidContainer(containerType, "must be a concrete class or record, not an interface");
    } else if (containerType.isEnum()) {
      throw invalidContainer(containerType, "must be a class or record, not an enum");
    } else if (containerType.isAnnotation()) {
      throw invalidContainer(containerType, "must be a class or record, not an annotation");
    } else if (containerType.isArray()) {
      throw invalidContainer(containerType, "must be a class or record, not an array");
    } else if (containerType.isPrimitive()) {
      throw invalidContainer(containerType, "must be a class or record, not a primitive");
    } else if (Modifier.isAbstract(containerType.getModifiers())) {
      throw invalidContainer(containerType, "must be concrete, not abstract");
    }
  }

  private TypeVariable<?> validateSingleTypeParameter(Class<?> containerType) {
    TypeVariable<?>[] typeParameters = containerType.getTypeParameters();

    if (typeParameters.length != 1) {
      throw invalidContainer(containerType, "must declare exactly one type parameter");
    }

    return typeParameters[0];
  }

  private void validateItemProperty(
      Class<?> containerType, String itemProperty, TypeVariable<?> itemTypeParameter) {
    if (itemProperty == null || itemProperty.isBlank()) {
      throw invalidContainer(
          containerType, ITEM_PROPERTY + itemProperty + "' must not be null or blank");
    }

    Field field = findDeclaredField(containerType, itemProperty);

    if (field == null) {
      throw invalidContainer(containerType, ITEM_PROPERTY + itemProperty + "' does not exist");
    }

    if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
      throw invalidContainer(
          containerType, ITEM_PROPERTY + itemProperty + "' must be an instance field");
    }

    Type fieldType = field.getGenericType();

    if (!(fieldType instanceof ParameterizedType parameterizedType)) {
      throw invalidContainer(
          containerType, ITEM_PROPERTY + itemProperty + "' must be List<T> or Set<T>");
    }

    Type rawType = parameterizedType.getRawType();

    if (!(rawType == List.class || rawType == Set.class)) {
      throw invalidContainer(
          containerType, ITEM_PROPERTY + itemProperty + "' must be List<T> or Set<T>");
    }

    Type[] arguments = parameterizedType.getActualTypeArguments();

    if (arguments.length != 1 || !sameTypeVariable(arguments[0], itemTypeParameter)) {
      throw invalidContainer(
          containerType,
          ITEM_PROPERTY + itemProperty + "' must use the container type parameter directly");
    }
  }

  private Field findDeclaredField(Class<?> type, String name) {
    try {
      return type.getDeclaredField(name);
    } catch (NoSuchFieldException ignored) {
      return null;
    }
  }

  private boolean sameTypeVariable(Type candidate, TypeVariable<?> expected) {
    if (!(candidate instanceof TypeVariable<?> typeVariable)) {
      return false;
    }

    return typeVariable.getGenericDeclaration() == expected.getGenericDeclaration()
        && typeVariable.getName().equals(expected.getName());
  }

  private OpenApiGenericsConfigurationException invalidContainer(
      Class<?> containerType, String reason) {
    return new OpenApiGenericsConfigurationException(
        "Unsupported container type '" + containerType.getName() + "': " + reason);
  }
}

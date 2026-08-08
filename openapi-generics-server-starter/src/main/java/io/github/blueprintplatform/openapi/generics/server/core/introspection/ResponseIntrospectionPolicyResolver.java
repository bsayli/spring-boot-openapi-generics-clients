package io.github.blueprintplatform.openapi.generics.server.core.introspection;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.server.autoconfigure.properties.EnvelopeProperties;
import io.github.blueprintplatform.openapi.generics.server.autoconfigure.properties.OpenApiGenericsProperties;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.SupportedContainerDescriptor;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.resolver.ConfiguredContainerTypesResolver;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.resolver.SupportedContainerTypesResolver;
import io.github.blueprintplatform.openapi.generics.server.core.schema.constant.PropertyNames;
import io.github.blueprintplatform.openapi.generics.server.exception.OpenApiGenericsConfigurationException;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Resolves the response introspection policy from built-in defaults and application configuration.
 *
 * <p>Determines the response envelope contract, payload property, and supported generic container
 * types used during response type introspection.
 */
public class ResponseIntrospectionPolicyResolver {

  private final SupportedContainerTypesResolver supportedContainerTypesResolver;
  private final ConfiguredContainerTypesResolver configuredContainerTypesResolver;

  public ResponseIntrospectionPolicyResolver(
      SupportedContainerTypesResolver supportedContainerTypesResolver,
      ConfiguredContainerTypesResolver configuredContainerTypesResolver) {
    this.supportedContainerTypesResolver = supportedContainerTypesResolver;
    this.configuredContainerTypesResolver = configuredContainerTypesResolver;
  }

  public ResponseIntrospectionPolicy resolve(OpenApiGenericsProperties properties) {
    String configuredType = extractConfiguredEnvelopeType(properties);
    Set<SupportedContainerDescriptor> supportedContainers = resolveSupportedContainers(properties);

    if (configuredType == null) {
      return new ResponseIntrospectionPolicy(
          ServiceResponse.class, PropertyNames.DATA, supportedContainers);
    }

    Class<?> envelopeType = resolveExternalEnvelopeType(configuredType);
    String payloadPropertyName = validateExternalEnvelopeType(envelopeType);

    return new ResponseIntrospectionPolicy(envelopeType, payloadPropertyName, supportedContainers);
  }

  private Set<SupportedContainerDescriptor> resolveSupportedContainers(
      OpenApiGenericsProperties properties) {

    Set<SupportedContainerDescriptor> containers =
        new LinkedHashSet<>(supportedContainerTypesResolver.resolve());

    if (properties != null) {
      containers.addAll(configuredContainerTypesResolver.resolve(properties.containers()));
    }

    return Set.copyOf(containers);
  }

  private String extractConfiguredEnvelopeType(OpenApiGenericsProperties properties) {
    if (properties == null || properties.envelope() == null) {
      return null;
    }

    EnvelopeProperties envelope = properties.envelope();
    String type = envelope.type();
    return (type == null || type.isBlank()) ? null : type;
  }

  private Class<?> resolveExternalEnvelopeType(String configuredType) {
    if (configuredType == null || configuredType.isBlank()) {
      throw new OpenApiGenericsConfigurationException("Envelope type must not be null or blank");
    }

    if (!configuredType.contains(".")) {
      throw new OpenApiGenericsConfigurationException(
          "Invalid envelope type '"
              + configuredType
              + "'. Expected fully-qualified class name (e.g. com.example.ApiResponse)");
    }

    try {
      return Class.forName(configuredType);
    } catch (ClassNotFoundException e) {
      throw new OpenApiGenericsConfigurationException(
          "Configured envelope class not found: '"
              + configuredType
              + "'. Ensure the class exists and is on the application classpath.",
          e);
    }
  }

  private String validateExternalEnvelopeType(Class<?> envelopeType) {
    validateConcreteClass(envelopeType);
    TypeVariable<?> payloadTypeParameter = validateSingleTypeParameter(envelopeType);
    return validateSingleDirectPayloadSlot(envelopeType, payloadTypeParameter);
  }

  private void validateConcreteClass(Class<?> envelopeType) {
    if (envelopeType.isInterface()) {
      throw invalidEnvelope(envelopeType, "must be a concrete class, not an interface");
    } else if (envelopeType.isRecord()) {
      throw invalidEnvelope(envelopeType, "must be a class, not a record");
    } else if (envelopeType.isEnum()) {
      throw invalidEnvelope(envelopeType, "must be a class, not an enum");
    } else if (envelopeType.isAnnotation()) {
      throw invalidEnvelope(envelopeType, "must be a class, not an annotation");
    } else if (envelopeType.isArray()) {
      throw invalidEnvelope(envelopeType, "must be a class, not an array");
    } else if (envelopeType.isPrimitive()) {
      throw invalidEnvelope(envelopeType, "must be a class, not a primitive");
    } else if (Modifier.isAbstract(envelopeType.getModifiers())) {
      throw invalidEnvelope(envelopeType, "must be a concrete class, not an abstract class");
    }
  }

  private TypeVariable<?> validateSingleTypeParameter(Class<?> envelopeType) {
    TypeVariable<?>[] typeParameters = envelopeType.getTypeParameters();

    if (typeParameters.length != 1) {
      throw invalidEnvelope(envelopeType, "must declare exactly one type parameter");
    }

    return typeParameters[0];
  }

  private String validateSingleDirectPayloadSlot(
      Class<?> envelopeType, TypeVariable<?> payloadTypeParameter) {
    String payloadPropertyName = null;

    for (Field field : envelopeType.getDeclaredFields()) {
      if (!field.isSynthetic() && !Modifier.isStatic(field.getModifiers())) {
        PayloadSlotKind kind = classifyPayloadSlot(field.getGenericType(), payloadTypeParameter);

        if (kind == PayloadSlotKind.NESTED) {
          throw invalidEnvelope(
              envelopeType,
              "contains unsupported nested generic payload slot in field '"
                  + field.getName()
                  + "'");
        }

        if (kind == PayloadSlotKind.DIRECT) {
          if (payloadPropertyName != null) {
            throw invalidEnvelope(
                envelopeType,
                "must declare exactly one direct payload field of type "
                    + payloadTypeParameter.getName());
          }

          payloadPropertyName = field.getName();
        }
      }
    }

    if (payloadPropertyName == null) {
      throw invalidEnvelope(
          envelopeType,
          "must declare exactly one direct payload field of type "
              + payloadTypeParameter.getName());
    }

    return payloadPropertyName;
  }

  private PayloadSlotKind classifyPayloadSlot(
      Type fieldType, TypeVariable<?> payloadTypeParameter) {
    if (fieldType instanceof TypeVariable<?> typeVariable) {
      return sameTypeVariable(typeVariable, payloadTypeParameter)
          ? PayloadSlotKind.DIRECT
          : PayloadSlotKind.NONE;
    }

    if (fieldType instanceof ParameterizedType || fieldType instanceof GenericArrayType) {
      return containsPayloadType(fieldType, payloadTypeParameter)
          ? PayloadSlotKind.NESTED
          : PayloadSlotKind.NONE;
    }

    return PayloadSlotKind.NONE;
  }

  private boolean containsPayloadType(Type type, TypeVariable<?> payloadTypeParameter) {
    if (type instanceof TypeVariable<?> typeVariable) {
      return sameTypeVariable(typeVariable, payloadTypeParameter);
    }

    if (type instanceof ParameterizedType parameterizedType) {
      for (Type actualTypeArgument : parameterizedType.getActualTypeArguments()) {
        if (containsPayloadType(actualTypeArgument, payloadTypeParameter)) {
          return true;
        }
      }
      return false;
    }

    if (type instanceof GenericArrayType genericArrayType) {
      return containsPayloadType(genericArrayType.getGenericComponentType(), payloadTypeParameter);
    }

    return false;
  }

  private boolean sameTypeVariable(TypeVariable<?> left, TypeVariable<?> right) {
    return left.getGenericDeclaration() == right.getGenericDeclaration()
        && left.getName().equals(right.getName());
  }

  private OpenApiGenericsConfigurationException invalidEnvelope(
      Class<?> envelopeType, String reason) {
    return new OpenApiGenericsConfigurationException(
        "Unsupported envelope type '" + envelopeType.getName() + "': " + reason);
  }

  private enum PayloadSlotKind {
    NONE,
    DIRECT,
    NESTED
  }
}

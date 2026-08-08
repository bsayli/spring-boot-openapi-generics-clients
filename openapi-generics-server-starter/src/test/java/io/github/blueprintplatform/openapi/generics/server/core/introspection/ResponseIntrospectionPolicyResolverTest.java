package io.github.blueprintplatform.openapi.generics.server.core.introspection;

import static org.junit.jupiter.api.Assertions.*;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.server.autoconfigure.properties.ContainerProperties;
import io.github.blueprintplatform.openapi.generics.server.autoconfigure.properties.EnvelopeProperties;
import io.github.blueprintplatform.openapi.generics.server.autoconfigure.properties.OpenApiGenericsProperties;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerMatchMode;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerShape;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerSource;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.SupportedContainerDescriptor;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.resolver.ConfiguredContainerTypesResolver;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.resolver.DefaultSupportedContainerTypesResolver;
import io.github.blueprintplatform.openapi.generics.server.exception.OpenApiGenericsConfigurationException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: ResponseIntrospectionPolicyResolver")
class ResponseIntrospectionPolicyResolverTest {

  private final ResponseIntrospectionPolicyResolver resolver =
      new ResponseIntrospectionPolicyResolver(
          new DefaultSupportedContainerTypesResolver(), new ConfiguredContainerTypesResolver());

  private static Set<SupportedContainerDescriptor> defaultContainers() {
    return Set.of(
        new SupportedContainerDescriptor(
            Page.class,
            "Page",
            "Page",
            ContainerShape.OBJECT_WITH_ITEM_ARRAY,
            "content",
            ContainerSource.BUILT_IN,
            ContainerMatchMode.EXACT),
        new SupportedContainerDescriptor(
            List.class,
            "List",
            "List",
            ContainerShape.DIRECT_ARRAY,
            null,
            ContainerSource.BUILT_IN,
            ContainerMatchMode.ASSIGNABLE),
        new SupportedContainerDescriptor(
            Set.class,
            "Set",
            "Set",
            ContainerShape.DIRECT_ARRAY,
            null,
            ContainerSource.BUILT_IN,
            ContainerMatchMode.ASSIGNABLE));
  }

  @Test
  @DisplayName("resolve -> should use supported container resolver for default policy")
  void resolve_shouldUseSupportedContainerResolverForDefaultPolicy() {
    SupportedContainerDescriptor pageContainer =
        new SupportedContainerDescriptor(
            Page.class,
            "Page",
            "Page",
            ContainerShape.OBJECT_WITH_ITEM_ARRAY,
            "content",
            ContainerSource.BUILT_IN,
            ContainerMatchMode.EXACT);

    ResponseIntrospectionPolicyResolver customResolver =
        new ResponseIntrospectionPolicyResolver(
            () -> Set.of(pageContainer), new ConfiguredContainerTypesResolver());

    ResponseIntrospectionPolicy policy = customResolver.resolve(null);

    assertEquals(Set.of(pageContainer), policy.supportedContainers());
  }

  @Test
  @DisplayName("resolve -> should include configured containers in default policy")
  void resolve_shouldIncludeConfiguredContainersInDefaultPolicy() {
    OpenApiGenericsProperties properties =
        new OpenApiGenericsProperties(
            null, List.of(new ContainerProperties(Paging.class.getName(), "content")));

    ResponseIntrospectionPolicy policy = resolver.resolve(properties);

    SupportedContainerDescriptor expected =
        new SupportedContainerDescriptor(
            Paging.class,
            "Paging",
            "Paging",
            ContainerShape.OBJECT_WITH_ITEM_ARRAY,
            "content",
            ContainerSource.CONFIGURED,
            ContainerMatchMode.EXACT);

    assertEquals(ServiceResponse.class, policy.envelopeType());
    assertEquals("data", policy.payloadPropertyName());
    assertTrue(policy.supportedContainers().containsAll(defaultContainers()));
    assertTrue(policy.supportedContainers().contains(expected));
  }

  @Test
  @DisplayName("resolve -> should return default policy when properties are null")
  void resolve_shouldReturnDefaultPolicy_whenPropertiesNull() {
    ResponseIntrospectionPolicy policy = resolver.resolve(null);

    assertEquals(ServiceResponse.class, policy.envelopeType());
    assertEquals("data", policy.payloadPropertyName());
    assertEquals(defaultContainers(), policy.supportedContainers());
  }

  @Test
  @DisplayName("resolve -> should return default policy when envelope is missing")
  void resolve_shouldReturnDefaultPolicy_whenEnvelopeMissing() {
    OpenApiGenericsProperties properties = new OpenApiGenericsProperties(null, null);

    ResponseIntrospectionPolicy policy = resolver.resolve(properties);

    assertEquals(ServiceResponse.class, policy.envelopeType());
    assertEquals("data", policy.payloadPropertyName());
    assertEquals(defaultContainers(), policy.supportedContainers());
  }

  @Test
  @DisplayName("resolve -> should return default policy when envelope type is blank")
  void resolve_shouldReturnDefaultPolicy_whenEnvelopeTypeBlank() {
    OpenApiGenericsProperties properties =
        new OpenApiGenericsProperties(new EnvelopeProperties("   "), null);

    ResponseIntrospectionPolicy policy = resolver.resolve(properties);

    assertEquals(ServiceResponse.class, policy.envelopeType());
    assertEquals("data", policy.payloadPropertyName());
    assertEquals(defaultContainers(), policy.supportedContainers());
  }

  @Test
  @DisplayName("resolve -> should resolve custom envelope with direct payload field")
  void resolve_shouldResolveCustomEnvelope_whenValid() {
    OpenApiGenericsProperties properties =
        new OpenApiGenericsProperties(new EnvelopeProperties(ValidEnvelope.class.getName()), null);

    ResponseIntrospectionPolicy policy = resolver.resolve(properties);

    assertEquals(ValidEnvelope.class, policy.envelopeType());
    assertEquals("payload", policy.payloadPropertyName());
    assertEquals(defaultContainers(), policy.supportedContainers());
  }

  @Test
  @DisplayName("resolve -> should include configured containers in custom envelope policy")
  void resolve_shouldIncludeConfiguredContainersInCustomEnvelopePolicy() {
    OpenApiGenericsProperties properties =
        new OpenApiGenericsProperties(
            new EnvelopeProperties(ValidEnvelope.class.getName()),
            List.of(new ContainerProperties(Paging.class.getName(), "content")));

    ResponseIntrospectionPolicy policy = resolver.resolve(properties);

    SupportedContainerDescriptor expected =
        new SupportedContainerDescriptor(
            Paging.class,
            "Paging",
            "Paging",
            ContainerShape.OBJECT_WITH_ITEM_ARRAY,
            "content",
            ContainerSource.CONFIGURED,
            ContainerMatchMode.EXACT);

    assertEquals(ValidEnvelope.class, policy.envelopeType());
    assertEquals("payload", policy.payloadPropertyName());
    assertTrue(policy.supportedContainers().containsAll(defaultContainers()));
    assertTrue(policy.supportedContainers().contains(expected));
  }

  @Test
  @DisplayName("resolve -> should reject non fqcn envelope type")
  void resolve_shouldRejectNonFqcnEnvelopeType() {
    OpenApiGenericsProperties properties =
        new OpenApiGenericsProperties(new EnvelopeProperties("ApiResponse"), null);

    OpenApiGenericsConfigurationException ex =
        assertThrows(
            OpenApiGenericsConfigurationException.class, () -> resolver.resolve(properties));

    assertTrue(ex.getMessage().contains("Expected fully-qualified class name"));
  }

  @Test
  @DisplayName("resolve -> should reject missing envelope class")
  void resolve_shouldRejectMissingEnvelopeClass() {
    OpenApiGenericsProperties properties =
        new OpenApiGenericsProperties(new EnvelopeProperties("com.example.DoesNotExist"), null);

    OpenApiGenericsConfigurationException ex =
        assertThrows(
            OpenApiGenericsConfigurationException.class, () -> resolver.resolve(properties));

    assertTrue(ex.getMessage().contains("Configured envelope class not found"));
  }

  @Test
  @DisplayName("resolve -> should reject interface envelope")
  void resolve_shouldRejectInterfaceEnvelope() {
    OpenApiGenericsProperties properties =
        new OpenApiGenericsProperties(
            new EnvelopeProperties(InvalidEnvelopeInterface.class.getName()), null);

    OpenApiGenericsConfigurationException ex =
        assertThrows(
            OpenApiGenericsConfigurationException.class, () -> resolver.resolve(properties));

    assertTrue(ex.getMessage().contains("must be a concrete class, not an interface"));
  }

  @Test
  @DisplayName("resolve -> should reject abstract envelope")
  void resolve_shouldRejectAbstractEnvelope() {
    OpenApiGenericsProperties properties =
        new OpenApiGenericsProperties(
            new EnvelopeProperties(AbstractEnvelope.class.getName()), null);

    OpenApiGenericsConfigurationException ex =
        assertThrows(
            OpenApiGenericsConfigurationException.class, () -> resolver.resolve(properties));

    assertTrue(ex.getMessage().contains("must be a concrete class, not an abstract class"));
  }

  @Test
  @DisplayName("resolve -> should reject record envelope")
  void resolve_shouldRejectRecordEnvelope() {
    OpenApiGenericsProperties properties =
        new OpenApiGenericsProperties(
            new EnvelopeProperties(InvalidEnvelopeRecord.class.getName()), null);

    OpenApiGenericsConfigurationException ex =
        assertThrows(
            OpenApiGenericsConfigurationException.class, () -> resolver.resolve(properties));

    assertTrue(ex.getMessage().contains("must be a class, not a record"));
  }

  @Test
  @DisplayName("resolve -> should reject envelope with multiple type parameters")
  void resolve_shouldRejectEnvelopeWithMultipleTypeParameters() {
    OpenApiGenericsProperties properties =
        new OpenApiGenericsProperties(
            new EnvelopeProperties(InvalidEnvelopeMultipleTypes.class.getName()), null);

    OpenApiGenericsConfigurationException ex =
        assertThrows(
            OpenApiGenericsConfigurationException.class, () -> resolver.resolve(properties));

    assertTrue(ex.getMessage().contains("must declare exactly one type parameter"));
  }

  @Test
  @DisplayName("resolve -> should reject envelope without direct payload field")
  void resolve_shouldRejectEnvelopeWithoutDirectPayloadField() {
    OpenApiGenericsProperties properties =
        new OpenApiGenericsProperties(
            new EnvelopeProperties(InvalidEnvelopeNoPayload.class.getName()), null);

    OpenApiGenericsConfigurationException ex =
        assertThrows(
            OpenApiGenericsConfigurationException.class, () -> resolver.resolve(properties));

    assertTrue(ex.getMessage().contains("must declare exactly one direct payload field"));
  }

  @Test
  @DisplayName("resolve -> should reject envelope with multiple direct payload fields")
  void resolve_shouldRejectEnvelopeWithMultipleDirectPayloadFields() {
    OpenApiGenericsProperties properties =
        new OpenApiGenericsProperties(
            new EnvelopeProperties(InvalidEnvelopeMultiplePayloads.class.getName()), null);

    OpenApiGenericsConfigurationException ex =
        assertThrows(
            OpenApiGenericsConfigurationException.class, () -> resolver.resolve(properties));

    assertTrue(ex.getMessage().contains("must declare exactly one direct payload field"));
  }

  @Test
  @DisplayName("resolve -> should reject envelope with nested payload field")
  void resolve_shouldRejectEnvelopeWithNestedPayloadField() {
    OpenApiGenericsProperties properties =
        new OpenApiGenericsProperties(
            new EnvelopeProperties(InvalidEnvelopeNestedPayload.class.getName()), null);

    OpenApiGenericsConfigurationException ex =
        assertThrows(
            OpenApiGenericsConfigurationException.class, () -> resolver.resolve(properties));

    assertTrue(ex.getMessage().contains("contains unsupported nested generic payload slot"));
  }

  @Test
  @DisplayName("resolve -> should reject enum envelope")
  void resolve_shouldRejectEnumEnvelope() {
    OpenApiGenericsProperties properties =
        new OpenApiGenericsProperties(
            new EnvelopeProperties(InvalidEnvelopeEnum.class.getName()), null);

    OpenApiGenericsConfigurationException ex =
        assertThrows(
            OpenApiGenericsConfigurationException.class, () -> resolver.resolve(properties));

    assertTrue(ex.getMessage().contains("must be a class, not an enum"));
  }

  @Test
  @DisplayName("resolve -> should reject annotation envelope (treated as interface)")
  void resolve_shouldRejectAnnotationEnvelope() {
    OpenApiGenericsProperties properties =
        new OpenApiGenericsProperties(
            new EnvelopeProperties(InvalidEnvelopeAnnotation.class.getName()), null);

    OpenApiGenericsConfigurationException ex =
        assertThrows(
            OpenApiGenericsConfigurationException.class, () -> resolver.resolve(properties));

    assertTrue(ex.getMessage().contains("must be a concrete class, not an interface"));
  }

  @Test
  @DisplayName("resolve -> should reject envelope with zero type parameters")
  void resolve_shouldRejectEnvelopeWithZeroTypeParameters() {
    OpenApiGenericsProperties properties =
        new OpenApiGenericsProperties(
            new EnvelopeProperties(InvalidEnvelopeNoGenerics.class.getName()), null);

    OpenApiGenericsConfigurationException ex =
        assertThrows(
            OpenApiGenericsConfigurationException.class, () -> resolver.resolve(properties));

    assertTrue(ex.getMessage().contains("must declare exactly one type parameter"));
  }

  @Test
  @DisplayName("resolve -> should ignore static fields when resolving payload slot")
  void resolve_shouldIgnoreStaticFields() {
    OpenApiGenericsProperties properties =
        new OpenApiGenericsProperties(
            new EnvelopeProperties(EnvelopeWithStaticField.class.getName()), null);

    ResponseIntrospectionPolicy policy = resolver.resolve(properties);

    assertEquals(EnvelopeWithStaticField.class, policy.envelopeType());
    assertEquals("payload", policy.payloadPropertyName());
  }

  @Test
  @DisplayName("resolve -> should detect nested payload in generic array")
  void resolve_shouldDetectNestedPayloadInGenericArray() {
    OpenApiGenericsProperties properties =
        new OpenApiGenericsProperties(
            new EnvelopeProperties(InvalidEnvelopeGenericArrayPayload.class.getName()), null);

    OpenApiGenericsConfigurationException ex =
        assertThrows(
            OpenApiGenericsConfigurationException.class, () -> resolver.resolve(properties));

    assertTrue(ex.getMessage().contains("contains unsupported nested generic payload slot"));
  }

  @Test
  @DisplayName("resolve -> should detect nested payload in deeply nested parameterized type")
  void resolve_shouldDetectDeeplyNestedPayload() {
    OpenApiGenericsProperties properties =
        new OpenApiGenericsProperties(
            new EnvelopeProperties(InvalidEnvelopeDeeplyNestedPayload.class.getName()), null);

    OpenApiGenericsConfigurationException ex =
        assertThrows(
            OpenApiGenericsConfigurationException.class, () -> resolver.resolve(properties));

    assertTrue(ex.getMessage().contains("contains unsupported nested generic payload slot"));
  }

  @Test
  @DisplayName("resolve -> should accept envelope where non-payload fields use unrelated generics")
  void resolve_shouldAcceptEnvelopeWithUnrelatedGenerics() {
    OpenApiGenericsProperties properties =
        new OpenApiGenericsProperties(
            new EnvelopeProperties(EnvelopeWithUnrelatedGenericField.class.getName()), null);

    ResponseIntrospectionPolicy policy = resolver.resolve(properties);

    assertEquals(EnvelopeWithUnrelatedGenericField.class, policy.envelopeType());
    assertEquals("payload", policy.payloadPropertyName());
  }

  @Test
  @DisplayName("resolve -> should use supported container resolver for custom envelope policy")
  void resolve_shouldUseSupportedContainerResolverForCustomEnvelopePolicy() {
    SupportedContainerDescriptor setContainer =
        new SupportedContainerDescriptor(
            Set.class,
            "Set",
            "Set",
            ContainerShape.DIRECT_ARRAY,
            null,
            ContainerSource.BUILT_IN,
            ContainerMatchMode.ASSIGNABLE);

    ResponseIntrospectionPolicyResolver customResolver =
        new ResponseIntrospectionPolicyResolver(
            () -> Set.of(setContainer), new ConfiguredContainerTypesResolver());

    OpenApiGenericsProperties properties =
        new OpenApiGenericsProperties(new EnvelopeProperties(ValidEnvelope.class.getName()), null);

    ResponseIntrospectionPolicy policy = customResolver.resolve(properties);

    assertEquals(ValidEnvelope.class, policy.envelopeType());
    assertEquals("payload", policy.payloadPropertyName());
    assertEquals(Set.of(setContainer), policy.supportedContainers());
  }

  interface InvalidEnvelopeInterface<T> {
    T payload();
  }

  static final class ValidEnvelope<T> {
    T payload;
    String status;
  }

  abstract static class AbstractEnvelope<T> {
    T payload;
  }

  record InvalidEnvelopeRecord<T>(T payload) {}

  static final class InvalidEnvelopeMultipleTypes<T, M> {
    T payload;
    M meta;
  }

  static final class InvalidEnvelopeNoPayload<T> {
    String status;
  }

  static final class InvalidEnvelopeMultiplePayloads<T> {
    T payload;
    T data;
  }

  static final class InvalidEnvelopeNestedPayload<T> {
    Wrapper<T> payload;
  }

  static final class Wrapper<T> {
    T value;
  }

  enum InvalidEnvelopeEnum {
    A,
    B
  }

  @interface InvalidEnvelopeAnnotation {}

  static final class InvalidEnvelopeNoGenerics {
    Object payload;
  }

  static final class EnvelopeWithStaticField<T> {
    static String ignored;
    T payload;
  }

  static final class InvalidEnvelopeGenericArrayPayload<T> {
    T[] payload;
  }

  static final class InvalidEnvelopeDeeplyNestedPayload<T> {
    java.util.Map<String, java.util.List<T>> payload;
  }

  static final class EnvelopeWithUnrelatedGenericField<T> {
    T payload;
    java.util.List<String> tags;
  }

  static final class Paging<T> {
    List<T> content;
  }
}

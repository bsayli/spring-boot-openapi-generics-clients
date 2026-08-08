package io.github.blueprintplatform.openapi.generics.server.core.introspection.container.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.server.autoconfigure.properties.ContainerProperties;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerMatchMode;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerShape;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerSource;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.SupportedContainerDescriptor;
import io.github.blueprintplatform.openapi.generics.server.exception.OpenApiGenericsConfigurationException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: ConfiguredContainerTypesResolver")
class ConfiguredContainerTypesResolverTest {

  private final ConfiguredContainerTypesResolver resolver = new ConfiguredContainerTypesResolver();

  @Test
  @DisplayName("resolve -> should return empty set when properties are null")
  void resolve_shouldReturnEmptySetWhenPropertiesAreNull() {
    Set<SupportedContainerDescriptor> result = resolver.resolve(null);

    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("resolve -> should return empty set when properties are empty")
  void resolve_shouldReturnEmptySetWhenPropertiesAreEmpty() {
    Set<SupportedContainerDescriptor> result = resolver.resolve(List.of());

    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("resolve -> should ignore null property entries")
  void resolve_shouldIgnoreNullPropertyEntries() {
    List<ContainerProperties> properties =
        java.util.Arrays.asList(null, properties(ValidListContainer.class, "items"));

    Set<SupportedContainerDescriptor> result = resolver.resolve(properties);

    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("resolve -> should create descriptor for valid List container")
  void resolve_shouldCreateDescriptorForValidListContainer() {
    Set<SupportedContainerDescriptor> result =
        resolver.resolve(List.of(properties(ValidListContainer.class, "items")));

    assertEquals(1, result.size());

    SupportedContainerDescriptor descriptor = result.iterator().next();

    assertEquals(ValidListContainer.class, descriptor.type());
    assertEquals("ValidListContainer", descriptor.schemaName());
    assertEquals("ValidListContainer", descriptor.containerName());
    assertEquals(ContainerShape.OBJECT_WITH_ITEM_ARRAY, descriptor.shape());
    assertEquals("items", descriptor.itemPropertyName());
    assertEquals(ContainerSource.CONFIGURED, descriptor.source());
    assertEquals(ContainerMatchMode.EXACT, descriptor.matchMode());
    assertEquals(ValidListContainer.class.getName(), descriptor.containerTypeName());
  }

  @Test
  @DisplayName("resolve -> should create descriptor for valid Set container")
  void resolve_shouldCreateDescriptorForValidSetContainer() {
    Set<SupportedContainerDescriptor> result =
        resolver.resolve(List.of(properties(ValidSetContainer.class, "elements")));

    assertEquals(1, result.size());

    SupportedContainerDescriptor descriptor = result.iterator().next();

    assertEquals(ValidSetContainer.class, descriptor.type());
    assertEquals("elements", descriptor.itemPropertyName());
    assertEquals(ContainerSource.CONFIGURED, descriptor.source());
    assertEquals(ContainerMatchMode.EXACT, descriptor.matchMode());
  }

  @Test
  @DisplayName("resolve -> should resolve multiple configured containers")
  void resolve_shouldResolveMultipleConfiguredContainers() {
    Set<SupportedContainerDescriptor> result =
        resolver.resolve(
            List.of(
                properties(ValidListContainer.class, "items"),
                properties(ValidSetContainer.class, "elements")));

    assertEquals(2, result.size());
  }

  @Test
  @DisplayName("resolve -> should reject null container type")
  void resolve_shouldRejectNullContainerType() {
    OpenApiGenericsConfigurationException exception =
        assertThrows(
            OpenApiGenericsConfigurationException.class,
            () -> resolver.resolve(List.of(new ContainerProperties(null, "items"))));

    assertEquals("Container type must not be null or blank", exception.getMessage());
  }

  @Test
  @DisplayName("resolve -> should reject blank container type")
  void resolve_shouldRejectBlankContainerType() {
    OpenApiGenericsConfigurationException exception =
        assertThrows(
            OpenApiGenericsConfigurationException.class,
            () -> resolver.resolve(List.of(new ContainerProperties(" ", "items"))));

    assertEquals("Container type must not be null or blank", exception.getMessage());
  }

  @Test
  @DisplayName("resolve -> should reject non fully qualified container type")
  void resolve_shouldRejectNonFullyQualifiedContainerType() {
    OpenApiGenericsConfigurationException exception =
        assertThrows(
            OpenApiGenericsConfigurationException.class,
            () -> resolver.resolve(List.of(new ContainerProperties("Paging", "items"))));

    assertEquals(
        "Invalid container type 'Paging'. Expected fully-qualified class name "
            + "(e.g. com.example.Paging)",
        exception.getMessage());
  }

  @Test
  @DisplayName("resolve -> should reject missing container class")
  void resolve_shouldRejectMissingContainerClass() {
    String type = "com.example.missing.DoesNotExist";

    OpenApiGenericsConfigurationException exception =
        assertThrows(
            OpenApiGenericsConfigurationException.class,
            () -> resolver.resolve(List.of(new ContainerProperties(type, "items"))));

    assertEquals(
        "Configured container class not found: '"
            + type
            + "'. Ensure the class exists and is on the application classpath.",
        exception.getMessage());

    assertNotNull(exception.getCause());
    assertInstanceOf(ClassNotFoundException.class, exception.getCause());
  }

  @Test
  @DisplayName("resolve -> should reject interface container")
  void resolve_shouldRejectInterfaceContainer() {
    OpenApiGenericsConfigurationException exception =
        assertInvalidContainer(GenericInterface.class, "items");

    assertEquals(
        unsupported(GenericInterface.class, "must be a concrete class or record, not an interface"),
        exception.getMessage());
  }

  @Test
  @DisplayName("resolve -> should reject enum container")
  void resolve_shouldRejectEnumContainer() {
    OpenApiGenericsConfigurationException exception =
        assertInvalidContainer(GenericEnum.class, "items");

    assertEquals(
        unsupported(GenericEnum.class, "must be a class or record, not an enum"),
        exception.getMessage());
  }

  @Test
  @DisplayName("resolve -> should reject annotation container")
  void resolve_shouldRejectAnnotationContainer() {
    OpenApiGenericsConfigurationException exception =
        assertInvalidContainer(GenericAnnotation.class, "items");

    assertEquals(
        unsupported(GenericAnnotation.class, "must be a class or record, not an annotation"),
        exception.getMessage());
  }

  @Test
  @DisplayName("resolve -> should reject array container")
  void resolve_shouldRejectArrayContainer() {
    OpenApiGenericsConfigurationException exception =
        assertInvalidContainer(String[].class, "items");

    assertEquals(
        unsupported(String[].class, "must be a class or record, not an array"),
        exception.getMessage());
  }

  @Test
  @DisplayName("resolve -> should reject abstract container")
  void resolve_shouldRejectAbstractContainer() {
    OpenApiGenericsConfigurationException exception =
        assertInvalidContainer(AbstractContainer.class, "items");

    assertEquals(
        unsupported(AbstractContainer.class, "must be concrete, not abstract"),
        exception.getMessage());
  }

  @Test
  @DisplayName("resolve -> should reject container without type parameter")
  void resolve_shouldRejectContainerWithoutTypeParameter() {
    OpenApiGenericsConfigurationException exception =
        assertInvalidContainer(NoTypeParameterContainer.class, "items");

    assertEquals(
        unsupported(NoTypeParameterContainer.class, "must declare exactly one type parameter"),
        exception.getMessage());
  }

  @Test
  @DisplayName("resolve -> should reject container with multiple type parameters")
  void resolve_shouldRejectContainerWithMultipleTypeParameters() {
    OpenApiGenericsConfigurationException exception =
        assertInvalidContainer(MultipleTypeParameterContainer.class, "items");

    assertEquals(
        unsupported(
            MultipleTypeParameterContainer.class, "must declare exactly one type parameter"),
        exception.getMessage());
  }

  @Test
  @DisplayName("resolve -> should reject null item property")
  void resolve_shouldRejectNullItemProperty() {
    OpenApiGenericsConfigurationException exception =
        assertInvalidContainer(ValidListContainer.class, null);

    assertEquals(
        unsupported(ValidListContainer.class, "item-property 'null' must not be null or blank"),
        exception.getMessage());
  }

  @Test
  @DisplayName("resolve -> should reject blank item property")
  void resolve_shouldRejectBlankItemProperty() {
    OpenApiGenericsConfigurationException exception =
        assertInvalidContainer(ValidListContainer.class, " ");

    assertEquals(
        unsupported(ValidListContainer.class, "item-property ' ' must not be null or blank"),
        exception.getMessage());
  }

  @Test
  @DisplayName("resolve -> should reject missing item property")
  void resolve_shouldRejectMissingItemProperty() {
    OpenApiGenericsConfigurationException exception =
        assertInvalidContainer(ValidListContainer.class, "missing");

    assertEquals(
        unsupported(ValidListContainer.class, "item-property 'missing' does not exist"),
        exception.getMessage());
  }

  @Test
  @DisplayName("resolve -> should reject static item property")
  void resolve_shouldRejectStaticItemProperty() {
    OpenApiGenericsConfigurationException exception =
        assertInvalidContainer(StaticFieldContainer.class, "items");

    assertEquals(
        unsupported(StaticFieldContainer.class, "item-property 'items' must be an instance field"),
        exception.getMessage());
  }

  @Test
  @DisplayName("resolve -> should reject non parameterized item property")
  void resolve_shouldRejectNonParameterizedItemProperty() {
    OpenApiGenericsConfigurationException exception =
        assertInvalidContainer(RawListContainer.class, "items");

    assertEquals(
        unsupported(RawListContainer.class, "item-property 'items' must be List<T> or Set<T>"),
        exception.getMessage());
  }

  @Test
  @DisplayName("resolve -> should reject unsupported collection item property")
  void resolve_shouldRejectUnsupportedCollectionItemProperty() {
    OpenApiGenericsConfigurationException exception =
        assertInvalidContainer(CollectionContainer.class, "items");

    assertEquals(
        unsupported(CollectionContainer.class, "item-property 'items' must be List<T> or Set<T>"),
        exception.getMessage());
  }

  @Test
  @DisplayName("resolve -> should reject concrete item type instead of container type parameter")
  void resolve_shouldRejectConcreteItemTypeInsteadOfContainerTypeParameter() {
    OpenApiGenericsConfigurationException exception =
        assertInvalidContainer(ConcreteItemContainer.class, "items");

    assertEquals(
        unsupported(
            ConcreteItemContainer.class,
            "item-property 'items' must use the container type parameter directly"),
        exception.getMessage());
  }

  @Test
  @DisplayName("resolve -> should reject nested generic item type")
  void resolve_shouldRejectNestedGenericItemType() {
    OpenApiGenericsConfigurationException exception =
        assertInvalidContainer(InheritedTypeVariableContainer.class, "items");

    assertEquals(
        unsupported(
            InheritedTypeVariableContainer.class,
            "item-property 'items' must use the container type parameter directly"),
        exception.getMessage());
  }

  private OpenApiGenericsConfigurationException assertInvalidContainer(
      Class<?> type, String itemProperty) {

    return assertThrows(
        OpenApiGenericsConfigurationException.class,
        () -> resolver.resolve(List.of(new ContainerProperties(type.getName(), itemProperty))));
  }

  private static ContainerProperties properties(Class<?> type, String itemProperty) {
    return new ContainerProperties(type.getName(), itemProperty);
  }

  private static String unsupported(Class<?> type, String reason) {
    return "Unsupported container type '" + type.getName() + "': " + reason;
  }

  private static final class ValidListContainer<T> {
    private List<T> items;
  }

  private static final class ValidSetContainer<T> {
    private Set<T> elements;
  }

  private interface GenericInterface<T> {}

  private enum GenericEnum {
    VALUE
  }

  private @interface GenericAnnotation {}

  private abstract static class AbstractContainer<T> {
    private List<T> items;
  }

  private static final class NoTypeParameterContainer {
    private List<String> items;
  }

  private static final class MultipleTypeParameterContainer<T, U> {
    private List<T> items;
  }

  private static final class StaticFieldContainer<T> {
    private static List<?> items;
  }

  @SuppressWarnings({"rawtypes", "unused"})
  private static final class RawListContainer<T> {
    private List items;
  }

  private static final class CollectionContainer<T> {
    private Collection<T> items;
  }

  private static final class ConcreteItemContainer<T> {
    private List<String> items;
  }

  private static class ParentContainer<T> {
    protected List<T> inherited;
  }

  private static final class InheritedTypeVariableContainer<T> extends ParentContainer<String> {

    private List<ParentTypeParameter<T>> items;
  }

  private static final class ParentTypeParameter<T> {}
}

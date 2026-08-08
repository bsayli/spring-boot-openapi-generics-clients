package io.github.blueprintplatform.openapi.generics.server.core.introspection.container.resolver;

import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.ContainerNames.LIST;
import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.ContainerNames.PAGE;
import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.ContainerNames.SET;
import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.SchemaConstants.PROPERTY_CONTENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerMatchMode;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerShape;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerSource;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.SupportedContainerDescriptor;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: DefaultSupportedContainerTypesResolver")
class DefaultSupportedContainerTypesResolverTest {

  private final DefaultSupportedContainerTypesResolver resolver =
      new DefaultSupportedContainerTypesResolver();

  @Test
  @DisplayName("resolve -> should return all built-in container descriptors")
  void resolve_shouldReturnAllBuiltInContainerDescriptors() {
    Set<SupportedContainerDescriptor> result = resolver.resolve();

    assertEquals(3, result.size());

    assertTrue(result.stream().anyMatch(descriptor -> descriptor.type() == Page.class));
    assertTrue(result.stream().anyMatch(descriptor -> descriptor.type() == List.class));
    assertTrue(result.stream().anyMatch(descriptor -> descriptor.type() == Set.class));
  }

  @Test
  @DisplayName("resolve -> should configure Page as exact object container")
  void resolve_shouldConfigurePageAsExactObjectContainer() {
    SupportedContainerDescriptor descriptor = descriptorFor(Page.class);

    assertEquals(Page.class, descriptor.type());
    assertEquals(PAGE, descriptor.schemaName());
    assertEquals(PAGE, descriptor.containerName());
    assertEquals(ContainerShape.OBJECT_WITH_ITEM_ARRAY, descriptor.shape());
    assertEquals(PROPERTY_CONTENT, descriptor.itemPropertyName());
    assertEquals(ContainerSource.BUILT_IN, descriptor.source());
    assertEquals(ContainerMatchMode.EXACT, descriptor.matchMode());
    assertEquals(Page.class.getName(), descriptor.containerTypeName());

    assertTrue(descriptor.matches(Page.class));
  }

  @Test
  @DisplayName("resolve -> should configure List as assignable direct array container")
  void resolve_shouldConfigureListAsAssignableDirectArrayContainer() {
    SupportedContainerDescriptor descriptor = descriptorFor(List.class);

    assertEquals(List.class, descriptor.type());
    assertEquals(LIST, descriptor.schemaName());
    assertEquals(LIST, descriptor.containerName());
    assertEquals(ContainerShape.DIRECT_ARRAY, descriptor.shape());
    assertEquals(null, descriptor.itemPropertyName());
    assertEquals(ContainerSource.BUILT_IN, descriptor.source());
    assertEquals(ContainerMatchMode.ASSIGNABLE, descriptor.matchMode());
    assertEquals(List.class.getName(), descriptor.containerTypeName());

    assertTrue(descriptor.matches(List.class));
    assertTrue(descriptor.matches(java.util.ArrayList.class));
  }

  @Test
  @DisplayName("resolve -> should configure Set as assignable direct array container")
  void resolve_shouldConfigureSetAsAssignableDirectArrayContainer() {
    SupportedContainerDescriptor descriptor = descriptorFor(Set.class);

    assertEquals(Set.class, descriptor.type());
    assertEquals(SET, descriptor.schemaName());
    assertEquals(SET, descriptor.containerName());
    assertEquals(ContainerShape.DIRECT_ARRAY, descriptor.shape());
    assertEquals(null, descriptor.itemPropertyName());
    assertEquals(ContainerSource.BUILT_IN, descriptor.source());
    assertEquals(ContainerMatchMode.ASSIGNABLE, descriptor.matchMode());
    assertEquals(Set.class.getName(), descriptor.containerTypeName());

    assertTrue(descriptor.matches(Set.class));
    assertTrue(descriptor.matches(java.util.HashSet.class));
  }

  private SupportedContainerDescriptor descriptorFor(Class<?> type) {
    SupportedContainerDescriptor descriptor =
        resolver.resolve().stream()
            .filter(candidate -> candidate.type() == type)
            .findFirst()
            .orElse(null);

    assertNotNull(descriptor, () -> "Missing built-in descriptor for " + type.getName());
    return descriptor;
  }
}

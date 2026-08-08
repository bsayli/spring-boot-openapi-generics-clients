package io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.server.exception.OpenApiDescriptorValidationException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: SupportedContainerDescriptor")
class SupportedContainerDescriptorTest {

  @Test
  @DisplayName("constructor -> should create valid object container descriptor")
  void constructor_shouldCreateValidObjectContainerDescriptor() {
    SupportedContainerDescriptor descriptor =
        new SupportedContainerDescriptor(
            Page.class,
            "Page",
            "Page",
            ContainerShape.OBJECT_WITH_ITEM_ARRAY,
            "content",
            ContainerSource.BUILT_IN,
            ContainerMatchMode.EXACT);

    assertEquals(Page.class, descriptor.type());
    assertEquals("Page", descriptor.schemaName());
    assertEquals("Page", descriptor.containerName());
    assertEquals(ContainerShape.OBJECT_WITH_ITEM_ARRAY, descriptor.shape());
    assertEquals("content", descriptor.itemPropertyName());
    assertEquals(ContainerSource.BUILT_IN, descriptor.source());
    assertEquals(ContainerMatchMode.EXACT, descriptor.matchMode());
    assertEquals(Page.class.getName(), descriptor.containerTypeName());
  }

  @Test
  @DisplayName("constructor -> should create valid direct array descriptor")
  void constructor_shouldCreateValidDirectArrayDescriptor() {
    SupportedContainerDescriptor descriptor =
        new SupportedContainerDescriptor(
            List.class,
            "List",
            "List",
            ContainerShape.DIRECT_ARRAY,
            null,
            ContainerSource.BUILT_IN,
            ContainerMatchMode.ASSIGNABLE);

    assertEquals(ContainerShape.DIRECT_ARRAY, descriptor.shape());
    assertEquals(ContainerMatchMode.ASSIGNABLE, descriptor.matchMode());
  }

  @Test
  @DisplayName("constructor -> should reject null type")
  void constructor_shouldRejectNullType() {
    OpenApiDescriptorValidationException exception =
        assertThrows(
            OpenApiDescriptorValidationException.class,
            () ->
                new SupportedContainerDescriptor(
                    null,
                    "Page",
                    "Page",
                    ContainerShape.OBJECT_WITH_ITEM_ARRAY,
                    "content",
                    ContainerSource.BUILT_IN,
                    ContainerMatchMode.EXACT));

    assertEquals("type must not be null", exception.getMessage());
  }

  @Test
  @DisplayName("constructor -> should reject blank schema name")
  void constructor_shouldRejectBlankSchemaName() {
    OpenApiDescriptorValidationException exception =
        assertThrows(
            OpenApiDescriptorValidationException.class,
            () ->
                new SupportedContainerDescriptor(
                    Page.class,
                    " ",
                    "Page",
                    ContainerShape.OBJECT_WITH_ITEM_ARRAY,
                    "content",
                    ContainerSource.BUILT_IN,
                    ContainerMatchMode.EXACT));

    assertEquals("schemaName must not be null or blank", exception.getMessage());
  }

  @Test
  @DisplayName("constructor -> should reject blank container name")
  void constructor_shouldRejectBlankContainerName() {
    OpenApiDescriptorValidationException exception =
        assertThrows(
            OpenApiDescriptorValidationException.class,
            () ->
                new SupportedContainerDescriptor(
                    Page.class,
                    "Page",
                    "",
                    ContainerShape.OBJECT_WITH_ITEM_ARRAY,
                    "content",
                    ContainerSource.BUILT_IN,
                    ContainerMatchMode.EXACT));

    assertEquals("containerName must not be null or blank", exception.getMessage());
  }

  @Test
  @DisplayName("constructor -> should reject null shape")
  void constructor_shouldRejectNullShape() {
    OpenApiDescriptorValidationException exception =
        assertThrows(
            OpenApiDescriptorValidationException.class,
            () ->
                new SupportedContainerDescriptor(
                    Page.class,
                    "Page",
                    "Page",
                    null,
                    "content",
                    ContainerSource.BUILT_IN,
                    ContainerMatchMode.EXACT));

    assertEquals("shape must not be null", exception.getMessage());
  }

  @Test
  @DisplayName("constructor -> should reject null source")
  void constructor_shouldRejectNullSource() {
    OpenApiDescriptorValidationException exception =
        assertThrows(
            OpenApiDescriptorValidationException.class,
            () ->
                new SupportedContainerDescriptor(
                    Page.class,
                    "Page",
                    "Page",
                    ContainerShape.OBJECT_WITH_ITEM_ARRAY,
                    "content",
                    null,
                    ContainerMatchMode.EXACT));

    assertEquals("source must not be null", exception.getMessage());
  }

  @Test
  @DisplayName("constructor -> should reject null match mode")
  void constructor_shouldRejectNullMatchMode() {
    OpenApiDescriptorValidationException exception =
        assertThrows(
            OpenApiDescriptorValidationException.class,
            () ->
                new SupportedContainerDescriptor(
                    Page.class,
                    "Page",
                    "Page",
                    ContainerShape.OBJECT_WITH_ITEM_ARRAY,
                    "content",
                    ContainerSource.BUILT_IN,
                    null));

    assertEquals("matchMode must not be null", exception.getMessage());
  }

  @Test
  @DisplayName("constructor -> should require item property for object container")
  void constructor_shouldRequireItemPropertyForObjectContainer() {
    OpenApiDescriptorValidationException exception =
        assertThrows(
            OpenApiDescriptorValidationException.class,
            () ->
                new SupportedContainerDescriptor(
                    Page.class,
                    "Page",
                    "Page",
                    ContainerShape.OBJECT_WITH_ITEM_ARRAY,
                    null,
                    ContainerSource.BUILT_IN,
                    ContainerMatchMode.EXACT));

    assertEquals(
        "itemPropertyName must not be null or blank for object containers", exception.getMessage());
  }

  @Test
  @DisplayName("constructor -> should reject item property for direct array")
  void constructor_shouldRejectItemPropertyForDirectArray() {
    OpenApiDescriptorValidationException exception =
        assertThrows(
            OpenApiDescriptorValidationException.class,
            () ->
                new SupportedContainerDescriptor(
                    List.class,
                    "List",
                    "List",
                    ContainerShape.DIRECT_ARRAY,
                    "items",
                    ContainerSource.BUILT_IN,
                    ContainerMatchMode.ASSIGNABLE));

    assertEquals(
        "itemPropertyName must be null for direct array containers", exception.getMessage());
  }

  @Test
  @DisplayName("matches -> should use exact matching")
  void matches_shouldUseExactMatching() {
    SupportedContainerDescriptor descriptor =
        new SupportedContainerDescriptor(
            List.class,
            "List",
            "List",
            ContainerShape.DIRECT_ARRAY,
            null,
            ContainerSource.BUILT_IN,
            ContainerMatchMode.EXACT);

    assertTrue(descriptor.matches(List.class));
    assertFalse(descriptor.matches(ArrayList.class));
    assertFalse(descriptor.matches(null));
  }

  @Test
  @DisplayName("matches -> should use assignable matching")
  void matches_shouldUseAssignableMatching() {
    SupportedContainerDescriptor descriptor =
        new SupportedContainerDescriptor(
            List.class,
            "List",
            "List",
            ContainerShape.DIRECT_ARRAY,
            null,
            ContainerSource.BUILT_IN,
            ContainerMatchMode.ASSIGNABLE);

    assertTrue(descriptor.matches(List.class));
    assertTrue(descriptor.matches(ArrayList.class));
    assertFalse(descriptor.matches(String.class));
    assertFalse(descriptor.matches(null));
  }

  private static final class Page<T> {
    List<T> content;
  }
}

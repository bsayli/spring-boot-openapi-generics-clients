package io.github.blueprintplatform.openapi.generics.server.core.introspection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerMatchMode;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerShape;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerSource;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.SupportedContainerDescriptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: ResponseTypeDescriptor")
class ResponseTypeDescriptorTest {

  private static final SupportedContainerDescriptor PAGE_CONTAINER =
      new SupportedContainerDescriptor(
          Page.class,
          "Page",
          "Page",
          ContainerShape.OBJECT_WITH_ITEM_ARRAY,
          "content",
          ContainerSource.BUILT_IN,
          ContainerMatchMode.EXACT);

  @Test
  @DisplayName("simple -> should create simple response descriptor")
  void simple_shouldCreateSimpleResponseDescriptor() {
    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

    assertEquals(ServiceResponse.class, descriptor.envelopeType());
    assertEquals("data", descriptor.payloadPropertyName());
    assertEquals("CustomerDto", descriptor.dataRefName());
    assertNull(descriptor.container());
    assertNull(descriptor.containerName());
    assertNull(descriptor.containerTypeName());
    assertNull(descriptor.itemRefName());
    assertFalse(descriptor.isContainer());
  }

  @Test
  @DisplayName("container -> should create container response descriptor")
  void container_shouldCreateContainerResponseDescriptor() {
    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.container(
            ServiceResponse.class, "data", PAGE_CONTAINER, "CustomerDto");

    assertEquals(ServiceResponse.class, descriptor.envelopeType());
    assertEquals("data", descriptor.payloadPropertyName());
    assertEquals("PageCustomerDto", descriptor.dataRefName());
    assertEquals(PAGE_CONTAINER, descriptor.container());
    assertEquals("Page", descriptor.containerName());
    assertEquals(Page.class.getName(), descriptor.containerTypeName());
    assertEquals("CustomerDto", descriptor.itemRefName());
    assertTrue(descriptor.isContainer());
  }

  @Test
  @DisplayName("equals -> should return true for same instance")
  void equals_shouldReturnTrueForSameInstance() {
    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

    assertEquals(descriptor, descriptor);
  }

  @Test
  @DisplayName("equals -> should return true for equivalent simple descriptors")
  void equals_shouldReturnTrueForEquivalentSimpleDescriptors() {
    ResponseTypeDescriptor first =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

    ResponseTypeDescriptor second =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  @DisplayName("equals -> should return true for equivalent container descriptors")
  void equals_shouldReturnTrueForEquivalentContainerDescriptors() {
    ResponseTypeDescriptor first =
        ResponseTypeDescriptor.container(
            ServiceResponse.class, "data", PAGE_CONTAINER, "CustomerDto");

    ResponseTypeDescriptor second =
        ResponseTypeDescriptor.container(
            ServiceResponse.class, "data", PAGE_CONTAINER, "CustomerDto");

    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  @DisplayName("equals -> should return false for null")
  void equals_shouldReturnFalseForNull() {
    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

    assertNotEquals(descriptor, null);
  }

  @Test
  @DisplayName("equals -> should return false for unrelated type")
  void equals_shouldReturnFalseForUnrelatedType() {
    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

    assertNotEquals(descriptor, "CustomerDto");
  }

  @Test
  @DisplayName("equals -> should detect different envelope type")
  void equals_shouldDetectDifferentEnvelopeType() {
    ResponseTypeDescriptor first =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

    ResponseTypeDescriptor second =
        ResponseTypeDescriptor.simple(AlternativeEnvelope.class, "data", "CustomerDto");

    assertNotEquals(first, second);
  }

  @Test
  @DisplayName("equals -> should detect different payload property")
  void equals_shouldDetectDifferentPayloadProperty() {
    ResponseTypeDescriptor first =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

    ResponseTypeDescriptor second =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "payload", "CustomerDto");

    assertNotEquals(first, second);
  }

  @Test
  @DisplayName("equals -> should detect different data reference")
  void equals_shouldDetectDifferentDataReference() {
    ResponseTypeDescriptor first =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

    ResponseTypeDescriptor second =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "OrderDto");

    assertNotEquals(first, second);
  }

  @Test
  @DisplayName("equals -> should detect simple versus container descriptor")
  void equals_shouldDetectSimpleVersusContainerDescriptor() {
    ResponseTypeDescriptor simple =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "PageCustomerDto");

    ResponseTypeDescriptor container =
        ResponseTypeDescriptor.container(
            ServiceResponse.class, "data", PAGE_CONTAINER, "CustomerDto");

    assertNotEquals(simple, container);
  }

  @Test
  @DisplayName("equals -> should detect different container")
  void equals_shouldDetectDifferentContainer() {
    SupportedContainerDescriptor alternativeContainer =
        new SupportedContainerDescriptor(
            CustomPage.class,
            "Page",
            "Page",
            ContainerShape.OBJECT_WITH_ITEM_ARRAY,
            "items",
            ContainerSource.CONFIGURED,
            ContainerMatchMode.EXACT);

    ResponseTypeDescriptor first =
        ResponseTypeDescriptor.container(
            ServiceResponse.class, "data", PAGE_CONTAINER, "CustomerDto");

    ResponseTypeDescriptor second =
        ResponseTypeDescriptor.container(
            ServiceResponse.class, "data", alternativeContainer, "CustomerDto");

    assertNotEquals(first, second);
  }

  @Test
  @DisplayName("equals -> should detect different item reference")
  void equals_shouldDetectDifferentItemReference() {
    ResponseTypeDescriptor first =
        ResponseTypeDescriptor.container(
            ServiceResponse.class, "data", PAGE_CONTAINER, "CustomerDto");

    ResponseTypeDescriptor second =
        ResponseTypeDescriptor.container(ServiceResponse.class, "data", PAGE_CONTAINER, "OrderDto");

    assertNotEquals(first, second);
  }

  @Test
  @DisplayName("toString -> should include simple descriptor state")
  void toString_shouldIncludeSimpleDescriptorState() {
    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

    assertEquals(
        "ResponseTypeDescriptor{"
            + "envelopeType=ServiceResponse"
            + ", payloadPropertyName='data'"
            + ", dataRefName='CustomerDto'"
            + ", containerName='null'"
            + ", containerTypeName='null'"
            + ", itemRefName='null'"
            + '}',
        descriptor.toString());
  }

  @Test
  @DisplayName("toString -> should include container descriptor state")
  void toString_shouldIncludeContainerDescriptorState() {
    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.container(
            ServiceResponse.class, "data", PAGE_CONTAINER, "CustomerDto");

    assertEquals(
        "ResponseTypeDescriptor{"
            + "envelopeType=ServiceResponse"
            + ", payloadPropertyName='data'"
            + ", dataRefName='PageCustomerDto'"
            + ", containerName='Page'"
            + ", containerTypeName='"
            + Page.class.getName()
            + "'"
            + ", itemRefName='CustomerDto'"
            + '}',
        descriptor.toString());
  }

  @Test
  @DisplayName("toString -> should handle null envelope type")
  void toString_shouldHandleNullEnvelopeType() {
    ResponseTypeDescriptor descriptor = ResponseTypeDescriptor.simple(null, "data", "CustomerDto");

    assertTrue(descriptor.toString().contains("envelopeType=null"));
  }

  private static final class AlternativeEnvelope<T> {}

  private static final class CustomPage<T> {}
}

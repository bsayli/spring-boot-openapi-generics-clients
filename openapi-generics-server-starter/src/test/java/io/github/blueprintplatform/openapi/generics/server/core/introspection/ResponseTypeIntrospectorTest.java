package io.github.blueprintplatform.openapi.generics.server.core.introspection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerMatchMode;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerShape;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerSource;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.SupportedContainerDescriptor;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncTask;

@Tag("unit")
@DisplayName("Unit Test: ResponseTypeIntrospector")
class ResponseTypeIntrospectorTest {

  private static final SupportedContainerDescriptor PAGE_CONTAINER =
      new SupportedContainerDescriptor(
          Page.class,
          "Page",
          "Page",
          ContainerShape.OBJECT_WITH_ITEM_ARRAY,
          "content",
          ContainerSource.BUILT_IN,
          ContainerMatchMode.EXACT);

  private static final SupportedContainerDescriptor LIST_CONTAINER =
      new SupportedContainerDescriptor(
          List.class,
          "List",
          "List",
          ContainerShape.DIRECT_ARRAY,
          null,
          ContainerSource.BUILT_IN,
          ContainerMatchMode.ASSIGNABLE);

  private static final SupportedContainerDescriptor PAGING_CONTAINER =
      new SupportedContainerDescriptor(
          Paging.class,
          "Paging",
          "Paging",
          ContainerShape.OBJECT_WITH_ITEM_ARRAY,
          "content",
          ContainerSource.CONFIGURED,
          ContainerMatchMode.EXACT);

  private static final ResponseIntrospectionPolicy DEFAULT_POLICY =
      new ResponseIntrospectionPolicy(
          ServiceResponse.class, "data", Set.of(PAGE_CONTAINER, LIST_CONTAINER));

  private static final ResponseIntrospectionPolicy CUSTOM_CONTAINER_POLICY =
      new ResponseIntrospectionPolicy(
          ServiceResponse.class, "data", Set.of(PAGE_CONTAINER, LIST_CONTAINER, PAGING_CONTAINER));

  private final ResponseTypeIntrospector introspector =
      new ResponseTypeIntrospector(DEFAULT_POLICY);

  @Test
  @DisplayName("extract -> should return simple descriptor for ServiceResponse<T>")
  void extract_shouldReturnSimpleDescriptor_forSimpleEnvelope() {
    ResolvableType type = envelopeOf(ResolvableType.forClass(CustomerDto.class));

    ResponseTypeDescriptor descriptor = introspector.extract(type).orElseThrow();

    assertEquals(ServiceResponse.class, descriptor.envelopeType());
    assertEquals("data", descriptor.payloadPropertyName());
    assertEquals("CustomerDto", descriptor.dataRefName());
    assertNull(descriptor.containerName());
    assertNull(descriptor.itemRefName());
    assertFalse(descriptor.isContainer());
  }

  @Test
  @DisplayName("extract -> should return container descriptor for ServiceResponse<Page<T>>")
  void extract_shouldReturnContainerDescriptor_forPageEnvelope() {
    ResolvableType pageType =
        ResolvableType.forClassWithGenerics(Page.class, ResolvableType.forClass(CustomerDto.class));

    ResponseTypeDescriptor descriptor = introspector.extract(envelopeOf(pageType)).orElseThrow();

    assertEquals(ServiceResponse.class, descriptor.envelopeType());
    assertEquals("data", descriptor.payloadPropertyName());
    assertEquals("PageCustomerDto", descriptor.dataRefName());
    assertEquals("Page", descriptor.containerName());
    assertEquals(Page.class.getName(), descriptor.containerTypeName());
    assertEquals("CustomerDto", descriptor.itemRefName());
    assertTrue(descriptor.isContainer());
  }

  @Test
  @DisplayName("extract -> should return container descriptor for List<T>")
  void extract_shouldReturnContainerDescriptor_forListEnvelope() {
    ResolvableType listType =
        ResolvableType.forClassWithGenerics(List.class, ResolvableType.forClass(CustomerDto.class));

    ResponseTypeDescriptor descriptor = introspector.extract(envelopeOf(listType)).orElseThrow();

    assertEquals("ListCustomerDto", descriptor.dataRefName());
    assertEquals("List", descriptor.containerName());
    assertEquals(List.class.getName(), descriptor.containerTypeName());
    assertEquals("CustomerDto", descriptor.itemRefName());
    assertTrue(descriptor.isContainer());
  }

  @Test
  @DisplayName("extract -> should return container descriptor for configured container")
  void extract_shouldReturnContainerDescriptor_forConfiguredContainer() {
    ResponseTypeIntrospector customIntrospector =
        new ResponseTypeIntrospector(CUSTOM_CONTAINER_POLICY);

    ResolvableType pagingType =
        ResolvableType.forClassWithGenerics(
            Paging.class, ResolvableType.forClass(CustomerDto.class));

    ResponseTypeDescriptor descriptor =
        customIntrospector.extract(envelopeOf(pagingType)).orElseThrow();

    assertEquals(ServiceResponse.class, descriptor.envelopeType());
    assertEquals("data", descriptor.payloadPropertyName());
    assertEquals("PagingCustomerDto", descriptor.dataRefName());
    assertEquals("Paging", descriptor.containerName());
    assertEquals(Paging.class.getName(), descriptor.containerTypeName());
    assertEquals("CustomerDto", descriptor.itemRefName());
    assertTrue(descriptor.isContainer());
  }

  @Test
  @DisplayName("extract -> should unwrap ResponseEntity<ServiceResponse<T>>")
  void extract_shouldUnwrapResponseEntity() {
    ResolvableType type =
        ResolvableType.forClassWithGenerics(
            ResponseEntity.class, envelopeOf(ResolvableType.forClass(CustomerDto.class)));

    ResponseTypeDescriptor descriptor = introspector.extract(type).orElseThrow();

    assertEquals("CustomerDto", descriptor.dataRefName());
  }

  @Test
  @DisplayName("extract -> should unwrap CompletionStage<ServiceResponse<T>>")
  void extract_shouldUnwrapCompletionStage() {
    ResolvableType type =
        ResolvableType.forClassWithGenerics(
            CompletionStage.class, envelopeOf(ResolvableType.forClass(CustomerDto.class)));

    ResponseTypeDescriptor descriptor = introspector.extract(type).orElseThrow();

    assertEquals("CustomerDto", descriptor.dataRefName());
  }

  @Test
  @DisplayName("extract -> should unwrap CompletableFuture<ServiceResponse<T>>")
  void extract_shouldUnwrapCompletableFuture() {
    ResolvableType type =
        ResolvableType.forClassWithGenerics(
            CompletableFuture.class, envelopeOf(ResolvableType.forClass(CustomerDto.class)));

    ResponseTypeDescriptor descriptor = introspector.extract(type).orElseThrow();

    assertEquals("CustomerDto", descriptor.dataRefName());
  }

  @Test
  @DisplayName("extract -> should unwrap Future<ServiceResponse<T>>")
  void extract_shouldUnwrapFuture() {
    ResolvableType type =
        ResolvableType.forClassWithGenerics(
            Future.class, envelopeOf(ResolvableType.forClass(CustomerDto.class)));

    ResponseTypeDescriptor descriptor = introspector.extract(type).orElseThrow();

    assertEquals("CustomerDto", descriptor.dataRefName());
  }

  @Test
  @DisplayName("extract -> should unwrap DeferredResult<ServiceResponse<T>>")
  void extract_shouldUnwrapDeferredResult() {
    ResolvableType type =
        ResolvableType.forClassWithGenerics(
            DeferredResult.class, envelopeOf(ResolvableType.forClass(CustomerDto.class)));

    ResponseTypeDescriptor descriptor = introspector.extract(type).orElseThrow();

    assertEquals("CustomerDto", descriptor.dataRefName());
  }

  @Test
  @DisplayName("extract -> should unwrap WebAsyncTask<ServiceResponse<T>>")
  void extract_shouldUnwrapWebAsyncTask() {
    ResolvableType type =
        ResolvableType.forClassWithGenerics(
            WebAsyncTask.class, envelopeOf(ResolvableType.forClass(CustomerDto.class)));

    ResponseTypeDescriptor descriptor = introspector.extract(type).orElseThrow();

    assertEquals("CustomerDto", descriptor.dataRefName());
  }

  @Test
  @DisplayName("extract -> should unwrap multiple supported response layers")
  void extract_shouldUnwrapMultipleSupportedResponseLayers() {
    ResolvableType type = envelopeOf(ResolvableType.forClass(CustomerDto.class));

    type = ResolvableType.forClassWithGenerics(ResponseEntity.class, type);
    type = ResolvableType.forClassWithGenerics(CompletionStage.class, type);
    type = ResolvableType.forClassWithGenerics(DeferredResult.class, type);
    type = ResolvableType.forClassWithGenerics(WebAsyncTask.class, type);

    ResponseTypeDescriptor descriptor = introspector.extract(type).orElseThrow();

    assertEquals("CustomerDto", descriptor.dataRefName());
  }

  @Test
  @DisplayName("extract -> should stop unwrapping after maximum depth")
  void extract_shouldStopUnwrappingAfterMaximumDepth() {
    ResolvableType type = envelopeOf(ResolvableType.forClass(CustomerDto.class));

    for (int i = 0; i < 9; i++) {
      type = ResolvableType.forClassWithGenerics(ResponseEntity.class, type);
    }

    assertTrue(introspector.extract(type).isEmpty());
  }

  @Test
  @DisplayName("extract -> should return empty for unresolved root type")
  void extract_shouldReturnEmpty_forUnresolvedRootType() {
    assertTrue(introspector.extract(ResolvableType.NONE).isEmpty());
  }

  @Test
  @DisplayName("extract -> should return empty for unsupported root type")
  void extract_shouldReturnEmpty_forUnsupportedRootType() {
    assertTrue(introspector.extract(ResolvableType.forClass(CustomerDto.class)).isEmpty());
  }

  @Test
  @DisplayName("extract -> should stop at unsupported wrapper")
  void extract_shouldStopAtUnsupportedWrapper() {
    ResolvableType type =
        ResolvableType.forClassWithGenerics(
            Wrapper.class, envelopeOf(ResolvableType.forClass(CustomerDto.class)));

    assertTrue(introspector.extract(type).isEmpty());
  }

  @Test
  @DisplayName("extract -> should return empty for unsupported nested generic payload")
  void extract_shouldReturnEmpty_forUnsupportedNestedGenericPayload() {
    ResolvableType nestedType =
        ResolvableType.forClassWithGenerics(
            Wrapper.class, ResolvableType.forClass(CustomerDto.class));

    assertTrue(introspector.extract(envelopeOf(nestedType)).isEmpty());
  }

  @Test
  @DisplayName("extract -> should return empty for unregistered generic container")
  void extract_shouldReturnEmpty_forUnregisteredGenericContainer() {
    ResolvableType pagingType =
        ResolvableType.forClassWithGenerics(
            Paging.class, ResolvableType.forClass(CustomerDto.class));

    assertTrue(introspector.extract(envelopeOf(pagingType)).isEmpty());
  }

  @Test
  @DisplayName("extract -> should return empty when container item type is unresolved")
  void extract_shouldReturnEmpty_whenContainerItemTypeUnresolved() {
    ResolvableType rawPageType = ResolvableType.forClass(Page.class);

    assertTrue(introspector.extract(envelopeOf(rawPageType)).isEmpty());
  }

  @Test
  @DisplayName("extract -> should return empty for nested generic container item")
  void extract_shouldReturnEmpty_forNestedGenericContainerItem() {
    ResolvableType nestedItem =
        ResolvableType.forClassWithGenerics(
            Wrapper.class, ResolvableType.forClass(CustomerDto.class));

    ResolvableType pageType = ResolvableType.forClassWithGenerics(Page.class, nestedItem);

    assertTrue(introspector.extract(envelopeOf(pageType)).isEmpty());
  }

  @Test
  @DisplayName("extract -> should support enum payload annotated with Schema enumAsRef")
  void extract_shouldSupportEnumPayload_whenEnumAsRefEnabled() {
    ResponseTypeDescriptor descriptor =
        introspector
            .extract(envelopeOf(ResolvableType.forClass(SupportedStatus.class)))
            .orElseThrow();

    assertEquals("SupportedStatus", descriptor.dataRefName());
    assertFalse(descriptor.isContainer());
  }

  @Test
  @DisplayName("extract -> should reject enum payload without Schema annotation")
  void extract_shouldRejectEnumPayload_withoutSchemaAnnotation() {
    assertTrue(
        introspector
            .extract(envelopeOf(ResolvableType.forClass(UnsupportedStatus.class)))
            .isEmpty());
  }

  @Test
  @DisplayName("extract -> should reject enum payload when enumAsRef is false")
  void extract_shouldRejectEnumPayload_whenEnumAsRefDisabled() {
    assertTrue(
        introspector.extract(envelopeOf(ResolvableType.forClass(InlineStatus.class))).isEmpty());
  }

  @Test
  @DisplayName("extract -> should support enum container item when enumAsRef is enabled")
  void extract_shouldSupportEnumContainerItem_whenEnumAsRefEnabled() {
    ResolvableType pageType =
        ResolvableType.forClassWithGenerics(
            Page.class, ResolvableType.forClass(SupportedStatus.class));

    ResponseTypeDescriptor descriptor = introspector.extract(envelopeOf(pageType)).orElseThrow();

    assertEquals("PageSupportedStatus", descriptor.dataRefName());
    assertEquals("SupportedStatus", descriptor.itemRefName());
    assertTrue(descriptor.isContainer());
  }

  @Test
  @DisplayName("extract -> should reject enum container item when enumAsRef is disabled")
  void extract_shouldRejectEnumContainerItem_whenEnumAsRefDisabled() {
    ResolvableType pageType =
        ResolvableType.forClassWithGenerics(
            Page.class, ResolvableType.forClass(UnsupportedStatus.class));

    assertTrue(introspector.extract(envelopeOf(pageType)).isEmpty());
  }

  private static ResolvableType envelopeOf(ResolvableType payloadType) {
    return ResolvableType.forClassWithGenerics(ServiceResponse.class, payloadType);
  }

  private static final class CustomerDto {}

  private static final class Wrapper<T> {}

  private static final class Paging<T> {
    private List<T> content;
  }

  @Schema(enumAsRef = true)
  private enum SupportedStatus {
    ACTIVE,
    PASSIVE
  }

  private enum UnsupportedStatus {
    ACTIVE,
    PASSIVE
  }

  @Schema(enumAsRef = false)
  private enum InlineStatus {
    ACTIVE,
    PASSIVE
  }
}

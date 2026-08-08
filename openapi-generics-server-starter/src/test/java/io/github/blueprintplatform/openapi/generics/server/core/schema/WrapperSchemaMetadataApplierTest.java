package io.github.blueprintplatform.openapi.generics.server.core.schema;

import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.ContainerNames.PAGE;
import static org.junit.jupiter.api.Assertions.*;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDescriptor;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerMatchMode;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerShape;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerSource;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.SupportedContainerDescriptor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.constant.VendorExtensions;
import io.github.blueprintplatform.openapi.generics.server.exception.OpenApiProjectionException;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: WrapperSchemaMetadataApplier")
class WrapperSchemaMetadataApplierTest {

  @Test
  @DisplayName("apply -> should add canonical metadata for default envelope simple response")
  void apply_shouldAddCanonicalMetadata_forDefaultEnvelopeSimpleResponse() {
    String wrapperName = "ServiceResponseCustomerDto";
    Schema<?> wrapper = new ObjectSchema();
    Map<String, Schema> schemas = schemas(wrapperName, wrapper);

    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

    Schema<?> result = WrapperSchemaMetadataApplier.apply(schemas, descriptor);

    assertSame(wrapper, result);
    assertNotNull(result.getExtensions());
    assertEquals(Boolean.TRUE, result.getExtensions().get(VendorExtensions.API_WRAPPER));
    assertEquals(
        ServiceResponse.class.getCanonicalName(),
        result.getExtensions().get(VendorExtensions.API_WRAPPER_TYPE));
    assertEquals("CustomerDto", result.getExtensions().get(VendorExtensions.API_WRAPPER_DATATYPE));
  }

  @Test
  @DisplayName("apply -> should add canonical metadata for custom envelope simple response")
  void apply_shouldAddCanonicalMetadata_forCustomEnvelopeSimpleResponse() {
    String wrapperName = "ApiResponseCustomerDto";
    Schema<?> wrapper = new ObjectSchema();
    Map<String, Schema> schemas = schemas(wrapperName, wrapper);

    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.simple(ApiResponse.class, "payload", "CustomerDto");

    Schema<?> result = WrapperSchemaMetadataApplier.apply(schemas, descriptor);

    assertSame(wrapper, result);
    assertNotNull(result.getExtensions());
    assertEquals(Boolean.TRUE, result.getExtensions().get(VendorExtensions.API_WRAPPER));
    assertEquals(
        ApiResponse.class.getCanonicalName(),
        result.getExtensions().get(VendorExtensions.API_WRAPPER_TYPE));
    assertEquals("CustomerDto", result.getExtensions().get(VendorExtensions.API_WRAPPER_DATATYPE));
  }

  @Test
  @DisplayName("apply -> should add canonical metadata for default envelope container response")
  void apply_shouldAddCanonicalMetadata_forDefaultEnvelopeContainerResponse() {
    String wrapperName = "ServiceResponsePageCustomerDto";
    Schema<?> wrapper = new ObjectSchema();
    Map<String, Schema> schemas = schemas(wrapperName, wrapper);

    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.container(
            ServiceResponse.class, "data", pageDescriptor(), "CustomerDto");

    Schema<?> result = WrapperSchemaMetadataApplier.apply(schemas, descriptor);

    assertSame(wrapper, result);
    assertNotNull(result.getExtensions());
    assertEquals(Boolean.TRUE, result.getExtensions().get(VendorExtensions.API_WRAPPER));
    assertEquals(
        ServiceResponse.class.getCanonicalName(),
        result.getExtensions().get(VendorExtensions.API_WRAPPER_TYPE));
    assertEquals(
        "PageCustomerDto", result.getExtensions().get(VendorExtensions.API_WRAPPER_DATATYPE));
  }

  @Test
  @DisplayName("apply -> should add canonical metadata for custom envelope container response")
  void apply_shouldAddCanonicalMetadata_forCustomEnvelopeContainerResponse() {
    String wrapperName = "ApiResponsePageCustomerDto";
    Schema<?> wrapper = new ObjectSchema();
    Map<String, Schema> schemas = schemas(wrapperName, wrapper);

    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.container(
            ApiResponse.class, "payload", pageDescriptor(), "CustomerDto");

    Schema<?> result = WrapperSchemaMetadataApplier.apply(schemas, descriptor);

    assertSame(wrapper, result);
    assertNotNull(result.getExtensions());
    assertEquals(Boolean.TRUE, result.getExtensions().get(VendorExtensions.API_WRAPPER));
    assertEquals(
        ApiResponse.class.getCanonicalName(),
        result.getExtensions().get(VendorExtensions.API_WRAPPER_TYPE));
    assertEquals(
        "PageCustomerDto", result.getExtensions().get(VendorExtensions.API_WRAPPER_DATATYPE));
  }

  @Test
  @DisplayName("apply -> should preserve existing vendor extensions")
  void apply_shouldPreserveExistingVendorExtensions() {
    String wrapperName = "ServiceResponseCustomerDto";
    Schema<?> wrapper = new ObjectSchema();
    wrapper.addExtension("x-existing-extension", "existing-value");

    Map<String, Schema> schemas = schemas(wrapperName, wrapper);

    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

    Schema<?> result = WrapperSchemaMetadataApplier.apply(schemas, descriptor);

    assertEquals("existing-value", result.getExtensions().get("x-existing-extension"));
    assertEquals(Boolean.TRUE, result.getExtensions().get(VendorExtensions.API_WRAPPER));
    assertEquals(
        ServiceResponse.class.getCanonicalName(),
        result.getExtensions().get(VendorExtensions.API_WRAPPER_TYPE));
    assertEquals("CustomerDto", result.getExtensions().get(VendorExtensions.API_WRAPPER_DATATYPE));
  }

  @Test
  @DisplayName("apply -> should fail when projected wrapper schema is missing")
  void apply_shouldFail_whenProjectedWrapperSchemaIsMissing() {
    Map<String, Schema> schemas = new LinkedHashMap<>();

    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

    OpenApiProjectionException exception =
        assertThrows(
            OpenApiProjectionException.class,
            () -> WrapperSchemaMetadataApplier.apply(schemas, descriptor));

    assertTrue(exception.getMessage().contains("Missing wrapper schema"));
    assertTrue(exception.getMessage().contains("ServiceResponseCustomerDto"));
  }

  private Map<String, Schema> schemas(String wrapperName, Schema<?> wrapper) {
    Map<String, Schema> schemas = new LinkedHashMap<>();
    wrapper.setName(wrapperName);
    schemas.put(wrapperName, wrapper);
    return schemas;
  }

  private SupportedContainerDescriptor pageDescriptor() {
    return new SupportedContainerDescriptor(
        Page.class,
        PAGE,
        PAGE,
        ContainerShape.OBJECT_WITH_ITEM_ARRAY,
        "content",
        ContainerSource.BUILT_IN,
        ContainerMatchMode.EXACT);
  }

  static final class ApiResponse<T> {
    T payload;
  }
}

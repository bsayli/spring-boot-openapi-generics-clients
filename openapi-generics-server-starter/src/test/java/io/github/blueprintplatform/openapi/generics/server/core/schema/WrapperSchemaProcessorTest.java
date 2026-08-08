package io.github.blueprintplatform.openapi.generics.server.core.schema;

import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.ContainerNames.PAGE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDescriptor;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerMatchMode;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerShape;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerSource;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.SupportedContainerDescriptor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.constant.VendorExtensions;
import io.github.blueprintplatform.openapi.generics.server.core.schema.enrichment.WrapperSchemaEnricher;
import io.github.blueprintplatform.openapi.generics.server.exception.OpenApiProjectionException;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: WrapperSchemaProcessor")
class WrapperSchemaProcessorTest {

  @Test
  @DisplayName("process -> should enrich simple wrapper and skip container enrichment")
  void process_shouldEnrichSimpleWrapper_andSkipContainerEnrichment() {
    WrapperSchemaEnricher enricher = mock(WrapperSchemaEnricher.class);
    WrapperSchemaProcessor processor = new WrapperSchemaProcessor(enricher);

    OpenAPI openApi = openApiWithWrapper("ServiceResponseCustomerDto");
    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

    processor.process(openApi, descriptor);

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponseCustomerDto");

    assertNotNull(wrapper);
    assertEquals(Boolean.TRUE, wrapper.getExtensions().get(VendorExtensions.API_WRAPPER));
    assertEquals("CustomerDto", wrapper.getExtensions().get(VendorExtensions.API_WRAPPER_DATATYPE));

    verifyNoInteractions(enricher);
  }

  @Test
  @DisplayName("process -> should enrich wrapper but skip container enrichment for custom envelope")
  void process_shouldSkipContainerEnrichment_whenCustomEnvelope() {
    WrapperSchemaEnricher enricher = mock(WrapperSchemaEnricher.class);
    WrapperSchemaProcessor processor = new WrapperSchemaProcessor(enricher);

    OpenAPI openApi = openApiWithWrapper("ApiResponseCustomerDto");
    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.simple(ApiResponse.class, "data", "CustomerDto");

    processor.process(openApi, descriptor);

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ApiResponseCustomerDto");

    assertNotNull(wrapper);
    assertEquals(Boolean.TRUE, wrapper.getExtensions().get(VendorExtensions.API_WRAPPER));
    assertEquals("CustomerDto", wrapper.getExtensions().get(VendorExtensions.API_WRAPPER_DATATYPE));

    verifyNoInteractions(enricher);
  }

  @Test
  @DisplayName(
      "process -> should delegate container enrichment for default envelope container response")
  void process_shouldDelegateContainerEnrichment_forDefaultEnvelopeContainerResponse() {
    WrapperSchemaEnricher enricher = mock(WrapperSchemaEnricher.class);
    WrapperSchemaProcessor processor = new WrapperSchemaProcessor(enricher);

    OpenAPI openApi = openApiWithWrapper("ServiceResponsePageCustomerDto");
    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.container(
            ServiceResponse.class,
            "data",
            new SupportedContainerDescriptor(
                Page.class,
                PAGE,
                PAGE,
                ContainerShape.OBJECT_WITH_ITEM_ARRAY,
                "content",
                ContainerSource.BUILT_IN,
                ContainerMatchMode.EXACT),
            "CustomerDto");

    processor.process(openApi, descriptor);

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ServiceResponsePageCustomerDto");

    assertNotNull(wrapper);
    assertEquals(Boolean.TRUE, wrapper.getExtensions().get(VendorExtensions.API_WRAPPER));
    assertEquals(
        "PageCustomerDto", wrapper.getExtensions().get(VendorExtensions.API_WRAPPER_DATATYPE));

    verify(enricher).enrich(openApi, "ServiceResponsePageCustomerDto", descriptor);
    verifyNoMoreInteractions(enricher);
  }

  @Test
  @DisplayName("process -> should fail when wrapper schema is missing")
  void process_shouldFail_whenWrapperMissing() {
    WrapperSchemaEnricher enricher = mock(WrapperSchemaEnricher.class);
    WrapperSchemaProcessor processor = new WrapperSchemaProcessor(enricher);

    OpenAPI openApi = new OpenAPI().components(new Components().schemas(new LinkedHashMap<>()));
    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

    OpenApiProjectionException ex =
        assertThrows(
            OpenApiProjectionException.class, () -> processor.process(openApi, descriptor));

    assertTrue(ex.getMessage().contains("Missing wrapper schema"));
    verifyNoInteractions(enricher);
  }

  @Test
  @DisplayName(
      "process -> should delegate container enrichment for custom envelope container response")
  void process_shouldDelegateContainerEnrichment_forCustomEnvelopeContainerResponse() {
    WrapperSchemaEnricher enricher = mock(WrapperSchemaEnricher.class);
    WrapperSchemaProcessor processor = new WrapperSchemaProcessor(enricher);

    OpenAPI openApi = openApiWithWrapper("ApiResponsePageCustomerDto");
    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.container(
            ApiResponse.class,
            "data",
            new SupportedContainerDescriptor(
                Page.class,
                PAGE,
                PAGE,
                ContainerShape.OBJECT_WITH_ITEM_ARRAY,
                "content",
                ContainerSource.BUILT_IN,
                ContainerMatchMode.EXACT),
            "CustomerDto");

    processor.process(openApi, descriptor);

    Schema<?> wrapper = openApi.getComponents().getSchemas().get("ApiResponsePageCustomerDto");

    assertNotNull(wrapper);
    assertEquals(Boolean.TRUE, wrapper.getExtensions().get(VendorExtensions.API_WRAPPER));
    assertEquals(
        "PageCustomerDto", wrapper.getExtensions().get(VendorExtensions.API_WRAPPER_DATATYPE));

    verify(enricher).enrich(openApi, "ApiResponsePageCustomerDto", descriptor);
    verifyNoMoreInteractions(enricher);
  }

  private OpenAPI openApiWithWrapper(String wrapperName) {
    Map<String, Schema> schemas = new LinkedHashMap<>();
    Schema<?> wrapper = new Schema<>().name(wrapperName);
    schemas.put(wrapperName, wrapper);

    return new OpenAPI().components(new Components().schemas(schemas));
  }

  static final class ApiResponse<T> {
    T data;
  }
}

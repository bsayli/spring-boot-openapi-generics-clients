package io.github.blueprintplatform.openapi.generics.codegen.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.blueprintplatform.openapi.generics.codegen.contract.CodegenVendorExtensions;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenModel;

@Tag("unit")
@DisplayName("Unit Test: EnvelopeMetadataResolver")
class EnvelopeMetadataResolverTest {

  private final EnvelopeMetadataResolver resolver = new EnvelopeMetadataResolver();

  @Test
  @DisplayName("apply -> should derive built-in envelope metadata from wrapper type")
  void apply_shouldDeriveBuiltInEnvelopeMetadata_fromWrapperType() {
    CodegenModel model =
        wrapperModel(
            "ServiceResponseCustomerDto",
            "io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse");

    resolver.apply(model);

    Map<String, Object> vendorExtensions = model.getVendorExtensions();

    assertEquals(
        "io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse",
        vendorExtensions.get(CodegenVendorExtensions.ENVELOPE_IMPORT));
    assertEquals("ServiceResponse", vendorExtensions.get(CodegenVendorExtensions.ENVELOPE_TYPE));
  }

  @Test
  @DisplayName("apply -> should derive custom envelope metadata from wrapper type")
  void apply_shouldDeriveCustomEnvelopeMetadata_fromWrapperType() {
    CodegenModel model = wrapperModel("ApiResponseCustomerDto", "io.example.contract.ApiResponse");

    resolver.apply(model);

    Map<String, Object> vendorExtensions = model.getVendorExtensions();

    assertEquals(
        "io.example.contract.ApiResponse",
        vendorExtensions.get(CodegenVendorExtensions.ENVELOPE_IMPORT));
    assertEquals("ApiResponse", vendorExtensions.get(CodegenVendorExtensions.ENVELOPE_TYPE));
  }

  @Test
  @DisplayName("apply -> should resolve envelope metadata independently for each wrapper model")
  void apply_shouldResolveEnvelopeMetadataIndependently_forEachWrapperModel() {
    CodegenModel serviceResponse =
        wrapperModel(
            "ServiceResponseCustomerDto",
            "io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse");

    CodegenModel apiResponse =
        wrapperModel("ApiResponseOrderDto", "io.example.contract.ApiResponse");

    resolver.apply(serviceResponse);
    resolver.apply(apiResponse);

    assertEquals(
        "io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse",
        serviceResponse.getVendorExtensions().get(CodegenVendorExtensions.ENVELOPE_IMPORT));
    assertEquals(
        "ServiceResponse",
        serviceResponse.getVendorExtensions().get(CodegenVendorExtensions.ENVELOPE_TYPE));

    assertEquals(
        "io.example.contract.ApiResponse",
        apiResponse.getVendorExtensions().get(CodegenVendorExtensions.ENVELOPE_IMPORT));
    assertEquals(
        "ApiResponse",
        apiResponse.getVendorExtensions().get(CodegenVendorExtensions.ENVELOPE_TYPE));
  }

  @Test
  @DisplayName("apply -> should not modify non-wrapper models")
  void apply_shouldSkip_whenNotWrapper() {
    CodegenModel model = new CodegenModel();
    model.name = "CustomerDto";
    model.vendorExtensions = new HashMap<>();
    model.vendorExtensions.put(
        CodegenVendorExtensions.API_WRAPPER_TYPE, "io.example.contract.ApiResponse");

    resolver.apply(model);

    assertFalse(model.vendorExtensions.containsKey(CodegenVendorExtensions.ENVELOPE_IMPORT));
    assertFalse(model.vendorExtensions.containsKey(CodegenVendorExtensions.ENVELOPE_TYPE));
  }

  @Test
  @DisplayName("apply -> should handle null vendor extensions safely")
  void apply_shouldSkip_whenVendorExtensionsNull() {
    CodegenModel model = new CodegenModel();
    model.name = "CustomerDto";
    model.vendorExtensions = null;

    resolver.apply(model);

    assertNull(model.vendorExtensions);
  }

  private CodegenModel wrapperModel(String name, String wrapperType) {
    CodegenModel model = new CodegenModel();
    model.name = name;
    model.vendorExtensions = new HashMap<>();
    model.vendorExtensions.put(CodegenVendorExtensions.API_WRAPPER, Boolean.TRUE);
    model.vendorExtensions.put(CodegenVendorExtensions.API_WRAPPER_TYPE, wrapperType);
    return model;
  }
}

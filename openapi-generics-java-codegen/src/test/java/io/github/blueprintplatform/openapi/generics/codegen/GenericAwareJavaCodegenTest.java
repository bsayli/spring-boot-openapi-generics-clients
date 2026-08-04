package io.github.blueprintplatform.openapi.generics.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.codegen.contract.CodegenVendorExtensions;
import io.swagger.v3.oas.models.media.Schema;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;

@Tag("unit")
@DisplayName("Smoke Test: GenericAwareJavaCodegen")
class GenericAwareJavaCodegenTest {

  private static final String SERVICE_RESPONSE_TYPE =
      "io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse";

  @Test
  @DisplayName("processOpts + fromModel + postProcessModels -> should filter external model")
  void shouldFilterExternalModel_andKeepOthers() {
    GenericAwareJavaCodegen codegen = new GenericAwareJavaCodegen();

    codegen
        .additionalProperties()
        .put("openapi-generics.response-contract.CustomerDto", "io.example.CustomerDto");

    codegen.processOpts();

    Schema<?> externalSchema = new Schema<>();
    Schema<?> normalSchema = new Schema<>();

    CodegenModel externalModel = codegen.fromModel("CustomerDto", externalSchema);
    CodegenModel normalModel = codegen.fromModel("OrderDto", normalSchema);

    ModelsMap modelsMap = modelsMap(externalModel, normalModel);

    ModelsMap result = codegen.postProcessModels(modelsMap);

    assertNotNull(result);
    assertNotNull(result.getModels());
    assertEquals(1, result.getModels().size());
    assertEquals("OrderDto", result.getModels().get(0).getModel().name);
  }

  @Test
  @DisplayName("fromModel -> should clean imports of ignored models")
  void shouldCleanImports_ofIgnoredModels() {
    GenericAwareJavaCodegen codegen = new GenericAwareJavaCodegen();

    codegen
        .additionalProperties()
        .put("openapi-generics.response-contract.CustomerDto", "io.example.CustomerDto");

    codegen.processOpts();

    Schema<?> schema = new Schema<>();

    CodegenModel model = codegen.fromModel("CustomerDto", schema);
    model.imports = new HashSet<>(List.of("CustomerDto", "OtherDto"));

    CodegenModel processed = codegen.fromModel("CustomerDto", schema);

    assertNotNull(processed);

    if (processed.imports != null) {
      assertFalse(processed.imports.contains("CustomerDto"));
    }
  }

  @Test
  @DisplayName("postProcessModels -> should inject external import into wrapper model")
  void shouldInjectExternalImport_intoWrapperModel() {
    GenericAwareJavaCodegen codegen = new GenericAwareJavaCodegen();

    codegen
        .additionalProperties()
        .put("openapi-generics.response-contract.CustomerDto", "io.example.CustomerDto");

    codegen.processOpts();

    CodegenModel wrapperModel = wrapperModel("ServiceResponseCustomerDto", SERVICE_RESPONSE_TYPE);
    wrapperModel.vendorExtensions.put(CodegenVendorExtensions.DATA_ITEM, "CustomerDto");

    ModelsMap result = codegen.postProcessModels(modelsMap(wrapperModel));

    CodegenModel processed = result.getModels().get(0).getModel();

    assertEquals(
        "io.example.CustomerDto",
        processed.vendorExtensions.get(CodegenVendorExtensions.EXTRA_IMPORTS));
    assertEquals(
        SERVICE_RESPONSE_TYPE,
        processed.vendorExtensions.get(CodegenVendorExtensions.ENVELOPE_IMPORT));
    assertEquals(
        "ServiceResponse", processed.vendorExtensions.get(CodegenVendorExtensions.ENVELOPE_TYPE));
  }

  @Test
  @DisplayName("postProcessModels -> should derive envelope metadata from wrapper schema metadata")
  void shouldDeriveEnvelopeMetadata_fromWrapperSchemaMetadata() {
    GenericAwareJavaCodegen codegen = new GenericAwareJavaCodegen();
    codegen.processOpts();

    CodegenModel wrapperModel =
        wrapperModel("ApiResponseCustomerDto", "io.example.contract.ApiResponse");

    ModelsMap result = codegen.postProcessModels(modelsMap(wrapperModel));

    CodegenModel processed = result.getModels().get(0).getModel();

    assertEquals(
        "io.example.contract.ApiResponse",
        processed.vendorExtensions.get(CodegenVendorExtensions.ENVELOPE_IMPORT));
    assertEquals(
        "ApiResponse", processed.vendorExtensions.get(CodegenVendorExtensions.ENVELOPE_TYPE));
  }

  @Test
  @DisplayName("postProcessModels -> should support multiple envelope identities")
  void shouldSupportMultipleEnvelopeIdentities() {
    GenericAwareJavaCodegen codegen = new GenericAwareJavaCodegen();
    codegen.processOpts();

    CodegenModel serviceResponse =
        wrapperModel("ServiceResponseCustomerDto", SERVICE_RESPONSE_TYPE);

    CodegenModel apiResponse =
        wrapperModel("ApiResponseOrderDto", "io.example.contract.ApiResponse");

    ModelsMap result = codegen.postProcessModels(modelsMap(serviceResponse, apiResponse));

    CodegenModel processedServiceResponse = result.getModels().get(0).getModel();
    CodegenModel processedApiResponse = result.getModels().get(1).getModel();

    assertEquals(
        SERVICE_RESPONSE_TYPE,
        processedServiceResponse.vendorExtensions.get(CodegenVendorExtensions.ENVELOPE_IMPORT));
    assertEquals(
        "ServiceResponse",
        processedServiceResponse.vendorExtensions.get(CodegenVendorExtensions.ENVELOPE_TYPE));

    assertEquals(
        "io.example.contract.ApiResponse",
        processedApiResponse.vendorExtensions.get(CodegenVendorExtensions.ENVELOPE_IMPORT));
    assertEquals(
        "ApiResponse",
        processedApiResponse.vendorExtensions.get(CodegenVendorExtensions.ENVELOPE_TYPE));
  }

  @Test
  @DisplayName("postProcessAllModels -> should remove ignored models from global model graph")
  void shouldRemoveIgnoredModels_fromGlobalModelGraph() {
    GenericAwareJavaCodegen codegen = new GenericAwareJavaCodegen();

    codegen
        .additionalProperties()
        .put("openapi-generics.response-contract.CustomerDto", "io.example.CustomerDto");

    codegen.processOpts();

    codegen.fromModel("CustomerDto", new Schema<>());
    codegen.fromModel("OrderDto", new Schema<>());

    ModelsMap externalModels = new ModelsMap();
    externalModels.setModels(new ArrayList<>());

    ModelsMap normalModels = new ModelsMap();
    normalModels.setModels(new ArrayList<>());

    Map<String, ModelsMap> allModels = new LinkedHashMap<>();
    allModels.put("CustomerDto", externalModels);
    allModels.put("OrderDto", normalModels);

    Map<String, ModelsMap> result = codegen.postProcessAllModels(allModels);

    assertFalse(result.containsKey("CustomerDto"));
    assertTrue(result.containsKey("OrderDto"));
  }

  @Test
  @DisplayName("getName -> should return custom generator name")
  void getName_shouldReturnCustomGeneratorName() {
    GenericAwareJavaCodegen codegen = new GenericAwareJavaCodegen();

    assertEquals("java-generics-contract", codegen.getName());
  }

  @Test
  @DisplayName(
      "postProcessModels -> should keep non-wrapper model unchanged when no contract metadata"
          + " applies")
  void shouldKeepNonWrapperModelUnchanged() {
    GenericAwareJavaCodegen codegen = new GenericAwareJavaCodegen();
    codegen.processOpts();

    CodegenModel model = new CodegenModel();
    model.name = "OrderDto";

    ModelsMap result = codegen.postProcessModels(modelsMap(model));

    CodegenModel processed = result.getModels().get(0).getModel();

    assertNotNull(processed);
    assertNull(processed.vendorExtensions.get(CodegenVendorExtensions.EXTRA_IMPORTS));
    assertNull(processed.vendorExtensions.get(CodegenVendorExtensions.ENVELOPE_IMPORT));
    assertNull(processed.vendorExtensions.get(CodegenVendorExtensions.ENVELOPE_TYPE));
  }

  private CodegenModel wrapperModel(String name, String wrapperType) {
    CodegenModel model = new CodegenModel();
    model.name = name;
    model.vendorExtensions.put(CodegenVendorExtensions.API_WRAPPER, Boolean.TRUE);
    model.vendorExtensions.put(CodegenVendorExtensions.API_WRAPPER_TYPE, wrapperType);
    return model;
  }

  private ModelsMap modelsMap(CodegenModel... models) {
    List<ModelMap> modelMaps = new ArrayList<>();

    for (CodegenModel model : models) {
      ModelMap modelMap = new ModelMap();
      modelMap.setModel(model);
      modelMaps.add(modelMap);
    }

    ModelsMap modelsMap = new ModelsMap();
    modelsMap.setModels(modelMaps);
    return modelsMap;
  }
}

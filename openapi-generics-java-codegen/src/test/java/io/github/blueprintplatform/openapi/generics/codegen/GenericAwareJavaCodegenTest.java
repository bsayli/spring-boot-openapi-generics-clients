package io.github.blueprintplatform.openapi.generics.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.codegen.contract.CodegenVendorExtensions;
import io.github.blueprintplatform.openapi.generics.codegen.exception.OpenApiGenericsContractException;
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
@DisplayName("Unit Test: GenericAwareJavaCodegen")
class GenericAwareJavaCodegenTest {

  private static final String SERVICE_RESPONSE_TYPE =
      "io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse";

  private static final String CUSTOMER_MAPPING_KEY =
      "openapi-generics.response-contract.CustomerDto";

  private static final String CUSTOMER_TYPE = "io.example.CustomerDto";

  @Test
  @DisplayName("processOpts + fromModel + postProcessModels -> should filter external model")
  void shouldFilterExternalModel_andKeepOthers() {
    GenericAwareJavaCodegen codegen = codegenWithExternalCustomer();

    Schema<?> externalSchema = new Schema<>();
    Schema<?> normalSchema = new Schema<>();

    CodegenModel externalModel = codegen.fromModel("CustomerDto", externalSchema);
    CodegenModel normalModel = codegen.fromModel("OrderDto", normalSchema);

    ModelsMap result = codegen.postProcessModels(modelsMap(externalModel, normalModel));

    assertNotNull(result);
    assertNotNull(result.getModels());
    assertEquals(1, result.getModels().size());
    assertEquals("OrderDto", result.getModels().get(0).getModel().name);
  }

  @Test
  @DisplayName("postProcessModels -> should inject external import into wrapper model")
  void shouldInjectExternalImport_intoWrapperModel() {
    GenericAwareJavaCodegen codegen = codegenWithExternalCustomer();

    CodegenModel wrapperModel = wrapperModel("ServiceResponseCustomerDto", SERVICE_RESPONSE_TYPE);

    ModelsMap result = codegen.postProcessModels(modelsMap(wrapperModel));

    CodegenModel processed = result.getModels().get(0).getModel();

    assertEquals(
        CUSTOMER_TYPE, processed.vendorExtensions.get(CodegenVendorExtensions.EXTRA_IMPORTS));

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
  @DisplayName(
      "postProcessModels -> should keep non-wrapper model unchanged when no contract metadata"
          + " applies")
  void shouldKeepNonWrapperModelUnchanged() {
    GenericAwareJavaCodegen codegen = new GenericAwareJavaCodegen();
    codegen.processOpts();

    CodegenModel model = model("OrderDto");

    ModelsMap result = codegen.postProcessModels(modelsMap(model));

    CodegenModel processed = result.getModels().get(0).getModel();

    assertNotNull(processed);

    assertNull(processed.vendorExtensions.get(CodegenVendorExtensions.EXTRA_IMPORTS));

    assertNull(processed.vendorExtensions.get(CodegenVendorExtensions.ENVELOPE_IMPORT));

    assertNull(processed.vendorExtensions.get(CodegenVendorExtensions.ENVELOPE_TYPE));
  }

  @Test
  @DisplayName("postProcessAllModels -> should remove ignored model from global model graph")
  void shouldRemoveIgnoredModels_fromGlobalModelGraph() {
    GenericAwareJavaCodegen codegen = codegenWithExternalCustomer();

    // Registers CustomerDto as ignored in the decider.
    codegen.fromModel("CustomerDto", new Schema<>());
    codegen.fromModel("OrderDto", new Schema<>());

    Map<String, ModelsMap> allModels = new LinkedHashMap<>();
    allModels.put("CustomerDto", modelsMap(model("CustomerDto")));
    allModels.put("OrderDto", modelsMap(model("OrderDto")));

    Map<String, ModelsMap> result = codegen.postProcessAllModels(allModels);

    assertFalse(result.containsKey("CustomerDto"));
    assertTrue(result.containsKey("OrderDto"));
  }

  @Test
  @DisplayName("postProcessAllModels -> should keep all models when none are ignored")
  void shouldKeepAllModels_whenNoneAreIgnored() {
    GenericAwareJavaCodegen codegen = new GenericAwareJavaCodegen();
    codegen.processOpts();

    Map<String, ModelsMap> allModels = new LinkedHashMap<>();
    allModels.put("CustomerDto", modelsMap(model("CustomerDto")));
    allModels.put("OrderDto", modelsMap(model("OrderDto")));

    Map<String, ModelsMap> result = codegen.postProcessAllModels(allModels);

    assertTrue(result.containsKey("CustomerDto"));
    assertTrue(result.containsKey("OrderDto"));
    assertEquals(2, result.size());
  }

  @Test
  @DisplayName("postProcessAllModels -> should remove ignored simple imports from model")
  void shouldRemoveIgnoredSimpleImports_fromModel() {
    GenericAwareJavaCodegen codegen = codegenWithExternalCustomer();

    codegen.fromModel("CustomerDto", new Schema<>());

    CodegenModel orderModel = model("OrderDto");
    orderModel.imports = new HashSet<>(List.of("CustomerDto", "AddressDto"));

    Map<String, ModelsMap> allModels = new LinkedHashMap<>();
    allModels.put("OrderDto", modelsMap(orderModel));

    Map<String, ModelsMap> result = codegen.postProcessAllModels(allModels);

    CodegenModel processed = result.get("OrderDto").getModels().get(0).getModel();

    assertFalse(processed.imports.contains("CustomerDto"));
    assertTrue(processed.imports.contains("AddressDto"));
  }

  @Test
  @DisplayName("postProcessAllModels -> should remove ignored qualified imports from model")
  void shouldRemoveIgnoredQualifiedImports_fromModel() {
    GenericAwareJavaCodegen codegen = codegenWithExternalCustomer();

    codegen.fromModel("CustomerDto", new Schema<>());

    CodegenModel orderModel = model("OrderDto");
    orderModel.imports = new HashSet<>(List.of("io.example.CustomerDto", "io.example.AddressDto"));

    Map<String, ModelsMap> allModels = new LinkedHashMap<>();
    allModels.put("OrderDto", modelsMap(orderModel));

    Map<String, ModelsMap> result = codegen.postProcessAllModels(allModels);

    CodegenModel processed = result.get("OrderDto").getModels().get(0).getModel();

    assertFalse(processed.imports.contains("io.example.CustomerDto"));
    assertTrue(processed.imports.contains("io.example.AddressDto"));
  }

  @Test
  @DisplayName("postProcessAllModels -> should clean imports exposed on ModelsMap")
  void shouldCleanIgnoredImports_fromModelsMapImports() {
    GenericAwareJavaCodegen codegen = codegenWithExternalCustomer();

    codegen.fromModel("CustomerDto", new Schema<>());

    ModelsMap orderModels = modelsMap(model("OrderDto"));

    List<Object> imports = new ArrayList<>();
    imports.add(importEntry("io.example.CustomerDto"));
    imports.add(importEntry("io.example.AddressDto"));
    imports.add("CustomerDto");
    imports.add("AddressDto");

    orderModels.put("imports", imports);

    Map<String, ModelsMap> allModels = new LinkedHashMap<>();
    allModels.put("OrderDto", orderModels);

    Map<String, ModelsMap> result = codegen.postProcessAllModels(allModels);

    Object processedImports = result.get("OrderDto").get("imports");

    assertTrue(processedImports instanceof List<?>);

    List<?> list = (List<?>) processedImports;

    assertFalse(list.contains("CustomerDto"));
    assertTrue(list.contains("AddressDto"));

    assertFalse(
        list.stream()
            .filter(Map.class::isInstance)
            .map(Map.class::cast)
            .anyMatch(entry -> "io.example.CustomerDto".equals(entry.get("import"))));

    assertTrue(
        list.stream()
            .filter(Map.class::isInstance)
            .map(Map.class::cast)
            .anyMatch(entry -> "io.example.AddressDto".equals(entry.get("import"))));
  }

  @Test
  @DisplayName("postProcessAllModels -> should preserve unrelated import entry shapes")
  void shouldPreserveUnrelatedImportEntryShapes() {
    GenericAwareJavaCodegen codegen = codegenWithExternalCustomer();

    codegen.fromModel("CustomerDto", new Schema<>());

    ModelsMap orderModels = modelsMap(model("OrderDto"));

    Map<String, Object> nonStringImport = new LinkedHashMap<>();
    nonStringImport.put("import", 42);

    Map<String, Object> unrelatedMap = new LinkedHashMap<>();
    unrelatedMap.put("other", "CustomerDto");

    Object marker = new Object();

    List<Object> imports = new ArrayList<>();
    imports.add(nonStringImport);
    imports.add(unrelatedMap);
    imports.add(marker);
    imports.add("AddressDto");

    orderModels.put("imports", imports);

    Map<String, ModelsMap> allModels = new LinkedHashMap<>();
    allModels.put("OrderDto", orderModels);

    Map<String, ModelsMap> result = codegen.postProcessAllModels(allModels);

    List<?> processed = (List<?>) result.get("OrderDto").get("imports");

    assertTrue(processed.contains(nonStringImport));
    assertTrue(processed.contains(unrelatedMap));
    assertTrue(processed.contains(marker));
    assertTrue(processed.contains("AddressDto"));
  }

  @Test
  @DisplayName("postProcessAllModels -> should handle model without imports")
  void shouldHandleModelWithoutImports() {
    GenericAwareJavaCodegen codegen = codegenWithExternalCustomer();

    codegen.fromModel("CustomerDto", new Schema<>());

    CodegenModel orderModel = model("OrderDto");
    orderModel.imports = null;

    Map<String, ModelsMap> allModels = new LinkedHashMap<>();
    allModels.put("OrderDto", modelsMap(orderModel));

    Map<String, ModelsMap> result = codegen.postProcessAllModels(allModels);

    assertNotNull(result);
    assertTrue(result.containsKey("OrderDto"));
    assertNull(result.get("OrderDto").getModels().get(0).getModel().imports);
  }

  @Test
  @DisplayName("postProcessAllModels -> should handle model with empty imports")
  void shouldHandleModelWithEmptyImports() {
    GenericAwareJavaCodegen codegen = codegenWithExternalCustomer();

    codegen.fromModel("CustomerDto", new Schema<>());

    CodegenModel orderModel = model("OrderDto");
    orderModel.imports = new HashSet<>();

    Map<String, ModelsMap> allModels = new LinkedHashMap<>();
    allModels.put("OrderDto", modelsMap(orderModel));

    Map<String, ModelsMap> result = codegen.postProcessAllModels(allModels);

    assertNotNull(result);
    assertTrue(result.containsKey("OrderDto"));
    assertTrue(result.get("OrderDto").getModels().get(0).getModel().imports.isEmpty());
  }

  @Test
  @DisplayName("getName -> should return custom generator name")
  void getName_shouldReturnCustomGeneratorName() {
    GenericAwareJavaCodegen codegen = new GenericAwareJavaCodegen();

    assertEquals("java-generics-contract", codegen.getName());
  }

  @Test
  @DisplayName("postProcessModels -> should return same processed model structure")
  void postProcessModels_shouldPreserveProcessedStructure() {
    GenericAwareJavaCodegen codegen = new GenericAwareJavaCodegen();
    codegen.processOpts();

    CodegenModel model = model("OrderDto");
    ModelsMap input = modelsMap(model);

    ModelsMap result = codegen.postProcessModels(input);

    assertNotNull(result);
    assertEquals(1, result.getModels().size());
    assertSame(model, result.getModels().get(0).getModel());
  }

  private GenericAwareJavaCodegen codegenWithExternalCustomer() {
    GenericAwareJavaCodegen codegen = new GenericAwareJavaCodegen();

    codegen.additionalProperties().put(CUSTOMER_MAPPING_KEY, CUSTOMER_TYPE);

    codegen.processOpts();
    return codegen;
  }

  private CodegenModel wrapperModel(String name, String wrapperType) {
    CodegenModel model = model(name);

    model.vendorExtensions.put(CodegenVendorExtensions.API_WRAPPER, Boolean.TRUE);

    model.vendorExtensions.put(CodegenVendorExtensions.API_WRAPPER_TYPE, wrapperType);

    model.vendorExtensions.put(CodegenVendorExtensions.API_WRAPPER_DATATYPE, "CustomerDto");

    return model;
  }

  private CodegenModel model(String name) {
    CodegenModel model = new CodegenModel();
    model.name = name;
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

  private Map<String, Object> importEntry(String imported) {
    Map<String, Object> entry = new LinkedHashMap<>();
    entry.put("import", imported);
    return entry;
  }
}

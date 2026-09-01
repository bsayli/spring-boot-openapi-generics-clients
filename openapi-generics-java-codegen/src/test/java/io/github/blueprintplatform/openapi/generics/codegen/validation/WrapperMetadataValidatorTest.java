package io.github.blueprintplatform.openapi.generics.codegen.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.github.blueprintplatform.openapi.generics.codegen.contract.CodegenVendorExtensions;
import io.github.blueprintplatform.openapi.generics.codegen.exception.OpenApiGenericsContractException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openapitools.codegen.CodegenModel;

@Tag("unit")
@DisplayName("Unit Test: WrapperMetadataValidator")
class WrapperMetadataValidatorTest {

  private static final String WRAPPER_TYPE = "io.example.contract.ApiResponse";
  private static final String CONTAINER_TYPE = "io.example.contract.Window";

  private final WrapperMetadataValidator validator = new WrapperMetadataValidator();

  @Test
  @DisplayName("validate -> should skip ordinary model when wrapper metadata is absent")
  void validate_shouldSkip_whenWrapperMetadataAbsent() {
    assertDoesNotThrow(() -> validator.validate(model("CustomerDto")));
  }

  @Test
  @DisplayName("validate -> should skip model when wrapper marker is false and no details exist")
  void validate_shouldSkip_whenWrapperMarkerFalseWithoutDetails() {
    CodegenModel model = model("CustomerDto");
    model.vendorExtensions.put(CodegenVendorExtensions.API_WRAPPER, Boolean.FALSE);

    assertDoesNotThrow(() -> validator.validate(model));
  }

  @Test
  @DisplayName("validate -> should fail when wrapper marker is missing but wrapper details exist")
  void validate_shouldFail_whenWrapperMarkerMissingButWrapperDetailsExist() {
    CodegenModel model = model("ApiResponseCustomerDto");
    model.vendorExtensions.put(CodegenVendorExtensions.API_WRAPPER_TYPE, WRAPPER_TYPE);
    model.vendorExtensions.put(
            CodegenVendorExtensions.API_WRAPPER_DATATYPE, "CustomerDto");

    OpenApiGenericsContractException exception =
            assertThrows(
                    OpenApiGenericsContractException.class,
                    () -> validator.validate(model));

    assertEquals(
            "Invalid OpenAPI Generics metadata for model 'ApiResponseCustomerDto': required vendor "
                    + "extension 'x-api-wrapper' is missing. Wrapper metadata was detected through: "
                    + "'x-api-wrapper-type', 'x-api-wrapper-datatype'.",
            exception.getMessage());
  }

  @Test
  @DisplayName("validate -> should fail when wrapper marker is missing but container details exist")
  void validate_shouldFail_whenWrapperMarkerMissingButContainerDetailsExist() {
    CodegenModel model = model("ApiResponseWindowCustomerDto");
    model.vendorExtensions.put(CodegenVendorExtensions.DATA_CONTAINER, "Window");
    model.vendorExtensions.put(
            CodegenVendorExtensions.DATA_CONTAINER_TYPE, CONTAINER_TYPE);
    model.vendorExtensions.put(
            CodegenVendorExtensions.DATA_ITEM, "CustomerDto");

    OpenApiGenericsContractException exception =
            assertThrows(
                    OpenApiGenericsContractException.class,
                    () -> validator.validate(model));

    assertEquals(
            "Invalid OpenAPI Generics metadata for model 'ApiResponseWindowCustomerDto': required "
                    + "vendor extension 'x-api-wrapper' is missing. Wrapper metadata was detected through: "
                    + "'x-data-container', 'x-data-container-type', 'x-data-item'.",
            exception.getMessage());
  }

  @Test
  @DisplayName("validate -> should fail when wrapper marker is false but wrapper details exist")
  void validate_shouldFail_whenWrapperMarkerFalseButWrapperDetailsExist() {
    CodegenModel model = validDirectWrapper();
    model.vendorExtensions.put(
            CodegenVendorExtensions.API_WRAPPER, Boolean.FALSE);

    OpenApiGenericsContractException exception =
            assertThrows(
                    OpenApiGenericsContractException.class,
                    () -> validator.validate(model));

    assertEquals(
            "Invalid OpenAPI Generics metadata for model 'ApiResponseCustomerDto': vendor extension "
                    + "'x-api-wrapper' is false, but wrapper metadata is also present: "
                    + "'x-api-wrapper-type', 'x-api-wrapper-datatype'. Remove the wrapper metadata or set "
                    + "'x-api-wrapper' to true.",
            exception.getMessage());
  }

  @Test
  @DisplayName("validate -> should fail when wrapper marker is false but container details exist")
  void validate_shouldFail_whenWrapperMarkerFalseButContainerDetailsExist() {
    CodegenModel model = validContainerWrapper();
    model.vendorExtensions.put(
            CodegenVendorExtensions.API_WRAPPER, Boolean.FALSE);

    OpenApiGenericsContractException exception =
            assertThrows(
                    OpenApiGenericsContractException.class,
                    () -> validator.validate(model));

    assertEquals(
            "Invalid OpenAPI Generics metadata for model 'ApiResponseWindowCustomerDto': vendor "
                    + "extension 'x-api-wrapper' is false, but wrapper metadata is also present: "
                    + "'x-api-wrapper-type', 'x-api-wrapper-datatype', 'x-data-container', "
                    + "'x-data-container-type', 'x-data-item'. Remove the wrapper metadata or set "
                    + "'x-api-wrapper' to true.",
            exception.getMessage());
  }

  @Test
  @DisplayName("validate -> should fail when wrapper marker has wrong type")
  void validate_shouldFail_whenWrapperMarkerHasWrongType() {
    CodegenModel model = model("ApiResponseCustomerDto");
    model.vendorExtensions.put(CodegenVendorExtensions.API_WRAPPER, "true");

    OpenApiGenericsContractException exception =
            assertThrows(
                    OpenApiGenericsContractException.class,
                    () -> validator.validate(model));

    assertEquals(
            "Invalid OpenAPI Generics metadata for model 'ApiResponseCustomerDto': vendor extension "
                    + "'x-api-wrapper' must be a Boolean but was java.lang.String.",
            exception.getMessage());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidRequiredMetadata")
  @DisplayName("validate -> should fail for invalid required wrapper metadata")
  void validate_shouldFail_forInvalidRequiredWrapperMetadata(
          String description,
          String key,
          Object value,
          String expectedMessage) {

    CodegenModel model = validDirectWrapper();

    if (value == MissingValue.INSTANCE) {
      model.vendorExtensions.remove(key);
    } else {
      model.vendorExtensions.put(key, value);
    }

    OpenApiGenericsContractException exception =
            assertThrows(
                    OpenApiGenericsContractException.class,
                    () -> validator.validate(model));

    assertEquals(expectedMessage, exception.getMessage());
  }

  @Test
  @DisplayName("validate -> should accept valid direct wrapper metadata")
  void validate_shouldAccept_validDirectWrapperMetadata() {
    assertDoesNotThrow(() -> validator.validate(validDirectWrapper()));
  }

  @ParameterizedTest(name = "missing {0}")
  @MethodSource("containerMetadataKeys")
  @DisplayName("validate -> should identify the single missing container metadata extension")
  void validate_shouldIdentifySingleMissingContainerMetadataExtension(
          String missingKey) {

    CodegenModel model = validContainerWrapper();
    model.vendorExtensions.remove(missingKey);

    OpenApiGenericsContractException exception =
            assertThrows(
                    OpenApiGenericsContractException.class,
                    () -> validator.validate(model));

    assertEquals(
            "Invalid OpenAPI Generics metadata for wrapper model "
                    + "'ApiResponseWindowCustomerDto': required vendor extension is missing: '"
                    + missingKey
                    + "'. Container metadata must declare these vendor extensions together: "
                    + "'x-data-container', 'x-data-container-type', 'x-data-item'.",
            exception.getMessage());
  }

  @Test
  @DisplayName("validate -> should identify all missing container metadata extensions")
  void validate_shouldIdentifyMultipleMissingContainerMetadataExtensions() {
    CodegenModel model = validContainerWrapper();
    model.vendorExtensions.remove(
            CodegenVendorExtensions.DATA_CONTAINER);
    model.vendorExtensions.remove(
            CodegenVendorExtensions.DATA_CONTAINER_TYPE);

    OpenApiGenericsContractException exception =
            assertThrows(
                    OpenApiGenericsContractException.class,
                    () -> validator.validate(model));

    assertEquals(
            "Invalid OpenAPI Generics metadata for wrapper model "
                    + "'ApiResponseWindowCustomerDto': required vendor extensions are missing: "
                    + "'x-data-container', 'x-data-container-type'. Container metadata must declare these "
                    + "vendor extensions together: 'x-data-container', 'x-data-container-type', "
                    + "'x-data-item'.",
            exception.getMessage());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidContainerMetadata")
  @DisplayName("validate -> should fail for invalid container metadata values")
  void validate_shouldFail_forInvalidContainerMetadata(
          String description,
          String key,
          Object value,
          String expectedMessage) {

    CodegenModel model = validContainerWrapper();
    model.vendorExtensions.put(key, value);

    OpenApiGenericsContractException exception =
            assertThrows(
                    OpenApiGenericsContractException.class,
                    () -> validator.validate(model));

    assertEquals(expectedMessage, exception.getMessage());
  }

  @Test
  @DisplayName("validate -> should accept valid container wrapper metadata")
  void validate_shouldAccept_validContainerWrapperMetadata() {
    assertDoesNotThrow(() -> validator.validate(validContainerWrapper()));
  }

  private static Stream<Arguments> invalidRequiredMetadata() {
    return Stream.of(
            arguments(
                    "missing wrapper type",
                    CodegenVendorExtensions.API_WRAPPER_TYPE,
                    MissingValue.INSTANCE,
                    "Invalid OpenAPI Generics metadata for wrapper model 'ApiResponseCustomerDto': "
                            + "required vendor extension 'x-api-wrapper-type' is missing."),
            arguments(
                    "blank wrapper type",
                    CodegenVendorExtensions.API_WRAPPER_TYPE,
                    " ",
                    "Invalid OpenAPI Generics metadata for wrapper model 'ApiResponseCustomerDto': "
                            + "vendor extension 'x-api-wrapper-type' must not be blank."),
            arguments(
                    "wrong wrapper type value",
                    CodegenVendorExtensions.API_WRAPPER_TYPE,
                    42,
                    "Invalid OpenAPI Generics metadata for model 'ApiResponseCustomerDto': vendor "
                            + "extension 'x-api-wrapper-type' must be a String but was java.lang.Integer."),
            arguments(
                    "unqualified wrapper type",
                    CodegenVendorExtensions.API_WRAPPER_TYPE,
                    "ApiResponse",
                    "Invalid OpenAPI Generics metadata for wrapper model 'ApiResponseCustomerDto': vendor "
                            + "extension 'x-api-wrapper-type' must contain a fully qualified Java type but was "
                            + "'ApiResponse'."),
            arguments(
                    "missing wrapper datatype",
                    CodegenVendorExtensions.API_WRAPPER_DATATYPE,
                    MissingValue.INSTANCE,
                    "Invalid OpenAPI Generics metadata for wrapper model 'ApiResponseCustomerDto': "
                            + "required vendor extension 'x-api-wrapper-datatype' is missing."),
            arguments(
                    "blank wrapper datatype",
                    CodegenVendorExtensions.API_WRAPPER_DATATYPE,
                    "",
                    "Invalid OpenAPI Generics metadata for wrapper model 'ApiResponseCustomerDto': vendor "
                            + "extension 'x-api-wrapper-datatype' must not be blank."),
            arguments(
                    "wrong wrapper datatype value",
                    CodegenVendorExtensions.API_WRAPPER_DATATYPE,
                    42,
                    "Invalid OpenAPI Generics metadata for model 'ApiResponseCustomerDto': vendor "
                            + "extension 'x-api-wrapper-datatype' must be a String but was "
                            + "java.lang.Integer."));
  }

  private static Stream<Arguments> invalidContainerMetadata() {
    return Stream.of(
            arguments(
                    "blank container name",
                    CodegenVendorExtensions.DATA_CONTAINER,
                    " ",
                    "Invalid OpenAPI Generics metadata for wrapper model "
                            + "'ApiResponseWindowCustomerDto': vendor extension 'x-data-container' must not "
                            + "be blank."),
            arguments(
                    "wrong container name type",
                    CodegenVendorExtensions.DATA_CONTAINER,
                    42,
                    "Invalid OpenAPI Generics metadata for model 'ApiResponseWindowCustomerDto': vendor "
                            + "extension 'x-data-container' must be a String but was java.lang.Integer."),
            arguments(
                    "blank container type",
                    CodegenVendorExtensions.DATA_CONTAINER_TYPE,
                    "",
                    "Invalid OpenAPI Generics metadata for wrapper model "
                            + "'ApiResponseWindowCustomerDto': vendor extension 'x-data-container-type' must "
                            + "not be blank."),
            arguments(
                    "wrong container type value",
                    CodegenVendorExtensions.DATA_CONTAINER_TYPE,
                    42,
                    "Invalid OpenAPI Generics metadata for model 'ApiResponseWindowCustomerDto': vendor "
                            + "extension 'x-data-container-type' must be a String but was "
                            + "java.lang.Integer."),
            arguments(
                    "unqualified container type",
                    CodegenVendorExtensions.DATA_CONTAINER_TYPE,
                    "Window",
                    "Invalid OpenAPI Generics metadata for wrapper model "
                            + "'ApiResponseWindowCustomerDto': vendor extension 'x-data-container-type' must "
                            + "contain a fully qualified Java type but was 'Window'."),
            arguments(
                    "blank container item",
                    CodegenVendorExtensions.DATA_ITEM,
                    " ",
                    "Invalid OpenAPI Generics metadata for wrapper model "
                            + "'ApiResponseWindowCustomerDto': vendor extension 'x-data-item' must not be "
                            + "blank."),
            arguments(
                    "wrong container item type",
                    CodegenVendorExtensions.DATA_ITEM,
                    42,
                    "Invalid OpenAPI Generics metadata for model 'ApiResponseWindowCustomerDto': vendor "
                            + "extension 'x-data-item' must be a String but was java.lang.Integer."));
  }

  private static Stream<String> containerMetadataKeys() {
    return Stream.of(
            CodegenVendorExtensions.DATA_CONTAINER,
            CodegenVendorExtensions.DATA_CONTAINER_TYPE,
            CodegenVendorExtensions.DATA_ITEM);
  }

  private CodegenModel validDirectWrapper() {
    CodegenModel model = model("ApiResponseCustomerDto");

    model.vendorExtensions.put(
            CodegenVendorExtensions.API_WRAPPER,
            Boolean.TRUE);
    model.vendorExtensions.put(
            CodegenVendorExtensions.API_WRAPPER_TYPE,
            WRAPPER_TYPE);
    model.vendorExtensions.put(
            CodegenVendorExtensions.API_WRAPPER_DATATYPE,
            "CustomerDto");

    return model;
  }

  private CodegenModel validContainerWrapper() {
    CodegenModel model = model("ApiResponseWindowCustomerDto");

    model.vendorExtensions.put(
            CodegenVendorExtensions.API_WRAPPER,
            Boolean.TRUE);
    model.vendorExtensions.put(
            CodegenVendorExtensions.API_WRAPPER_TYPE,
            WRAPPER_TYPE);
    model.vendorExtensions.put(
            CodegenVendorExtensions.API_WRAPPER_DATATYPE,
            "WindowCustomerDto");
    model.vendorExtensions.put(
            CodegenVendorExtensions.DATA_CONTAINER,
            "Window");
    model.vendorExtensions.put(
            CodegenVendorExtensions.DATA_CONTAINER_TYPE,
            CONTAINER_TYPE);
    model.vendorExtensions.put(
            CodegenVendorExtensions.DATA_ITEM,
            "CustomerDto");

    return model;
  }

  private CodegenModel model(String name) {
    CodegenModel model = new CodegenModel();
    model.name = name;
    model.vendorExtensions = new HashMap<>();
    return model;
  }

  private enum MissingValue {
    INSTANCE
  }
}
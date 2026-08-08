package io.github.blueprintplatform.openapi.generics.server.core.schema.extraction;

import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.SchemaConstants.COMPONENT_SCHEMA_REF_PREFIX;
import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.SchemaConstants.TYPE_ARRAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.JsonSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.LinkedHashSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: ArrayItemReferenceExtractor")
class ArrayItemReferenceExtractorTest {

  private final ArrayItemReferenceExtractor extractor = new ArrayItemReferenceExtractor();

  @Test
  @DisplayName("extractItemName -> should return null for null schema")
  void extractItemName_shouldReturnNull_forNullSchema() {
    assertNull(extractor.extractItemName(null));
  }

  @Test
  @DisplayName("extractItemName -> should extract component name from ArraySchema")
  void extractItemName_shouldExtractComponentName_fromArraySchema() {
    ArraySchema arraySchema = new ArraySchema();
    arraySchema.setItems(componentRef("CustomerDto"));

    String result = extractor.extractItemName(arraySchema);

    assertEquals("CustomerDto", result);
  }

  @Test
  @DisplayName("extractItemName -> should extract component name from schema with array type")
  void extractItemName_shouldExtractComponentName_fromSchemaWithArrayType() {
    Schema<Object> arraySchema = new Schema<>();
    arraySchema.setType(TYPE_ARRAY);
    arraySchema.setItems(componentRef("OrderDto"));

    String result = extractor.extractItemName(arraySchema);

    assertEquals("OrderDto", result);
  }

  @Test
  @DisplayName("extractItemName -> should extract component name from JsonSchema array")
  void extractItemName_shouldExtractComponentName_fromJsonSchemaArray() {
    JsonSchema arraySchema = new JsonSchema();
    arraySchema.setTypes(new LinkedHashSet<>());
    arraySchema.getTypes().add(TYPE_ARRAY);
    arraySchema.setItems(componentRef("InvoiceDto"));

    String result = extractor.extractItemName(arraySchema);

    assertEquals("InvoiceDto", result);
  }

  @Test
  @DisplayName("extractItemName -> should return null when ArraySchema has no items")
  void extractItemName_shouldReturnNull_whenArraySchemaHasNoItems() {
    ArraySchema arraySchema = new ArraySchema();

    assertNull(extractor.extractItemName(arraySchema));
  }

  @Test
  @DisplayName("extractItemName -> should return null when array schema has no items")
  void extractItemName_shouldReturnNull_whenTypedArraySchemaHasNoItems() {
    Schema<Object> arraySchema = new Schema<>();
    arraySchema.setType(TYPE_ARRAY);

    assertNull(extractor.extractItemName(arraySchema));
  }

  @Test
  @DisplayName("extractItemName -> should return null when JsonSchema array has no items")
  void extractItemName_shouldReturnNull_whenJsonSchemaArrayHasNoItems() {
    JsonSchema arraySchema = new JsonSchema();
    arraySchema.setTypes(new LinkedHashSet<>());
    arraySchema.getTypes().add(TYPE_ARRAY);

    assertNull(extractor.extractItemName(arraySchema));
  }

  @Test
  @DisplayName("extractItemName -> should return null for non-array schema")
  void extractItemName_shouldReturnNull_forNonArraySchema() {
    Schema<Object> schema = new Schema<>();
    schema.setType("object");
    schema.setItems(componentRef("CustomerDto"));

    assertNull(extractor.extractItemName(schema));
  }

  @Test
  @DisplayName("extractItemName -> should return null for JsonSchema without types")
  void extractItemName_shouldReturnNull_forJsonSchemaWithoutTypes() {
    JsonSchema schema = new JsonSchema();
    schema.setItems(componentRef("CustomerDto"));

    assertNull(extractor.extractItemName(schema));
  }

  @Test
  @DisplayName("extractItemName -> should return null for JsonSchema without array type")
  void extractItemName_shouldReturnNull_forJsonSchemaWithoutArrayType() {
    JsonSchema schema = new JsonSchema();
    schema.setTypes(new LinkedHashSet<>());
    schema.getTypes().add("object");
    schema.setItems(componentRef("CustomerDto"));

    assertNull(extractor.extractItemName(schema));
  }

  @Test
  @DisplayName("extractItemName -> should return null when item reference is null")
  void extractItemName_shouldReturnNull_whenItemReferenceIsNull() {
    ArraySchema arraySchema = new ArraySchema();
    arraySchema.setItems(new Schema<>());

    assertNull(extractor.extractItemName(arraySchema));
  }

  @Test
  @DisplayName(
      "extractItemName -> should return null when item reference is not a component schema")
  void extractItemName_shouldReturnNull_whenItemReferenceIsNotComponentSchema() {
    Schema<Object> item = new Schema<>();
    item.set$ref("#/definitions/CustomerDto");

    ArraySchema arraySchema = new ArraySchema();
    arraySchema.setItems(item);

    assertNull(extractor.extractItemName(arraySchema));
  }

  @Test
  @DisplayName("extractItemName -> should preserve nested component schema name")
  void extractItemName_shouldPreserveNestedComponentSchemaName() {
    ArraySchema arraySchema = new ArraySchema();
    arraySchema.setItems(componentRef("PageCustomerDto"));

    String result = extractor.extractItemName(arraySchema);

    assertEquals("PageCustomerDto", result);
  }

  private static Schema<Object> componentRef(String schemaName) {
    Schema<Object> schema = new Schema<>();
    schema.set$ref(COMPONENT_SCHEMA_REF_PREFIX + schemaName);
    return schema;
  }
}

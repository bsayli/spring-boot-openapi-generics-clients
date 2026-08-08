package io.github.blueprintplatform.openapi.generics.server.core.schema.resolution;

import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.SchemaConstants.TYPE_ARRAY;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.JsonSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: WrapperPayloadArraySchemaResolver")
class WrapperPayloadArraySchemaResolverTest {

  private static final String WRAPPER_NAME = "ServiceResponseListCustomerDto";
  private static final String PAYLOAD_PROPERTY = "data";
  private static final String DATA_REF_NAME = "ListCustomerDto";

  private final WrapperPayloadArraySchemaResolver resolver =
      new WrapperPayloadArraySchemaResolver();

  @Test
  @DisplayName("resolve -> should return null when wrapper schema does not exist")
  void resolve_shouldReturnNull_whenWrapperSchemaDoesNotExist() {
    Map<String, Schema> schemas = new HashMap<>();

    Schema<?> result = resolver.resolve(schemas, DATA_REF_NAME, WRAPPER_NAME, PAYLOAD_PROPERTY);

    assertNull(result);
  }

  @Test
  @DisplayName("resolve -> should return null when wrapper properties are missing")
  void resolve_shouldReturnNull_whenWrapperPropertiesAreMissing() {
    Map<String, Schema> schemas = new HashMap<>();

    Schema<Object> wrapper = new Schema<>();
    schemas.put(WRAPPER_NAME, wrapper);

    Schema<?> result = resolver.resolve(schemas, DATA_REF_NAME, WRAPPER_NAME, PAYLOAD_PROPERTY);

    assertNull(result);
  }

  @Test
  @DisplayName("resolve -> should return null when payload property does not exist")
  void resolve_shouldReturnNull_whenPayloadPropertyDoesNotExist() {
    Map<String, Schema> schemas = new HashMap<>();

    Schema<Object> wrapper = new Schema<>();
    wrapper.setProperties(new HashMap<>());
    schemas.put(WRAPPER_NAME, wrapper);

    Schema<?> result = resolver.resolve(schemas, DATA_REF_NAME, WRAPPER_NAME, PAYLOAD_PROPERTY);

    assertNull(result);
  }

  @Test
  @DisplayName("resolve -> should return ArraySchema payload")
  void resolve_shouldReturnArraySchemaPayload() {
    ArraySchema dataProperty = new ArraySchema();

    Schema<?> result = resolveWithPayload(dataProperty);

    assertSame(dataProperty, result);
  }

  @Test
  @DisplayName("resolve -> should return schema whose type is array")
  void resolve_shouldReturnSchema_whenTypeIsArray() {
    Schema<Object> dataProperty = new Schema<>();
    dataProperty.setType(TYPE_ARRAY);

    Schema<?> result = resolveWithPayload(dataProperty);

    assertSame(dataProperty, result);
  }

  @Test
  @DisplayName("resolve -> should return JsonSchema whose types include array")
  void resolve_shouldReturnJsonSchema_whenTypesIncludeArray() {
    JsonSchema dataProperty = new JsonSchema();
    dataProperty.setTypes(new LinkedHashSet<>());
    dataProperty.getTypes().add(TYPE_ARRAY);

    Schema<?> result = resolveWithPayload(dataProperty);

    assertSame(dataProperty, result);
  }

  @Test
  @DisplayName("resolve -> should return null for non-array schema")
  void resolve_shouldReturnNull_forNonArraySchema() {
    Schema<Object> dataProperty = new Schema<>();
    dataProperty.setType("object");

    Schema<?> result = resolveWithPayload(dataProperty);

    assertNull(result);
  }

  @Test
  @DisplayName("resolve -> should return null for JsonSchema without types")
  void resolve_shouldReturnNull_forJsonSchemaWithoutTypes() {
    JsonSchema dataProperty = new JsonSchema();

    Schema<?> result = resolveWithPayload(dataProperty);

    assertNull(result);
  }

  @Test
  @DisplayName("resolve -> should return null for JsonSchema without array type")
  void resolve_shouldReturnNull_forJsonSchemaWithoutArrayType() {
    JsonSchema dataProperty = new JsonSchema();
    dataProperty.setTypes(new LinkedHashSet<>());
    dataProperty.getTypes().add("object");

    Schema<?> result = resolveWithPayload(dataProperty);

    assertNull(result);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private Schema<?> resolveWithPayload(Schema<?> dataProperty) {
    Map<String, Schema> schemas = new HashMap<>();

    Schema<Object> wrapper = new Schema<>();
    Map<String, Schema> properties = new HashMap<>();
    properties.put(PAYLOAD_PROPERTY, dataProperty);

    wrapper.setProperties(properties);
    schemas.put(WRAPPER_NAME, wrapper);

    return resolver.resolve(schemas, DATA_REF_NAME, WRAPPER_NAME, PAYLOAD_PROPERTY);
  }
}

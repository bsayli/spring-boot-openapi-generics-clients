package io.github.blueprintplatform.openapi.generics.server.core.schema.resolution;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.JsonSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: ComponentContainerSchemaResolver")
class ComponentContainerSchemaResolverTest {

  private final ComponentContainerSchemaResolver resolver = new ComponentContainerSchemaResolver();

  @Test
  @DisplayName("resolve -> should return null when data ref name does not exist")
  void resolve_shouldReturnNull_whenDataRefNameDoesNotExist() {
    Schema<?> result = resolver.resolve(Map.of(), "MissingDto", "WrapperDto", "data");

    assertNull(result);
  }

  @Test
  @DisplayName("resolve -> should resolve object container schema")
  void resolve_shouldResolveObjectContainerSchema() {
    ObjectSchema container = new ObjectSchema();
    container.addProperty("content", new ArraySchema());

    Schema<?> result =
        resolver.resolve(
            schemas("PageCustomerDto", container), "PageCustomerDto", "WrapperDto", "data");

    assertSame(container, result);
  }

  @Test
  @DisplayName("resolve -> should return null for object type without properties")
  void resolve_shouldReturnNull_forObjectTypeWithoutProperties() {
    Schema<?> container = new Schema<>().type("object");

    Schema<?> result =
        resolver.resolve(
            schemas("PagingCustomerDto", container), "PagingCustomerDto", "WrapperDto", "data");

    assertNull(result);
  }

  @Test
  @DisplayName("resolve -> should resolve schema with properties")
  void resolve_shouldResolveSchemaWithProperties() {
    Schema<?> container = new Schema<>();
    container.addProperty("items", new ArraySchema());

    Schema<?> result =
        resolver.resolve(
            schemas("WindowCustomerDto", container), "WindowCustomerDto", "WrapperDto", "data");

    assertSame(container, result);
  }

  @Test
  @DisplayName("resolve -> should resolve array schema")
  void resolve_shouldResolveArraySchema() {
    ArraySchema container = new ArraySchema();

    Schema<?> result =
        resolver.resolve(
            schemas("ListCustomerDto", container), "ListCustomerDto", "WrapperDto", "data");

    assertSame(container, result);
  }

  @Test
  @DisplayName("resolve -> should resolve schema with array type")
  void resolve_shouldResolveSchemaWithArrayType() {
    Schema<?> container = new Schema<>().type("array");

    Schema<?> result =
        resolver.resolve(
            schemas("ListCustomerDto", container), "ListCustomerDto", "WrapperDto", "data");

    assertSame(container, result);
  }

  @Test
  @DisplayName("resolve -> should resolve json schema with array type")
  void resolve_shouldResolveJsonSchemaWithArrayType() {
    JsonSchema container = new JsonSchema();
    container.setTypes(java.util.Set.of("array"));

    Schema<?> result =
        resolver.resolve(
            schemas("ListCustomerDto", container), "ListCustomerDto", "WrapperDto", "data");

    assertSame(container, result);
  }

  @Test
  @DisplayName("resolve -> should dereference component schema")
  void resolve_shouldDereferenceComponentSchema() {
    ObjectSchema container = new ObjectSchema();
    container.addProperty("content", new ArraySchema());

    Schema<?> ref = new Schema<>().$ref("#/components/schemas/PageCustomerDto");

    Map<String, Schema> schemas =
        schemas(
            "PageCustomerDto", container,
            "PageCustomerDtoRef", ref);

    Schema<?> result = resolver.resolve(schemas, "PageCustomerDtoRef", "WrapperDto", "data");

    assertSame(container, result);
  }

  @Test
  @DisplayName("resolve -> should resolve container inside allOf")
  void resolve_shouldResolveContainerInsideAllOf() {
    ObjectSchema container = new ObjectSchema();
    container.addProperty("content", new ArraySchema());

    ComposedSchema composed = new ComposedSchema();
    composed.setAllOf(List.of(new ObjectSchema(), container));

    Schema<?> result =
        resolver.resolve(
            schemas("PageCustomerDto", composed), "PageCustomerDto", "WrapperDto", "data");

    assertSame(container, result);
  }

  @Test
  @DisplayName("resolve -> should resolve referenced container inside allOf")
  void resolve_shouldResolveReferencedContainerInsideAllOf() {
    ObjectSchema container = new ObjectSchema();
    container.addProperty("content", new ArraySchema());

    ComposedSchema composed = new ComposedSchema();
    composed.setAllOf(List.of(new Schema<>().$ref("#/components/schemas/PageCustomerDto")));

    Map<String, Schema> schemas =
        schemas(
            "PageCustomerDto", container,
            "ComposedPageCustomerDto", composed);

    Schema<?> result = resolver.resolve(schemas, "ComposedPageCustomerDto", "WrapperDto", "data");

    assertSame(container, result);
  }

  @Test
  @DisplayName("resolve -> should return null when schema is not container-like")
  void resolve_shouldReturnNull_whenSchemaIsNotContainerLike() {
    Schema<?> scalar = new Schema<>().type("string");

    Schema<?> result =
        resolver.resolve(schemas("StringDto", scalar), "StringDto", "WrapperDto", "data");

    assertNull(result);
  }

  @Test
  @DisplayName("resolve -> should return null when component ref is missing")
  void resolve_shouldReturnNull_whenComponentRefIsMissing() {
    Schema<?> ref = new Schema<>().$ref("#/components/schemas/MissingDto");

    Schema<?> result =
        resolver.resolve(schemas("BrokenRefDto", ref), "BrokenRefDto", "WrapperDto", "data");

    assertNull(result);
  }

  @Test
  @DisplayName("resolve -> should return null when component ref cycle is detected")
  void resolve_shouldReturnNull_whenComponentRefCycleIsDetected() {
    Schema<?> first = new Schema<>().$ref("#/components/schemas/SecondDto");
    Schema<?> second = new Schema<>().$ref("#/components/schemas/FirstDto");

    Map<String, Schema> schemas =
        schemas(
            "FirstDto", first,
            "SecondDto", second);

    Schema<?> result = resolver.resolve(schemas, "FirstDto", "WrapperDto", "data");

    assertNull(result);
  }

  @Test
  @DisplayName("resolve -> should return null when mapped component schema is null")
  void resolve_shouldReturnNull_whenMappedComponentSchemaIsNull() {
    Map<String, Schema> schemas = new LinkedHashMap<>();
    schemas.put("NullDto", null);

    Schema<?> result = resolver.resolve(schemas, "NullDto", "WrapperDto", "data");

    assertNull(result);
  }

  @Test
  @DisplayName("resolve -> should return null for JsonSchema without types")
  void resolve_shouldReturnNull_forJsonSchemaWithoutTypes() {
    JsonSchema schema = new JsonSchema();

    Schema<?> result =
        resolver.resolve(schemas("JsonDto", schema), "JsonDto", "WrapperDto", "data");

    assertNull(result);
  }

  @Test
  @DisplayName("resolve -> should return null for JsonSchema without array type")
  void resolve_shouldReturnNull_forJsonSchemaWithoutArrayType() {
    JsonSchema schema = new JsonSchema();
    schema.setTypes(java.util.Set.of("object"));

    Schema<?> result =
        resolver.resolve(schemas("JsonDto", schema), "JsonDto", "WrapperDto", "data");

    assertNull(result);
  }

  @Test
  @DisplayName("resolve -> should return null for composed schema without allOf")
  void resolve_shouldReturnNull_forComposedSchemaWithoutAllOf() {
    ComposedSchema composed = new ComposedSchema();

    Schema<?> result =
        resolver.resolve(schemas("ComposedDto", composed), "ComposedDto", "WrapperDto", "data");

    assertNull(result);
  }

  @Test
  @DisplayName("resolve -> should not dereference non-component reference")
  void resolve_shouldNotDereferenceNonComponentReference() {
    Schema<?> ref = new Schema<>().$ref("#/definitions/PageCustomerDto");

    Schema<?> result =
        resolver.resolve(schemas("ExternalRefDto", ref), "ExternalRefDto", "WrapperDto", "data");

    assertNull(result);
  }

  @Test
  @DisplayName("resolve -> should return null when no allOf candidate is container-like")
  void resolve_shouldReturnNull_whenNoAllOfCandidateIsContainerLike() {
    ComposedSchema composed = new ComposedSchema();
    composed.setAllOf(List.of(new Schema<>().type("string"), new Schema<>().type("integer")));

    Schema<?> result =
        resolver.resolve(schemas("ComposedDto", composed), "ComposedDto", "WrapperDto", "data");

    assertNull(result);
  }

  private Map<String, Schema> schemas(String name, Schema<?> schema) {
    Map<String, Schema> schemas = new LinkedHashMap<>();
    schemas.put(name, schema);
    return schemas;
  }

  private Map<String, Schema> schemas(
      String firstName, Schema<?> firstSchema, String secondName, Schema<?> secondSchema) {
    Map<String, Schema> schemas = new LinkedHashMap<>();
    schemas.put(firstName, firstSchema);
    schemas.put(secondName, secondSchema);
    return schemas;
  }
}

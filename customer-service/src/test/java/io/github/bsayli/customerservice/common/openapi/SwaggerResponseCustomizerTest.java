package io.github.bsayli.customerservice.common.openapi;

import static org.junit.jupiter.api.Assertions.*;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;

@Tag("unit")
@DisplayName("Unit Test: SwaggerResponseCustomizer")
class SwaggerResponseCustomizerTest {

  private static final String REF_PREFIX = "#/components/schemas/";

  @Test
  @DisplayName("Creates Sort, Meta, ServiceResponse and ServiceResponseVoid schemas when missing")
  void createsEnvelopeSchemas_whenMissing() {
    OpenApiCustomizer customizer = new SwaggerResponseCustomizer().responseEnvelopeSchemas();

    var openAPI = new OpenAPI().components(new Components());
    customizer.customise(openAPI);

    Map<String, Schema> schemas = openAPI.getComponents().getSchemas();
    assertNotNull(schemas);
    assertTrue(schemas.containsKey(OpenApiSchemas.SCHEMA_SORT));
    assertTrue(schemas.containsKey(OpenApiSchemas.SCHEMA_META));
    assertTrue(schemas.containsKey(OpenApiSchemas.SCHEMA_SERVICE_RESPONSE));
    assertTrue(schemas.containsKey(OpenApiSchemas.SCHEMA_SERVICE_RESPONSE_VOID));
  }

  @Test
  @DisplayName("Meta schema has requestId, serverTime, and sort[] referencing Sort")
  void metaSchema_structure_isCorrect() {
    OpenApiCustomizer customizer = new SwaggerResponseCustomizer().responseEnvelopeSchemas();

    var openAPI = new OpenAPI().components(new Components());
    customizer.customise(openAPI);

    var meta = openAPI.getComponents().getSchemas().get(OpenApiSchemas.SCHEMA_META);
    assertNotNull(meta);
    assertNotNull(meta.getProperties());
    assertTrue(meta.getProperties().containsKey("requestId"));
    assertTrue(meta.getProperties().containsKey("serverTime"));
    assertTrue(meta.getProperties().containsKey("sort"));

    var sortProp = meta.getProperties().get("sort");
    assertInstanceOf(ArraySchema.class, sortProp);
    var array = (ArraySchema) sortProp;
    assertNotNull(array.getItems());
    assertEquals(REF_PREFIX + OpenApiSchemas.SCHEMA_SORT, array.getItems().get$ref());
  }

  @Test
  @DisplayName("ServiceResponse schema has 'data' (object) and 'meta' ($ref Meta)")
  void serviceResponseSchema_structure_isCorrect() {
    OpenApiCustomizer customizer = new SwaggerResponseCustomizer().responseEnvelopeSchemas();

    var openAPI = new OpenAPI().components(new Components());
    customizer.customise(openAPI);

    var sr = openAPI.getComponents().getSchemas().get(OpenApiSchemas.SCHEMA_SERVICE_RESPONSE);
    assertNotNull(sr);
    assertNotNull(sr.getProperties());
    assertTrue(sr.getProperties().containsKey(OpenApiSchemas.PROP_DATA));
    assertTrue(sr.getProperties().containsKey(OpenApiSchemas.PROP_META));

    var metaProp = (Schema<?>) sr.getProperties().get(OpenApiSchemas.PROP_META);
    assertEquals(REF_PREFIX + OpenApiSchemas.SCHEMA_META, metaProp.get$ref());
  }

  @Test
  @DisplayName("ServiceResponseVoid schema mirrors ServiceResponse structure")
  void serviceResponseVoidSchema_structure_isCorrect() {
    OpenApiCustomizer customizer = new SwaggerResponseCustomizer().responseEnvelopeSchemas();

    var openAPI = new OpenAPI().components(new Components());
    customizer.customise(openAPI);

    var srv = openAPI.getComponents().getSchemas().get(OpenApiSchemas.SCHEMA_SERVICE_RESPONSE_VOID);
    assertNotNull(srv);
    assertNotNull(srv.getProperties());
    assertTrue(srv.getProperties().containsKey(OpenApiSchemas.PROP_DATA));
    assertTrue(srv.getProperties().containsKey(OpenApiSchemas.PROP_META));

    var metaProp = (Schema<?>) srv.getProperties().get(OpenApiSchemas.PROP_META);
    assertEquals(REF_PREFIX + OpenApiSchemas.SCHEMA_META, metaProp.get$ref());
  }

  @Test
  @DisplayName("Idempotent: running the customizer twice doesn't duplicate or error")
  void idempotentCustomization() {
    OpenApiCustomizer customizer = new SwaggerResponseCustomizer().responseEnvelopeSchemas();

    var openAPI = new OpenAPI().components(new Components());
    customizer.customise(openAPI);
    customizer.customise(openAPI);

    var schemas = openAPI.getComponents().getSchemas();
    assertNotNull(schemas);
    assertTrue(schemas.containsKey(OpenApiSchemas.SCHEMA_SORT));
    assertTrue(schemas.containsKey(OpenApiSchemas.SCHEMA_META));
    assertTrue(schemas.containsKey(OpenApiSchemas.SCHEMA_SERVICE_RESPONSE));
    assertTrue(schemas.containsKey(OpenApiSchemas.SCHEMA_SERVICE_RESPONSE_VOID));
  }

  @Test
  @DisplayName("Uses existing schemas map if present (does not overwrite)")
  void respectsExistingSchemasMap() {
    OpenApiCustomizer customizer = new SwaggerResponseCustomizer().responseEnvelopeSchemas();

    var preExisting = new LinkedHashMap<String, Schema>();
    preExisting.put("PreExisting", new Schema<>());
    var openAPI = new OpenAPI().components(new Components().schemas(preExisting));

    customizer.customise(openAPI);

    var schemas = openAPI.getComponents().getSchemas();
    assertSame(preExisting, schemas);
    assertTrue(schemas.containsKey("PreExisting"));
    assertTrue(schemas.containsKey(OpenApiSchemas.SCHEMA_SORT));
    assertTrue(schemas.containsKey(OpenApiSchemas.SCHEMA_META));
    assertTrue(schemas.containsKey(OpenApiSchemas.SCHEMA_SERVICE_RESPONSE));
    assertTrue(schemas.containsKey(OpenApiSchemas.SCHEMA_SERVICE_RESPONSE_VOID));
  }
}

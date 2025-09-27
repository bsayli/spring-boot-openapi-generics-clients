package io.github.bsayli.customerservice.common.openapi;

import static org.junit.jupiter.api.Assertions.*;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;

@Tag("unit")
@DisplayName("Unit Test: SwaggerResponseCustomizer")
class SwaggerResponseCustomizerTest {

  private final SwaggerResponseCustomizer config = new SwaggerResponseCustomizer();
  private final OpenApiCustomizer customizer = config.responseEnvelopeSchemas();

  @Test
  @DisplayName("Should add missing schemas when absent")
  void shouldAddSchemasWhenAbsent() {
    OpenAPI openAPI = new OpenAPI().components(new Components().schemas(new HashMap<>()));

    customizer.customise(openAPI);

    @SuppressWarnings("rawtypes")
    Map<String, Schema> schemas = openAPI.getComponents().getSchemas();
    assertNotNull(schemas.get(OpenApiSchemas.SCHEMA_SERVICE_RESPONSE));
    assertNotNull(schemas.get(OpenApiSchemas.SCHEMA_SERVICE_RESPONSE_VOID));
  }

  @Test
  @DisplayName("Should not override existing schemas")
  void shouldNotOverrideExistingSchemas() {
    Schema<?> existing = new Schema<>().description("pre-existing");
    Components components = new Components().schemas(new HashMap<>());
    components.addSchemas(OpenApiSchemas.SCHEMA_SERVICE_RESPONSE, existing);
    OpenAPI openAPI = new OpenAPI().components(components);

    customizer.customise(openAPI);

    Schema<?> result =
        openAPI.getComponents().getSchemas().get(OpenApiSchemas.SCHEMA_SERVICE_RESPONSE);
    assertSame(existing, result, "Existing schema should remain unchanged");
  }
}

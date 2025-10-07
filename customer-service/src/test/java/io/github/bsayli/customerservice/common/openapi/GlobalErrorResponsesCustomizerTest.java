package io.github.bsayli.customerservice.common.openapi;

import static org.junit.jupiter.api.Assertions.*;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;

@Tag("unit")
@DisplayName("Unit Test: GlobalErrorResponsesCustomizer")
class GlobalErrorResponsesCustomizerTest {

  private static final String MT_PROBLEM_JSON = "application/problem+json";
  private static final String REF_PREFIX = "#/components/schemas/";

  @Test
  @DisplayName("Adds ProblemDetail and ErrorItem schemas when missing")
  void addsSchemas_whenMissing() {
    OpenApiCustomizer customizer =
        new GlobalErrorResponsesCustomizer().addDefaultProblemResponses();

    var openAPI =
        new OpenAPI()
            .components(new Components().schemas(new LinkedHashMap<>()))
            .paths(minimalPathWithOperation());

    customizer.customise(openAPI);

    Map<String, Schema> schemas = openAPI.getComponents().getSchemas();
    assertNotNull(schemas, "schemas map should exist");
    assertTrue(schemas.containsKey(OpenApiSchemas.SCHEMA_PROBLEM_DETAIL), "ProblemDetail schema");
    assertTrue(schemas.containsKey("ErrorItem"), "ErrorItem schema");
  }

  @Test
  @DisplayName(
      "Registers default problem responses (400, 404, 405, 500) with application/problem+json")
  void registersDefaultProblemResponses() {
    OpenApiCustomizer customizer =
        new GlobalErrorResponsesCustomizer().addDefaultProblemResponses();

    var openAPI =
        new OpenAPI()
            .components(new Components().schemas(new LinkedHashMap<>()))
            .paths(minimalPathWithOperation());

    customizer.customise(openAPI);

    var op =
        openAPI.getPaths().values().iterator().next().readOperations().stream()
            .findFirst()
            .orElseThrow();
    var responses = op.getResponses();

    for (String code : new String[] {"400", "404", "405", "500"}) {
      assertTrue(responses.containsKey(code), "response " + code + " should be present");
      Content content = responses.get(code).getContent();
      assertNotNull(content, "content must exist for " + code);
      MediaType mt = content.get(MT_PROBLEM_JSON);
      assertNotNull(mt, "media type should be " + MT_PROBLEM_JSON + " for " + code);
      Schema<?> schema = mt.getSchema();
      assertNotNull(schema, "schema must exist for " + code);
      assertEquals(
          REF_PREFIX + OpenApiSchemas.SCHEMA_PROBLEM_DETAIL,
          schema.get$ref(),
          "schema ref must target ProblemDetail for " + code);
    }
  }

  @Test
  @DisplayName("No-op when components is null (does not throw)")
  void noOp_whenComponentsNull() {
    OpenApiCustomizer customizer =
        new GlobalErrorResponsesCustomizer().addDefaultProblemResponses();

    var openAPI = new OpenAPI(); // components == null
    assertDoesNotThrow(() -> customizer.customise(openAPI));
  }

  @Test
  @DisplayName("Idempotent customization (second run does not break or duplicate)")
  void idempotentCustomization() {
    OpenApiCustomizer customizer =
        new GlobalErrorResponsesCustomizer().addDefaultProblemResponses();

    var openAPI =
        new OpenAPI()
            .components(new Components().schemas(new LinkedHashMap<>()))
            .paths(minimalPathWithOperation());

    customizer.customise(openAPI);
    customizer.customise(openAPI); // run twice

    var schemas = openAPI.getComponents().getSchemas();
    assertTrue(schemas.containsKey(OpenApiSchemas.SCHEMA_PROBLEM_DETAIL));
    assertTrue(schemas.containsKey("ErrorItem"));

    var op =
        openAPI.getPaths().values().iterator().next().readOperations().stream()
            .findFirst()
            .orElseThrow();
    var responses = op.getResponses();

    assertEquals(4, responses.size(), "should still have exactly 4 default responses");
  }

  private Paths minimalPathWithOperation() {
    var op = new Operation().responses(new ApiResponses());
    var pi = new PathItem().get(op);
    return new Paths().addPathItem("/test", pi);
  }
}

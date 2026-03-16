package io.github.bsayli.customerservice.common.openapi;

import static io.github.bsayli.customerservice.common.openapi.OpenApiSchemas.*;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.LinkedHashMap;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the canonical base response-envelope schemas used by the published OpenAPI document.
 *
 * <p>This customizer defines the shared structural building blocks required by the contract:
 *
 * <ul>
 *   <li>{@code Sort}
 *   <li>{@code Meta}
 *   <li>{@code ServiceResponse}
 *   <li>{@code ServiceResponseVoid}
 * </ul>
 *
 * <p>These schemas act as the stable base layer on top of which contract-aware wrapper schemas are
 * composed. They do not introduce endpoint-specific payload types; instead, they provide the common
 * envelope structure that other OpenAPI customizers can extend deterministically.
 *
 * <p>This class affects only the generated OpenAPI specification. It does not change runtime JSON
 * serialization, controller behavior, or Spring MVC response handling.
 *
 * <p>The intent is to keep the published contract explicit and stable so that:
 *
 * <ul>
 *   <li>server and client share the same response-envelope semantics,
 *   <li>client generators can bind to a known canonical structure,
 *   <li>schema composition remains deterministic across endpoints.
 * </ul>
 */
@Configuration
public class SwaggerResponseCustomizer {

  private static final String COMPONENTS_SCHEMAS = "#/components/schemas/";

  /**
   * Registers the base envelope-related component schemas if they are not already present.
   *
   * <p>The registration is idempotent: existing schemas are preserved and missing ones are added.
   *
   * @return OpenAPI customizer that enriches {@code components.schemas} with canonical envelope
   *     definitions
   */
  @Bean
  public OpenApiCustomizer responseEnvelopeSchemas() {
    return openApi -> {
      if (openApi.getComponents() == null) {
        openApi.setComponents(new Components());
      }

      var schemas = openApi.getComponents().getSchemas();
      if (schemas == null) {
        openApi.getComponents().setSchemas(new LinkedHashMap<>());
        schemas = openApi.getComponents().getSchemas();
      }

      if (!schemas.containsKey(SCHEMA_SORT)) {
        schemas.put(
            SCHEMA_SORT,
            new ObjectSchema()
                .addProperty("field", new StringSchema())
                .addProperty("direction", new StringSchema()._enum(List.of("asc", "desc"))));
      }

      if (!schemas.containsKey(SCHEMA_META)) {
        schemas.put(
            SCHEMA_META,
            new ObjectSchema()
                .addProperty("serverTime", new StringSchema().format("date-time"))
                .addProperty(
                    "sort",
                    new ArraySchema()
                        .items(new Schema<>().$ref(COMPONENTS_SCHEMAS + SCHEMA_SORT))));
      }

      if (!schemas.containsKey(SCHEMA_SERVICE_RESPONSE)) {
        schemas.put(
            SCHEMA_SERVICE_RESPONSE,
            new ObjectSchema()
                .addProperty(PROP_DATA, new ObjectSchema())
                .addProperty(PROP_META, new Schema<>().$ref(COMPONENTS_SCHEMAS + SCHEMA_META)));
      }

      if (!schemas.containsKey(SCHEMA_SERVICE_RESPONSE_VOID)) {
        schemas.put(
            SCHEMA_SERVICE_RESPONSE_VOID,
            new ObjectSchema()
                .addProperty(PROP_DATA, new ObjectSchema())
                .addProperty(PROP_META, new Schema<>().$ref(COMPONENTS_SCHEMAS + SCHEMA_META)));
      }
    };
  }
}
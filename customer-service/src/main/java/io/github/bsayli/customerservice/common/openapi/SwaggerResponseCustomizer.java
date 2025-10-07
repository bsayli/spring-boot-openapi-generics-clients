package io.github.bsayli.customerservice.common.openapi;

import static io.github.bsayli.customerservice.common.openapi.OpenApiSchemas.*;

import io.swagger.v3.oas.models.media.*;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerResponseCustomizer {

  private static final String COMPONENTS_SCHEMAS = "#/components/schemas/";

  @Bean
  public OpenApiCustomizer responseEnvelopeSchemas() {
    return openApi -> {
      var schemas = openApi.getComponents().getSchemas();
      if (schemas == null) {
        openApi.getComponents().setSchemas(new java.util.LinkedHashMap<>());
        schemas = openApi.getComponents().getSchemas();
      }

      if (!schemas.containsKey(SCHEMA_SORT)) {
        schemas.put(
            SCHEMA_SORT,
            new ObjectSchema()
                .addProperty("field", new StringSchema())
                .addProperty(
                    "direction", new StringSchema()._enum(java.util.List.of("asc", "desc"))));
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

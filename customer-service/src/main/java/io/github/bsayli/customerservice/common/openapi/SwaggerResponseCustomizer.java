package io.github.bsayli.customerservice.common.openapi;

import static io.github.bsayli.customerservice.common.openapi.OpenApiSchemas.*;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerResponseCustomizer {

  @Bean
  public OpenApiCustomizer responseEnvelopeSchemas() {
    return openApi -> {
      if (!openApi.getComponents().getSchemas().containsKey(SCHEMA_SERVICE_RESPONSE)) {
        openApi
            .getComponents()
            .addSchemas(
                SCHEMA_SERVICE_RESPONSE,
                new ObjectSchema()
                    .addProperty(PROP_STATUS, new IntegerSchema().format("int32"))
                    .addProperty(PROP_MESSAGE, new StringSchema())
                    .addProperty(
                        PROP_ERRORS,
                        new ArraySchema()
                            .items(
                                new ObjectSchema()
                                    .addProperty(PROP_ERROR_CODE, new StringSchema())
                                    .addProperty(PROP_MESSAGE, new StringSchema()))));
      }

      if (!openApi.getComponents().getSchemas().containsKey(SCHEMA_SERVICE_RESPONSE_VOID)) {
        openApi
            .getComponents()
            .addSchemas(
                SCHEMA_SERVICE_RESPONSE_VOID,
                new ObjectSchema()
                    .addProperty(PROP_STATUS, new IntegerSchema().format("int32"))
                    .addProperty(PROP_MESSAGE, new StringSchema())
                    .addProperty(PROP_DATA, new ObjectSchema())
                    .addProperty(
                        PROP_ERRORS,
                        new ArraySchema()
                            .items(
                                new ObjectSchema()
                                    .addProperty(PROP_ERROR_CODE, new StringSchema())
                                    .addProperty(PROP_MESSAGE, new StringSchema()))));
      }
    };
  }
}

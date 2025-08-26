package com.example.demo.common.api.config;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.example.demo.common.api.config.OpenApiSchemas.*;

@Configuration
public class SwaggerResponseCustomizer {

    @Bean
    public OpenApiCustomizer responseEnvelopeSchemas() {
        return openApi -> {
            if (!openApi.getComponents().getSchemas().containsKey(SCHEMA_API_RESPONSE)) {
                openApi.getComponents().addSchemas(
                        SCHEMA_API_RESPONSE,
                        new ObjectSchema()
                                .addProperty(PROP_STATUS, new IntegerSchema().format("int32"))
                                .addProperty(PROP_MESSAGE, new StringSchema())
                                .addProperty(
                                        PROP_ERRORS,
                                        new ArraySchema().items(
                                                new ObjectSchema()
                                                        .addProperty(PROP_ERROR_CODE, new StringSchema())
                                                        .addProperty(PROP_MESSAGE, new StringSchema())
                                        ))
                );
            }

            if (!openApi.getComponents().getSchemas().containsKey(SCHEMA_API_RESPONSE_VOID)) {
                openApi.getComponents().addSchemas(
                        SCHEMA_API_RESPONSE_VOID,
                        new ObjectSchema()
                                .addProperty(PROP_STATUS, new IntegerSchema().format("int32"))
                                .addProperty(PROP_MESSAGE, new StringSchema())
                                .addProperty(PROP_DATA, new ObjectSchema())
                                .addProperty(
                                        PROP_ERRORS,
                                        new ArraySchema().items(
                                                new ObjectSchema()
                                                        .addProperty(PROP_ERROR_CODE, new StringSchema())
                                                        .addProperty(PROP_MESSAGE, new StringSchema())
                                        ))
                );
            }
        };
    }
}
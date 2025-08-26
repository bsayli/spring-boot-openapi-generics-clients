package com.example.demo.common.api.config;

import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;

import static com.example.demo.common.api.config.OpenApiSchemas.*;

public final class OpenApiSchemaUtils {
    private OpenApiSchemaUtils() {
    }

    public static Schema<?> createComposedWrapper(String dataRefName) {
        var schema = new ComposedSchema();
        schema.setAllOf(List.of(
                new Schema<>().$ref("#/components/schemas/" + SCHEMA_API_RESPONSE),
                new ObjectSchema().addProperty(
                        PROP_DATA, new Schema<>().$ref("#/components/schemas/" + dataRefName))
        ));
        schema.addExtension(EXT_API_WRAPPER, true);
        schema.addExtension(EXT_API_WRAPPER_DATATYPE, dataRefName);
        return schema;
    }
}
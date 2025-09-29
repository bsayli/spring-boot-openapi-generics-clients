package io.github.bsayli.customerservice.common.openapi;

import static io.github.bsayli.customerservice.common.openapi.OpenApiSchemas.*;

import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;

public final class ApiResponseSchemaFactory {
  private ApiResponseSchemaFactory() {}

  public static Schema<?> createComposedWrapper(String dataRefName) {
    return createComposedWrapper(dataRefName, null);
  }

  public static Schema<?> createComposedWrapper(String dataRefName, String classExtraAnnotation) {
    var schema = new ComposedSchema();
    schema.setAllOf(
        List.of(
            new Schema<>().$ref("#/components/schemas/" + SCHEMA_SERVICE_RESPONSE),
            new ObjectSchema()
                .addProperty(
                    PROP_DATA, new Schema<>().$ref("#/components/schemas/" + dataRefName))));

    schema.addExtension(EXT_API_WRAPPER, true);
    schema.addExtension(EXT_API_WRAPPER_DATATYPE, dataRefName);

    if (classExtraAnnotation != null && !classExtraAnnotation.isBlank()) {
      schema.addExtension(EXT_CLASS_EXTRA_ANNOTATION, classExtraAnnotation);
    }

    return schema;
  }
}

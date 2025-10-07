package io.github.bsayli.customerservice.common.openapi;

import static io.github.bsayli.customerservice.common.openapi.OpenApiSchemas.*;

import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ApiResponseSchemaFactory {

  private static final Logger log = LoggerFactory.getLogger(ApiResponseSchemaFactory.class);

  private ApiResponseSchemaFactory() {}

  public static Schema<?> createComposedWrapper(String dataRefName) {
    return createComposedWrapper(dataRefName, null);
  }

  public static Schema<?> createComposedWrapper(String dataRefName, String classExtraAnnotation) {
    if (log.isDebugEnabled()) {
      log.debug(
          "Creating composed wrapper for dataRef='{}', extraAnnotation='{}'",
          dataRefName,
          classExtraAnnotation);
    }

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
      if (log.isDebugEnabled()) {
        log.debug("Added extension {}='{}'", EXT_CLASS_EXTRA_ANNOTATION, classExtraAnnotation);
      }
    }

    if (log.isDebugEnabled()) {
      log.debug(
          "Composed schema created for '{}': extensions={}", dataRefName, schema.getExtensions());
    }

    return schema;
  }
}

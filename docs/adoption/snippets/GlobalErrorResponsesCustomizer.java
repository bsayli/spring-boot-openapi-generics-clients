package io.github.bsayli.customerservice.common.openapi;

import static io.github.bsayli.customerservice.common.openapi.OpenApiSchemas.SCHEMA_PROBLEM_DETAIL;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlobalErrorResponsesCustomizer {

  private static final String MEDIA_TYPE_PROBLEM_JSON = "application/problem+json";
  private static final String REF_PROBLEM_DETAIL = "#/components/schemas/" + SCHEMA_PROBLEM_DETAIL;
  private static final String SCHEMA_ERROR_ITEM = "ErrorItem";

  private static final String STATUS_400 = "400";
  private static final String STATUS_404 = "404";
  private static final String STATUS_405 = "405";
  private static final String STATUS_500 = "500";

  private static final String DESC_BAD_REQUEST = "Bad Request";
  private static final String DESC_NOT_FOUND = "Not Found";
  private static final String DESC_METHOD_NOT_ALLOWED = "Method Not Allowed";
  private static final String DESC_INTERNAL_ERROR = "Internal Server Error";

  @Bean
  OpenApiCustomizer addDefaultProblemResponses() {
    return openApi -> {
      var components = openApi.getComponents();
      if (components == null) return;

      ensureErrorItemSchema(components.getSchemas());
      ensureProblemDetailSchema(components.getSchemas());

      openApi
          .getPaths()
          .forEach(
              (path, item) ->
                  item.readOperations()
                      .forEach(
                          op -> {
                            var responses = op.getResponses();
                            var problemContent =
                                new Content()
                                    .addMediaType(
                                        MEDIA_TYPE_PROBLEM_JSON,
                                        new MediaType()
                                            .schema(new Schema<>().$ref(REF_PROBLEM_DETAIL)));

                            responses.addApiResponse(
                                STATUS_400,
                                new ApiResponse()
                                    .description(DESC_BAD_REQUEST)
                                    .content(problemContent));
                            responses.addApiResponse(
                                STATUS_404,
                                new ApiResponse()
                                    .description(DESC_NOT_FOUND)
                                    .content(problemContent));
                            responses.addApiResponse(
                                STATUS_405,
                                new ApiResponse()
                                    .description(DESC_METHOD_NOT_ALLOWED)
                                    .content(problemContent));
                            responses.addApiResponse(
                                STATUS_500,
                                new ApiResponse()
                                    .description(DESC_INTERNAL_ERROR)
                                    .content(problemContent));
                          }));
    };
  }

  @SuppressWarnings("rawtypes")
  private void ensureProblemDetailSchema(Map<String, Schema> schemas) {
    if (schemas == null) return;
    if (schemas.containsKey(SCHEMA_PROBLEM_DETAIL)) return;

    ObjectSchema pd = new ObjectSchema();

    StringSchema type = new StringSchema();
    type.setFormat("uri");
    type.setDescription("Problem type as a URI.");
    pd.addProperty("type", type);

    StringSchema title = new StringSchema();
    title.setDescription("Short, human-readable summary of the problem type.");
    pd.addProperty("title", title);

    IntegerSchema status = new IntegerSchema();
    status.setFormat("int32");
    status.setDescription("HTTP status code for this problem.");
    pd.addProperty("status", status);

    StringSchema detail = new StringSchema();
    detail.setDescription("Human-readable explanation specific to this occurrence.");
    pd.addProperty("detail", detail);

    StringSchema instance = new StringSchema();
    instance.setFormat("uri");
    instance.setDescription("URI that identifies this specific occurrence.");
    pd.addProperty("instance", instance);

    StringSchema errorCode = new StringSchema();
    errorCode.setDescription("Application-specific error code.");
    pd.addProperty("errorCode", errorCode);

    // extensions.errors[]
    ArraySchema errorsArray = new ArraySchema();
    errorsArray.setItems(new Schema<>().$ref("#/components/schemas/" + SCHEMA_ERROR_ITEM));
    errorsArray.setDescription("List of error items (field-level or domain-specific).");

    ObjectSchema extensions = new ObjectSchema();
    extensions.addProperty("errors", errorsArray);
    extensions.setDescription("Additional problem metadata.");
    extensions.setAdditionalProperties(Boolean.FALSE);

    pd.addProperty("extensions", extensions);

    pd.setAdditionalProperties(Boolean.TRUE);

    schemas.put(SCHEMA_PROBLEM_DETAIL, pd);
  }

  @SuppressWarnings("rawtypes")
  private void ensureErrorItemSchema(Map<String, Schema> schemas) {
    if (schemas == null) return;
    if (schemas.containsKey(SCHEMA_ERROR_ITEM)) return;

    ObjectSchema errorItem = new ObjectSchema();

    StringSchema code = new StringSchema();
    code.setDescription("Short application-specific error code.");
    errorItem.addProperty("code", code);

    StringSchema message = new StringSchema();
    message.setDescription("Human-readable error message.");
    errorItem.addProperty("message", message);

    StringSchema field = new StringSchema();
    field.setDescription("Field name when error is field-specific.");
    errorItem.addProperty("field", field);

    StringSchema resource = new StringSchema();
    resource.setDescription("Domain resource name if applicable.");
    errorItem.addProperty("resource", resource);

    StringSchema id = new StringSchema();
    id.setDescription("Resource identifier if applicable.");
    errorItem.addProperty("id", id);

    errorItem.setDescription("Standard error item structure.");
    errorItem.setRequired(java.util.List.of("code", "message"));
    errorItem.setAdditionalProperties(Boolean.FALSE);

    schemas.put(SCHEMA_ERROR_ITEM, errorItem);
  }
}

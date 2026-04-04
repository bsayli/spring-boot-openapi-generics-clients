package io.github.blueprintplatform.openapi.generics.server.core.schema.base;

import io.github.blueprintplatform.openapi.generics.server.core.schema.contract.PropertyNames;
import io.github.blueprintplatform.openapi.generics.server.core.schema.contract.SchemaNames;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registers canonical base schemas required by the contract-aware response model.
 *
 * <p>This component ensures that foundational envelope schemas exist before wrapper
 * composition begins.
 *
 * <h2>Responsibilities</h2>
 *
 * <ul>
 *   <li>Register {@code Sort}</li>
 *   <li>Register {@code Meta}</li>
 *   <li>Register {@code ServiceResponse}</li>
 *   <li>Register {@code ServiceResponseVoid}</li>
 * </ul>
 *
 * <h2>Design Principles</h2>
 *
 * <ul>
 *   <li><b>Idempotent</b> → safe to call multiple times</li>
 *   <li><b>Non-invasive</b> → does not override existing schemas</li>
 *   <li><b>Contract-aligned</b> → schema names derived from contract classes</li>
 * </ul>
 *
 * <p>This class operates purely on the OpenAPI model and contains no framework dependencies.
 */
public class BaseSchemaRegistrar {

    private static final String COMPONENTS_SCHEMAS = "#/components/schemas/";

    private static final String FIELD = "field";
    private static final String DIRECTION = "direction";
    private static final String SERVER_TIME = "serverTime";
    private static final String SORT = "sort";

    private static final String FORMAT_DATE_TIME = "date-time";

    private static final List<String> SORT_DIRECTIONS = List.of("asc", "desc");


    /**
     * Registers base schemas into the given OpenAPI document.
     *
     * <p>Ensures all required base schemas are present before wrapper schema generation begins.
     *
     * @param openApi OpenAPI document (must not be {@code null})
     */
    public void register(OpenAPI openApi) {

        ensureComponents(openApi);

        Map<String, Schema> schemas = openApi.getComponents().getSchemas();

        registerSort(schemas);
        registerMeta(schemas);
        registerServiceResponse(schemas);
        registerServiceResponseVoid(schemas);
    }

    // -------------------------------------------------------------------------
    // Base schema registrations
    // -------------------------------------------------------------------------

    /**
     * Registers {@code Sort} schema if absent.
     *
     * @param schemas schema registry map
     */
    private void registerSort(Map<String, Schema> schemas) {
        schemas.computeIfAbsent(
                SchemaNames.SORT,
                key ->
                        new ObjectSchema()
                                .addProperty(FIELD, new StringSchema())
                                .addProperty(DIRECTION, new StringSchema()._enum(SORT_DIRECTIONS)));
    }

    /**
     * Registers {@code Meta} schema if absent.
     *
     * @param schemas schema registry map
     */
    private void registerMeta(Map<String, Schema> schemas) {
        schemas.computeIfAbsent(
                SchemaNames.META,
                key ->
                        new ObjectSchema()
                                .addProperty(SERVER_TIME, new StringSchema().format(FORMAT_DATE_TIME))
                                .addProperty(
                                        SORT,
                                        new ArraySchema()
                                                .items(new Schema<>().$ref(ref(SchemaNames.SORT)))));
    }

    /**
     * Registers {@code ServiceResponse} base schema if absent.
     *
     * @param schemas schema registry map
     */
    private void registerServiceResponse(Map<String, Schema> schemas) {
        schemas.computeIfAbsent(
                SchemaNames.SERVICE_RESPONSE,
                key -> {
                    ObjectSchema schema = new ObjectSchema();

                    schema.addProperty(PropertyNames.DATA, new ObjectSchema());
                    schema.addProperty(
                            PropertyNames.META,
                            new Schema<>().$ref(ref(SchemaNames.META)));

                    schema.setRequired(List.of(PropertyNames.META));

                    return schema;
                });
    }

    /**
     * Registers {@code ServiceResponseVoid} schema if absent.
     *
     * @param schemas schema registry map
     */
    private void registerServiceResponseVoid(Map<String, Schema> schemas) {
        schemas.computeIfAbsent(
                SchemaNames.SERVICE_RESPONSE_VOID,
                key -> {
                    ObjectSchema schema = new ObjectSchema();

                    schema.addProperty(PropertyNames.DATA, new ObjectSchema());
                    schema.addProperty(
                            PropertyNames.META,
                            new Schema<>().$ref(ref(SchemaNames.META)));

                    schema.setRequired(List.of(PropertyNames.META));

                    return schema;
                });
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Ensures {@link Components} and schema map exist on the OpenAPI instance.
     *
     * @param openApi OpenAPI document
     */
    private void ensureComponents(OpenAPI openApi) {
        if (openApi.getComponents() == null) {
            openApi.setComponents(new Components());
        }

        if (openApi.getComponents().getSchemas() == null) {
            openApi.getComponents().setSchemas(new LinkedHashMap<>());
        }
    }

    /**
     * Builds a schema reference path.
     *
     * @param schemaName target schema name
     * @return reference string (e.g. {@code #/components/schemas/SchemaName})
     */
    private String ref(String schemaName) {
        return COMPONENTS_SCHEMAS + schemaName;
    }
}
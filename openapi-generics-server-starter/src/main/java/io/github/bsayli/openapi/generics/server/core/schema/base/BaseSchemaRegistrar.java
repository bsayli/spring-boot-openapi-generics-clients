package io.github.bsayli.openapi.generics.server.core.schema.base;

import io.github.bsayli.openapi.generics.server.core.schema.contract.PropertyNames;
import io.github.bsayli.openapi.generics.server.core.schema.contract.SchemaNames;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registers canonical base schemas required by the contract-aware response model.
 *
 * <p>This component is responsible for ensuring that foundational envelope schemas
 * exist before wrapper composition begins.
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
 * <h2>Why OpenAPI instead of Map?</h2>
 *
 * <ul>
 *   <li>Encapsulates schema access logic</li>
 *   <li>Prevents callers from dealing with null components</li>
 *   <li>Aligns with higher-level orchestration APIs</li>
 * </ul>
 *
 * <p>This class contains no framework dependencies and operates purely on the OpenAPI model.
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
     * <p>This method guarantees that all required base schemas are present
     * before wrapper schema generation begins.
     *
     * @param openApi OpenAPI document (must not be null)
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

    private void registerSort(Map<String, Schema> schemas) {
        schemas.computeIfAbsent(
                SchemaNames.SORT,
                key ->
                        new ObjectSchema()
                                .addProperty(FIELD, new StringSchema())
                                .addProperty(DIRECTION, new StringSchema()._enum(SORT_DIRECTIONS)));
    }

    private void registerMeta(Map<String, Schema> schemas) {
        schemas.computeIfAbsent(
                SchemaNames.META,
                key ->
                        new ObjectSchema()
                                .addProperty(SERVER_TIME, new StringSchema().format(FORMAT_DATE_TIME))
                                .addProperty(
                                        SORT,
                                        new ArraySchema().items(new Schema<>().$ref(ref(SchemaNames.SORT)))));
    }

    private void registerServiceResponse(Map<String, Schema> schemas) {
        schemas.computeIfAbsent(
                SchemaNames.SERVICE_RESPONSE,
                key ->
                        new ObjectSchema()
                                .addProperty(PropertyNames.DATA, new ObjectSchema())
                                .addProperty(
                                        PropertyNames.META,
                                        new Schema<>().$ref(ref(SchemaNames.META))));
    }

    private void registerServiceResponseVoid(Map<String, Schema> schemas) {
        schemas.computeIfAbsent(
                SchemaNames.SERVICE_RESPONSE_VOID,
                key ->
                        new ObjectSchema()
                                .addProperty(PropertyNames.DATA, new ObjectSchema())
                                .addProperty(
                                        PropertyNames.META,
                                        new Schema<>().$ref(ref(SchemaNames.META))));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void ensureComponents(OpenAPI openApi) {
        if (openApi.getComponents() == null) {
            openApi.setComponents(new Components());
        }

        if (openApi.getComponents().getSchemas() == null) {
            openApi.getComponents().setSchemas(new LinkedHashMap<>());
        }
    }

    private String ref(String schemaName) {
        return COMPONENTS_SCHEMAS + schemaName;
    }
}
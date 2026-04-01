package io.github.bsayli.openapi.generics.server.core.schema.base;

import io.github.bsayli.openapi.generics.server.core.schema.contract.SchemaNames;
import io.github.bsayli.openapi.generics.server.core.schema.contract.VendorExtensions;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;

/**
 * Marks canonical base schemas as non-generatable in the OpenAPI document.
 *
 * <p>This component applies the {@code x-ignore-model} vendor extension to a fixed set of
 * infrastructure-level schemas such as:
 *
 * <ul>
 *   <li>{@code ServiceResponse}</li>
 *   <li>{@code ServiceResponseVoid}</li>
 *   <li>{@code Meta}</li>
 *   <li>{@code Sort}</li>
 * </ul>
 *
 * <h2>Purpose</h2>
 *
 * <p>These schemas are part of the shared API contract and are expected to be provided
 * externally (e.g. via a core library). Therefore, they must:
 *
 * <ul>
 *   <li>Exist in the OpenAPI schema registry</li>
 *   <li>Be referenceable by other schemas</li>
 *   <li><b>Not</b> be generated as client-side models</li>
 * </ul>
 *
 * <h2>Execution Context</h2>
 *
 * <ul>
 *   <li>Executed as a pipeline step after schema registration and wrapper processing</li>
 *   <li>Before code generation templates are applied</li>
 * </ul>
 *
 * <h2>Design Characteristics</h2>
 *
 * <ul>
 *   <li><b>Deterministic</b> → always marks the same set of base schemas</li>
 *   <li><b>Idempotent</b> → safe to execute multiple times</li>
 *   <li><b>Non-invasive</b> → does not modify schema structure, only metadata</li>
 * </ul>
 *
 * <h2>Important</h2>
 *
 * <ul>
 *   <li>This class does <b>not</b> create schemas</li>
 *   <li>This class does <b>not</b> perform validation</li>
 *   <li>This class only assigns generation behavior via vendor extensions</li>
 * </ul>
 *
 * <h2>Example</h2>
 *
 * <pre>
 * ServiceResponse:
 *   type: object
 *   x-ignore-model: true
 * </pre>
 *
 * <p>The presence of {@code x-ignore-model} allows templates to skip model generation:
 *
 * <pre>
 * {{^vendorExtensions.x-ignore-model}}
 *   // generate model
 * {{/vendorExtensions.x-ignore-model}}
 * </pre>
 */
public class BaseSchemaIgnoreMarker {

    /**
     * Applies {@code x-ignore-model} to all canonical base schemas.
     *
     * @param openApi the OpenAPI document (must not be null)
     */
    public void mark(OpenAPI openApi) {
        if (openApi == null
                || openApi.getComponents() == null
                || openApi.getComponents().getSchemas() == null) {
            return;
        }

        Map<String, Schema> schemas = openApi.getComponents().getSchemas();

        markIgnore(schemas, SchemaNames.SERVICE_RESPONSE);
        markIgnore(schemas, SchemaNames.SERVICE_RESPONSE_VOID);
        markIgnore(schemas, SchemaNames.META);
        markIgnore(schemas, SchemaNames.SORT);
    }

    /**
     * Marks a schema with {@code x-ignore-model} if present.
     */
    private void markIgnore(Map<String, Schema> schemas, String schemaName) {
        Schema schema = schemas.get(schemaName);
        if (schema != null) {
            schema.addExtension(VendorExtensions.IGNORE_MODEL, true);
        }
    }
}
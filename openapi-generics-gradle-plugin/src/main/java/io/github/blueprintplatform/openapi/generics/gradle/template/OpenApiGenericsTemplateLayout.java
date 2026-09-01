package io.github.blueprintplatform.openapi.generics.gradle.template;

/**
 * Shared paths used by the effective-template preparation pipeline.
 */
public final class OpenApiGenericsTemplateLayout {

    public static final String PREPARE_TASK_NAME =
            "prepareOpenApiGenericsTemplates";

    public static final String TASK_GROUP =
            "OpenAPI Generics";

    public static final String EFFECTIVE_TEMPLATE_DIRECTORY =
            "openapi-generics/templates/effective";

    public static final String UPSTREAM_MODEL_TEMPLATE =
            "Java/model.mustache";

    public static final String OVERLAY_ROOT =
            "META-INF/openapi-generics/templates/";

    private OpenApiGenericsTemplateLayout() {
    }
}

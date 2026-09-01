package io.github.blueprintplatform.openapi.generics.gradle.support;

/**
 * Minimal upstream {@code Java/model.mustache} fragment that matches the
 * OpenAPI Generics patch anchors.
 */
public final class UpstreamJavaModelTemplate {

    public static final String UNPATCHED =
            """
                    {{#models}}
                    {{#model}}
                    {{#isEnum}}
                    ENUM_BODY
                    {{/isEnum}}
                    {{^isEnum}}
                    POJO_BODY
                    {{/isEnum}}
                    {{/model}}
                    {{/models}}
                    """;

    public static final String ALREADY_PATCHED =
            """
                    {{#models}}{{#model}}{{#vendorExtensions.x-api-wrapper}}{{>api_wrapper}}{{/vendorExtensions.x-api-wrapper}}{{^vendorExtensions.x-api-wrapper}}{{#isEnum}}
                    ENUM_BODY
                    {{/isEnum}}{{/vendorExtensions.x-api-wrapper}}{{/model}}{{/models}}
                    """;

    private UpstreamJavaModelTemplate() {}
}

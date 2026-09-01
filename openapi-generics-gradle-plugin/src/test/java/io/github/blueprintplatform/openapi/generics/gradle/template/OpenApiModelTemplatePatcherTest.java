package io.github.blueprintplatform.openapi.generics.gradle.template;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.gradle.exception.TemplatePreparationException;
import io.github.blueprintplatform.openapi.generics.gradle.support.UpstreamJavaModelTemplate;
import org.junit.jupiter.api.Test;

class OpenApiModelTemplatePatcherTest {

    private final OpenApiModelTemplatePatcher patcher = new OpenApiModelTemplatePatcher();

    @Test
    void insertsWrapperDispatchAroundUpstreamModelAnchors() {
        String patched = patcher.patch(UpstreamJavaModelTemplate.UNPATCHED);

        assertTrue(patched.contains("{{#vendorExtensions.x-api-wrapper}}"));
        assertTrue(patched.contains("{{>api_wrapper}}"));
        assertTrue(patched.contains("{{^vendorExtensions.x-api-wrapper}}"));
        assertTrue(patched.contains("ENUM_BODY"));
        assertTrue(patched.contains("POJO_BODY"));
        assertTrue(patched.contains("{{/isEnum}}{{/vendorExtensions.x-api-wrapper}}"));
    }

    @Test
    void rejectsAlreadyPatchedTemplate() {
        assertThrows(
                TemplatePreparationException.class,
                () -> patcher.patch(UpstreamJavaModelTemplate.ALREADY_PATCHED));
    }

    @Test
    void rejectsMissingOpeningAnchor() {
        assertThrows(
                TemplatePreparationException.class,
                () -> patcher.patch("{{#models}}\n{{#model}}\nPOJO\n{{/model}}\n{{/models}}\n"));
    }

    @Test
    void rejectsMissingClosingAnchor() {
        assertThrows(
                TemplatePreparationException.class,
                () ->
                        patcher.patch(
                                """
                                        {{#models}}
                                        {{#model}}
                                        {{#isEnum}}
                                        ENUM_BODY
                                        {{/isEnum}}
                                        {{/model}}
                                        """));
    }

    @Test
    void rejectsAmbiguousOpeningAnchor() {
        String duplicated =
                UpstreamJavaModelTemplate.UNPATCHED + UpstreamJavaModelTemplate.UNPATCHED;

        assertThrows(TemplatePreparationException.class, () -> patcher.patch(duplicated));
    }

    @Test
    void rejectsNullTemplate() {
        assertThrows(NullPointerException.class, () -> patcher.patch(null));
    }
}

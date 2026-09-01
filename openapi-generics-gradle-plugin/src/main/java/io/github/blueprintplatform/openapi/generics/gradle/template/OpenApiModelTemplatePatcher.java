package io.github.blueprintplatform.openapi.generics.gradle.template;

import io.github.blueprintplatform.openapi.generics.gradle.exception.TemplatePreparationException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Applies the OpenAPI Generics wrapper dispatch to the upstream Java model template.
 *
 * <p>The patch preserves the upstream enum, one-of, and POJO dispatch for ordinary models.
 * Models marked with {@code x-api-wrapper=true} are instead rendered through the
 * {@code api_wrapper} partial.
 *
 * <p>The implementation intentionally relies on the single model-dispatch block contained in the
 * upstream Java {@code model.mustache}. If OpenAPI Generator changes that structure, template
 * preparation fails rather than silently producing incorrect generated sources.
 */
public final class OpenApiModelTemplatePatcher {

    private static final String WRAPPER_EXTENSION =
            "vendorExtensions.x-api-wrapper";

    private static final String WRAPPER_PARTIAL =
            "{{>api_wrapper}}";

    private static final Pattern OPENING_ANCHOR =
            mustacheSequence(
                    "{{#models}}",
                    "{{#model}}",
                    "{{#isEnum}}");

    private static final Pattern CLOSING_ANCHOR =
            mustacheSequence(
                    "{{/isEnum}}",
                    "{{/model}}",
                    "{{/models}}");

    private static final String OPENING_REPLACEMENT =
            "{{#models}}{{#model}}"
                    + "{{#vendorExtensions.x-api-wrapper}}"
                    + "{{>api_wrapper}}"
                    + "{{/vendorExtensions.x-api-wrapper}}"
                    + "{{^vendorExtensions.x-api-wrapper}}"
                    + "{{#isEnum}}";

    private static final String CLOSING_REPLACEMENT =
            "{{/isEnum}}"
                    + "{{/vendorExtensions.x-api-wrapper}}"
                    + "{{/model}}{{/models}}";

    public String patch(String upstreamTemplate) {
        Objects.requireNonNull(
                upstreamTemplate,
                "upstreamTemplate must not be null");

        requireUnpatched(upstreamTemplate);

        String patched =
                replaceExactlyOnce(
                        upstreamTemplate,
                        OPENING_ANCHOR,
                        OPENING_REPLACEMENT,
                        "opening model dispatch");

        patched =
                replaceExactlyOnce(
                        patched,
                        CLOSING_ANCHOR,
                        CLOSING_REPLACEMENT,
                        "closing model dispatch");

        verifyPatch(patched);

        return patched;
    }

    private static void requireUnpatched(
            String upstreamTemplate) {

        if (upstreamTemplate.contains(WRAPPER_EXTENSION)
                || upstreamTemplate.contains(WRAPPER_PARTIAL)) {

            throw new TemplatePreparationException(
                    "The upstream Java model template already contains "
                            + "OpenAPI Generics wrapper dispatch.");
        }
    }

    private static String replaceExactlyOnce(
            String input,
            Pattern anchor,
            String replacement,
            String description) {

        Matcher matcher =
                anchor.matcher(input);

        if (!matcher.find()) {
            throw new TemplatePreparationException(
                    "OpenAPI Generator Java model template changed: "
                            + description
                            + " was not found.");
        }

        int start =
                matcher.start();

        int end =
                matcher.end();

        if (matcher.find()) {
            throw new TemplatePreparationException(
                    "OpenAPI Generator Java model template is ambiguous: "
                            + description
                            + " occurred more than once.");
        }

        return input.substring(
                0,
                start)
                + replacement
                + input.substring(
                end);
    }

    private static void verifyPatch(
            String patchedTemplate) {

        if (!patchedTemplate.contains(OPENING_REPLACEMENT)) {
            throw new TemplatePreparationException(
                    "OpenAPI Generics template patch verification failed: "
                            + "the wrapper opening dispatch was not produced.");
        }

        if (!patchedTemplate.contains(CLOSING_REPLACEMENT)) {
            throw new TemplatePreparationException(
                    "OpenAPI Generics template patch verification failed: "
                            + "the wrapper closing dispatch was not produced.");
        }
    }

    private static Pattern mustacheSequence(
            String... tokens) {

        StringBuilder expression =
                new StringBuilder();

        for (int index = 0;
             index < tokens.length;
             index++) {

            if (index > 0) {
                expression.append("\\s*");
            }

            expression.append(
                    Pattern.quote(
                            tokens[index]));
        }

        return Pattern.compile(
                expression.toString());
    }
}
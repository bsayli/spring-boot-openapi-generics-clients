package io.github.blueprintplatform.openapi.generics.gradle.template.io;

import io.github.blueprintplatform.openapi.generics.gradle.exception.TemplatePreparationException;
import io.github.blueprintplatform.openapi.generics.gradle.template.OpenApiGenericsTemplateLayout;
import io.github.blueprintplatform.openapi.generics.gradle.template.OpenApiModelTemplatePatcher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Coordinates preparation of the effective OpenAPI Generics template directory.
 *
 * <p>The pipeline extracts the upstream Java model template, applies the OpenAPI Generics wrapper
 * patch, writes the patched model template, and overlays the templates distributed by the custom
 * codegen artifact.
 */
public final class OpenApiGenericsTemplatePreparer {

    private final TemplateArtifactResourceReader resourceReader;
    private final OpenApiModelTemplatePatcher modelTemplatePatcher;
    private final TemplateOverlayCopier overlayCopier;
    private final TemplateOutputDirectory outputDirectory;

    public OpenApiGenericsTemplatePreparer() {
        this(
                new TemplateArtifactResourceReader(),
                new OpenApiModelTemplatePatcher(),
                new TemplateOverlayCopier(),
                new TemplateOutputDirectory());
    }

    OpenApiGenericsTemplatePreparer(
            TemplateArtifactResourceReader resourceReader,
            OpenApiModelTemplatePatcher modelTemplatePatcher,
            TemplateOverlayCopier overlayCopier,
            TemplateOutputDirectory outputDirectory) {

        this.resourceReader = resourceReader;
        this.modelTemplatePatcher = modelTemplatePatcher;
        this.overlayCopier = overlayCopier;
        this.outputDirectory = outputDirectory;
    }

    public void prepare(
            List<Path> generatorArtifacts,
            List<Path> codegenArtifacts,
            Path effectiveTemplateDirectory) {

        try {
            outputDirectory.recreate(
                    effectiveTemplateDirectory);

            String upstreamTemplate =
                    resourceReader.readUniqueTextResource(
                            generatorArtifacts,
                            OpenApiGenericsTemplateLayout
                                    .UPSTREAM_MODEL_TEMPLATE,
                            true);

            String patchedTemplate =
                    modelTemplatePatcher.patch(
                            upstreamTemplate);

            outputDirectory.writeText(
                    effectiveTemplateDirectory.resolve(
                            OpenApiGenericsTemplateLayout
                                    .UPSTREAM_MODEL_TEMPLATE),
                    patchedTemplate);

            int overlayCount =
                    overlayCopier.copy(
                            codegenArtifacts,
                            OpenApiGenericsTemplateLayout
                                    .OVERLAY_ROOT,
                            effectiveTemplateDirectory.resolve("Java"));

            if (overlayCount == 0) {
                throw new TemplatePreparationException(
                        "No OpenAPI Generics template overlays were found under '"
                                + OpenApiGenericsTemplateLayout.OVERLAY_ROOT
                                + "'.");
            }
        } catch (IOException exception) {
            throw new TemplatePreparationException(
                    "Failed to prepare OpenAPI Generics templates in '"
                            + effectiveTemplateDirectory
                            + "'.",
                    exception);
        }
    }
}
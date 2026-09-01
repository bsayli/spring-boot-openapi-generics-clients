package io.github.blueprintplatform.openapi.generics.gradle.template.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.gradle.exception.TemplatePreparationException;
import io.github.blueprintplatform.openapi.generics.gradle.support.TemplateArtifactFixtures;
import io.github.blueprintplatform.openapi.generics.gradle.support.UpstreamJavaModelTemplate;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OpenApiGenericsTemplatePreparerTest {

    private final OpenApiGenericsTemplatePreparer preparer = new OpenApiGenericsTemplatePreparer();

    @TempDir
    Path tempDir;

    @Test
    void patchesUpstreamModelAndOverlaysWrapperTemplate() throws IOException {
        Path generatorJar =
                TemplateArtifactFixtures.generatorJar(
                        tempDir, UpstreamJavaModelTemplate.UNPATCHED);
        Path codegenJar = TemplateArtifactFixtures.codegenJar(tempDir, "WRAPPER_OVERLAY");
        Path effective = tempDir.resolve("effective");

        preparer.prepare(List.of(generatorJar), List.of(codegenJar), effective);

        String patchedModel =
                Files.readString(effective.resolve("Java/model.mustache"));
        String overlay =
                Files.readString(effective.resolve("Java/api_wrapper.mustache"));

        assertTrue(patchedModel.contains("vendorExtensions.x-api-wrapper"));
        assertTrue(patchedModel.contains("{{>api_wrapper}}"));
        assertEquals("WRAPPER_OVERLAY", overlay);
    }

    @Test
    void failsWhenOverlayIsMissing() throws IOException {
        Path generatorJar =
                TemplateArtifactFixtures.generatorJar(
                        tempDir, UpstreamJavaModelTemplate.UNPATCHED);
        Path emptyCodegen =
                TemplateArtifactFixtures.jar(
                        tempDir.resolve("empty-codegen.jar"),
                        "META-INF/MANIFEST.MF",
                        "Manifest-Version: 1.0\n");

        TemplatePreparationException exception =
                assertThrows(
                        TemplatePreparationException.class,
                        () ->
                                preparer.prepare(
                                        List.of(generatorJar),
                                        List.of(emptyCodegen),
                                        tempDir.resolve("effective")));

        assertTrue(exception.getMessage().contains("No OpenAPI Generics template overlays"));
    }

    @Test
    void failsWhenUpstreamModelTemplateIsMissing() throws IOException {
        Path emptyGenerator =
                TemplateArtifactFixtures.jar(
                        tempDir.resolve("empty-generator.jar"),
                        "README.txt",
                        "no templates");
        Path codegenJar = TemplateArtifactFixtures.codegenJar(tempDir, "WRAPPER");

        assertThrows(
                TemplatePreparationException.class,
                () ->
                        preparer.prepare(
                                List.of(emptyGenerator),
                                List.of(codegenJar),
                                tempDir.resolve("effective")));
    }
}

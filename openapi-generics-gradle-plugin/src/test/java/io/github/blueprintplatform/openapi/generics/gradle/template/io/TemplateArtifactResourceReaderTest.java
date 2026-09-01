package io.github.blueprintplatform.openapi.generics.gradle.template.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.gradle.exception.TemplatePreparationException;
import io.github.blueprintplatform.openapi.generics.gradle.support.TemplateArtifactFixtures;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TemplateArtifactResourceReaderTest {

    private final TemplateArtifactResourceReader reader = new TemplateArtifactResourceReader();

    @TempDir
    Path tempDir;

    @Test
    void readsUniqueResourceFromJarWithSuffixMatch() throws IOException {
        Path jar =
                TemplateArtifactFixtures.generatorJar(
                        tempDir, "UPSTREAM_MODEL");

        String content =
                reader.readUniqueTextResource(
                        List.of(jar),
                        TemplateArtifactFixtures.UPSTREAM_MODEL_TEMPLATE,
                        true);

        assertEquals("UPSTREAM_MODEL", content);
    }

    @Test
    void readsUniqueResourceFromDirectory() throws IOException {
        Path directory =
                TemplateArtifactFixtures.directoryArtifact(
                        tempDir.resolve("exploded"),
                        TemplateArtifactFixtures.UPSTREAM_MODEL_TEMPLATE,
                        "DIRECTORY_MODEL");

        String content =
                reader.readUniqueTextResource(
                        List.of(directory),
                        TemplateArtifactFixtures.UPSTREAM_MODEL_TEMPLATE,
                        false);

        assertEquals("DIRECTORY_MODEL", content);
    }

    @Test
    void failsWhenResourceIsMissing() throws IOException {
        Path emptyJar =
                TemplateArtifactFixtures.jar(
                        tempDir.resolve("empty.jar"),
                        "unrelated.txt",
                        "nope");

        TemplatePreparationException exception =
                assertThrows(
                        TemplatePreparationException.class,
                        () ->
                                reader.readUniqueTextResource(
                                        List.of(emptyJar),
                                        TemplateArtifactFixtures.UPSTREAM_MODEL_TEMPLATE,
                                        true));

        assertTrue(exception.getMessage().contains("was not found"));
    }

    @Test
    void failsWhenResourceIsDuplicated() throws IOException {
        Path first = TemplateArtifactFixtures.generatorJar(tempDir.resolve("one"), "A");
        Path second = TemplateArtifactFixtures.generatorJar(tempDir.resolve("two"), "B");

        TemplatePreparationException exception =
                assertThrows(
                        TemplatePreparationException.class,
                        () ->
                                reader.readUniqueTextResource(
                                        List.of(first, second),
                                        TemplateArtifactFixtures.UPSTREAM_MODEL_TEMPLATE,
                                        true));

        assertTrue(exception.getMessage().contains("more than once"));
    }
}

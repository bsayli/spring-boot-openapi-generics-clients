package io.github.blueprintplatform.openapi.generics.gradle.template.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.gradle.support.TemplateArtifactFixtures;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TemplateOverlayCopierTest {

    private static final String OVERLAY_ROOT = "META-INF/openapi-generics/templates/";

    private final TemplateOverlayCopier copier = new TemplateOverlayCopier();

    @TempDir
    Path tempDir;

    @Test
    void copiesOverlayFromJarIntoJavaTemplateRoot() throws IOException {
        Path jar = TemplateArtifactFixtures.codegenJar(tempDir, "WRAPPER");
        Path destination = tempDir.resolve("effective/Java");

        int copied = copier.copy(List.of(jar), OVERLAY_ROOT, destination);

        assertEquals(1, copied);
        assertEquals(
                "WRAPPER",
                Files.readString(destination.resolve("api_wrapper.mustache")));
    }

    @Test
    void copiesOverlayFromDirectoryArtifact() throws IOException {
        Path directory =
                TemplateArtifactFixtures.directoryArtifact(
                        tempDir.resolve("exploded"),
                        OVERLAY_ROOT + "api_wrapper.mustache",
                        "DIRECTORY_WRAPPER");
        Path destination = tempDir.resolve("effective/Java");

        int copied = copier.copy(List.of(directory), OVERLAY_ROOT, destination);

        assertEquals(1, copied);
        assertEquals(
                "DIRECTORY_WRAPPER",
                Files.readString(destination.resolve("api_wrapper.mustache")));
    }

    @Test
    void returnsZeroWhenOverlayRootIsAbsent() throws IOException {
        Path emptyJar =
                TemplateArtifactFixtures.jar(
                        tempDir.resolve("empty.jar"),
                        "unrelated.txt",
                        "nope");

        int copied =
                copier.copy(
                        List.of(emptyJar),
                        OVERLAY_ROOT,
                        tempDir.resolve("effective/Java"));

        assertEquals(0, copied);
        assertTrue(Files.notExists(tempDir.resolve("effective/Java/api_wrapper.mustache")));
    }
}

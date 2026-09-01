package io.github.blueprintplatform.openapi.generics.gradle.template.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TemplateOutputDirectoryTest {

    private final TemplateOutputDirectory outputDirectory = new TemplateOutputDirectory();

    @TempDir
    Path tempDir;

    @Test
    void recreatesExistingDirectory() throws IOException {
        Path directory = tempDir.resolve("effective");
        Path stale = directory.resolve("stale.txt");
        Files.createDirectories(directory);
        Files.writeString(stale, "old");

        outputDirectory.recreate(directory);

        assertTrue(Files.isDirectory(directory));
        assertTrue(Files.notExists(stale));
    }

    @Test
    void writesTextCreatingParents() throws IOException {
        Path destination = tempDir.resolve("effective/Java/model.mustache");

        outputDirectory.writeText(destination, "PATCHED");

        assertEquals("PATCHED", Files.readString(destination));
    }
}

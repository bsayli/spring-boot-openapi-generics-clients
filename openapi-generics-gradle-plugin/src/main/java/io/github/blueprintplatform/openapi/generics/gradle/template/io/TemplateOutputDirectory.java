package io.github.blueprintplatform.openapi.generics.gradle.template.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * Manages the deterministic output directory produced by template preparation.
 */
public final class TemplateOutputDirectory {

    public void recreate(Path directory)
            throws IOException {

        if (Files.exists(directory)) {
            deleteRecursively(
                    directory);
        }

        Files.createDirectories(
                directory);
    }

    public void writeText(
            Path destination,
            String content)
            throws IOException {

        Files.createDirectories(
                destination.getParent());

        Files.writeString(
                destination,
                content,
                StandardCharsets.UTF_8);
    }

    private void deleteRecursively(Path root)
            throws IOException {

        try (var paths = Files.walk(root)) {
            List<Path> orderedPaths =
                    paths.sorted(
                                    Comparator.reverseOrder())
                            .toList();

            for (Path path : orderedPaths) {
                Files.delete(path);
            }
        }
    }
}
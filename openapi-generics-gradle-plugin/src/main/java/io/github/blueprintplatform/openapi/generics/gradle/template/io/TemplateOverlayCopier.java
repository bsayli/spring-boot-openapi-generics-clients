package io.github.blueprintplatform.openapi.generics.gradle.template.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Copies OpenAPI Generics template overlays from artifact directories and JAR files.
 */
public final class TemplateOverlayCopier {

    public int copy(
            List<Path> artifacts,
            String overlayRootPath,
            Path destinationRoot)
            throws IOException {

        int copiedResourceCount = 0;

        for (Path artifact : artifacts) {
            if (Files.isDirectory(artifact)) {
                copiedResourceCount +=
                        copyFromDirectory(
                                artifact,
                                overlayRootPath,
                                destinationRoot);

                continue;
            }

            if (isJar(artifact)) {
                copiedResourceCount +=
                        copyFromJar(
                                artifact,
                                overlayRootPath,
                                destinationRoot);
            }
        }

        return copiedResourceCount;
    }

    private int copyFromDirectory(
            Path artifactRoot,
            String overlayRootPath,
            Path destinationRoot)
            throws IOException {

        Path overlayRoot =
                artifactRoot.resolve(
                        overlayRootPath);

        if (!Files.isDirectory(overlayRoot)) {
            return 0;
        }

        int copiedResourceCount = 0;

        try (var paths = Files.walk(overlayRoot)) {
            List<Path> files =
                    paths.filter(Files::isRegularFile)
                            .toList();

            for (Path source : files) {
                Path destination =
                        destinationRoot.resolve(
                                overlayRoot.relativize(
                                        source));

                copyFile(
                        source,
                        destination);

                copiedResourceCount++;
            }
        }

        return copiedResourceCount;
    }

    private int copyFromJar(
            Path artifact,
            String overlayRootPath,
            Path destinationRoot)
            throws IOException {

        int copiedResourceCount = 0;

        try (JarFile jarFile =
                     new JarFile(
                             artifact.toFile())) {

            var entries =
                    jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry =
                        entries.nextElement();

                String entryName =
                        entry.getName();

                if (entry.isDirectory()
                        || !entryName.startsWith(
                        overlayRootPath)) {

                    continue;
                }

                String relativePath =
                        entryName.substring(
                                overlayRootPath.length());

                if (relativePath.isBlank()) {
                    continue;
                }

                Path destination =
                        destinationRoot.resolve(
                                relativePath);

                Files.createDirectories(
                        destination.getParent());

                try (InputStream inputStream =
                             jarFile.getInputStream(entry)) {

                    Files.copy(
                            inputStream,
                            destination,
                            StandardCopyOption.REPLACE_EXISTING);
                }

                copiedResourceCount++;
            }
        }

        return copiedResourceCount;
    }

    private void copyFile(
            Path source,
            Path destination)
            throws IOException {

        Files.createDirectories(
                destination.getParent());

        Files.copy(
                source,
                destination,
                StandardCopyOption.REPLACE_EXISTING);
    }

    private boolean isJar(Path artifact) {
        Path fileName =
                artifact.getFileName();

        return fileName != null
                && fileName.toString()
                .endsWith(".jar");
    }
}
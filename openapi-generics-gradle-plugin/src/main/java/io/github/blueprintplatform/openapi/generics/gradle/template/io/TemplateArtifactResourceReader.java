package io.github.blueprintplatform.openapi.generics.gradle.template.io;

import io.github.blueprintplatform.openapi.generics.gradle.exception.TemplatePreparationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Reads template resources from resolved artifact directories and JAR files.
 */
public final class TemplateArtifactResourceReader {

    public String readUniqueTextResource(
            List<Path> artifacts,
            String resourcePath,
            boolean allowSuffixMatch)
            throws IOException {

        List<ResourceContent> matches =
                new ArrayList<>();

        for (Path artifact : artifacts) {
            matches.addAll(
                    findResources(
                            artifact,
                            resourcePath,
                            allowSuffixMatch));
        }

        if (matches.isEmpty()) {
            throw new TemplatePreparationException(
                    "Required template resource '"
                            + resourcePath
                            + "' was not found.");
        }

        if (matches.size() > 1) {
            throw new TemplatePreparationException(
                    "Required template resource '"
                            + resourcePath
                            + "' was found more than once: "
                            + matches.stream()
                            .map(ResourceContent::source)
                            .sorted()
                            .toList());
        }

        return matches.get(0).content();
    }

    private List<ResourceContent> findResources(
            Path artifact,
            String resourcePath,
            boolean allowSuffixMatch)
            throws IOException {

        if (Files.isDirectory(artifact)) {
            return findResourcesInDirectory(
                    artifact,
                    resourcePath,
                    allowSuffixMatch);
        }

        if (!isJar(artifact)) {
            return List.of();
        }

        return findResourcesInJar(
                artifact,
                resourcePath,
                allowSuffixMatch);
    }

    private List<ResourceContent> findResourcesInDirectory(
            Path root,
            String resourcePath,
            boolean allowSuffixMatch)
            throws IOException {

        List<ResourceContent> matches =
                new ArrayList<>();

        try (var paths = Files.walk(root)) {
            List<Path> files =
                    paths.filter(Files::isRegularFile)
                            .toList();

            for (Path file : files) {
                String relativePath =
                        normalizePath(
                                root.relativize(file));

                if (!matchesPath(
                        relativePath,
                        resourcePath,
                        allowSuffixMatch)) {

                    continue;
                }

                matches.add(
                        new ResourceContent(
                                file.toString(),
                                Files.readString(
                                        file,
                                        StandardCharsets.UTF_8)));
            }
        }

        return matches;
    }

    private List<ResourceContent> findResourcesInJar(
            Path artifact,
            String resourcePath,
            boolean allowSuffixMatch)
            throws IOException {

        List<ResourceContent> matches =
                new ArrayList<>();

        try (JarFile jarFile =
                     new JarFile(
                             artifact.toFile())) {

            var entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry =
                        entries.nextElement();

                if (entry.isDirectory()
                        || !matchesPath(
                        entry.getName(),
                        resourcePath,
                        allowSuffixMatch)) {

                    continue;
                }

                try (InputStream inputStream =
                             jarFile.getInputStream(entry)) {

                    matches.add(
                            new ResourceContent(
                                    artifact
                                            + "!"
                                            + entry.getName(),
                                    new String(
                                            inputStream.readAllBytes(),
                                            StandardCharsets.UTF_8)));
                }
            }
        }

        return matches;
    }

    private boolean matchesPath(
            String candidate,
            String expected,
            boolean allowSuffixMatch) {

        if (candidate.equals(expected)) {
            return true;
        }

        return allowSuffixMatch
                && candidate.endsWith(
                "/"
                        + expected);
    }

    private boolean isJar(Path artifact) {
        Path fileName =
                artifact.getFileName();

        return fileName != null
                && fileName.toString()
                .endsWith(".jar");
    }

    private String normalizePath(Path path) {
        return path.toString()
                .replace(
                        '\\',
                        '/');
    }

    private record ResourceContent(
            String source,
            String content) {
    }
}
package io.github.blueprintplatform.openapi.generics.gradle.support;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public final class TemplateArtifactFixtures {

    public static final String UPSTREAM_MODEL_TEMPLATE = "Java/model.mustache";

    public static final String WRAPPER_OVERLAY =
            "META-INF/openapi-generics/templates/api_wrapper.mustache";

    private TemplateArtifactFixtures() {}

    public static Path generatorJar(Path directory, String template)
            throws IOException {

        return jar(
                directory.resolve("openapi-generator.jar"),
                UPSTREAM_MODEL_TEMPLATE,
                template);
    }

    public static Path codegenJar(Path directory, String overlay)
            throws IOException {

        return jar(
                directory.resolve("openapi-generics-java-codegen.jar"),
                WRAPPER_OVERLAY,
                overlay);
    }

    public static Path jar(Path destination, String entryName, String content)
            throws IOException {

        Files.createDirectories(destination.getParent());

        try (OutputStream outputStream = Files.newOutputStream(destination);
                JarOutputStream jarOutputStream = new JarOutputStream(outputStream)) {

            jarOutputStream.putNextEntry(new JarEntry(entryName));
            jarOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
            jarOutputStream.closeEntry();
        }

        return destination;
    }

    public static Path directoryArtifact(Path root, String relativePath, String content)
            throws IOException {

        Path file = root.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return root;
    }
}

package io.github.blueprintplatform.openapi.generics.gradle.dependency;

import io.github.blueprintplatform.openapi.generics.gradle.exception.PluginVersionResolutionException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;

/**
 * Registers and exposes the OpenAPI Generics custom codegen artifact.
 */
public final class OpenApiGenericsCodegenClasspath {

    public static final String OPENAPI_GENERICS_GROUP =
            "io.github.blueprint-platform";

    public static final String OPENAPI_GENERICS_CODEGEN_MODULE =
            "openapi-generics-java-codegen";

    private final Class<?> pluginImplementationType;

    public OpenApiGenericsCodegenClasspath(
            Class<?> pluginImplementationType) {

        this.pluginImplementationType = pluginImplementationType;
    }

    public Dependency register(
            Project project,
            Configuration generatorExtraClasspath) {

        String pluginVersion = resolvePluginVersion();

        Dependency existingDependency =
                generatorExtraClasspath
                        .getDependencies()
                        .stream()
                        .filter(
                                dependency ->
                                        OPENAPI_GENERICS_GROUP.equals(
                                                dependency.getGroup())
                                                && OPENAPI_GENERICS_CODEGEN_MODULE.equals(
                                                dependency.getName())
                                                && pluginVersion.equals(
                                                dependency.getVersion()))
                        .findFirst()
                        .orElse(null);

        if (existingDependency != null) {
            return existingDependency;
        }

        return project.getDependencies()
                .add(
                        generatorExtraClasspath.getName(),
                        "%s:%s:%s"
                                .formatted(
                                        OPENAPI_GENERICS_GROUP,
                                        OPENAPI_GENERICS_CODEGEN_MODULE,
                                        pluginVersion));
    }

    private String resolvePluginVersion() {
        String implementationVersion =
                pluginImplementationType
                        .getPackage()
                        .getImplementationVersion();

        if (implementationVersion == null
                || implementationVersion.isBlank()) {

            throw new PluginVersionResolutionException(
                    "OpenAPI Generics Gradle Plugin",
                    pluginImplementationType);
        }

        return implementationVersion;
    }
}

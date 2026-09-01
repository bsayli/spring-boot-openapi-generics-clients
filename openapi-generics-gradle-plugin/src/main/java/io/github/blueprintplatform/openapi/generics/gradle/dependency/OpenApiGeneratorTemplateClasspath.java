package io.github.blueprintplatform.openapi.generics.gradle.dependency;

import io.github.blueprintplatform.openapi.generics.gradle.exception.PluginVersionResolutionException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates the private classpath used to read the upstream Java model template.
 *
 * <p>The configuration is resolvable because template preparation consumes the OpenAPI Generator
 * artifact. It is not consumable because it does not expose a project variant to other builds or
 * projects.
 *
 * <p>The OpenAPI Generator version is resolved from the running official Gradle plugin artifact.
 * Manifest metadata is preferred when available. When the artifact does not publish an
 * {@code Implementation-Version}, the version is extracted from the official plugin JAR name.
 */
public final class OpenApiGeneratorTemplateClasspath {

    public static final String CONFIGURATION_NAME =
            "openApiGenericsTemplateTooling";

    private static final String OPENAPI_GENERATOR_GROUP =
            "org.openapitools";

    private static final String OPENAPI_GENERATOR_MODULE =
            "openapi-generator";

    private static final String OPENAPI_GENERATOR_GRADLE_PLUGIN_MODULE =
            "openapi-generator-gradle-plugin";

    private static final Pattern GRADLE_PLUGIN_JAR_PATTERN =
            Pattern.compile(
                    "^"
                            + Pattern.quote(
                            OPENAPI_GENERATOR_GRADLE_PLUGIN_MODULE)
                            + "-(.+)\\.jar$");

    public Configuration create(Project project) {
        String generatorVersion =
                resolveGeneratorVersion();

        Configuration configuration =
                project.getConfigurations()
                        .maybeCreate(CONFIGURATION_NAME);

        configureClasspath(configuration);

        registerGeneratorDependency(
                project,
                configuration,
                generatorVersion);

        return configuration;
    }

    private void configureClasspath(
            Configuration configuration) {

        configuration.setCanBeResolved(true);
        configuration.setCanBeConsumed(false);
        configuration.setDescription(
                "Resolves the OpenAPI Generator artifact used to prepare "
                        + "OpenAPI Generics templates.");
    }

    private void registerGeneratorDependency(
            Project project,
            Configuration configuration,
            String generatorVersion) {

        boolean dependencyPresent =
                configuration.getDependencies()
                        .stream()
                        .anyMatch(
                                dependency ->
                                        matchesGeneratorDependency(
                                                dependency,
                                                generatorVersion));

        if (dependencyPresent) {
            return;
        }

        project.getDependencies()
                .add(
                        configuration.getName(),
                        generatorCoordinates(
                                generatorVersion));
    }

    private boolean matchesGeneratorDependency(
            Dependency dependency,
            String generatorVersion) {

        return OPENAPI_GENERATOR_GROUP.equals(
                dependency.getGroup())
                && OPENAPI_GENERATOR_MODULE.equals(
                dependency.getName())
                && generatorVersion.equals(
                dependency.getVersion());
    }

    private String generatorCoordinates(
            String generatorVersion) {

        return "%s:%s:%s"
                .formatted(
                        OPENAPI_GENERATOR_GROUP,
                        OPENAPI_GENERATOR_MODULE,
                        generatorVersion);
    }

    private String resolveGeneratorVersion() {
        String implementationVersion =
                GenerateTask.class
                        .getPackage()
                        .getImplementationVersion();

        if (implementationVersion != null
                && !implementationVersion.isBlank()) {

            return implementationVersion;
        }

        String artifactVersion =
                resolveVersionFromArtifactLocation();

        if (artifactVersion != null
                && !artifactVersion.isBlank()) {

            return artifactVersion;
        }

        throw new PluginVersionResolutionException(
                "OpenAPI Generator Gradle Plugin",
                GenerateTask.class);
    }

    private String resolveVersionFromArtifactLocation() {
        CodeSource codeSource =
                GenerateTask.class
                        .getProtectionDomain()
                        .getCodeSource();

        if (codeSource == null) {
            return null;
        }

        URL location =
                codeSource.getLocation();

        if (location == null
                || !"file".equalsIgnoreCase(
                location.getProtocol())) {

            return null;
        }

        try {
            URI locationUri =
                    location.toURI();

            Path artifactPath =
                    Path.of(locationUri);

            Path fileName =
                    artifactPath.getFileName();

            if (fileName == null) {
                return null;
            }

            Matcher matcher =
                    GRADLE_PLUGIN_JAR_PATTERN.matcher(
                            fileName.toString());

            if (!matcher.matches()) {
                return null;
            }

            return matcher.group(1);
        } catch (URISyntaxException
                 | IllegalArgumentException exception) {

            return null;
        }
    }
}
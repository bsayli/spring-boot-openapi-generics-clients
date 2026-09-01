package io.github.blueprintplatform.openapi.generics.gradle;

import io.github.blueprintplatform.openapi.generics.gradle.dependency.OpenApiGeneratorTemplateClasspath;
import io.github.blueprintplatform.openapi.generics.gradle.dependency.OpenApiGenericsCodegenClasspath;
import io.github.blueprintplatform.openapi.generics.gradle.generation.OpenApiGenericsGenerateTaskConfigurer;
import io.github.blueprintplatform.openapi.generics.gradle.generation.OpenApiGenericsGeneratedSourceRegistrar;
import io.github.blueprintplatform.openapi.generics.gradle.generation.OpenApiGenericsSourceLayout;
import io.github.blueprintplatform.openapi.generics.gradle.integration.OpenApiGeneratorCompatibility;
import io.github.blueprintplatform.openapi.generics.gradle.integration.OpenApiGeneratorIntegration;
import io.github.blueprintplatform.openapi.generics.gradle.template.OpenApiGenericsTemplateLifecycle;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jspecify.annotations.NonNull;

/**
 * Entry point for OpenAPI Generics client generation on Gradle.
 *
 * <p>The plugin augments the official {@code org.openapi.generator} plugin. Generator selection,
 * specifications, packages, libraries, and ordinary generator options remain consumer-owned.
 */
public final class OpenApiGenericsGradlePlugin implements Plugin<Project> {

    static final String OPENAPI_GENERATOR_PLUGIN_ID = "org.openapi.generator";

    @Override
    public void apply(@NonNull Project project) {
        OpenApiGenericsTemplateLifecycle templateLifecycle =
                new OpenApiGenericsTemplateLifecycle(
                        new OpenApiGeneratorTemplateClasspath(),
                        new OpenApiGenericsCodegenClasspath(
                                OpenApiGenericsGradlePlugin.class));

        OpenApiGenericsGenerateTaskConfigurer taskConfigurer =
                new OpenApiGenericsGenerateTaskConfigurer(
                        new OpenApiGenericsGeneratedSourceRegistrar(
                                new OpenApiGenericsSourceLayout()));

        OpenApiGeneratorIntegration integration =
                new OpenApiGeneratorIntegration(
                        new OpenApiGeneratorCompatibility(),
                        templateLifecycle,
                        taskConfigurer);

        project.getPluginManager()
                .withPlugin(
                        OPENAPI_GENERATOR_PLUGIN_ID,
                        ignored -> integration.install(project));
    }
}

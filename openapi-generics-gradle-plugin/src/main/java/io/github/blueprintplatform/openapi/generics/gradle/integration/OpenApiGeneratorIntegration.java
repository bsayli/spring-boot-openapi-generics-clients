package io.github.blueprintplatform.openapi.generics.gradle.integration;

import io.github.blueprintplatform.openapi.generics.gradle.generation.OpenApiGenericsGenerateTaskConfigurer;
import io.github.blueprintplatform.openapi.generics.gradle.template.OpenApiGenericsTemplateLifecycle;
import io.github.blueprintplatform.openapi.generics.gradle.template.OpenApiGenericsTemplateRuntime;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask;

/**
 * Installs OpenAPI Generics behavior after the official OpenAPI Generator Gradle plugin is present.
 */
public final class OpenApiGeneratorIntegration {

    private final OpenApiGeneratorCompatibility compatibility;
    private final OpenApiGenericsTemplateLifecycle templateLifecycle;
    private final OpenApiGenericsGenerateTaskConfigurer taskConfigurer;

    public OpenApiGeneratorIntegration(
            OpenApiGeneratorCompatibility compatibility,
            OpenApiGenericsTemplateLifecycle templateLifecycle,
            OpenApiGenericsGenerateTaskConfigurer taskConfigurer) {

        this.compatibility = compatibility;
        this.templateLifecycle = templateLifecycle;
        this.taskConfigurer = taskConfigurer;
    }

    public void install(Project project) {
        Configuration generatorRuntimeClasspath =
                compatibility.requireGeneratorExtraConfiguration(project);

        OpenApiGenericsTemplateRuntime templateRuntime =
                templateLifecycle.register(
                        project,
                        generatorRuntimeClasspath);

        /*
         * Project-level Java compilation wiring must be installed before entering
         * GenerateTask.configureEach callbacks. Gradle 9.x mutation guards reject configuring
         * another task provider from inside a task-container configuration callback.
         */
        taskConfigurer.install(project);

        project.getTasks()
                .withType(GenerateTask.class)
                .configureEach(
                        task ->
                                taskConfigurer.configure(
                                        project,
                                        task,
                                        templateRuntime,
                                        generatorRuntimeClasspath));

        project.getLogger()
                .info(
                        "OpenAPI Generics integration installed with template task '{}'.",
                        templateRuntime.preparationTask().getName());
    }
}

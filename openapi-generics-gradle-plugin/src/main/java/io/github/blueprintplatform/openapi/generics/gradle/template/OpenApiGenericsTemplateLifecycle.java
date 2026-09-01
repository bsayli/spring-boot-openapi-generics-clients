package io.github.blueprintplatform.openapi.generics.gradle.template;

import io.github.blueprintplatform.openapi.generics.gradle.dependency.OpenApiGeneratorTemplateClasspath;
import io.github.blueprintplatform.openapi.generics.gradle.dependency.OpenApiGenericsCodegenClasspath;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

/**
 * Registers the single project-level effective-template preparation pipeline.
 */
public final class OpenApiGenericsTemplateLifecycle {

    private final OpenApiGeneratorTemplateClasspath templateClasspath;
    private final OpenApiGenericsCodegenClasspath codegenClasspath;

    public OpenApiGenericsTemplateLifecycle(
            OpenApiGeneratorTemplateClasspath templateClasspath,
            OpenApiGenericsCodegenClasspath codegenClasspath) {

        this.templateClasspath = templateClasspath;
        this.codegenClasspath = codegenClasspath;
    }

    public OpenApiGenericsTemplateRuntime register(
            Project project,
            Configuration generatorExtraClasspath) {

        Configuration upstreamTemplates =
                templateClasspath.create(project);

        Dependency codegenDependency =
                codegenClasspath.register(
                        project,
                        generatorExtraClasspath);

        Provider<Directory> effectiveTemplates =
                project.getLayout()
                        .getBuildDirectory()
                        .dir(
                                OpenApiGenericsTemplateLayout
                                        .EFFECTIVE_TEMPLATE_DIRECTORY);

        TaskProvider<PrepareOpenApiGenericsTemplates> preparationTask =
                project.getTasks()
                        .register(
                                OpenApiGenericsTemplateLayout
                                        .PREPARE_TASK_NAME,
                                PrepareOpenApiGenericsTemplates.class,
                                task -> {
                                    task.setGroup(
                                            OpenApiGenericsTemplateLayout
                                                    .TASK_GROUP);

                                    task.setDescription(
                                            "Prepares patched and overlaid templates for OpenAPI Generics.");

                                    task.getOpenApiGeneratorArtifacts()
                                            .from(upstreamTemplates);

                                    task.getOpenApiGenericsCodegenArtifacts()
                                            .from(generatorExtraClasspath);

                                    task.getOutputDirectory()
                                            .set(effectiveTemplates);
                                });

        project.getLogger()
                .info(
                        "OpenAPI Generics registered codegen dependency '{}:{}:{}'.",
                        codegenDependency.getGroup(),
                        codegenDependency.getName(),
                        codegenDependency.getVersion());

        return new OpenApiGenericsTemplateRuntime(
                preparationTask,
                effectiveTemplates);
    }
}

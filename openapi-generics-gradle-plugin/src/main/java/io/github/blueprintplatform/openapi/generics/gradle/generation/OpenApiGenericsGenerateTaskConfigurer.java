package io.github.blueprintplatform.openapi.generics.gradle.generation;

import io.github.blueprintplatform.openapi.generics.gradle.template.OpenApiGenericsTemplateRuntime;
import java.util.concurrent.Callable;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask;

/**
 * Adds OpenAPI Generics-owned template, runtime classpath, and source lifecycle wiring to official
 * generation tasks.
 *
 * <p>The configurer never assigns or overrides {@code generatorName}. Generator selection remains
 * an explicit consumer decision.
 */
public final class OpenApiGenericsGenerateTaskConfigurer {

    private final OpenApiGenericsGeneratedSourceRegistrar generatedSourceRegistrar;

    public OpenApiGenericsGenerateTaskConfigurer(
            OpenApiGenericsGeneratedSourceRegistrar generatedSourceRegistrar) {

        this.generatedSourceRegistrar = generatedSourceRegistrar;
    }

    /**
     * Installs project-level Java compilation wiring before GenerateTask configuration callbacks
     * begin.
     */
    public void install(Project project) {
        generatedSourceRegistrar.install(project);
    }

    public void configure(
            Project project,
            GenerateTask task,
            OpenApiGenericsTemplateRuntime templateRuntime,
            FileCollection generatorRuntimeClasspath) {

        OpenApiGenericsTaskSelection selection =
                OpenApiGenericsTaskSelection.from(task);

        Provider<Directory> effectiveJavaTemplateDirectory =
                selection.isSelected()
                        .flatMap(
                                selected ->
                                        selected
                                                ? templateRuntime
                                                        .effectiveTemplateDirectory()
                                                        .map(directory -> directory.dir("Java"))
                                                : project.getProviders()
                                                        .provider(() -> null));

        Provider<FileCollection> selectedGeneratorRuntimeClasspath =
                selection.isSelected()
                        .map(
                                selected ->
                                        selected
                                                ? generatorRuntimeClasspath
                                                : project.files());

        /*
         * The official OpenAPI Generator plugin has already connected
         * GenerateTask.templateDir with its extension property using set(provider).
         * A convention added here therefore does not reliably become the effective
         * value. Selected OpenAPI Generics tasks must explicitly use the prepared
         * Java template root.
         */
        task.getTemplateDir()
                .set(effectiveJavaTemplateDirectory);

        /*
         * Custom GenerateTask instances do not inherit the default openApiGenerate
         * task's generator worker classpath. The selected task therefore receives
         * the official openApiGeneratorExtra configuration explicitly.
         *
         * The provider remains lazy so generatorName can still be configured by
         * the consumer after this callback runs.
         */
        task.getGeneratorClasspath()
                .from(selectedGeneratorRuntimeClasspath);

        task.dependsOn(
                (Callable<Object>)
                        () ->
                                selection.isSelected().get()
                                        ? templateRuntime.preparationTask()
                                        : null);

        generatedSourceRegistrar.register(
                project,
                task,
                selection);

        task.doFirst(
                ignored -> {
                    reportSelection(
                            task,
                            selection);

                    if (selection.isSelected().get()) {
                        task.getLogger()
                                .lifecycle(
                                        "OpenAPI Generics effective templateDir for '{}': {}",
                                        task.getPath(),
                                        task.getTemplateDir()
                                                .get()
                                                .getAsFile()
                                                .getAbsolutePath());
                    }
                });
    }

    private void reportSelection(
            GenerateTask task,
            OpenApiGenericsTaskSelection selection) {

        if (selection.isSelected().get()) {
            task.getLogger()
                    .info(
                            "OpenAPI Generics selected GenerateTask '{}' "
                                    + "with generatorName '{}'.",
                            selection.getTaskPath(),
                            selection.getDisplayGeneratorName());

            return;
        }

        task.getLogger()
                .info(
                        "OpenAPI Generics skipped GenerateTask '{}' "
                                + "because generatorName is '{}'.",
                        selection.getTaskPath(),
                        selection.getDisplayGeneratorName());
    }
}

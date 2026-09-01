package io.github.blueprintplatform.openapi.generics.gradle.generation;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Registers OpenAPI Generics generated Java sources with the main Java source set and installs the
 * Java compilation dependency on selected generation tasks.
 *
 * <p>Both the compilation dependency and the source-directory contribution are installed once at
 * project integration time. They are resolved lazily after consumer task configuration, so
 * generator selection remains consumer-owned.
 *
 * <p>Per-task {@code GenerateTask.configureEach} callbacks must not mutate the Java source set.
 * Gradle 9.x snapshots or rejects that mutation.
 *
 * <p>Projects that do not apply the Java plugin can still use generation without source-set
 * integration.
 */
public final class OpenApiGenericsGeneratedSourceRegistrar {

    private final OpenApiGenericsSourceLayout sourceLayout;

    public OpenApiGenericsGeneratedSourceRegistrar(
            OpenApiGenericsSourceLayout sourceLayout) {

        this.sourceLayout = sourceLayout;
    }

    /**
     * Installs Java compilation and source-set wiring once, outside GenerateTask configuration
     * callbacks.
     *
     * <p>This method must be called from project integration setup, not from a
     * {@code GenerateTask.configureEach(...)} action.
     */
    public void install(Project project) {
        project.getPluginManager()
                .withPlugin(
                        "java",
                        ignored ->
                                installJavaIntegration(
                                        project));
    }

    /**
     * Per-task hook invoked from GenerateTask configuration.
     *
     * <p>Source-set mutation is intentionally not performed here.
     */
    public void register(
            Project project,
            GenerateTask task,
            OpenApiGenericsTaskSelection selection) {
    }

    private void installJavaIntegration(
            Project project) {

        JavaPluginExtension javaPlugin =
                project.getExtensions()
                        .getByType(
                                JavaPluginExtension.class);

        SourceSet mainSourceSet =
                javaPlugin.getSourceSets()
                        .getByName(
                                SourceSet.MAIN_SOURCE_SET_NAME);

        project.getTasks()
                .named(
                        mainSourceSet.getCompileJavaTaskName())
                .configure(
                        compileJava ->
                                compileJava.dependsOn(
                                        (Callable<Object>)
                                                () ->
                                                        selectedGenerateTasks(
                                                                project)));

        mainSourceSet
                .getJava()
                .srcDirs(
                        project.getProviders()
                                .provider(
                                        () ->
                                                selectedGeneratedDirectories(
                                                        project)));
    }

    private List<GenerateTask> selectedGenerateTasks(
            Project project) {

        return project.getTasks()
                .withType(
                        GenerateTask.class)
                .stream()
                .filter(
                        task ->
                                OpenApiGenericsTaskSelection
                                        .from(task)
                                        .isSelected()
                                        .get())
                .toList();
    }

    private List<File> selectedGeneratedDirectories(
            Project project) {

        List<File> directories =
                new ArrayList<>();

        for (GenerateTask task :
                selectedGenerateTasks(
                        project)) {

            File directory =
                    sourceLayout
                            .generatedJavaDirectory(
                                    project,
                                    task,
                                    OpenApiGenericsTaskSelection
                                            .from(task))
                            .getOrNull();

            if (directory != null) {
                directories.add(
                        directory);
            }
        }

        return directories;
    }
}

package io.github.blueprintplatform.openapi.generics.gradle.generation;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask;

import java.io.File;
import java.util.Map;

/**
 * Derives the generated Java source root from official GenerateTask properties.
 */
public final class OpenApiGenericsSourceLayout {

    public static final String SOURCE_FOLDER_OPTION =
            "sourceFolder";

    public static final String DEFAULT_JAVA_SOURCE_FOLDER =
            "src/main/java";

    public Provider<File> generatedJavaDirectory(
            Project project,
            GenerateTask task,
            OpenApiGenericsTaskSelection selection) {

        Provider<File> directory =
                task.getOutputDir()
                        .zip(
                                task.getConfigOptions(),
                                (outputDirectory, options) ->
                                        new File(
                                                project.file(
                                                        outputDirectory),
                                                sourceFolder(options)));

        return selection.isSelected()
                .flatMap(
                        selected ->
                                selected
                                        ? directory
                                        : project.getProviders()
                                        .provider(
                                                () -> null));
    }

    private String sourceFolder(
            Map<String, String> configOptions) {

        return configOptions.getOrDefault(
                SOURCE_FOLDER_OPTION,
                DEFAULT_JAVA_SOURCE_FOLDER);
    }
}

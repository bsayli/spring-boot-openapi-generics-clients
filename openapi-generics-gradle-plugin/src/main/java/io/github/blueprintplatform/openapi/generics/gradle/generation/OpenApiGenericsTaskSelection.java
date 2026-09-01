package io.github.blueprintplatform.openapi.generics.gradle.generation;

import org.gradle.api.provider.Provider;
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask;

/**
 * Represents the lazy OpenAPI Generics selection state of an official generation task.
 */
public final class OpenApiGenericsTaskSelection {

    public static final String OPENAPI_GENERICS_GENERATOR_NAME =
            "java-generics-contract";

    private static final String MISSING_GENERATOR_NAME =
            "<not configured>";

    private final String taskPath;
    private final Provider<String> generatorName;
    private final Provider<Boolean> selected;

    private OpenApiGenericsTaskSelection(
            String taskPath,
            Provider<String> generatorName) {

        this.taskPath = taskPath;
        this.generatorName = generatorName;
        this.selected =
                generatorName
                        .map(
                                OPENAPI_GENERICS_GENERATOR_NAME::equals)
                        .orElse(false);
    }

    public static OpenApiGenericsTaskSelection from(
            GenerateTask task) {

        return new OpenApiGenericsTaskSelection(
                task.getPath(),
                task.getGeneratorName());
    }

    public String getTaskPath() {
        return taskPath;
    }

    public Provider<Boolean> isSelected() {
        return selected;
    }

    public Provider<String> getGeneratorName() {
        return generatorName;
    }

    public String getDisplayGeneratorName() {
        return generatorName.getOrElse(
                MISSING_GENERATOR_NAME);
    }
}

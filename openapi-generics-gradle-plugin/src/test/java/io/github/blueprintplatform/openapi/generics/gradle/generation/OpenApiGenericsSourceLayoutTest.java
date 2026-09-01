package io.github.blueprintplatform.openapi.generics.gradle.generation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.util.Map;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask;

class OpenApiGenericsSourceLayoutTest {

    @Test
    void derivesConfiguredSourceFolder() {
        Project project = createProject();
        GenerateTask task = selectedTask(project, "generated/client", "src/gen/java");

        File generatedJavaDirectory =
                new OpenApiGenericsSourceLayout()
                        .generatedJavaDirectory(
                                project, task, OpenApiGenericsTaskSelection.from(task))
                        .get();

        assertEquals(
                project.getLayout()
                        .getBuildDirectory()
                        .dir("generated/client/src/gen/java")
                        .get()
                        .getAsFile(),
                generatedJavaDirectory);
    }

    @Test
    void usesDefaultJavaSourceFolderWhenUnconfigured() {
        Project project = createProject();
        GenerateTask task = selectedTask(project, "generated/client", null);

        File generatedJavaDirectory =
                new OpenApiGenericsSourceLayout()
                        .generatedJavaDirectory(
                                project, task, OpenApiGenericsTaskSelection.from(task))
                        .get();

        assertEquals(
                project.getLayout()
                        .getBuildDirectory()
                        .dir("generated/client/" + OpenApiGenericsSourceLayout.DEFAULT_JAVA_SOURCE_FOLDER)
                        .get()
                        .getAsFile(),
                generatedJavaDirectory);
    }

    @Test
    void returnsAbsentProviderForAnotherGenerator() {
        Project project = createProject();
        GenerateTask task = createGenerateTask(project, "generateKotlin");
        task.getGeneratorName().set("kotlin");
        task.getOutputDir()
                .set(
                        project.getLayout()
                                .getBuildDirectory()
                                .dir("generated/kotlin")
                                .get()
                                .getAsFile());
        task.getConfigOptions().set(Map.of());

        assertFalse(
                new OpenApiGenericsSourceLayout()
                        .generatedJavaDirectory(
                                project, task, OpenApiGenericsTaskSelection.from(task))
                        .isPresent());
    }

    private GenerateTask selectedTask(
            Project project, String outputDirectory, String sourceFolder) {

        GenerateTask task = createGenerateTask(project, "generateClient");
        task.getGeneratorName()
                .set(OpenApiGenericsTaskSelection.OPENAPI_GENERICS_GENERATOR_NAME);
        task.getOutputDir()
                .set(
                        project.getLayout()
                                .getBuildDirectory()
                                .dir(outputDirectory)
                                .get()
                                .getAsFile());
        task.getConfigOptions()
                .set(
                        sourceFolder == null
                                ? Map.of()
                                : Map.of(
                                        OpenApiGenericsSourceLayout.SOURCE_FOLDER_OPTION,
                                        sourceFolder));
        return task;
    }

    private Project createProject() {
        return ProjectBuilder.builder().build();
    }

    private GenerateTask createGenerateTask(Project project, String taskName) {
        return project.getTasks().register(taskName, GenerateTask.class).get();
    }
}

package io.github.blueprintplatform.openapi.generics.gradle.generation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.gradle.template.OpenApiGenericsTemplateRuntime;
import io.github.blueprintplatform.openapi.generics.gradle.template.PrepareOpenApiGenericsTemplates;
import java.io.File;
import java.io.IOException;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask;

class OpenApiGenericsGenerateTaskConfigurerTest {

    @TempDir
    File temporaryDirectory;

    @Test
    void addsGeneratorRuntimeClasspathAfterTaskBecomesSelected() throws IOException {
        Project project = ProjectBuilder.builder().build();
        GenerateTask task = createGenerateTask(project, "generateClient");
        File codegenJar = createCodegenJar();
        FileCollection runtimeClasspath = project.files(codegenJar);

        configure(project, task, runtimeClasspath);

        task.getGeneratorName()
                .set(OpenApiGenericsTaskSelection.OPENAPI_GENERICS_GENERATOR_NAME);

        assertTrue(task.getGeneratorClasspath().getFiles().contains(codegenJar));
    }

    @Test
    void doesNotAddGeneratorRuntimeClasspathToAnotherGenerator() throws IOException {
        Project project = ProjectBuilder.builder().build();
        GenerateTask task = createGenerateTask(project, "generateKotlin");
        File codegenJar = createCodegenJar();
        FileCollection runtimeClasspath = project.files(codegenJar);

        configure(project, task, runtimeClasspath);

        task.getGeneratorName().set("kotlin");

        assertFalse(task.getGeneratorClasspath().getFiles().contains(codegenJar));
    }

    private void configure(
            Project project,
            GenerateTask task,
            FileCollection runtimeClasspath) {

        OpenApiGenericsGenerateTaskConfigurer configurer =
                new OpenApiGenericsGenerateTaskConfigurer(
                        new OpenApiGenericsGeneratedSourceRegistrar(
                                new OpenApiGenericsSourceLayout()));

        configurer.configure(
                project,
                task,
                templateRuntime(project),
                runtimeClasspath);
    }

    private OpenApiGenericsTemplateRuntime templateRuntime(Project project) {
        TaskProvider<PrepareOpenApiGenericsTemplates> preparationTask =
                project.getTasks()
                        .register(
                                "prepareOpenApiGenericsTemplates",
                                PrepareOpenApiGenericsTemplates.class);

        return new OpenApiGenericsTemplateRuntime(
                preparationTask,
                project.getLayout()
                        .getBuildDirectory()
                        .dir("openapi-generics/templates/effective"));
    }

    private GenerateTask createGenerateTask(Project project, String name) {
        return project.getTasks()
                .register(name, GenerateTask.class)
                .get();
    }

    private File createCodegenJar() throws IOException {
        File codegenJar =
                new File(
                        temporaryDirectory,
                        "openapi-generics-java-codegen-test.jar");

        assertTrue(codegenJar.createNewFile());
        return codegenJar;
    }
}

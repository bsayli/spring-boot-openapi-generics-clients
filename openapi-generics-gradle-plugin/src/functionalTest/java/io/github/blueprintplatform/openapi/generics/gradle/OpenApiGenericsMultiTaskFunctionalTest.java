package io.github.blueprintplatform.openapi.generics.gradle;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.gradle.support.ConsumerBuildFiles;
import io.github.blueprintplatform.openapi.generics.gradle.support.FunctionalTestEnvironment;
import java.io.IOException;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies plugin wiring when one consumer project declares more than one
 * {@code java-generics-contract} generation task.
 */
class OpenApiGenericsMultiTaskFunctionalTest {

    @TempDir
    Path projectDirectory;

    private FunctionalTestEnvironment environment;

    @BeforeEach
    void prepare() throws IOException {
        environment = FunctionalTestEnvironment.create(projectDirectory);
        environment.writeMultipleGenericsTasksConsumer();
    }

    @Test
    void wiresSharedTemplatePreparationToEverySelectedTask() {
        BuildResult result =
                environment.run(ConsumerBuildFiles.PRINT_TEMPLATE_DEPENDENCIES);

        assertTrue(
                result.getOutput()
                        .contains(
                                "TEMPLATE_DEPENDENCY_"
                                        + ConsumerBuildFiles.CUSTOMER_GENERATE_TASK
                                        + "=true"),
                result::getOutput);

        assertTrue(
                result.getOutput()
                        .contains(
                                "TEMPLATE_DEPENDENCY_"
                                        + ConsumerBuildFiles.SERVICE_RESPONSE_GENERATE_TASK
                                        + "=true"),
                result::getOutput);
    }

    @Test
    void assignsTheEffectiveTemplateDirectoryToEverySelectedTask() {
        BuildResult result =
                environment.run(ConsumerBuildFiles.PRINT_TEMPLATE_DIRS);

        assertSelectedTaskUsesEffectiveTemplates(
                result,
                ConsumerBuildFiles.CUSTOMER_GENERATE_TASK);

        assertSelectedTaskUsesEffectiveTemplates(
                result,
                ConsumerBuildFiles.SERVICE_RESPONSE_GENERATE_TASK);
    }


    @Test
    void addsGeneratorRuntimeClasspathToEverySelectedTask() {
        BuildResult result =
                environment.run(ConsumerBuildFiles.PRINT_GENERATOR_CLASSPATHS);

        assertSelectedTaskHasCodegenRuntime(
                result,
                ConsumerBuildFiles.CUSTOMER_GENERATE_TASK);

        assertSelectedTaskHasCodegenRuntime(
                result,
                ConsumerBuildFiles.SERVICE_RESPONSE_GENERATE_TASK);
    }

    @Test
    void registersEverySelectedGeneratedSourceRoot() {
        BuildResult result =
                environment.run(ConsumerBuildFiles.PRINT_MAIN_JAVA_SRC_DIRS);

        assertGeneratedSourceRegistered(
                result,
                "generated/customer");

        assertGeneratedSourceRegistered(
                result,
                "generated/service-response");
    }

    @Test
    void makesCompileJavaDependOnEverySelectedGenerateTask() {
        BuildResult result =
                environment.run("compileJava", "--dry-run");

        assertTrue(
                result.getOutput()
                        .contains(
                                ":" + ConsumerBuildFiles.CUSTOMER_GENERATE_TASK),
                result::getOutput);

        assertTrue(
                result.getOutput()
                        .contains(
                                ":"
                                        + ConsumerBuildFiles
                                                .SERVICE_RESPONSE_GENERATE_TASK),
                result::getOutput);

        assertTrue(
                result.getOutput()
                        .contains(":prepareOpenApiGenericsTemplates"),
                result::getOutput);
    }


    private static void assertSelectedTaskHasCodegenRuntime(
            BuildResult result,
            String taskName) {

        assertTrue(
                result.getOutput()
                        .lines()
                        .anyMatch(
                                line ->
                                        line.startsWith(
                                                        "GENERATOR_CLASSPATH_"
                                                                + taskName
                                                                + "=")
                                                && line.contains(
                                                        "openapi-generics-java-codegen")),
                () ->
                        "Expected selected task '%s' to include the OpenAPI Generics "
                                + "codegen runtime.%n%n%s"
                                .formatted(
                                        taskName,
                                        result.getOutput()));
    }

    private static void assertSelectedTaskUsesEffectiveTemplates(
            BuildResult result,
            String taskName) {

        String templateLine =
                result.getOutput()
                        .lines()
                        .filter(
                                line ->
                                        line.startsWith(
                                                "TEMPLATE_DIR_"
                                                        + taskName
                                                        + "="))
                        .findFirst()
                        .orElse("");

        assertTrue(
                templateLine.contains(
                        "openapi-generics/templates/effective/Java"),
                () ->
                        "Expected selected task '%s' to use effective templates.%n%n%s"
                                .formatted(
                                        taskName,
                                        result.getOutput()));
    }

    private static void assertGeneratedSourceRegistered(
            BuildResult result,
            String outputDirectory) {

        assertTrue(
                result.getOutput()
                        .lines()
                        .anyMatch(
                                line ->
                                        line.startsWith("MAIN_JAVA_SRC=")
                                                && line.contains(outputDirectory)
                                                && line.contains("src/gen/java")),
                () ->
                        "Expected generated source root for '%s'.%n%n%s"
                                .formatted(
                                        outputDirectory,
                                        result.getOutput()));
    }
}

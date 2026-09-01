package io.github.blueprintplatform.openapi.generics.gradle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.gradle.support.ConsumerBuildFiles;
import io.github.blueprintplatform.openapi.generics.gradle.support.FunctionalTestEnvironment;
import java.io.IOException;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OpenApiGenericsTaskWiringFunctionalTest {

    @TempDir
    Path projectDirectory;

    private FunctionalTestEnvironment environment;

    @BeforeEach
    void prepare() throws IOException {
        environment = FunctionalTestEnvironment.create(projectDirectory);
        environment.writeMixedGeneratorConsumer();
    }

    @Test
    void preservesConsumerGeneratorSelection() {
        BuildResult result = environment.run(ConsumerBuildFiles.PRINT_TEMPLATE_DIRS);

        assertTrue(
                result.getOutput()
                        .contains(
                                "GENERATOR_NAME_"
                                        + ConsumerBuildFiles.GENERICS_GENERATE_TASK
                                        + "=java-generics-contract"),
                result::getOutput);
        assertTrue(
                result.getOutput()
                        .contains(
                                "GENERATOR_NAME_"
                                        + ConsumerBuildFiles.KOTLIN_GENERATE_TASK
                                        + "=kotlin"),
                result::getOutput);
    }

    @Test
    void wiresTemplatePreparationOnlyToGenericsTask() {
        BuildResult generics =
                environment.run(
                        ConsumerBuildFiles.GENERICS_GENERATE_TASK,
                        "--dry-run");
        BuildResult kotlin =
                environment.run(
                        ConsumerBuildFiles.KOTLIN_GENERATE_TASK,
                        "--dry-run");

        assertTrue(
                generics.getOutput().contains(":prepareOpenApiGenericsTemplates"),
                generics::getOutput);
        assertTrue(
                kotlin.getOutput()
                        .lines()
                        .noneMatch(line -> line.contains(":prepareOpenApiGenericsTemplates")),
                kotlin::getOutput);
    }

    @Test
    void overridesTemplateDirOnlyForSelectedTask() {
        BuildResult result = environment.run(ConsumerBuildFiles.PRINT_TEMPLATE_DIRS);
        String output = result.getOutput();

        assertTrue(
                output.contains(
                        "TEMPLATE_DIR_"
                                + ConsumerBuildFiles.GENERICS_GENERATE_TASK
                                + "="),
                result::getOutput);
        assertTrue(
                output.contains("openapi-generics/templates/effective/Java"),
                result::getOutput);

        String kotlinLine =
                output.lines()
                        .filter(
                                line ->
                                        line.startsWith(
                                                "TEMPLATE_DIR_"
                                                        + ConsumerBuildFiles
                                                                .KOTLIN_GENERATE_TASK
                                                        + "="))
                        .findFirst()
                        .orElse("");

        assertFalse(
                kotlinLine.contains("openapi-generics/templates/effective/Java"),
                () -> "Kotlin task should keep the official templateDir:\n" + output);
    }
}

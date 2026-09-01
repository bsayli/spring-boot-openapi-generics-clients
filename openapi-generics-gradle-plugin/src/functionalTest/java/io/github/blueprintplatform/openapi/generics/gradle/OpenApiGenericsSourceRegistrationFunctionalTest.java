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

class OpenApiGenericsSourceRegistrationFunctionalTest {

    @TempDir
    Path projectDirectory;

    private FunctionalTestEnvironment environment;

    @BeforeEach
    void prepare() throws IOException {
        environment = FunctionalTestEnvironment.create(projectDirectory);
    }

    @Test
    void registersOfficialGenerateOutputOnMainJavaSourceSet() throws IOException {
        environment.writeOfficialJavaConsumer();

        BuildResult result = environment.run(ConsumerBuildFiles.PRINT_MAIN_JAVA_SRC_DIRS);
        String output = result.getOutput();

        assertTrue(output.contains("MAIN_JAVA_SRC="), result::getOutput);
        assertTrue(
                output.lines()
                        .anyMatch(
                                line ->
                                        (line.startsWith("MAIN_JAVA_SRC=")
                                                        || line.startsWith(
                                                                "MAIN_JAVA_SOURCE_DIR="))
                                                && line.contains("generated/openapi")
                                                && line.contains("src/gen/java")),
                result::getOutput);
    }

    @Test
    void makesCompileJavaDependOnSelectedGenerateTaskOnly() throws IOException {
        environment.writeMixedGeneratorConsumer();

        BuildResult result = environment.run("compileJava", "--dry-run");
        String output = result.getOutput();

        assertTrue(
                output.contains(":" + ConsumerBuildFiles.GENERICS_GENERATE_TASK),
                result::getOutput);
        assertTrue(output.contains(":prepareOpenApiGenericsTemplates"), result::getOutput);
        assertTrue(
                output.lines()
                        .noneMatch(
                                line ->
                                        line.contains(
                                                        "task ':"
                                                                + ConsumerBuildFiles
                                                                        .KOTLIN_GENERATE_TASK
                                                                + "'")
                                                || line.endsWith(
                                                        ":"
                                                                + ConsumerBuildFiles
                                                                        .KOTLIN_GENERATE_TASK)),
                result::getOutput);
    }

    @Test
    void makesCompileJavaDependOnOfficialGenerateTask() throws IOException {
        environment.writeOfficialJavaConsumer();

        BuildResult result = environment.run("compileJava", "--dry-run");
        String output = result.getOutput();

        assertTrue(
                output.contains(":" + ConsumerBuildFiles.OFFICIAL_GENERATE_TASK),
                result::getOutput);
        assertTrue(output.contains(":prepareOpenApiGenericsTemplates"), result::getOutput);
    }
}

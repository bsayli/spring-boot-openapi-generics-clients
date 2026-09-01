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

class OpenApiGenericsApplyContractFunctionalTest {

    @TempDir
    Path projectDirectory;

    private FunctionalTestEnvironment environment;

    @BeforeEach
    void prepare() throws IOException {
        environment = FunctionalTestEnvironment.create(projectDirectory);
    }

    @Test
    void doesNothingUntilOpenApiGeneratorPluginIsApplied() throws IOException {
        environment.writeBuildFile(
                ConsumerBuildFiles.genericsPluginWithoutOpenApiGenerator(
                        environment.pluginVersion()));

        BuildResult result =
                environment.run(ConsumerBuildFiles.PRINT_REGISTERED_TASKS);

        assertTrue(
                result.getOutput().contains("PREPARE_TASK_PRESENT=null"),
                result::getOutput);
    }

    @Test
    void installsAfterOfficialPluginIsApplied() throws IOException {
        environment.writeOfficialJavaConsumer();

        BuildResult result = environment.run("tasks", "--all");

        assertTrue(
                result.getOutput().contains("prepareOpenApiGenericsTemplates"),
                result::getOutput);
        assertFalse(
                result.getOutput().contains("BUILD FAILED"),
                result::getOutput);
    }
}

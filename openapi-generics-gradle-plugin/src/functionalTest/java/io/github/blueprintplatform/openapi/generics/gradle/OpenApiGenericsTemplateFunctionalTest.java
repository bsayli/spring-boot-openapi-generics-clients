package io.github.blueprintplatform.openapi.generics.gradle;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.gradle.support.FunctionalTestEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OpenApiGenericsTemplateFunctionalTest {

    @TempDir
    Path projectDirectory;

    private FunctionalTestEnvironment environment;

    @BeforeEach
    void prepare() throws IOException {
        environment = FunctionalTestEnvironment.create(projectDirectory);
        environment.writeOfficialJavaConsumer();
    }

    @Test
    void preparesPatchedAndOverlaidTemplates() throws IOException {
        BuildResult result = environment.run("prepareOpenApiGenericsTemplates");

        BuildTask task = result.task(":prepareOpenApiGenericsTemplates");
        assertEquals(SUCCESS, task != null ? task.getOutcome() : null, result::getOutput);

        Path modelTemplate =
                environment.effectiveJavaTemplateDirectory().resolve("model.mustache");
        Path wrapperTemplate =
                environment.effectiveJavaTemplateDirectory().resolve("api_wrapper.mustache");

        assertTrue(Files.isRegularFile(modelTemplate), result::getOutput);
        assertTrue(
                Files.readString(modelTemplate).contains("vendorExtensions.x-api-wrapper"),
                result::getOutput);
        assertTrue(
                Files.readString(modelTemplate).contains("{{>api_wrapper}}"),
                result::getOutput);
        assertTrue(Files.isRegularFile(wrapperTemplate), result::getOutput);
        assertTrue(
                Files.readString(wrapperTemplate).contains("OPENAPI_GENERICS_TEST_WRAPPER"),
                result::getOutput);
    }
}

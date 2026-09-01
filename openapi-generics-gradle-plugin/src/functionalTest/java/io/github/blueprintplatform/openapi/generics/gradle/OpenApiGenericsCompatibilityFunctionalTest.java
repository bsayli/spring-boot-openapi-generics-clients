package io.github.blueprintplatform.openapi.generics.gradle;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.gradle.support.ConsumerBuildFiles;
import io.github.blueprintplatform.openapi.generics.gradle.support.FunctionalTestEnvironment;
import java.io.IOException;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OpenApiGenericsCompatibilityFunctionalTest {

    @TempDir
    Path projectDirectory;

    @Test
    void rejectsOfficialPluginWithoutRequiredCapability() throws IOException {
        FunctionalTestEnvironment environment =
                FunctionalTestEnvironment.create(projectDirectory);

        environment.writeBuildFile(
                ConsumerBuildFiles.officialJavaConsumer(
                        FunctionalTestEnvironment.UNSUPPORTED_OPENAPI_GENERATOR_VERSION,
                        environment.pluginVersion(),
                        environment.fixtureRepositoryUri()));

        BuildResult result = environment.runAndFail("help");

        assertTrue(
                result.getOutput()
                        .contains(
                                "OpenAPI Generics Gradle Plugin requires "
                                        + "org.openapi.generator Gradle Plugin "
                                        + "7.25.0 or later"),
                result::getOutput);
    }
}

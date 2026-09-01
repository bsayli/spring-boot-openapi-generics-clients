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

class OpenApiGenericsClasspathFunctionalTest {

    @TempDir
    Path projectDirectory;

    private FunctionalTestEnvironment environment;

    @BeforeEach
    void prepare() throws IOException {
        environment = FunctionalTestEnvironment.create(projectDirectory);
        environment.writeOfficialJavaConsumer();
    }

    @Test
    void registersCodegenOnOfficialExtraConfiguration() {
        BuildResult result =
                environment.run(ConsumerBuildFiles.PRINT_EXTRA_DEPENDENCIES);

        assertTrue(
                result.getOutput()
                        .contains(
                                "OPENAPI_GENERATOR_EXTRA_DEPENDENCY="
                                        + environment.expectedCodegenDependency()),
                result::getOutput);
    }
}

package io.github.blueprintplatform.openapi.generics.gradle;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Golden-path reconstruction against the real {@code java-generics-contract}
 * artifact. Functional tests must not duplicate this compile proof.
 */
class OpenApiGenericsRealCodegenE2ETest {

    private static final String GENERATED_MODEL_PACKAGE =
            "io.github.blueprintplatform.functional.generated.model";

    private static final String GENERATED_MODEL_DIRECTORY =
            "build/generated/openapi/src/gen/java/"
                    + "io/github/blueprintplatform/functional/generated/model";

    private static final String EFFECTIVE_TEMPLATE_DIRECTORY =
            "build/openapi-generics/templates/effective/Java";

    private static final Pattern SIMPLE_WRAPPER_DECLARATION =
            Pattern.compile(
                    "extends\\s+ServiceResponse\\s*<\\s*TypeSummaryDto\\s*>");

    private static final Pattern PAGE_WRAPPER_DECLARATION =
            Pattern.compile(
                    "extends\\s+ServiceResponse\\s*"
                            + "<\\s*Page\\s*<\\s*TypeSummaryDto\\s*>\\s*>");

    /*
     * Intentionally persistent for template/debug inspection.
     *
     * Unlike @TempDir, this directory remains under the plugin module's
     * build directory after a failed nested TestKit build.
     */
    private final Path projectDirectory =
            Path.of(
                            "build",
                            "debug-e2e-consumer")
                    .toAbsolutePath()
                    .normalize();

    @BeforeEach
    void prepareConsumerProject() throws IOException {
        deleteRecursively(projectDirectory);
        Files.createDirectories(projectDirectory);

        copyFixture("settings.gradle.kts");
        copyFixture("build.gradle.kts");
        copyFixture(
                "src/main/openapi/service-response-e2e.yaml");
    }

    @Test
    void reconstructsAndCompilesGenericServiceResponseContracts()
            throws IOException {

        BuildResult result =
                GradleRunner.create()
                        .withProjectDir(projectDirectory.toFile())
                        .withArguments(
                                "compileJava",
                                "--info",
                                "--stacktrace",
                                "-PopenApiGeneratorVersion=7.25.0",
                                "-PopenApiGenericsPluginVersion="
                                        + requireSystemProperty(
                                                "pluginUnderTestVersion"),
                                "-PopenApiGenericsPluginRepository="
                                        + requireSystemProperty(
                                                "functionalTestPluginRepository"),
                                "-PopenApiGenericsCodegenRepository="
                                        + requireSystemProperty(
                                                "functionalTestCodegenRepository"))
                        .build();

        assertSuccessfulTask(
                result,
                ":prepareOpenApiGenericsTemplates");
        assertSuccessfulTask(
                result,
                ":openApiGenerate");
        assertSuccessfulTask(
                result,
                ":compileJava");

        Path effectiveTemplateDirectory =
                projectDirectory.resolve(
                        EFFECTIVE_TEMPLATE_DIRECTORY);

        Path patchedModelTemplate =
                effectiveTemplateDirectory.resolve(
                        "model.mustache");

        Path wrapperTemplate =
                effectiveTemplateDirectory.resolve(
                        "api_wrapper.mustache");

        assertRegularFile(
                "patched OpenAPI Generator model template",
                patchedModelTemplate,
                result);

        assertRegularFile(
                "OpenAPI Generics wrapper template",
                wrapperTemplate,
                result);

        String patchedModelTemplateContent =
                Files.readString(
                        patchedModelTemplate);

        assertTrue(
                patchedModelTemplateContent.contains(
                        "vendorExtensions.x-api-wrapper"),
                () -> sourceMismatchMessage(
                        "patched wrapper dispatch marker",
                        patchedModelTemplate,
                        patchedModelTemplateContent,
                        result));

        assertTrue(
                patchedModelTemplateContent.contains(
                        "{{>api_wrapper}}"),
                () -> sourceMismatchMessage(
                        "api_wrapper partial dispatch",
                        patchedModelTemplate,
                        patchedModelTemplateContent,
                        result));

        Path generatedModelDirectory =
                projectDirectory.resolve(
                        GENERATED_MODEL_DIRECTORY);

        Path payloadSource =
                generatedModelDirectory.resolve(
                        "TypeSummaryDto.java");

        Path simpleWrapperSource =
                generatedModelDirectory.resolve(
                        "ServiceResponseTypeSummaryDto.java");

        Path pageWrapperSource =
                generatedModelDirectory.resolve(
                        "ServiceResponsePageTypeSummaryDto.java");

        assertRegularFile(
                "generated payload model",
                payloadSource,
                result);

        assertRegularFile(
                "generated simple generic wrapper",
                simpleWrapperSource,
                result);

        assertRegularFile(
                "generated paged generic wrapper",
                pageWrapperSource,
                result);

        /*
         * Infrastructure schemas must participate in reconstruction but must
         * not become duplicate generated models.
         */
        assertFalse(
                Files.exists(
                        generatedModelDirectory.resolve(
                                "Meta.java")),
                () -> unexpectedFileMessage(
                        "ignored Meta infrastructure model",
                        generatedModelDirectory.resolve(
                                "Meta.java"),
                        result));

        assertFalse(
                Files.exists(
                        generatedModelDirectory.resolve(
                                "PageTypeSummaryDto.java")),
                () -> unexpectedFileMessage(
                        "ignored projected Page infrastructure model",
                        generatedModelDirectory.resolve(
                                "PageTypeSummaryDto.java"),
                        result));

        String simpleWrapper =
                Files.readString(
                        simpleWrapperSource);

        String pageWrapper =
                Files.readString(
                        pageWrapperSource);

        assertTrue(
                simpleWrapper.contains(
                        "io.github.blueprintplatform.openapi.generics"
                                + ".contract.envelope.ServiceResponse"),
                () -> sourceMismatchMessage(
                        "ServiceResponse import",
                        simpleWrapperSource,
                        simpleWrapper,
                        result));

        assertTrue(
                SIMPLE_WRAPPER_DECLARATION
                        .matcher(simpleWrapper)
                        .find(),
                () -> sourceMismatchMessage(
                        "ServiceResponse<TypeSummaryDto> inheritance",
                        simpleWrapperSource,
                        simpleWrapper,
                        result));

        assertTrue(
                pageWrapper.contains(
                        "io.github.blueprintplatform.openapi.generics"
                                + ".contract.paging.Page"),
                () -> sourceMismatchMessage(
                        "Page import",
                        pageWrapperSource,
                        pageWrapper,
                        result));

        assertTrue(
                PAGE_WRAPPER_DECLARATION
                        .matcher(pageWrapper)
                        .find(),
                () -> sourceMismatchMessage(
                        "ServiceResponse<Page<TypeSummaryDto>> inheritance",
                        pageWrapperSource,
                        pageWrapper,
                        result));

        assertCompiledClass(
                "payload model",
                "TypeSummaryDto",
                result);

        assertCompiledClass(
                "simple generic wrapper",
                "ServiceResponseTypeSummaryDto",
                result);

        assertCompiledClass(
                "paged generic wrapper",
                "ServiceResponsePageTypeSummaryDto",
                result);
    }

    private void copyFixture(String relativePath)
            throws IOException {

        String resourcePath =
                "real-codegen-consumer/" + relativePath;

        Path destination =
                projectDirectory.resolve(
                        relativePath);

        Path parent =
                destination.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (InputStream input =
                getClass()
                        .getClassLoader()
                        .getResourceAsStream(
                                resourcePath)) {

            if (input == null) {
                throw new IllegalStateException(
                        "Missing E2E fixture resource: "
                                + resourcePath);
            }

            Files.copy(
                    input,
                    destination,
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void assertCompiledClass(
            String description,
            String simpleClassName,
            BuildResult result) {

        Path compiledClass =
                projectDirectory.resolve(
                        "build/classes/java/main/"
                                + GENERATED_MODEL_PACKAGE.replace(
                                        '.',
                                        '/')
                                + "/"
                                + simpleClassName
                                + ".class");

        assertRegularFile(
                "compiled " + description,
                compiledClass,
                result);
    }

    private static void assertSuccessfulTask(
            BuildResult result,
            String taskPath) {

        BuildTask task =
                result.task(
                        taskPath);

        assertNotNull(
                task,
                () -> "Expected task '%s' was not in the task graph.%n%n%s"
                        .formatted(
                                taskPath,
                                result.getOutput()));

        assertEquals(
                SUCCESS,
                task.getOutcome(),
                () -> "Expected task '%s' to succeed but was '%s'.%n%n%s"
                        .formatted(
                                taskPath,
                                task.getOutcome(),
                                result.getOutput()));
    }

    private static void assertRegularFile(
            String description,
            Path expected,
            BuildResult result) {

        assertTrue(
                Files.isRegularFile(
                        expected),
                () -> missingFileMessage(
                        description,
                        expected,
                        result));
    }

    private static void deleteRecursively(
            Path directory)
            throws IOException {

        if (Files.notExists(directory)) {
            return;
        }

        try (Stream<Path> paths =
                Files.walk(directory)) {

            try {
                paths.sorted(
                                Comparator.reverseOrder())
                        .forEach(
                                OpenApiGenericsRealCodegenE2ETest
                                        ::deletePath);
            } catch (UncheckedIOException exception) {
                throw exception.getCause();
            }
        }
    }

    private static void deletePath(
            Path path) {

        try {
            Files.delete(path);
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Could not delete E2E debug path: "
                            + path,
                    exception);
        }
    }

    private static String missingFileMessage(
            String description,
            Path expected,
            BuildResult result) {

        return """
                Expected %s was not found.

                Expected:
                %s

                Persistent E2E project:
                %s

                Build output:
                %s
                """
                .formatted(
                        description,
                        expected,
                        expected
                                .toAbsolutePath()
                                .normalize(),
                        result.getOutput());
    }

    private static String unexpectedFileMessage(
            String description,
            Path unexpected,
            BuildResult result) {

        return """
                Unexpected %s was generated.

                Unexpected:
                %s

                Build output:
                %s
                """
                .formatted(
                        description,
                        unexpected,
                        result.getOutput());
    }

    private static String sourceMismatchMessage(
            String expectation,
            Path sourcePath,
            String source,
            BuildResult result) {

        return """
                Generated source did not satisfy expectation:
                %s

                Source:
                %s

                Generated content:
                %s

                Build output:
                %s
                """
                .formatted(
                        expectation,
                        sourcePath,
                        source,
                        result.getOutput());
    }

    private static String requireSystemProperty(
            String name) {

        String value =
                System.getProperty(
                        name);

        if (value == null
                || value.isBlank()) {

            throw new IllegalStateException(
                    "Required E2E system property '%s' is missing."
                            .formatted(
                                    name));
        }

        return value;
    }
}
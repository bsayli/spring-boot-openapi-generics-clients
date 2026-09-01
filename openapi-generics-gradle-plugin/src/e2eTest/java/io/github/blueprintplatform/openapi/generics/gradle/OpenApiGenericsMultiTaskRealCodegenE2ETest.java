package io.github.blueprintplatform.openapi.generics.gradle;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Real-codegen proof for two selected {@code GenerateTask} instances in one
 * Gradle consumer project.
 */
class OpenApiGenericsMultiTaskRealCodegenE2ETest {

    private static final List<String> FIXTURE_FILES =
            List.of(
                    "settings.gradle.kts",
                    "build.gradle.kts",
                    "src/main/openapi/customer.yaml",
                    "src/main/openapi/order.yaml",
                    "src/main/java/io/github/blueprintplatform/functional/"
                            + "multitask/MultiTaskGenerationProbe.java");

    private final Path projectDirectory =
            Path.of(
                            "build",
                            "debug-multi-task-e2e-consumer")
                    .toAbsolutePath()
                    .normalize();

    @BeforeEach
    void prepareConsumerProject() throws IOException {
        deleteRecursively(projectDirectory);
        Files.createDirectories(projectDirectory);

        for (String fixtureFile : FIXTURE_FILES) {
            copyFixture(fixtureFile);
        }
    }

    @Test
    void generatesAndCompilesTwoIsolatedGenericClientsWithConfigurationCache()
            throws IOException {

        BuildResult first = runCompileJava();

        assertSuccessfulOrUpToDate(
                first,
                ":prepareOpenApiGenericsTemplates");
        assertSuccessfulOrUpToDate(
                first,
                ":generateCustomerClient");
        assertSuccessfulOrUpToDate(
                first,
                ":generateOrderClient");
        assertSuccessfulOrUpToDate(
                first,
                ":compileJava");

        assertTrue(
                first.getOutput().contains("Configuration cache entry stored")
                        || first.getOutput().contains(
                                "Calculating task graph as no cached"),
                first::getOutput);

        assertGeneratedAndCompiledOutputs(first);

        BuildResult second = runCompileJava();

        assertTrue(
                second.getOutput().contains("Reusing configuration cache"),
                second::getOutput);

        assertSuccessfulOrUpToDate(
                second,
                ":generateCustomerClient");
        assertSuccessfulOrUpToDate(
                second,
                ":generateOrderClient");
        assertSuccessfulOrUpToDate(
                second,
                ":compileJava");

        assertGeneratedAndCompiledOutputs(second);
    }

    private BuildResult runCompileJava() {
        return GradleRunner.create()
                .withProjectDir(projectDirectory.toFile())
                .withArguments(
                        "compileJava",
                        "--configuration-cache",
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
    }

    private void assertGeneratedAndCompiledOutputs(BuildResult result)
            throws IOException {

        Path customerModels =
                projectDirectory.resolve(
                        "build/generated/customer/src/gen/java/"
                                + "io/github/blueprintplatform/functional/"
                                + "multitask/customer/model");

        Path orderModels =
                projectDirectory.resolve(
                        "build/generated/order/src/gen/java/"
                                + "io/github/blueprintplatform/functional/"
                                + "multitask/order/model");

        Path customerWrapper =
                customerModels.resolve(
                        "ServiceResponseCustomerDto.java");

        Path orderWrapper =
                orderModels.resolve(
                        "ServiceResponsePageOrderDto.java");

        assertRegularFile(
                customerWrapper,
                result);

        assertRegularFile(
                orderWrapper,
                result);

        String customerSource =
                Files.readString(customerWrapper);

        String orderSource =
                Files.readString(orderWrapper);

        assertTrue(
                customerSource.contains(
                        "extends ServiceResponse<CustomerDto>"),
                () ->
                        sourceMismatchMessage(
                                customerWrapper,
                                customerSource,
                                result));

        assertTrue(
                orderSource.contains(
                        "extends ServiceResponse<Page<OrderDto>>"),
                () ->
                        sourceMismatchMessage(
                                orderWrapper,
                                orderSource,
                                result));

        assertRegularFile(
                compiledClass(
                        "customer/model/ServiceResponseCustomerDto"),
                result);

        assertRegularFile(
                compiledClass(
                        "order/model/ServiceResponsePageOrderDto"),
                result);

        assertRegularFile(
                projectDirectory.resolve(
                        "build/classes/java/main/"
                                + "io/github/blueprintplatform/functional/"
                                + "multitask/MultiTaskGenerationProbe.class"),
                result);
    }

    private Path compiledClass(String relativeClassName) {
        return projectDirectory.resolve(
                "build/classes/java/main/"
                        + "io/github/blueprintplatform/functional/"
                        + "multitask/"
                        + relativeClassName
                        + ".class");
    }

    private void copyFixture(String relativePath)
            throws IOException {

        String resourcePath =
                "multi-task-real-codegen-consumer/"
                        + relativePath;

        Path destination =
                projectDirectory.resolve(
                        relativePath);

        Path parent = destination.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (InputStream input =
                getClass()
                        .getClassLoader()
                        .getResourceAsStream(resourcePath)) {

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

    private static void assertSuccessfulOrUpToDate(
            BuildResult result,
            String taskPath) {

        BuildTask task = result.task(taskPath);

        assertNotNull(
                task,
                () ->
                        "Expected task '%s' was not in the task graph.%n%n%s"
                                .formatted(
                                        taskPath,
                                        result.getOutput()));

        assertTrue(
                task.getOutcome() == SUCCESS
                        || task.getOutcome() == UP_TO_DATE,
                () ->
                        "Expected task '%s' to succeed or be up-to-date "
                                + "but was '%s'.%n%n%s"
                                .formatted(
                                        taskPath,
                                        task.getOutcome(),
                                        result.getOutput()));
    }

    private static void assertRegularFile(
            Path expected,
            BuildResult result) {

        assertTrue(
                Files.isRegularFile(expected),
                () ->
                        "Expected file was not found:%n%s%n%nBuild output:%n%s"
                                .formatted(
                                        expected,
                                        result.getOutput()));
    }

    private static void deleteRecursively(Path directory)
            throws IOException {

        if (Files.notExists(directory)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(directory)) {
            try {
                paths.sorted(Comparator.reverseOrder())
                        .forEach(
                                OpenApiGenericsMultiTaskRealCodegenE2ETest
                                        ::deletePath);
            } catch (UncheckedIOException exception) {
                throw exception.getCause();
            }
        }
    }

    private static void deletePath(Path path) {
        try {
            Files.delete(path);
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Could not delete multi-task E2E debug path: "
                            + path,
                    exception);
        }
    }

    private static String sourceMismatchMessage(
            Path sourcePath,
            String source,
            BuildResult result) {

        return """
                Generated source did not contain the expected generic inheritance.

                Source:
                %s

                Generated content:
                %s

                Build output:
                %s
                """
                .formatted(
                        sourcePath,
                        source,
                        result.getOutput());
    }

    private static String requireSystemProperty(String name) {
        String value = System.getProperty(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Required E2E system property '%s' is missing."
                            .formatted(name));
        }

        return value;
    }
}

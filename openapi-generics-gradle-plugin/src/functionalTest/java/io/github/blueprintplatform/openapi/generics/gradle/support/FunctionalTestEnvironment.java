package io.github.blueprintplatform.openapi.generics.gradle.support;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

public final class FunctionalTestEnvironment {

    public static final String OPENAPI_GENERATOR_VERSION = "7.25.0";

    public static final String UNSUPPORTED_OPENAPI_GENERATOR_VERSION = "7.24.0";

    public static final String EFFECTIVE_JAVA_TEMPLATE_DIRECTORY =
            "build/openapi-generics/templates/effective/Java";

    private final Path projectDirectory;
    private final String pluginRepositoryUri;
    private final String pluginVersion;
    private final Path fixtureRepository;

    private FunctionalTestEnvironment(
            Path projectDirectory,
            String pluginRepositoryUri,
            String pluginVersion,
            Path fixtureRepository) {

        this.projectDirectory = projectDirectory;
        this.pluginRepositoryUri = pluginRepositoryUri;
        this.pluginVersion = pluginVersion;
        this.fixtureRepository = fixtureRepository;
    }

    public static FunctionalTestEnvironment create(Path projectDirectory)
            throws IOException {

        String pluginRepositoryUri =
                Path.of(requireSystemProperty("functionalTestPluginRepository"))
                        .toUri()
                        .toString();

        String pluginVersion = requireSystemProperty("pluginUnderTestVersion");

        Path fixtureRepository = projectDirectory.resolve("fixture-repository");

        FunctionalTestEnvironment environment =
                new FunctionalTestEnvironment(
                        projectDirectory,
                        pluginRepositoryUri,
                        pluginVersion,
                        fixtureRepository);

        environment.publishCodegenFixture();
        environment.writeSettings();
        return environment;
    }

    public Path projectDirectory() {
        return projectDirectory;
    }

    public String pluginVersion() {
        return pluginVersion;
    }

    public String fixtureRepositoryUri() {
        return fixtureRepository.toUri().toString();
    }

    public String expectedCodegenDependency() {
        return "io.github.blueprint-platform:openapi-generics-java-codegen:" + pluginVersion;
    }

    public Path effectiveJavaTemplateDirectory() {
        return projectDirectory.resolve(EFFECTIVE_JAVA_TEMPLATE_DIRECTORY);
    }

    public void writeBuildFile(String content) throws IOException {
        Files.writeString(projectDirectory.resolve("build.gradle.kts"), content);
    }

    public void writeOfficialJavaConsumer() throws IOException {
        writeBuildFile(
                ConsumerBuildFiles.officialJavaConsumer(
                        OPENAPI_GENERATOR_VERSION,
                        pluginVersion,
                        fixtureRepositoryUri()));
    }

    public void writeMixedGeneratorConsumer() throws IOException {
        writeBuildFile(
                ConsumerBuildFiles.mixedGeneratorConsumer(
                        OPENAPI_GENERATOR_VERSION,
                        pluginVersion,
                        fixtureRepositoryUri()));
    }

    public void writeMultipleGenericsTasksConsumer() throws IOException {
        writeBuildFile(
                ConsumerBuildFiles.multipleGenericsTasksConsumer(
                        OPENAPI_GENERATOR_VERSION,
                        pluginVersion,
                        fixtureRepositoryUri()));
    }

    public BuildResult run(String... arguments) {
        return runner(arguments).build();
    }

    public BuildResult runAndFail(String... arguments) {
        return runner(arguments).buildAndFail();
    }

    public BuildResult runWithConfigurationCache(String... arguments) {
        String[] complete = new String[arguments.length + 1];
        complete[0] = "--configuration-cache";
        System.arraycopy(arguments, 0, complete, 1, arguments.length);
        return run(complete);
    }

    private GradleRunner runner(String... arguments) {
        String[] completeArguments = new String[arguments.length + 2];
        System.arraycopy(arguments, 0, completeArguments, 0, arguments.length);
        completeArguments[arguments.length] = "--info";
        completeArguments[arguments.length + 1] = "--stacktrace";

        return GradleRunner.create()
                .withProjectDir(projectDirectory.toFile())
                .withArguments(completeArguments);
    }

    private void writeSettings() throws IOException {
        Files.writeString(
                projectDirectory.resolve("settings.gradle.kts"),
                """
                        pluginManagement {
                            repositories {
                                maven {
                                    url = uri("%s")
                                }

                                gradlePluginPortal()
                                mavenCentral()
                            }
                        }

                        rootProject.name = "openapi-generics-consumer-test"
                        """
                        .formatted(pluginRepositoryUri));
    }

    private void publishCodegenFixture() throws IOException {
        Path artifactDirectory =
                fixtureRepository.resolve(
                        "io/github/blueprint-platform/openapi-generics-java-codegen/"
                                + pluginVersion);

        Files.createDirectories(artifactDirectory);

        Files.writeString(
                artifactDirectory.resolve(
                        "openapi-generics-java-codegen-" + pluginVersion + ".pom"),
                """
                        <project xmlns="http://maven.apache.org/POM/4.0.0">
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>io.github.blueprint-platform</groupId>
                            <artifactId>openapi-generics-java-codegen</artifactId>
                            <version>%s</version>
                        </project>
                        """
                        .formatted(pluginVersion));

        Path jar =
                artifactDirectory.resolve(
                        "openapi-generics-java-codegen-" + pluginVersion + ".jar");

        try (OutputStream outputStream = Files.newOutputStream(jar);
                JarOutputStream jarOutputStream = new JarOutputStream(outputStream)) {

            jarOutputStream.putNextEntry(
                    new JarEntry("META-INF/openapi-generics/templates/api_wrapper.mustache"));
            jarOutputStream.write(
                    "OPENAPI_GENERICS_TEST_WRAPPER".getBytes(StandardCharsets.UTF_8));
            jarOutputStream.closeEntry();
        }
    }

    private static String requireSystemProperty(String name) {
        String value = System.getProperty(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Required functional-test system property '%s' is missing."
                            .formatted(name));
        }

        return value;
    }
}

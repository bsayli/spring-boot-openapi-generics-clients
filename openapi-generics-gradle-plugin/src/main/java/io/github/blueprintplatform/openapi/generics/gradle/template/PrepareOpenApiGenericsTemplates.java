package io.github.blueprintplatform.openapi.generics.gradle.template;

import io.github.blueprintplatform.openapi.generics.gradle.template.io.OpenApiGenericsTemplatePreparer;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * Produces the effective Java template directory used by OpenAPI Generics generation tasks.
 *
 * <p>The task declares Gradle inputs and outputs and delegates template preparation to the
 * framework-independent preparation pipeline.
 */
@CacheableTask
public abstract class PrepareOpenApiGenericsTemplates extends DefaultTask {

    @Classpath
    public abstract ConfigurableFileCollection getOpenApiGeneratorArtifacts();

    @Classpath
    public abstract ConfigurableFileCollection getOpenApiGenericsCodegenArtifacts();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void prepareTemplates() {
        List<Path> generatorArtifacts =
                getOpenApiGeneratorArtifacts()
                        .getFiles()
                        .stream()
                        .map(File::toPath)
                        .toList();

        List<Path> codegenArtifacts =
                getOpenApiGenericsCodegenArtifacts()
                        .getFiles()
                        .stream()
                        .map(File::toPath)
                        .toList();

        Path outputDirectory =
                getOutputDirectory()
                        .get()
                        .getAsFile()
                        .toPath();

        new OpenApiGenericsTemplatePreparer()
                .prepare(
                        generatorArtifacts,
                        codegenArtifacts,
                        outputDirectory);
    }
}
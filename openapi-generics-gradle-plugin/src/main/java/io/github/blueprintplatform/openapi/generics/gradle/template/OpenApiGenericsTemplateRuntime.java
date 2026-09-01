package io.github.blueprintplatform.openapi.generics.gradle.template;

import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

/**
 * Project-level template preparation handles shared by every OpenAPI Generics generation task.
 */
public record OpenApiGenericsTemplateRuntime(
        TaskProvider<PrepareOpenApiGenericsTemplates> preparationTask,
        Provider<Directory> effectiveTemplateDirectory) {
}

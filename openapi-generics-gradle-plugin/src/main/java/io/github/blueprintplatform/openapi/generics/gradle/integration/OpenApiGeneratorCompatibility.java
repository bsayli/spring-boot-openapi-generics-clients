package io.github.blueprintplatform.openapi.generics.gradle.integration;

import io.github.blueprintplatform.openapi.generics.gradle.exception.OpenApiGeneratorCompatibilityException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

/**
 * Validates the public OpenAPI Generator capabilities required by OpenAPI Generics.
 */
public final class OpenApiGeneratorCompatibility {

    public static final String MINIMUM_SUPPORTED_VERSION = "7.25.0";
    public static final String GENERATOR_EXTRA_CONFIGURATION_NAME =
            "openApiGeneratorExtra";

    public Configuration requireGeneratorExtraConfiguration(
            Project project) {

        Configuration configuration =
                project.getConfigurations()
                        .findByName(
                                GENERATOR_EXTRA_CONFIGURATION_NAME);

        if (configuration == null) {
            throw incompatible(
                    "required configuration '%s' was not found"
                            .formatted(
                                    GENERATOR_EXTRA_CONFIGURATION_NAME));
        }

        if (!configuration.isCanBeResolved()) {
            throw incompatible(
                    "configuration '%s' is not resolvable"
                            .formatted(
                                    GENERATOR_EXTRA_CONFIGURATION_NAME));
        }

        if (configuration.isCanBeConsumed()) {
            throw incompatible(
                    "configuration '%s' is unexpectedly consumable"
                            .formatted(
                                    GENERATOR_EXTRA_CONFIGURATION_NAME));
        }

        return configuration;
    }

    private OpenApiGeneratorCompatibilityException incompatible(
            String detail) {

        return new OpenApiGeneratorCompatibilityException(
                MINIMUM_SUPPORTED_VERSION,
                detail);
    }
}

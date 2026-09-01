package io.github.blueprintplatform.openapi.generics.gradle.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.blueprintplatform.openapi.generics.gradle.exception.OpenApiGeneratorCompatibilityException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

class OpenApiGeneratorCompatibilityTest {

    private final OpenApiGeneratorCompatibility compatibility =
            new OpenApiGeneratorCompatibility();

    @Test
    void acceptsResolvableNonConsumableExtraConfiguration() {
        Project project = ProjectBuilder.builder().build();
        Configuration configuration =
                project.getConfigurations()
                        .create(
                                OpenApiGeneratorCompatibility.GENERATOR_EXTRA_CONFIGURATION_NAME);
        configuration.setCanBeResolved(true);
        configuration.setCanBeConsumed(false);

        assertSame(configuration, compatibility.requireGeneratorExtraConfiguration(project));
    }

    @Test
    void rejectsMissingExtraConfiguration() {
        Project project = ProjectBuilder.builder().build();

        OpenApiGeneratorCompatibilityException exception =
                assertThrows(
                        OpenApiGeneratorCompatibilityException.class,
                        () -> compatibility.requireGeneratorExtraConfiguration(project));

        assertTrue(exception.getMessage().contains("7.25.0 or later"));
        assertTrue(exception.getMessage().contains("was not found"));
    }

    @Test
    void rejectsNonResolvableExtraConfiguration() {
        Project project = ProjectBuilder.builder().build();
        Configuration configuration =
                project.getConfigurations()
                        .create(
                                OpenApiGeneratorCompatibility.GENERATOR_EXTRA_CONFIGURATION_NAME);
        configuration.setCanBeResolved(false);
        configuration.setCanBeConsumed(false);

        OpenApiGeneratorCompatibilityException exception =
                assertThrows(
                        OpenApiGeneratorCompatibilityException.class,
                        () -> compatibility.requireGeneratorExtraConfiguration(project));

        assertTrue(exception.getMessage().contains("is not resolvable"));
    }

    @Test
    void rejectsConsumableExtraConfiguration() {
        Project project = ProjectBuilder.builder().build();
        Configuration configuration =
                project.getConfigurations()
                        .create(
                                OpenApiGeneratorCompatibility.GENERATOR_EXTRA_CONFIGURATION_NAME);
        configuration.setCanBeResolved(true);
        configuration.setCanBeConsumed(true);

        OpenApiGeneratorCompatibilityException exception =
                assertThrows(
                        OpenApiGeneratorCompatibilityException.class,
                        () -> compatibility.requireGeneratorExtraConfiguration(project));

        assertTrue(exception.getMessage().contains("unexpectedly consumable"));
        assertEquals("7.25.0", OpenApiGeneratorCompatibility.MINIMUM_SUPPORTED_VERSION);
    }
}

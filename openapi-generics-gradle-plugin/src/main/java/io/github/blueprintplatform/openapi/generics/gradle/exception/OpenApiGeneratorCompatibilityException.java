package io.github.blueprintplatform.openapi.generics.gradle.exception;

/**
 * Raised when the applied OpenAPI Generator Gradle plugin does not expose the capabilities required
 * by OpenAPI Generics.
 */
public final class OpenApiGeneratorCompatibilityException
        extends OpenApiGenericsGradleException {

    public OpenApiGeneratorCompatibilityException(
            String minimumSupportedVersion,
            String detail) {

        super(
                """
                        OpenAPI Generics Gradle Plugin requires \
                        org.openapi.generator Gradle Plugin %s or later: %s.
                        """
                        .formatted(
                                minimumSupportedVersion,
                                detail)
                        .strip());
    }
}

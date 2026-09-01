package io.github.blueprintplatform.openapi.generics.gradle.exception;

/**
 * Raised when an implementation version cannot be read from an artifact manifest.
 */
public final class PluginVersionResolutionException
        extends OpenApiGenericsGradleException {

    public PluginVersionResolutionException(
            String component,
            Class<?> implementationType) {

        super(
                "Could not determine "
                        + component
                        + " version from the artifact manifest for implementation class '"
                        + implementationType.getName()
                        + "'.");
    }
}

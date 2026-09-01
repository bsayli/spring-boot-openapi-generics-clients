package io.github.blueprintplatform.openapi.generics.gradle.exception;

import org.gradle.api.GradleException;

import java.io.Serial;

/**
 * Base exception for failures raised by the OpenAPI Generics Gradle plugin.
 */
public abstract class OpenApiGenericsGradleException extends GradleException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected OpenApiGenericsGradleException(String message) {
        super(message);
    }

    protected OpenApiGenericsGradleException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}

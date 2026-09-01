package io.github.blueprintplatform.openapi.generics.gradle.exception;

/**
 * Raised when effective OpenAPI Generics templates cannot be prepared deterministically.
 */
public final class TemplatePreparationException
        extends OpenApiGenericsGradleException {

    public TemplatePreparationException(String message) {
        super(message);
    }

    public TemplatePreparationException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}

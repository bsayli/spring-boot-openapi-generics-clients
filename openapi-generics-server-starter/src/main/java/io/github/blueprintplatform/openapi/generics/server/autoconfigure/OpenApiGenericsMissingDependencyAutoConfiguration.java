package io.github.blueprintplatform.openapi.generics.server.autoconfigure;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;

/**
 * Emits a clear warning when Springdoc is not present on the classpath.
 *
 * <p>This starter activates OpenAPI customization only when Springdoc is available. If Springdoc is
 * missing, the feature remains inactive.
 *
 * <p>This component ensures the user is explicitly informed instead of failing silently.
 */
@AutoConfiguration
@ConditionalOnMissingClass("org.springdoc.core.customizers.OpenApiCustomizer")
public class OpenApiGenericsMissingDependencyAutoConfiguration {

  private static final Logger log =
      LoggerFactory.getLogger(OpenApiGenericsMissingDependencyAutoConfiguration.class);

  private static final String MESSAGE =
      """

            -------------------------------------------------------------------------
            OpenAPI Generics Starter is inactive

            Reason:
              Springdoc OpenAPI is not detected on the classpath.

            This starter activates only when Springdoc is present.

            To enable OpenAPI customization, add one of the following:

              - org.springdoc:springdoc-openapi-starter-webmvc-ui
              - org.springdoc:springdoc-openapi-starter-webflux-ui

            -------------------------------------------------------------------------
            """;

  @PostConstruct
  public void logWarning() {
    log.warn(MESSAGE);
  }
}

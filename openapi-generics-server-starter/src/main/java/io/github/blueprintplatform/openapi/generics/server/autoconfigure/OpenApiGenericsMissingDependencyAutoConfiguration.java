package io.github.blueprintplatform.openapi.generics.server.autoconfigure;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;

/**
 * Emits a warning when Springdoc is not present on the classpath.
 *
 * <p>This auto-configuration is activated only when
 * {@code org.springdoc.core.customizers.OpenApiCustomizer} is missing.
 *
 * <p>In such cases, the OpenAPI Generics starter remains inactive,
 * and this component logs a clear message to inform the user.
 *
 * <h2>Purpose</h2>
 *
 * <ul>
 *   <li>Prevents silent misconfiguration</li>
 *   <li>Provides explicit guidance to the user</li>
 *   <li>Keeps startup non-blocking (no failure)</li>
 * </ul>
 *
 * <h2>Activation Condition</h2>
 *
 * <ul>
 *   <li>Springdoc is <b>not</b> present on the classpath</li>
 * </ul>
 *
 * <p>If Springdoc is available, this configuration is not loaded.
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

  /**
   * Logs a warning message indicating that Springdoc is missing.
   *
   * <p>This method is invoked after bean initialization and provides
   * a clear diagnostic message to the user.
   */
  @PostConstruct
  public void logWarning() {
    log.warn(MESSAGE);
  }
}
package io.github.blueprintplatform.openapi.generics.server.core.introspection;

import java.util.Set;
import org.springframework.core.ResolvableType;

/**
 * Strategy interface responsible for discovering response types from the underlying web framework.
 *
 * <p>This abstraction separates framework-specific endpoint scanning from framework-independent
 * OpenAPI schema generation.
 *
 * <p>Implementations are expected to:
 *
 * <ul>
 *   <li>Scan the runtime environment (e.g. Spring MVC, WebFlux)
 *   <li>Extract response return types from handler definitions
 *   <li>Normalize them into {@link ResolvableType}
 * </ul>
 *
 * <p>The resulting types are later analyzed by {@code ResponseTypeIntrospector} to determine
 * whether they match supported contract-aware shapes.
 *
 * <p>This interface is the primary extension point for supporting multiple web frameworks without
 * changing core logic.
 */
public interface ResponseTypeDiscoveryStrategy {

  /**
   * Discovers response types exposed by the application.
   *
   * @return a set of response types represented as {@link ResolvableType}
   */
  Set<ResolvableType> discover();
}

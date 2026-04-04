    package io.github.blueprintplatform.openapi.generics.server.mvc;

import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDiscoveryStrategy;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.ResolvableType;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
     * Spring MVC implementation of {@link ResponseTypeDiscoveryStrategy}.
     *
     * <p>This strategy scans Spring MVC handler mappings to discover controller methods and extracts
     * their return types as {@link ResolvableType}.
     *
     * <p>The extracted types are framework-neutral representations and are later analyzed by the core
     * introspection layer.
     *
     * <p>This class is intentionally limited to:
     *
     * <ul>
     *   <li>Discovering handler methods from {@link RequestMappingHandlerMapping}
     *   <li>Extracting raw return types
     * </ul>
     *
     * <p>It does NOT:
     *
     * <ul>
     *   <li>Interpret generic structures
     *   <li>Apply any contract rules
     *   <li>Perform OpenAPI-related logic
     * </ul>
     *
     * <p>This keeps the MVC-specific logic isolated from the core system.
     */
    public class MvcResponseTypeDiscoveryStrategy implements ResponseTypeDiscoveryStrategy {

      private final ListableBeanFactory beanFactory;

      public MvcResponseTypeDiscoveryStrategy(ListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
      }

      /**
       * Scans all {@link RequestMappingHandlerMapping} beans and collects controller method return
       * types.
       *
       * @return a set of discovered response types as {@link ResolvableType}
       */
      @Override
      public Set<ResolvableType> discover() {

        Set<ResolvableType> result = new LinkedHashSet<>();

        Map<String, RequestMappingHandlerMapping> mappings =
            beanFactory.getBeansOfType(RequestMappingHandlerMapping.class);

        if (mappings.isEmpty()) {
          return result;
        }

        mappings
            .values()
            .forEach(
                mapping ->
                    mapping
                        .getHandlerMethods()
                        .values()
                        .forEach(
                            handlerMethod -> {
                              Method method = handlerMethod.getMethod();

                              ResolvableType type = ResolvableType.forMethodReturnType(method);

                              // Defensive: skip completely unresolved types
                              if (type.resolve() == null) {
                                return;
                              }

                              result.add(type);
                            }));

        return result;
      }
    }

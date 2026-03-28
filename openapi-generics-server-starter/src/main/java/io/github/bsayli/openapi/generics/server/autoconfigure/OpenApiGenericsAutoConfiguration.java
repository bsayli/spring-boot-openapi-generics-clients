package io.github.bsayli.openapi.generics.server.autoconfigure;

import io.github.bsayli.openapi.generics.server.core.customizer.BaseSchemaCustomizer;
import io.github.bsayli.openapi.generics.server.core.customizer.WrapperSchemaCustomizer;
import io.github.bsayli.openapi.generics.server.core.introspection.ResponseTypeDiscoveryStrategy;
import io.github.bsayli.openapi.generics.server.core.introspection.ResponseTypeIntrospector;
import io.github.bsayli.openapi.generics.server.core.schema.WrapperSchemaEnricher;
import io.github.bsayli.openapi.generics.server.core.validation.OpenApiContractGuard;
import io.github.bsayli.openapi.generics.server.mvc.MvcResponseTypeDiscoveryStrategy;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Auto-configuration entry point for generics-aware OpenAPI support.
 *
 * <p>This configuration assembles a deterministic pipeline for generating contract-aware OpenAPI
 * schemas for {@code ServiceResponse<T>} APIs.
 *
 * <h2>Pipeline stages</h2>
 *
 * <ul>
 *   <li><b>Base schema registration</b> → canonical envelope definitions
 *   <li><b>Discovery</b> → extract response types
 *   <li><b>Introspection</b> → identify generic structures
 *   <li><b>Wrapper generation</b> → create composed schemas
 *   <li><b>Validation</b> → fail-fast contract verification
 * </ul>
 *
 * <h2>Execution order</h2>
 *
 * <ul>
 *   <li>{@link BaseSchemaCustomizer} → HIGHEST_PRECEDENCE
 *   <li>{@link WrapperSchemaCustomizer} → LOWEST_PRECEDENCE - 10
 *   <li>{@link OpenApiContractGuard} → LOWEST_PRECEDENCE
 * </ul>
 *
 * <p>This guarantees:
 *
 * <ul>
 *   <li>Base schemas exist before wrapper generation
 *   <li>Wrappers are fully generated before validation
 *   <li>Validation sees final OpenAPI state
 * </ul>
 *
 * <p>This configuration is:
 *
 * <ul>
 *   <li>Deterministic
 *   <li>Fail-fast
 *   <li>Non-intrusive (backs off if user overrides)
 * </ul>
 */
@AutoConfiguration
@ConditionalOnClass(OpenApiCustomizer.class)
@ConditionalOnWebApplication
public class OpenApiGenericsAutoConfiguration {

  // -------------------------------------------------------------------------
  // Base schemas
  // -------------------------------------------------------------------------

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE)
  @ConditionalOnMissingBean(name = "baseSchemaCustomizer")
  public OpenApiCustomizer baseSchemaCustomizer() {
    return new BaseSchemaCustomizer();
  }

  // -------------------------------------------------------------------------
  // Discovery
  // -------------------------------------------------------------------------

  @Bean
  @ConditionalOnClass(RequestMappingHandlerMapping.class)
  @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
  @ConditionalOnMissingBean(ResponseTypeDiscoveryStrategy.class)
  public ResponseTypeDiscoveryStrategy mvcStrategy(ListableBeanFactory beanFactory) {
    return new MvcResponseTypeDiscoveryStrategy(beanFactory);
  }

  // -------------------------------------------------------------------------
  // Introspection
  // -------------------------------------------------------------------------

  @Bean
  @ConditionalOnMissingBean
  public ResponseTypeIntrospector responseTypeIntrospector() {
    return new ResponseTypeIntrospector();
  }

  // -------------------------------------------------------------------------
  // Enrichment
  // -------------------------------------------------------------------------

  @Bean
  @ConditionalOnMissingBean
  public WrapperSchemaEnricher wrapperSchemaEnricher() {
    return new WrapperSchemaEnricher();
  }

  // -------------------------------------------------------------------------
  // Wrapper generation
  // -------------------------------------------------------------------------

  @Bean
  @Order(Ordered.LOWEST_PRECEDENCE - 10)
  @ConditionalOnBean(ResponseTypeDiscoveryStrategy.class)
  @ConditionalOnMissingBean
  public OpenApiCustomizer wrapperSchemaCustomizer(
      ResponseTypeDiscoveryStrategy discoveryStrategy,
      ResponseTypeIntrospector introspector,
      WrapperSchemaEnricher enricher,
      @Value("${app.openapi.wrapper.class-extra-annotation:}") String extraAnnotation) {
    return new WrapperSchemaCustomizer(discoveryStrategy, introspector, enricher, extraAnnotation);
  }

  // -------------------------------------------------------------------------
  // Contract validation (fail-fast)
  // -------------------------------------------------------------------------

  @Bean
  @Order(Ordered.LOWEST_PRECEDENCE)
  @ConditionalOnProperty(
      name = "app.openapi.contract.validation.enabled",
      havingValue = "true",
      matchIfMissing = true)
  public OpenApiCustomizer openApiContractGuard() {
    return new OpenApiContractGuard();
  }
}

package io.github.bsayli.openapi.generics.server.autoconfigure;

import io.github.bsayli.openapi.generics.server.core.introspection.ResponseTypeDiscoveryStrategy;
import io.github.bsayli.openapi.generics.server.core.introspection.ResponseTypeIntrospector;
import io.github.bsayli.openapi.generics.server.core.pipeline.OpenApiPipelineOrchestrator;
import io.github.bsayli.openapi.generics.server.core.schema.WrapperSchemaEnricher;
import io.github.bsayli.openapi.generics.server.core.schema.WrapperSchemaProcessor;
import io.github.bsayli.openapi.generics.server.core.schema.base.BaseSchemaRegistrar;
import io.github.bsayli.openapi.generics.server.core.validation.OpenApiContractGuard;
import io.github.bsayli.openapi.generics.server.mvc.MvcResponseTypeDiscoveryStrategy;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Auto-configuration entry point for generics-aware OpenAPI support.
 *
 * <p>This configuration assembles a <b>single deterministic pipeline</b>
 * for generating contract-aware OpenAPI schemas.
 *
 * <h2>Architecture</h2>
 *
 * <ul>
 *   <li>No multiple customizers</li>
 *   <li>No ordering hacks</li>
 *   <li>Single entry point → {@link OpenApiPipelineOrchestrator}</li>
 * </ul>
 *
 * <h2>Pipeline Flow</h2>
 *
 * <pre>
 * OpenApiCustomizer
 *        ↓
 * OpenApiPipelineOrchestrator
 *        ↓
 * [Base → Discovery → Introspection → Processing → Validation]
 * </pre>
 *
 * <h2>Design Guarantees</h2>
 *
 * <ul>
 *   <li><b>Deterministic</b> → single execution path</li>
 *   <li><b>Fail-fast</b> → validation enforced at the end</li>
 *   <li><b>Extensible</b> → replaceable components</li>
 *   <li><b>Non-intrusive</b> → backs off when user overrides beans</li>
 * </ul>
 */
@AutoConfiguration
@ConditionalOnClass(OpenApiCustomizer.class)
@ConditionalOnWebApplication
public class OpenApiGenericsAutoConfiguration {

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
  // Core components
  // -------------------------------------------------------------------------

  @Bean
  @ConditionalOnMissingBean
  public ResponseTypeIntrospector responseTypeIntrospector() {
    return new ResponseTypeIntrospector();
  }

  @Bean
  @ConditionalOnMissingBean
  public BaseSchemaRegistrar baseSchemaRegistrar() {
    return new BaseSchemaRegistrar();
  }

  @Bean
  @ConditionalOnMissingBean
  public WrapperSchemaEnricher wrapperSchemaEnricher() {
    return new WrapperSchemaEnricher();
  }

  @Bean
  @ConditionalOnMissingBean
  public WrapperSchemaProcessor wrapperSchemaProcessor(
          WrapperSchemaEnricher enricher,
          @Value("${app.openapi.wrapper.class-extra-annotation:}") String extraAnnotation) {

    return new WrapperSchemaProcessor(enricher, extraAnnotation);
  }

  @Bean
  @ConditionalOnMissingBean
  public OpenApiContractGuard openApiContractGuard() {
    return new OpenApiContractGuard();
  }

  // -------------------------------------------------------------------------
  // Orchestrator
  // -------------------------------------------------------------------------

  @Bean
  @ConditionalOnMissingBean
  public OpenApiPipelineOrchestrator openApiPipelineOrchestrator(
          BaseSchemaRegistrar baseSchemaRegistrar,
          ResponseTypeDiscoveryStrategy discoveryStrategy,
          ResponseTypeIntrospector introspector,
          WrapperSchemaProcessor wrapperSchemaProcessor,
          OpenApiContractGuard contractGuard) {

    return new OpenApiPipelineOrchestrator(
            baseSchemaRegistrar,
            discoveryStrategy,
            introspector,
            wrapperSchemaProcessor,
            contractGuard);
  }

  // -------------------------------------------------------------------------
  // Springdoc integration (single entry point)
  // -------------------------------------------------------------------------

  @Bean
  @ConditionalOnMissingBean(name = "openApiGenericsCustomizer")
  public OpenApiCustomizer openApiGenericsCustomizer(
          OpenApiPipelineOrchestrator orchestrator) {

    return orchestrator::run;
  }
}
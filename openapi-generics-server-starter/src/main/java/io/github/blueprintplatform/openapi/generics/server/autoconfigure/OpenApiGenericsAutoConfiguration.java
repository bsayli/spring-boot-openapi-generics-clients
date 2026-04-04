package io.github.blueprintplatform.openapi.generics.server.autoconfigure;

import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDiscoveryStrategy;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeIntrospector;
import io.github.blueprintplatform.openapi.generics.server.core.pipeline.OpenApiPipelineOrchestrator;
import io.github.blueprintplatform.openapi.generics.server.core.schema.WrapperSchemaEnricher;
import io.github.blueprintplatform.openapi.generics.server.core.schema.WrapperSchemaProcessor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.base.BaseSchemaRegistrar;
import io.github.blueprintplatform.openapi.generics.server.core.schema.base.SchemaGenerationControlMarker;
import io.github.blueprintplatform.openapi.generics.server.core.validation.OpenApiContractGuard;
import io.github.blueprintplatform.openapi.generics.server.mvc.MvcResponseTypeDiscoveryStrategy;
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
 * <p>This configuration assembles a <b>single deterministic pipeline</b> for generating
 * contract-aware OpenAPI schemas.
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
 * [Base → Discovery → Introspection → Processing → Ignore Marking → Validation]
 * </pre>
 *
 * <h2>Key Responsibilities</h2>
 *
 * <ul>
 *   <li>Registers infrastructure beans required for OpenAPI transformation</li>
 *   <li>Ensures pipeline components are replaceable via {@code @ConditionalOnMissingBean}</li>
 *   <li>Provides a single Springdoc integration point</li>
 * </ul>
 *
 * <h2>Design Guarantees</h2>
 *
 * <ul>
 *   <li><b>Deterministic</b> → single execution path</li>
 *   <li><b>Fail-fast</b> → validation enforced at the end</li>
 *   <li><b>Extensible</b> → replaceable components</li>
 *   <li><b>Non-intrusive</b> → backs off when user overrides beans</li>
 * </ul>
 *
 * <h2>Important</h2>
 *
 * <ul>
 *   <li>This class performs <b>dependency wiring only</b></li>
 *   <li>Execution logic resides in {@link OpenApiPipelineOrchestrator}</li>
 *   <li>{@link SchemaGenerationControlMarker} is injected into the pipeline but executed there</li>
 * </ul>
 */
@AutoConfiguration
@ConditionalOnClass(OpenApiCustomizer.class)
@ConditionalOnWebApplication
public class OpenApiGenericsAutoConfiguration {

  /**
   * Creates MVC-based response type discovery strategy.
   *
   * @param beanFactory Spring bean factory
   * @return discovery strategy for MVC environments
   */
  @Bean
  @ConditionalOnClass(RequestMappingHandlerMapping.class)
  @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
  @ConditionalOnMissingBean(ResponseTypeDiscoveryStrategy.class)
  public ResponseTypeDiscoveryStrategy mvcStrategy(ListableBeanFactory beanFactory) {
    return new MvcResponseTypeDiscoveryStrategy(beanFactory);
  }

  /**
   * Provides response type introspection logic.
   *
   * @return introspector instance
   */
  @Bean
  @ConditionalOnMissingBean
  public ResponseTypeIntrospector responseTypeIntrospector() {
    return new ResponseTypeIntrospector();
  }

  /**
   * Registers base schemas derived from the contract.
   *
   * @return base schema registrar
   */
  @Bean
  @ConditionalOnMissingBean
  public BaseSchemaRegistrar baseSchemaRegistrar() {
    return new BaseSchemaRegistrar();
  }

  /**
   * Controls schema generation visibility (ignore markers).
   *
   * @return schema generation control marker
   */
  @Bean
  @ConditionalOnMissingBean
  public SchemaGenerationControlMarker schemaGenerationControlMarker() {
    return new SchemaGenerationControlMarker();
  }

  /**
   * Provides wrapper schema enrichment logic.
   *
   * @return wrapper schema enricher
   */
  @Bean
  @ConditionalOnMissingBean
  public WrapperSchemaEnricher wrapperSchemaEnricher() {
    return new WrapperSchemaEnricher();
  }

  /**
   * Creates wrapper schema processor.
   *
   * @param enricher wrapper enricher
   * @param extraAnnotation optional extra annotation applied to generated wrappers
   * @return wrapper schema processor
   */
  @Bean
  @ConditionalOnMissingBean
  public WrapperSchemaProcessor wrapperSchemaProcessor(
          WrapperSchemaEnricher enricher,
          @Value("${app.openapi.wrapper.class-extra-annotation:}") String extraAnnotation) {

    return new WrapperSchemaProcessor(enricher, extraAnnotation);
  }

  /**
   * Provides contract validation logic.
   *
   * @return contract guard
   */
  @Bean
  @ConditionalOnMissingBean
  public OpenApiContractGuard openApiContractGuard() {
    return new OpenApiContractGuard();
  }

  /**
   * Creates the central OpenAPI pipeline orchestrator.
   *
   * @param baseSchemaRegistrar base schema registrar
   * @param schemaGenerationControlMarker generation control marker
   * @param discoveryStrategy response type discovery strategy
   * @param introspector response type introspector
   * @param wrapperSchemaProcessor wrapper processor
   * @param contractGuard contract guard
   * @return orchestrator instance
   */
  @Bean
  @ConditionalOnMissingBean
  public OpenApiPipelineOrchestrator openApiPipelineOrchestrator(
          BaseSchemaRegistrar baseSchemaRegistrar,
          SchemaGenerationControlMarker schemaGenerationControlMarker,
          ResponseTypeDiscoveryStrategy discoveryStrategy,
          ResponseTypeIntrospector introspector,
          WrapperSchemaProcessor wrapperSchemaProcessor,
          OpenApiContractGuard contractGuard) {

    return new OpenApiPipelineOrchestrator(
            baseSchemaRegistrar,
            schemaGenerationControlMarker,
            discoveryStrategy,
            introspector,
            wrapperSchemaProcessor,
            contractGuard);
  }

  /**
   * Registers the single Springdoc customization entry point.
   *
   * @param orchestrator pipeline orchestrator
   * @return OpenApiCustomizer delegating to orchestrator
   */
  @Bean
  @ConditionalOnMissingBean(name = "openApiGenericsCustomizer")
  public OpenApiCustomizer openApiGenericsCustomizer(OpenApiPipelineOrchestrator orchestrator) {
    return orchestrator::run;
  }
}
package io.github.bsayli.customerservice.common.openapi.autoreg;

import io.github.bsayli.customerservice.common.openapi.ApiResponseSchemaFactory;
import io.github.bsayli.customerservice.common.openapi.OpenApiSchemas;
import io.github.bsayli.customerservice.common.openapi.introspector.ResponseTypeIntrospector;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class AutoWrapperSchemaCustomizer {

  private final Set<String> dataRefs;
  private final String classExtraAnnotation;

  public AutoWrapperSchemaCustomizer(
      ListableBeanFactory beanFactory,
      ResponseTypeIntrospector introspector,
      @Value("${app.openapi.wrapper.class-extra-annotation:}") String classExtraAnnotation) {

    Set<String> refs = new LinkedHashSet<>();
    beanFactory
        .getBeansOfType(RequestMappingHandlerMapping.class)
        .values()
        .forEach(
            rmh ->
                rmh.getHandlerMethods().values().stream()
                    .map(HandlerMethod::getMethod)
                    .forEach(m -> introspector.extractDataRefName(m).ifPresent(refs::add)));

    this.dataRefs = Collections.unmodifiableSet(refs);
    this.classExtraAnnotation =
        (classExtraAnnotation == null || classExtraAnnotation.isBlank())
            ? null
            : classExtraAnnotation;
  }

  @Bean
  public OpenApiCustomizer autoResponseWrappers() {
    return openApi ->
        dataRefs.forEach(
            ref -> {
              String name = OpenApiSchemas.SCHEMA_SERVICE_RESPONSE + ref;
              openApi
                  .getComponents()
                  .addSchemas(
                      name,
                      ApiResponseSchemaFactory.createComposedWrapper(ref, classExtraAnnotation));
            });
  }
}

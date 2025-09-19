package com.example.demo.common.openapi.autoreg;

import com.example.demo.common.openapi.ApiResponseSchemaFactory;
import com.example.demo.common.openapi.OpenApiSchemas;
import com.example.demo.common.openapi.introspector.ResponseTypeIntrospector;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class AutoWrapperSchemaCustomizer {

  private final Set<String> dataRefs;

  public AutoWrapperSchemaCustomizer(
      ListableBeanFactory beanFactory, ResponseTypeIntrospector introspector) {
    Set<String> refs = new LinkedHashSet<>();
    Map<String, RequestMappingHandlerMapping> mappings =
        beanFactory.getBeansOfType(RequestMappingHandlerMapping.class);
    mappings
        .values()
        .forEach(
            rmh ->
                rmh.getHandlerMethods().values().stream()
                    .map(HandlerMethod::getMethod)
                    .forEach(m -> introspector.extractDataRefName(m).ifPresent(refs::add)));
    this.dataRefs = Collections.unmodifiableSet(refs);
  }

  @Bean
  public OpenApiCustomizer autoResponseWrappers() {
    return openApi ->
        dataRefs.forEach(
            ref -> {
              String name = OpenApiSchemas.SCHEMA_SERVICE_RESPONSE + ref;
              openApi
                  .getComponents()
                  .addSchemas(name, ApiResponseSchemaFactory.createComposedWrapper(ref));
            });
  }
}

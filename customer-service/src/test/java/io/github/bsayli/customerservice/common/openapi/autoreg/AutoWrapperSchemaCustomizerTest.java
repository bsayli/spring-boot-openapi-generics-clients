package io.github.bsayli.customerservice.common.openapi.autoreg;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.customerservice.common.openapi.OpenApiSchemas;
import io.github.bsayli.customerservice.common.openapi.introspector.ResponseTypeIntrospector;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Tag("unit")
@DisplayName("Unit Test: AutoWrapperSchemaCustomizer")
class AutoWrapperSchemaCustomizerTest {

  @Test
  @DisplayName("Registers composed wrapper schemas for all discovered data refs")
  void registersSchemas_forDiscoveredRefs() throws Exception {
    var beanFactory = mock(ListableBeanFactory.class);
    var handlerMapping = mock(RequestMappingHandlerMapping.class);
    var introspector = mock(ResponseTypeIntrospector.class);

    var controller = new SampleController();
    Method foo = SampleController.class.getMethod("foo");
    Method bar = SampleController.class.getMethod("bar");

    var handlerMap = new LinkedHashMap<RequestMappingInfo, HandlerMethod>();
    handlerMap.put(mock(RequestMappingInfo.class), new HandlerMethod(controller, foo));
    handlerMap.put(mock(RequestMappingInfo.class), new HandlerMethod(controller, bar));

    when(handlerMapping.getHandlerMethods()).thenReturn(handlerMap);
    when(beanFactory.getBeansOfType(RequestMappingHandlerMapping.class))
        .thenReturn(Map.of("rmh", handlerMapping));

    when(introspector.extractDataRefName(any(Method.class)))
        .then(
            inv -> {
              Method m = inv.getArgument(0);
              return switch (m.getName()) {
                case "foo" -> Optional.of("FooRef");
                case "bar" -> Optional.of("BarRef");
                default -> Optional.empty();
              };
            });

    var customizerCfg = new AutoWrapperSchemaCustomizer(beanFactory, introspector);
    OpenApiCustomizer customizer = customizerCfg.autoResponseWrappers();

    var openAPI = new OpenAPI().components(new Components());

    customizer.customise(openAPI);

    var schemas = openAPI.getComponents().getSchemas();
    assertNotNull(schemas, "schemas should be initialized");
    assertTrue(
        schemas.containsKey(OpenApiSchemas.SCHEMA_SERVICE_RESPONSE + "FooRef"),
        "Should contain composed schema for FooRef");
    assertTrue(
        schemas.containsKey(OpenApiSchemas.SCHEMA_SERVICE_RESPONSE + "BarRef"),
        "Should contain composed schema for BarRef");
  }

  @Test
  @DisplayName("Does nothing when no data refs are discovered")
  void noRefs_noSchemasAdded() {
    var beanFactory = mock(ListableBeanFactory.class);
    var introspector = mock(ResponseTypeIntrospector.class);

    when(beanFactory.getBeansOfType(RequestMappingHandlerMapping.class)).thenReturn(Map.of());

    var customizerCfg = new AutoWrapperSchemaCustomizer(beanFactory, introspector);
    OpenApiCustomizer customizer = customizerCfg.autoResponseWrappers();

    var openAPI = new OpenAPI().components(new Components());

    customizer.customise(openAPI);

    assertTrue(
        openAPI.getComponents().getSchemas() == null
            || openAPI.getComponents().getSchemas().isEmpty(),
        "No schemas should be added when no refs exist");
  }

  static class SampleController {
    public String foo() {
      return "ok";
    }

    public Integer bar() {
      return 42;
    }
  }
}

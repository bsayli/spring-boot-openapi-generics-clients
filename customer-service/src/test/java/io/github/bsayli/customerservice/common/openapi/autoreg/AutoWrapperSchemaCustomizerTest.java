package io.github.bsayli.customerservice.common.openapi.autoreg;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.customerservice.common.openapi.OpenApiSchemas;
import io.github.bsayli.customerservice.common.openapi.introspector.ResponseTypeIntrospector;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
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
  @DisplayName(
      "Registers composed wrapper schemas for discovered refs that already exist in components")
  void registersSchemas_forDiscoveredRefsThatExistInComponents() throws Exception {
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
        .thenAnswer(
            inv -> {
              Method m = inv.getArgument(0);
              return switch (m.getName()) {
                case "foo" -> Optional.of("FooRef");
                case "bar" -> Optional.of("BarRef");
                default -> Optional.empty();
              };
            });

    var customizerCfg = new AutoWrapperSchemaCustomizer(beanFactory, introspector, null);
    OpenApiCustomizer customizer = customizerCfg.autoResponseWrappers();

    // start without components/schemas to verify initializer behavior
    var openAPI = new OpenAPI();

    // but we must ensure data schemas exist, otherwise guard will skip
    customizer.customise(openAPI); // initializes components/schemas

    openAPI.getComponents().getSchemas().put("FooRef", new ObjectSchema());
    openAPI.getComponents().getSchemas().put("BarRef", new ObjectSchema());

    // run again to apply wrappers with guard satisfied
    customizer.customise(openAPI);

    var schemas = openAPI.getComponents().getSchemas();

    assertTrue(
        schemas.containsKey(OpenApiSchemas.SCHEMA_SERVICE_RESPONSE + "FooRef"),
        "Should contain composed schema for FooRef");
    assertTrue(
        schemas.containsKey(OpenApiSchemas.SCHEMA_SERVICE_RESPONSE + "BarRef"),
        "Should contain composed schema for BarRef");
  }

  @Test
  @DisplayName(
      "Skips wrapper registration when discovered refs are missing from components/schemas")
  void skipsRefsMissingFromComponentsSchemas() throws Exception {
    var beanFactory = mock(ListableBeanFactory.class);
    var handlerMapping = mock(RequestMappingHandlerMapping.class);
    var introspector = mock(ResponseTypeIntrospector.class);

    var controller = new SampleController();
    Method foo = SampleController.class.getMethod("foo");

    var handlerMap = new LinkedHashMap<RequestMappingInfo, HandlerMethod>();
    handlerMap.put(mock(RequestMappingInfo.class), new HandlerMethod(controller, foo));

    when(handlerMapping.getHandlerMethods()).thenReturn(handlerMap);
    when(beanFactory.getBeansOfType(RequestMappingHandlerMapping.class))
        .thenReturn(Map.of("rmh", handlerMapping));
    when(introspector.extractDataRefName(any(Method.class))).thenReturn(Optional.of("FooRef"));

    var customizerCfg = new AutoWrapperSchemaCustomizer(beanFactory, introspector, null);
    OpenApiCustomizer customizer = customizerCfg.autoResponseWrappers();

    var openAPI = new OpenAPI();
    customizer.customise(openAPI); // initializes components/schemas

    // note: FooRef schema is NOT added on purpose -> guard should skip
    assertFalse(
        openAPI
            .getComponents()
            .getSchemas()
            .containsKey(OpenApiSchemas.SCHEMA_SERVICE_RESPONSE + "FooRef"),
        "Wrapper must not be created when FooRef schema is missing");
  }

  @Test
  @DisplayName("Does nothing when no data refs are discovered (but initializes components/schemas)")
  void noRefs_noSchemasAdded() {
    var beanFactory = mock(ListableBeanFactory.class);
    var introspector = mock(ResponseTypeIntrospector.class);

    when(beanFactory.getBeansOfType(RequestMappingHandlerMapping.class)).thenReturn(Map.of());

    var customizerCfg = new AutoWrapperSchemaCustomizer(beanFactory, introspector, null);
    OpenApiCustomizer customizer = customizerCfg.autoResponseWrappers();

    var openAPI = new OpenAPI(); // no components

    customizer.customise(openAPI);

    assertNotNull(openAPI.getComponents(), "components should be initialized even when no refs");
    assertNotNull(openAPI.getComponents().getSchemas(), "schemas should be initialized");
    assertTrue(
        openAPI.getComponents().getSchemas().isEmpty(),
        "No schemas should be added when no refs exist");
  }

  @Test
  @DisplayName("Adds x-class-extra-annotation when classExtraAnnotation is provided")
  void addsClassExtraAnnotation_whenConfigured() throws Exception {
    var beanFactory = mock(ListableBeanFactory.class);
    var handlerMapping = mock(RequestMappingHandlerMapping.class);
    var introspector = mock(ResponseTypeIntrospector.class);

    var controller = new SampleController();
    Method foo = SampleController.class.getMethod("foo");

    var handlerMap = new LinkedHashMap<RequestMappingInfo, HandlerMethod>();
    handlerMap.put(mock(RequestMappingInfo.class), new HandlerMethod(controller, foo));

    when(handlerMapping.getHandlerMethods()).thenReturn(handlerMap);
    when(beanFactory.getBeansOfType(RequestMappingHandlerMapping.class))
        .thenReturn(Map.of("rmh", handlerMapping));
    when(introspector.extractDataRefName(any(Method.class))).thenReturn(Optional.of("FooRef"));

    String classExtraAnn =
        "@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)";

    var customizerCfg = new AutoWrapperSchemaCustomizer(beanFactory, introspector, classExtraAnn);
    OpenApiCustomizer customizer = customizerCfg.autoResponseWrappers();

    var openAPI = new OpenAPI();
    customizer.customise(openAPI); // initializes components/schemas

    // guard requires the underlying data schema
    openAPI.getComponents().getSchemas().put("FooRef", new ObjectSchema());

    customizer.customise(openAPI);

    var schemaName = OpenApiSchemas.SCHEMA_SERVICE_RESPONSE + "FooRef";
    var schema = openAPI.getComponents().getSchemas().get(schemaName);
    assertNotNull(schema, "schema must exist for FooRef");
    assertNotNull(schema.getExtensions(), "extensions map must be initialized");
    assertEquals(
        classExtraAnn,
        schema.getExtensions().get(OpenApiSchemas.EXT_CLASS_EXTRA_ANNOTATION),
        "x-class-extra-annotation must equal provided value");
  }

  @Test
  @DisplayName("Does not add x-class-extra-annotation when classExtraAnnotation is blank")
  void doesNotAddClassExtraAnnotation_whenBlank() throws Exception {
    var beanFactory = mock(ListableBeanFactory.class);
    var handlerMapping = mock(RequestMappingHandlerMapping.class);
    var introspector = mock(ResponseTypeIntrospector.class);

    var controller = new SampleController();
    Method foo = SampleController.class.getMethod("foo");

    var handlerMap = new LinkedHashMap<RequestMappingInfo, HandlerMethod>();
    handlerMap.put(mock(RequestMappingInfo.class), new HandlerMethod(controller, foo));

    when(handlerMapping.getHandlerMethods()).thenReturn(handlerMap);
    when(beanFactory.getBeansOfType(RequestMappingHandlerMapping.class))
        .thenReturn(Map.of("rmh", handlerMapping));
    when(introspector.extractDataRefName(any(Method.class))).thenReturn(Optional.of("FooRef"));

    var customizerCfg = new AutoWrapperSchemaCustomizer(beanFactory, introspector, "  ");
    OpenApiCustomizer customizer = customizerCfg.autoResponseWrappers();

    var openAPI = new OpenAPI();
    customizer.customise(openAPI); // initializes components/schemas

    // guard requires the underlying data schema
    openAPI.getComponents().getSchemas().put("FooRef", new ObjectSchema());

    customizer.customise(openAPI);

    var schemaName = OpenApiSchemas.SCHEMA_SERVICE_RESPONSE + "FooRef";
    var schema = openAPI.getComponents().getSchemas().get(schemaName);
    assertNotNull(schema, "schema must exist for FooRef");

    var ext = schema.getExtensions();
    assertTrue(
        ext == null || !ext.containsKey(OpenApiSchemas.EXT_CLASS_EXTRA_ANNOTATION),
        "x-class-extra-annotation must not be present when blank");
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

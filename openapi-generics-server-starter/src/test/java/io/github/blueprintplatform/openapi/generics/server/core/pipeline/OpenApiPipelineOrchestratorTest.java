package io.github.blueprintplatform.openapi.generics.server.core.pipeline;

import static io.github.blueprintplatform.openapi.generics.server.core.schema.constant.ContainerNames.PAGE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDescriptor;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeDiscoveryStrategy;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.ResponseTypeIntrospector;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerMatchMode;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerShape;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.ContainerSource;
import io.github.blueprintplatform.openapi.generics.server.core.introspection.container.descriptor.SupportedContainerDescriptor;
import io.github.blueprintplatform.openapi.generics.server.core.schema.ContractSchemaExclusionApplier;
import io.github.blueprintplatform.openapi.generics.server.core.schema.WrapperSchemaProcessor;
import io.github.blueprintplatform.openapi.generics.server.core.validation.OpenApiContractGuard;
import io.github.blueprintplatform.openapi.generics.server.exception.OpenApiProjectionException;
import io.swagger.v3.oas.models.OpenAPI;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;

@Tag("unit")
@DisplayName("Unit Test: OpenApiPipelineOrchestrator")
class OpenApiPipelineOrchestratorTest {

  @Test
  @DisplayName("run -> should execute full pipeline for discovered descriptors")
  void run_shouldExecuteFullPipeline() {
    ContractSchemaExclusionApplier marker = mock(ContractSchemaExclusionApplier.class);
    ResponseTypeDiscoveryStrategy discoveryStrategy = mock(ResponseTypeDiscoveryStrategy.class);
    ResponseTypeIntrospector introspector = mock(ResponseTypeIntrospector.class);
    WrapperSchemaProcessor wrapperSchemaProcessor = mock(WrapperSchemaProcessor.class);
    OpenApiContractGuard contractGuard = mock(OpenApiContractGuard.class);

    OpenApiPipelineOrchestrator orchestrator =
        new OpenApiPipelineOrchestrator(
            marker, discoveryStrategy, introspector, wrapperSchemaProcessor, contractGuard);

    OpenAPI openApi = new OpenAPI();

    ResolvableType type1 = ResolvableType.forClass(String.class);
    ResolvableType type2 = ResolvableType.forClass(Integer.class);

    ResponseTypeDescriptor descriptor1 =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

    ResponseTypeDescriptor descriptor2 =
        ResponseTypeDescriptor.container(
            ServiceResponse.class,
            "data",
            new SupportedContainerDescriptor(
                Page.class,
                PAGE,
                PAGE,
                ContainerShape.OBJECT_WITH_ITEM_ARRAY,
                "content",
                ContainerSource.BUILT_IN,
                ContainerMatchMode.EXACT),
            "OrderDto");

    when(discoveryStrategy.discover()).thenReturn(Set.of(type1, type2));
    when(introspector.extract(type1)).thenReturn(Optional.of(descriptor1));
    when(introspector.extract(type2)).thenReturn(Optional.of(descriptor2));

    orchestrator.run(openApi);

    verify(discoveryStrategy).discover();
    verify(introspector).extract(type1);
    verify(introspector).extract(type2);
    verify(wrapperSchemaProcessor).process(openApi, descriptor1);
    verify(wrapperSchemaProcessor).process(openApi, descriptor2);
    verify(marker).apply(openApi, Set.of(descriptor1, descriptor2));
    verify(contractGuard).validate(openApi, Set.of(descriptor1, descriptor2));
    verifyNoMoreInteractions(wrapperSchemaProcessor, marker, contractGuard);
  }

  @Test
  @DisplayName("run -> should skip unsupported response types")
  void run_shouldSkipUnsupportedResponseTypes() {
    ContractSchemaExclusionApplier marker = mock(ContractSchemaExclusionApplier.class);
    ResponseTypeDiscoveryStrategy discoveryStrategy = mock(ResponseTypeDiscoveryStrategy.class);
    ResponseTypeIntrospector introspector = mock(ResponseTypeIntrospector.class);
    WrapperSchemaProcessor wrapperSchemaProcessor = mock(WrapperSchemaProcessor.class);
    OpenApiContractGuard contractGuard = mock(OpenApiContractGuard.class);

    OpenApiPipelineOrchestrator orchestrator =
        new OpenApiPipelineOrchestrator(
            marker, discoveryStrategy, introspector, wrapperSchemaProcessor, contractGuard);

    OpenAPI openApi = new OpenAPI();

    ResolvableType supported = ResolvableType.forClass(String.class);
    ResolvableType unsupported = ResolvableType.forClass(Double.class);

    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

    when(discoveryStrategy.discover()).thenReturn(Set.of(supported, unsupported));
    when(introspector.extract(supported)).thenReturn(Optional.of(descriptor));
    when(introspector.extract(unsupported)).thenReturn(Optional.empty());

    orchestrator.run(openApi);

    verify(wrapperSchemaProcessor).process(openApi, descriptor);
    verify(marker).apply(openApi, Set.of(descriptor));
    verify(contractGuard).validate(openApi, Set.of(descriptor));
    verifyNoMoreInteractions(wrapperSchemaProcessor, marker, contractGuard);
  }

  @Test
  @DisplayName("run -> should continue with empty descriptor set")
  void run_shouldContinueWithEmptyDescriptorSet() {
    ContractSchemaExclusionApplier marker = mock(ContractSchemaExclusionApplier.class);
    ResponseTypeDiscoveryStrategy discoveryStrategy = mock(ResponseTypeDiscoveryStrategy.class);
    ResponseTypeIntrospector introspector = mock(ResponseTypeIntrospector.class);
    WrapperSchemaProcessor wrapperSchemaProcessor = mock(WrapperSchemaProcessor.class);
    OpenApiContractGuard contractGuard = mock(OpenApiContractGuard.class);

    OpenApiPipelineOrchestrator orchestrator =
        new OpenApiPipelineOrchestrator(
            marker, discoveryStrategy, introspector, wrapperSchemaProcessor, contractGuard);

    OpenAPI openApi = new OpenAPI();
    ResolvableType unsupported = ResolvableType.forClass(String.class);

    when(discoveryStrategy.discover()).thenReturn(Set.of(unsupported));
    when(introspector.extract(unsupported)).thenReturn(Optional.empty());

    orchestrator.run(openApi);

    verify(discoveryStrategy).discover();
    verify(introspector).extract(unsupported);
    verifyNoInteractions(wrapperSchemaProcessor);
    verify(marker).apply(openApi, Set.of());
    verify(contractGuard).validate(openApi, Set.of());
  }

  @Test
  @DisplayName("run -> should execute pipeline only once per OpenAPI instance")
  void run_shouldExecuteOnlyOncePerOpenApiInstance() {
    ContractSchemaExclusionApplier marker = mock(ContractSchemaExclusionApplier.class);
    ResponseTypeDiscoveryStrategy discoveryStrategy = mock(ResponseTypeDiscoveryStrategy.class);
    ResponseTypeIntrospector introspector = mock(ResponseTypeIntrospector.class);
    WrapperSchemaProcessor wrapperSchemaProcessor = mock(WrapperSchemaProcessor.class);
    OpenApiContractGuard contractGuard = mock(OpenApiContractGuard.class);

    OpenApiPipelineOrchestrator orchestrator =
        new OpenApiPipelineOrchestrator(
            marker, discoveryStrategy, introspector, wrapperSchemaProcessor, contractGuard);

    OpenAPI openApi = new OpenAPI();
    ResolvableType type = ResolvableType.forClass(String.class);
    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

    when(discoveryStrategy.discover()).thenReturn(Set.of(type));
    when(introspector.extract(type)).thenReturn(Optional.of(descriptor));

    orchestrator.run(openApi);
    orchestrator.run(openApi);

    verify(discoveryStrategy, times(1)).discover();
    verify(introspector, times(1)).extract(type);
    verify(wrapperSchemaProcessor, times(1)).process(openApi, descriptor);
    verify(marker, times(1)).apply(openApi, Set.of(descriptor));
    verify(contractGuard, times(1)).validate(openApi, Set.of(descriptor));
  }

  @Test
  @DisplayName("run -> should process different OpenAPI instances independently")
  void run_shouldProcessDifferentOpenApiInstancesIndependently() {
    ContractSchemaExclusionApplier marker = mock(ContractSchemaExclusionApplier.class);
    ResponseTypeDiscoveryStrategy discoveryStrategy = mock(ResponseTypeDiscoveryStrategy.class);
    ResponseTypeIntrospector introspector = mock(ResponseTypeIntrospector.class);
    WrapperSchemaProcessor wrapperSchemaProcessor = mock(WrapperSchemaProcessor.class);
    OpenApiContractGuard contractGuard = mock(OpenApiContractGuard.class);

    OpenApiPipelineOrchestrator orchestrator =
        new OpenApiPipelineOrchestrator(
            marker, discoveryStrategy, introspector, wrapperSchemaProcessor, contractGuard);

    OpenAPI openApi1 = new OpenAPI();
    OpenAPI openApi2 = new OpenAPI();

    ResolvableType type = ResolvableType.forClass(String.class);
    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

    when(discoveryStrategy.discover()).thenReturn(Set.of(type));
    when(introspector.extract(type)).thenReturn(Optional.of(descriptor));

    orchestrator.run(openApi1);
    orchestrator.run(openApi2);

    verify(discoveryStrategy, times(2)).discover();
    verify(introspector, times(2)).extract(type);
    verify(wrapperSchemaProcessor).process(same(openApi1), eq(descriptor));
    verify(wrapperSchemaProcessor).process(same(openApi2), eq(descriptor));
    verify(marker).apply(same(openApi1), eq(Set.of(descriptor)));
    verify(marker).apply(same(openApi2), eq(Set.of(descriptor)));
    verify(contractGuard).validate(same(openApi1), eq(Set.of(descriptor)));
    verify(contractGuard).validate(same(openApi2), eq(Set.of(descriptor)));
  }

  @Test
  @DisplayName("run -> should propagate schema processing failure")
  void run_shouldPropagateSchemaProcessingFailure() {
    ContractSchemaExclusionApplier marker = mock(ContractSchemaExclusionApplier.class);
    ResponseTypeDiscoveryStrategy discoveryStrategy = mock(ResponseTypeDiscoveryStrategy.class);
    ResponseTypeIntrospector introspector = mock(ResponseTypeIntrospector.class);
    WrapperSchemaProcessor wrapperSchemaProcessor = mock(WrapperSchemaProcessor.class);
    OpenApiContractGuard contractGuard = mock(OpenApiContractGuard.class);

    OpenApiPipelineOrchestrator orchestrator =
        new OpenApiPipelineOrchestrator(
            marker, discoveryStrategy, introspector, wrapperSchemaProcessor, contractGuard);

    OpenAPI openApi = new OpenAPI();
    ResolvableType type = ResolvableType.forClass(String.class);
    ResponseTypeDescriptor descriptor =
        ResponseTypeDescriptor.simple(ServiceResponse.class, "data", "CustomerDto");

    when(discoveryStrategy.discover()).thenReturn(Set.of(type));
    when(introspector.extract(type)).thenReturn(Optional.of(descriptor));

    OpenApiProjectionException failure = new OpenApiProjectionException("schema failure");

    doThrow(failure).when(wrapperSchemaProcessor).process(openApi, descriptor);

    OpenApiProjectionException exception =
        assertThrows(OpenApiProjectionException.class, () -> orchestrator.run(openApi));

    assertSame(failure, exception);
    assertEquals("schema failure", exception.getMessage());

    verify(wrapperSchemaProcessor).process(openApi, descriptor);
    verifyNoInteractions(marker, contractGuard);
  }
}

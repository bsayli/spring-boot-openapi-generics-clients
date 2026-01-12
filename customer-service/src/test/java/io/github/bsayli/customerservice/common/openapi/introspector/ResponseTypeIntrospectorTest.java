package io.github.bsayli.customerservice.common.openapi.introspector;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.apicontract.envelope.ServiceResponse;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncTask;

@Tag("unit")
@DisplayName("Unit Test: ResponseTypeIntrospector")
class ResponseTypeIntrospectorTest {

  private final ResponseTypeIntrospector introspector = new ResponseTypeIntrospector();

  private Method method(String name) throws Exception {
    for (Method m : Samples.class.getDeclaredMethods()) {
      if (m.getName().equals(name)) return m;
    }
    throw new NoSuchMethodException(name);
  }

  @Test
  @DisplayName("Returns T simple name for plain ServiceResponse<T>")
  void plain_serviceResponse() throws Exception {
    Optional<String> ref = introspector.extractDataRefName(method("plain"));
    assertEquals(Optional.of("Foo"), ref);
  }

  @Test
  @DisplayName("Unwraps ResponseEntity<ServiceResponse<T>> to T")
  void responseEntity_wrapper() throws Exception {
    assertEquals(Optional.of("Foo"), introspector.extractDataRefName(method("responseEntity")));
  }

  @Test
  @DisplayName("Unwraps CompletionStage<ServiceResponse<T>> to T")
  void completionStage_wrapper() throws Exception {
    assertEquals(Optional.of("Foo"), introspector.extractDataRefName(method("completionStage")));
  }

  @Test
  @DisplayName("Unwraps CompletableFuture<ServiceResponse<T>> to T")
  void future_wrapper() throws Exception {
    assertEquals(Optional.of("Foo"), introspector.extractDataRefName(method("future")));
  }

  @Test
  @DisplayName("Unwraps DeferredResult<ServiceResponse<T>> to T")
  void deferredResult_wrapper() throws Exception {
    assertEquals(Optional.of("Foo"), introspector.extractDataRefName(method("deferredResult")));
  }

  @Test
  @DisplayName("Unwraps WebAsyncTask<ServiceResponse<T>> to T")
  void webAsyncTask_wrapper() throws Exception {
    assertEquals(Optional.of("Foo"), introspector.extractDataRefName(method("webAsyncTask")));
  }

  @Test
  @DisplayName("Unwraps deeply nested wrappers down to ServiceResponse<T>")
  void deepNesting_unwraps() throws Exception {
    assertEquals(Optional.of("Bar"), introspector.extractDataRefName(method("deepNesting")));
  }

  @Test
  @DisplayName("Returns empty when return type is not a wrapper")
  void notAWrapper_empty() throws Exception {
    assertTrue(introspector.extractDataRefName(method("notAWrapper")).isEmpty());
  }

  static class Samples {
    public ServiceResponse<Foo> plain() {
      return null;
    }

    public ResponseEntity<ServiceResponse<Foo>> responseEntity() {
      return null;
    }

    public CompletionStage<ServiceResponse<Foo>> completionStage() {
      return null;
    }

    public CompletableFuture<ServiceResponse<Foo>> future() {
      return null;
    }

    public DeferredResult<ServiceResponse<Foo>> deferredResult() {
      return null;
    }

    public WebAsyncTask<ServiceResponse<Foo>> webAsyncTask() {
      return null;
    }

    public ResponseEntity<CompletionStage<DeferredResult<ServiceResponse<Bar>>>> deepNesting() {
      return null;
    }

    @SuppressWarnings("rawtypes")
    public ServiceResponse rawServiceResponse() {
      return null;
    }

    public Foo notAWrapper() {
      return null;
    }

    static class Foo {}

    static class Bar {}
  }
}

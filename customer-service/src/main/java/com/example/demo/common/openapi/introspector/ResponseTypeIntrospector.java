package com.example.demo.common.openapi.introspector;

import com.example.demo.common.api.response.ServiceResponse;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.core.ResolvableType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ResponseTypeIntrospector {

  private static final List<String> ASYNC_WRAPPERS =
      List.of(
          "reactor.core.publisher.Mono",
          "reactor.core.publisher.Flux",
          "java.util.concurrent.CompletionStage",
          "java.util.concurrent.Future",
          "org.springframework.web.context.request.async.DeferredResult",
          "org.springframework.web.context.request.async.WebAsyncTask");

  public Optional<String> extractDataRefName(Method method) {
    if (method == null) return Optional.empty();

    ResolvableType rt = ResolvableType.forMethodReturnType(method);

    rt = unwrapIf(rt);

    for (String wrapper : ASYNC_WRAPPERS) {
      rt = unwrapIf(rt, wrapper);
    }

    Class<?> raw = rt.resolve();
    if (raw == null || !ServiceResponse.class.isAssignableFrom(raw)) {
      return Optional.empty();
    }

    if (rt.getGenerics().length == 0) {
      return Optional.empty();
    }

    Class<?> dataClass = rt.getGeneric(0).resolve();
    return Optional.ofNullable(dataClass).map(Class::getSimpleName);
  }

  private ResolvableType unwrapIf(ResolvableType type) {
    Class<?> raw = type.resolve();
    if (raw != null && ResponseEntity.class.isAssignableFrom(raw)) {
      return type.getGeneric(0);
    }
    return type;
  }

  private ResolvableType unwrapIf(ResolvableType type, String wrapperClassName) {
    Class<?> raw = type.resolve();
    if (raw != null && Objects.equals(raw.getName(), wrapperClassName)) {
      return type.getGeneric(0);
    }
    return type;
  }
}

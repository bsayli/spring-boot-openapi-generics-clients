package io.github.bsayli.customerservice.common.openapi.introspector;

import io.github.bsayli.customerservice.common.api.response.ServiceResponse;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncTask;

@Component
public final class ResponseTypeIntrospector {

  private static final Logger log = LoggerFactory.getLogger(ResponseTypeIntrospector.class);

  private static final int MAX_UNWRAP_DEPTH = 8;

  private static final Set<String> REACTOR_WRAPPERS =
      Set.of("reactor.core.publisher.Mono", "reactor.core.publisher.Flux");

  public Optional<String> extractDataRefName(Method method) {
    if (method == null) return Optional.empty();

    ResolvableType type = ResolvableType.forMethodReturnType(method);
    type = unwrapToServiceResponse(type);

    Class<?> raw = type.resolve();
    if (raw == null || !ServiceResponse.class.isAssignableFrom(raw)) return Optional.empty();
    if (!type.hasGenerics()) return Optional.empty();

    Class<?> dataClass = type.getGeneric(0).resolve();
    Optional<String> ref = Optional.ofNullable(dataClass).map(Class::getSimpleName);

    if (log.isDebugEnabled()) {
      log.debug(
          "Introspected method [{}]: wrapper [{}], data [{}]",
          method.toGenericString(),
          raw.getSimpleName(),
          ref.orElse("<none>"));
    }

    return ref;
  }

  private ResolvableType unwrapToServiceResponse(ResolvableType type) {
    for (int guard = 0; guard < MAX_UNWRAP_DEPTH; guard++) {
      Class<?> raw = type.resolve();
      if (raw == null || ServiceResponse.class.isAssignableFrom(raw)) {
        return type;
      }
      ResolvableType next = nextLayer(type, raw);
      if (next == null) {
        return type;
      }
      type = next;
    }
    return type;
  }

  private ResolvableType nextLayer(ResolvableType current, Class<?> raw) {
    return switch (raw) {
      case Class<?> c when ResponseEntity.class.isAssignableFrom(c) -> current.getGeneric(0);
      case Class<?> c
          when CompletionStage.class.isAssignableFrom(c) || Future.class.isAssignableFrom(c) ->
          current.getGeneric(0);
      case Class<?> c
          when DeferredResult.class.isAssignableFrom(c) || WebAsyncTask.class.isAssignableFrom(c) ->
          current.getGeneric(0);
      case Class<?> c when REACTOR_WRAPPERS.contains(c.getName()) -> current.getGeneric(0);
      default -> null;
    };
  }
}

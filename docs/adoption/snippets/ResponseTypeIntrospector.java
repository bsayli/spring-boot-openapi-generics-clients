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

    ResolvableType t = ResolvableType.forMethodReturnType(method);
    t = unwrapToServiceResponse(t);

    Class<?> raw = t.resolve();
    if (raw == null || !ServiceResponse.class.isAssignableFrom(raw)) return Optional.empty();
    if (!t.hasGenerics()) return Optional.empty();

    ResolvableType dataType = t.getGeneric(0);
    String ref = buildRefName(dataType);

    if (log.isDebugEnabled()) {
      log.debug("Introspected method [{}]: dataRef={}", method.toGenericString(), ref);
    }
    return Optional.of(ref);
  }

  private ResolvableType unwrapToServiceResponse(ResolvableType type) {
    for (int i = 0; i < MAX_UNWRAP_DEPTH; i++) {
      Class<?> raw = type.resolve();
      if (raw == null || ServiceResponse.class.isAssignableFrom(raw)) return type;
      ResolvableType next = nextLayer(type, raw);
      if (next == null) return type;
      type = next;
    }
    return type;
  }

  private ResolvableType nextLayer(ResolvableType current, Class<?> raw) {
    if (ResponseEntity.class.isAssignableFrom(raw)) return current.getGeneric(0);
    if (CompletionStage.class.isAssignableFrom(raw) || Future.class.isAssignableFrom(raw))
      return current.getGeneric(0);
    if (DeferredResult.class.isAssignableFrom(raw) || WebAsyncTask.class.isAssignableFrom(raw))
      return current.getGeneric(0);
    if (REACTOR_WRAPPERS.contains(raw.getName())) return current.getGeneric(0);
    return null;
  }

  private String buildRefName(ResolvableType type) {
    Class<?> raw = type.resolve();
    if (raw == null) return "Object";
    String base = raw.getSimpleName();
    if (!type.hasGenerics()) return base;

    StringBuilder sb = new StringBuilder(base);
    for (ResolvableType g : type.getGenerics()) {
      sb.append(buildRefName(g));
    }
    return sb.toString();
  }
}

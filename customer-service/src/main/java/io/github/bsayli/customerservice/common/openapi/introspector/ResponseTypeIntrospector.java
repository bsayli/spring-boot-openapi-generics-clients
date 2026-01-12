package io.github.bsayli.customerservice.common.openapi.introspector;

import io.github.bsayli.apicontract.envelope.ServiceResponse;
import io.github.bsayli.apicontract.paging.Page;
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

  private static final String FALLBACK_OBJECT_REF = "Object";

  private static final Set<String> REACTOR_WRAPPERS =
      Set.of("reactor.core.publisher.Mono", "reactor.core.publisher.Flux");

  public Optional<String> extractDataRefName(Method method) {
    if (method == null) return Optional.empty();

    ResolvableType type = ResolvableType.forMethodReturnType(method);
    type = unwrapToServiceResponse(type);

    Class<?> raw = type.resolve();
    if (raw == null || !ServiceResponse.class.isAssignableFrom(raw)) return Optional.empty();
    if (!type.hasGenerics()) return Optional.empty();

    ResolvableType dataType = type.getGeneric(0);
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

    if (CompletionStage.class.isAssignableFrom(raw) || Future.class.isAssignableFrom(raw)) {
      return current.getGeneric(0);
    }

    if (DeferredResult.class.isAssignableFrom(raw) || WebAsyncTask.class.isAssignableFrom(raw)) {
      return current.getGeneric(0);
    }

    if (REACTOR_WRAPPERS.contains(raw.getName())) return current.getGeneric(0);

    return null;
  }

  /**
   * Contract rule: - Nested generics are supported ONLY for Page<T>. - For any other generic type
   * (List<T>, Map<K,V>, Foo<Bar>), generics are ignored and only the raw type name is used.
   */
  private String buildRefName(ResolvableType type) {
    Class<?> raw = type.resolve();
    if (raw == null) return FALLBACK_OBJECT_REF;

    // Special-case: Page<T> is the only allowed "container" that contributes its item type
    if (Page.class.isAssignableFrom(raw)) {
      ResolvableType itemType = safeGeneric(type, 0);
      String itemRef = buildRefName(itemType);
      return raw.getSimpleName() + itemRef;
    }

    // Default: ignore generics to keep the contract deterministic
    return raw.getSimpleName();
  }

  private ResolvableType safeGeneric(ResolvableType type, int index) {
    if (type == null || !type.hasGenerics()) return ResolvableType.forClass(Object.class);

    ResolvableType[] generics = type.getGenerics();
    if (index < 0 || index >= generics.length) return ResolvableType.forClass(Object.class);

    ResolvableType g = generics[index];
    return (g.resolve() == null) ? ResolvableType.forClass(Object.class) : g;
  }
}

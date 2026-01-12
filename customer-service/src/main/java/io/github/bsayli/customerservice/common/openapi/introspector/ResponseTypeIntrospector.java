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

/**
 * Detects contract-aware response shapes from Spring MVC controller return types.
 *
 * <p>This component exists to keep the published OpenAPI specification deterministic for a small,
 * explicitly supported set of generic shapes. It does <b>not</b> try to "understand generics in
 * general".
 *
 * <h2>What it does</h2>
 *
 * <ul>
 *   <li>Unwraps common wrappers (e.g. {@code ResponseEntity}, async wrappers) until it reaches
 *       {@code ServiceResponse<T>}.
 *   <li>Extracts a deterministic component schema name for the {@code data} part of the response
 *       <b>only</b> for the shapes guaranteed by this setup.
 * </ul>
 *
 * <h2>Guaranteed shapes</h2>
 *
 * <ul>
 *   <li>{@code ServiceResponse<T>} where {@code T} is a non-generic DTO type
 *   <li>{@code ServiceResponse<Page<T>>} (pagination-only nested generic)
 * </ul>
 *
 * <h2>Everything else</h2>
 *
 * <p>Other compositions such as {@code ServiceResponse<List<T>>}, {@code
 * ServiceResponse<Map<K,V>>}, or arbitrary nested generics are intentionally treated as out of
 * scope for auto-wrapper registration. In those cases, this introspector returns {@link
 * java.util.Optional#empty()} so the OpenAPI output remains on Springdoc defaults and client
 * generation follows OpenAPI Generator's default behavior.
 *
 * <p>In short: this class defines <b>what the server explicitly marks as contract-aware</b> in the
 * spec — not what Java could theoretically express.
 */
@Component
public final class ResponseTypeIntrospector {

  private static final Logger log = LoggerFactory.getLogger(ResponseTypeIntrospector.class);

  // Prevent infinite recursion on exotic wrappers
  private static final int MAX_UNWRAP_DEPTH = 8;

  private static final Set<String> REACTOR_WRAPPERS =
      Set.of("reactor.core.publisher.Mono", "reactor.core.publisher.Flux");

  /**
   * Extracts the schema ref name for the {@code data} field of {@code ServiceResponse<T>}.
   *
   * <p><b>Guaranteed shapes</b> (auto-registered wrapper schemas):
   *
   * <ul>
   *   <li>{@code ServiceResponse<T>} where {@code T} is a non-generic DTO type
   *   <li>{@code ServiceResponse<Page<T>>}
   * </ul>
   *
   * <p><b>Out of scope</b> (left to Springdoc/OpenAPI Generator defaults):
   *
   * <ul>
   *   <li>{@code ServiceResponse<List<T>>}, {@code ServiceResponse<Map<K,V>>}, {@code
   *       ServiceResponse<Foo<Bar>>}, ...
   * </ul>
   *
   * <p>For out-of-scope shapes, this method returns {@link Optional#empty()} so no wrapper schema
   * is auto-registered for them.
   */
  public Optional<String> extractDataRefName(Method method) {
    if (method == null) return Optional.empty();

    ResolvableType type = ResolvableType.forMethodReturnType(method);
    type = unwrapToServiceResponse(type);

    Class<?> raw = type.resolve();
    if (raw == null || !ServiceResponse.class.isAssignableFrom(raw)) return Optional.empty();
    if (!type.hasGenerics()) return Optional.empty();

    ResolvableType dataType = type.getGeneric(0);

    // We only auto-register wrappers for guaranteed shapes.
    Optional<String> refOpt = buildGuaranteedRefName(dataType);

    if (log.isDebugEnabled()) {
      log.debug(
          "Introspected method [{}]: dataType={}, guaranteedRef={}",
          method.toGenericString(),
          safeToString(dataType),
          refOpt.orElse("<default>"));
    }

    return refOpt;
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
   * Returns a deterministic wrapper-suffix ref name ONLY for the shapes explicitly guaranteed by
   * the contract.
   *
   * <p>Rules:
   *
   * <ul>
   *   <li>If {@code data} is {@code Page<T>}, return {@code "Page" + <TRef>} (e.g. {@code
   *       PageCustomerDto}).
   *   <li>If {@code data} is a plain (non-generic) DTO type, return its simple name (e.g. {@code
   *       CustomerDto}).
   *   <li>Otherwise (List/Map/any other generic container), return empty to preserve default
   *       generator behavior.
   * </ul>
   */
  private Optional<String> buildGuaranteedRefName(ResolvableType dataType) {
    if (dataType == null) return Optional.empty();

    Class<?> raw = dataType.resolve();
    if (raw == null) return Optional.empty();

    // Guaranteed nested container: Page<T>
    if (Page.class.isAssignableFrom(raw)) {
      ResolvableType itemType = safeGeneric(dataType, 0);
      Class<?> itemRaw = itemType.resolve();
      if (itemRaw == null) return Optional.empty();

      // Only allow Page<Dto> where Dto resolves cleanly
      return Optional.of(raw.getSimpleName() + itemRaw.getSimpleName());
    }

    // Guaranteed simple DTO: T without generics
    if (!dataType.hasGenerics()) {
      return Optional.of(raw.getSimpleName());
    }

    // Everything else is out of contract scope: do not auto-register wrapper schemas.
    return Optional.empty();
  }

  private ResolvableType safeGeneric(ResolvableType type, int index) {
    if (type == null || !type.hasGenerics()) return ResolvableType.forClass(Object.class);

    ResolvableType[] generics = type.getGenerics();
    if (index < 0 || index >= generics.length) return ResolvableType.forClass(Object.class);

    ResolvableType g = generics[index];
    return (g.resolve() == null) ? ResolvableType.forClass(Object.class) : g;
  }

  private String safeToString(ResolvableType type) {
    try {
      return String.valueOf(type);
    } catch (Exception ignored) {
      return "<unprintable>";
    }
  }
}

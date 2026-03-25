package io.github.bsayli.customerservice.common.openapi.introspector;

import io.github.bsayli.apicontract.envelope.ServiceResponse;
import io.github.bsayli.apicontract.paging.Page;
import java.lang.reflect.Method;
import java.util.Optional;
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
 * explicitly supported set of response shapes. It does not attempt to interpret Java generics in
 * general.
 *
 * <p>Supported contract-aware shapes:
 *
 * <ul>
 *   <li>{@code ServiceResponse<T>} where {@code T} is a plain non-generic DTO type
 *   <li>{@code ServiceResponse<Page<T>>}
 * </ul>
 *
 * <p>Everything else is intentionally left outside this contract-aware scope. Shapes such as {@code
 * ServiceResponse<List<T>>}, {@code ServiceResponse<Map<K,V>>}, or arbitrary nested generics return
 * {@link Optional#empty()} so Springdoc and OpenAPI Generator can continue with their default
 * behavior.
 *
 * <p>This class therefore defines what the server explicitly marks as contract-aware in the
 * published specification, not every shape Java could theoretically express.
 */
@Component
public final class ResponseTypeIntrospector {

  private static final Logger log = LoggerFactory.getLogger(ResponseTypeIntrospector.class);

  /** Maximum unwrap depth used as a guard against unusual or recursive wrapper combinations. */
  private static final int MAX_UNWRAP_DEPTH = 8;

  /**
   * Extracts the deterministic schema reference suffix for the {@code data} field inside {@code
   * ServiceResponse<T>}.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>{@code ServiceResponse<CustomerDto>} -> {@code CustomerDto}
   *   <li>{@code ServiceResponse<Page<CustomerDto>>} -> {@code PageCustomerDto}
   * </ul>
   *
   * <p>If the method return type does not resolve to a supported contract-aware shape, this method
   * returns {@link Optional#empty()}.
   *
   * @param method controller method to inspect
   * @return wrapper data schema suffix when the shape is explicitly supported, otherwise empty
   */
  public Optional<String> extractDataRefName(Method method) {
    if (method == null) return Optional.empty();

    ResolvableType type = ResolvableType.forMethodReturnType(method);
    type = unwrapToServiceResponse(type);

    Class<?> raw = type.resolve();
    if (raw == null || !ServiceResponse.class.isAssignableFrom(raw)) return Optional.empty();
    if (!type.hasGenerics()) return Optional.empty();

    ResolvableType dataType = type.getGeneric(0);
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

  /**
   * Unwraps common Spring MVC and Java async wrappers until {@code ServiceResponse<?>} is reached,
   * or until no further supported wrapper can be removed.
   *
   * @param type method return type to unwrap
   * @return unwrapped type, possibly already equal to the input when no supported wrapper exists
   */
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

  /**
   * Returns the next inner layer for a supported wrapper type.
   *
   * <p>Supported wrappers here are limited to common Spring MVC and Java async abstractions:
   *
   * <ul>
   *   <li>{@link ResponseEntity}
   *   <li>{@link CompletionStage}
   *   <li>{@link Future}
   *   <li>{@link DeferredResult}
   *   <li>{@link WebAsyncTask}
   * </ul>
   *
   * @param current current wrapper type
   * @param raw resolved raw class of the current wrapper
   * @return inner generic layer when the wrapper is supported, otherwise {@code null}
   */
  private ResolvableType nextLayer(ResolvableType current, Class<?> raw) {
    if (ResponseEntity.class.isAssignableFrom(raw)) {
      return current.getGeneric(0);
    }

    if (CompletionStage.class.isAssignableFrom(raw) || Future.class.isAssignableFrom(raw)) {
      return current.getGeneric(0);
    }

    if (DeferredResult.class.isAssignableFrom(raw) || WebAsyncTask.class.isAssignableFrom(raw)) {
      return current.getGeneric(0);
    }

    return null;
  }

  /**
   * Builds the deterministic schema reference suffix only for the explicitly supported
   * contract-aware shapes.
   *
   * <p>Rules:
   *
   * <ul>
   *   <li>{@code Page<T>} -> {@code Page + TSimpleName}
   *   <li>Plain non-generic DTO type -> its simple class name
   *   <li>Anything else -> empty
   * </ul>
   *
   * @param dataType resolved {@code T} inside {@code ServiceResponse<T>}
   * @return deterministic suffix for supported shapes, otherwise empty
   */
  private Optional<String> buildGuaranteedRefName(ResolvableType dataType) {
    if (dataType == null) return Optional.empty();

    Class<?> raw = dataType.resolve();
    if (raw == null) return Optional.empty();

    if (Page.class.isAssignableFrom(raw)) {
      ResolvableType itemType = safeGeneric(dataType, 0);
      Class<?> itemRaw = itemType.resolve();
      if (itemRaw == null) return Optional.empty();

      return Optional.of(raw.getSimpleName() + itemRaw.getSimpleName());
    }

    if (!dataType.hasGenerics()) {
      return Optional.of(raw.getSimpleName());
    }

    return Optional.empty();
  }

  /**
   * Safely reads a generic parameter by index.
   *
   * <p>If the type has no generics, the index is invalid, or the generic cannot be resolved, this
   * method returns {@code Object.class} as a defensive fallback.
   *
   * @param type source type
   * @param index generic parameter index
   * @return resolved generic type or {@code Object.class} fallback
   */
  private ResolvableType safeGeneric(ResolvableType type, int index) {
    if (type == null || !type.hasGenerics()) {
      return ResolvableType.forClass(Object.class);
    }

    ResolvableType[] generics = type.getGenerics();
    if (index < 0 || index >= generics.length) {
      return ResolvableType.forClass(Object.class);
    }

    ResolvableType generic = generics[index];
    return generic.resolve() == null ? ResolvableType.forClass(Object.class) : generic;
  }

  /**
   * Converts a {@link ResolvableType} into a safe debug string without allowing formatting failures
   * to affect the caller.
   *
   * @param type type to render
   * @return printable representation or {@code <unprintable>} fallback
   */
  private String safeToString(ResolvableType type) {
    try {
      return String.valueOf(type);
    } catch (Exception ignored) {
      return "<unprintable>";
    }
  }
}

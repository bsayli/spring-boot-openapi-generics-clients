package io.github.bsayli.openapi.generics.server.core.introspection;

import io.github.bsayli.apicontract.envelope.ServiceResponse;
import io.github.bsayli.apicontract.paging.Page;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncTask;

/**
 * Framework-independent introspector that detects contract-aware response shapes from {@link
 * ResolvableType} representations.
 *
 * <p>This component operates purely on type information and is intentionally decoupled from any
 * specific web framework (e.g. Spring MVC or WebFlux). Framework-specific layers are responsible
 * for discovering response types and providing them as {@link ResolvableType}.
 *
 * <p>Supported contract-aware shapes:
 *
 * <ul>
 *   <li>{@code ServiceResponse<T>} where {@code T} is a plain non-generic DTO
 *   <li>{@code ServiceResponse<Page<T>>}
 * </ul>
 *
 * <p>All other shapes (e.g. {@code ServiceResponse<List<T>>}, nested generics, maps, etc.) are
 * intentionally ignored and left to default OpenAPI generation.
 *
 * <p>This class defines the boundary of what is considered "contract-aware" in the published
 * OpenAPI specification.
 */
public final class ResponseTypeIntrospector {

  private static final Logger log = LoggerFactory.getLogger(ResponseTypeIntrospector.class);

  private static final int MAX_UNWRAP_DEPTH = 8;

  /**
   * Extracts a deterministic schema reference name for the {@code data} field inside {@code
   * ServiceResponse<T>}.
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>{@code ServiceResponse<CustomerDto>} → {@code CustomerDto}
   *   <li>{@code ServiceResponse<Page<CustomerDto>>} → {@code PageCustomerDto}
   * </ul>
   *
   * <p>If the provided type does not match a supported contract-aware shape, {@link
   * Optional#empty()} is returned.
   *
   * @param type response type (framework-agnostic)
   * @return deterministic schema suffix or empty if unsupported
   */
  public Optional<String> extractDataRefName(ResolvableType type) {
    if (type == null) return Optional.empty();

    type = unwrapToServiceResponse(type);

    Class<?> raw = type.resolve();
    if (raw == null || !ServiceResponse.class.isAssignableFrom(raw)) {
      return Optional.empty();
    }

    if (!type.hasGenerics()) {
      return Optional.empty();
    }

    ResolvableType dataType = type.getGeneric(0);
    Optional<String> refOpt = buildGuaranteedRefName(dataType);

    if (log.isDebugEnabled()) {
      log.debug(
          "Introspected type [{}]: dataType={}, resolvedRef={}",
          safeToString(type),
          safeToString(dataType),
          refOpt.orElse("<default>"));
    }

    return refOpt;
  }

  /**
   * Unwraps known wrapper types until {@code ServiceResponse<?>} is reached or no further
   * unwrapping is possible.
   */
  private ResolvableType unwrapToServiceResponse(ResolvableType type) {
    for (int i = 0; i < MAX_UNWRAP_DEPTH; i++) {
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

  /** Resolves the next inner layer for supported wrapper types. */
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

  /** Builds deterministic schema name only for explicitly supported shapes. */
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

  private String safeToString(ResolvableType type) {
    try {
      return String.valueOf(type);
    } catch (Exception ignored) {
      return "<unprintable>";
    }
  }
}

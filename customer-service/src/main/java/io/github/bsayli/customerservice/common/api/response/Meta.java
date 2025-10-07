package io.github.bsayli.customerservice.common.api.response;

import io.github.bsayli.customerservice.common.api.sort.Sort;
import java.time.Instant;
import java.util.List;

/**
 * Common metadata going with every successful API response. Typically, it includes request-level
 * context such as identifiers, timestamps, and sorting details. Future-proof can be extended with
 * fields like traceId, locale, or tenantId if needed.
 */
public record Meta(String requestId, Instant serverTime, List<Sort> sort) {

  public static Meta now() {
    return new Meta(null, Instant.now(), List.of());
  }

  public static Meta now(List<Sort> sort) {
    return new Meta(null, Instant.now(), sort == null ? List.of() : List.copyOf(sort));
  }
}

package io.github.bsayli.customerservice.common.api.response;

import io.github.bsayli.customerservice.common.api.sort.Sort;
import io.github.bsayli.customerservice.common.api.sort.SortDirection;
import io.github.bsayli.customerservice.common.api.sort.SortField;
import java.time.Instant;
import java.util.List;

/**
 * Common metadata going with every successful API response. Typically, it includes request-level
 * context such as identifiers, timestamps, and sorting details. Future-proof can be extended with
 * fields like traceId, locale, or tenantId if needed.
 */
public record Meta(Instant serverTime, List<Sort> sort) {

  public static Meta now() {
    return new Meta(Instant.now(), List.of());
  }

  public static Meta now(List<Sort> sort) {
    return new Meta(Instant.now(), sort == null ? List.of() : List.copyOf(sort));
  }

  public static Meta now(Sort... sort) {
    return new Meta(Instant.now(), sort == null ? List.of() : List.of(sort));
  }

  public static Meta now(SortField field, SortDirection direction) {
    return new Meta(Instant.now(), List.of(new Sort(field, direction)));
  }
}


package io.github.bsayli.apicontract.envelope;

import io.github.bsayli.apicontract.paging.Sort;
import io.github.bsayli.apicontract.paging.SortDirection;
import java.time.Instant;
import java.util.List;

/**
 * Common metadata attached to every successful API response.
 *
 * @param serverTime server-side timestamp produced when the response metadata is created
 * @param sort sorting information associated with the response, if any
 */
public record Meta(Instant serverTime, List<Sort> sort) {

  /**
   * Creates metadata with the current server time and no sorting information.
   *
   * @return metadata instance with current time
   */
  public static Meta now() {
    return new Meta(Instant.now(), List.of());
  }

  /**
   * Creates metadata with the current server time and the provided sorting information.
   *
   * @param sort sorting information to attach
   * @return metadata instance with current time
   */
  public static Meta now(List<Sort> sort) {
    return new Meta(Instant.now(), sort == null ? List.of() : List.copyOf(sort));
  }

  /**
   * Creates metadata with the current server time and the provided sorting information.
   *
   * @param sort sorting information to attach
   * @return metadata instance with current time
   */
  public static Meta now(Sort... sort) {
    return new Meta(Instant.now(), sort == null ? List.of() : List.of(sort));
  }

  /**
   * Creates metadata with a single sort entry.
   *
   * @param field field name used for sorting
   * @param direction sorting direction
   * @return metadata instance with current time
   */
  public static Meta now(String field, SortDirection direction) {
    if (field == null || field.isBlank()) {
      return new Meta(Instant.now(), List.of());
    }
    return new Meta(Instant.now(), List.of(new Sort(field, direction)));
  }
}
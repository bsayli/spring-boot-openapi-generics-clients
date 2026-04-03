package io.github.blueprintplatform.openapi.generics.contract.paging;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Sorting descriptor included in response metadata.
 *
 * @param field field name used for sorting
 * @param direction sorting direction
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Sort(String field, SortDirection direction) {

  /**
   * Creates a sorting descriptor.
   *
   * @param field field name used for sorting
   * @param direction sorting direction
   * @return sort descriptor
   */
  public static Sort of(String field, SortDirection direction) {
    return new Sort(field, direction);
  }
}
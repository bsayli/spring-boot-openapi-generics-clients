package io.github.bsayli.apicontract.paging;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Sort(String field, SortDirection direction) {

  public static Sort of(String field, SortDirection direction) {
    return new Sort(field, direction);
  }
}

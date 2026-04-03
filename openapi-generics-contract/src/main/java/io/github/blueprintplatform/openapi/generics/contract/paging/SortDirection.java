package io.github.blueprintplatform.openapi.generics.contract.paging;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Supported sorting directions.
 */
public enum SortDirection {

  /**
   * Ascending order.
   */
  ASC("asc"),

  /**
   * Descending order.
   */
  DESC("desc");

  private final String value;

  SortDirection(String value) {
    this.value = value;
  }

  /**
   * Resolves a sorting direction from string input.
   *
   * @param s raw direction value
   * @return {@link #DESC} when the value is {@code desc}, otherwise {@link #ASC}
   */
  public static SortDirection from(String s) {
    if (s == null) return ASC;
    return "desc".equalsIgnoreCase(s) ? DESC : ASC;
  }

  /**
   * Returns the serialized direction value.
   *
   * @return serialized direction value
   */
  @JsonValue
  public String value() {
    return value;
  }
}
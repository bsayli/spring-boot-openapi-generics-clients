package io.github.bsayli.apicontract.paging;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SortDirection {
  ASC("asc"),
  DESC("desc");

  private final String value;

  SortDirection(String value) {
    this.value = value;
  }

  public static SortDirection from(String s) {
    if (s == null) return ASC;
    return "desc".equalsIgnoreCase(s) ? DESC : ASC;
  }

  @JsonValue
  public String value() {
    return value;
  }
}

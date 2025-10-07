package io.github.bsayli.customerservice.common.api.sort;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SortField {
  CUSTOMER_ID("customerId"),
  NAME("name"),
  EMAIL("email");

  private final String value;

  SortField(String value) {
    this.value = value;
  }

  public static SortField from(String s) {
    if (s == null) return CUSTOMER_ID;
    for (var f : values()) {
      if (f.value.equalsIgnoreCase(s)) return f;
    }
    throw new IllegalArgumentException("Unsupported sort field: " + s);
  }

  @JsonValue
  public String value() {
    return value;
  }
}

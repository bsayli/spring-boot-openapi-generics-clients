package io.github.bsayli.openapi.client.common.sort;

public enum ClientSortField {
  CUSTOMER_ID("customerId"),
  NAME("name"),
  EMAIL("email");

  private final String value;

  ClientSortField(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
}

package io.github.bsayli.openapi.client.customer;

public enum CustomerSortField {
  CUSTOMER_ID("customerId"),
  NAME("name"),
  EMAIL("email");

  private final String value;

  CustomerSortField(String value) {
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

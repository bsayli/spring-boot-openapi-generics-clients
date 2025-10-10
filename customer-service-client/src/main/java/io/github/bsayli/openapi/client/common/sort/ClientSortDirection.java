package io.github.bsayli.openapi.client.common.sort;

public enum ClientSortDirection {
  ASC("asc"),
  DESC("desc");

  private final String value;

  ClientSortDirection(String value) {
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

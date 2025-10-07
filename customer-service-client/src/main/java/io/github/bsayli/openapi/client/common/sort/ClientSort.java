package io.github.bsayli.openapi.client.common.sort;

public record ClientSort(SortField field, SortDirection direction) {

  public ClientSort {
    if (field == null) {
      field = SortField.CUSTOMER_ID;
    }
    if (direction == null) {
      direction = SortDirection.ASC;
    }
  }
}

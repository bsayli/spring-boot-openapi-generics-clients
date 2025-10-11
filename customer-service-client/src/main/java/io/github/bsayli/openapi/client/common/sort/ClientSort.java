package io.github.bsayli.openapi.client.common.sort;

public record ClientSort(ClientSortField field, ClientSortDirection direction) {

  public ClientSort {
    if (field == null) {
      field = ClientSortField.CUSTOMER_ID;
    }
    if (direction == null) {
      direction = ClientSortDirection.ASC;
    }
  }
}

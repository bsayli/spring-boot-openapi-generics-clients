package io.github.bsayli.openapi.client.adapter.impl;

import io.github.bsayli.openapi.client.adapter.CustomerClientAdapter;
import io.github.bsayli.openapi.client.common.Page;
import io.github.bsayli.openapi.client.common.ServiceClientResponse;
import io.github.bsayli.openapi.client.common.sort.ClientSortDirection;
import io.github.bsayli.openapi.client.common.sort.ClientSortField;
import io.github.bsayli.openapi.client.generated.api.CustomerControllerApi;
import io.github.bsayli.openapi.client.generated.dto.*;
import org.springframework.stereotype.Service;

@Service
public class CustomerClientAdapterImpl implements CustomerClientAdapter {

  private final CustomerControllerApi api;

  public CustomerClientAdapterImpl(CustomerControllerApi customerControllerApi) {
    this.api = customerControllerApi;
  }

  @Override
  public ServiceClientResponse<CustomerDto> createCustomer(CustomerCreateRequest request) {
    return api.createCustomer(request);
  }

  @Override
  public ServiceClientResponse<CustomerDto> getCustomer(Integer customerId) {
    return api.getCustomer(customerId);
  }

  @Override
  public ServiceClientResponse<Page<CustomerDto>> getCustomers() {
    return getCustomers(null, null, 0, 5, ClientSortField.CUSTOMER_ID, ClientSortDirection.ASC);
  }

  @Override
  public ServiceClientResponse<Page<CustomerDto>> getCustomers(
      String name,
      String email,
      Integer page,
      Integer size,
      ClientSortField sortBy,
      ClientSortDirection direction) {

    return api.getCustomers(
        name,
        email,
        page,
        size,
        sortBy != null ? sortBy.value() : ClientSortField.CUSTOMER_ID.value(),
        direction != null ? direction.value() : ClientSortDirection.ASC.value());
  }

  @Override
  public ServiceClientResponse<CustomerDto> updateCustomer(
      Integer customerId, CustomerUpdateRequest request) {
    return api.updateCustomer(customerId, request);
  }

  @Override
  public ServiceClientResponse<CustomerDeleteResponse> deleteCustomer(Integer customerId) {
    return api.deleteCustomer(customerId);
  }
}

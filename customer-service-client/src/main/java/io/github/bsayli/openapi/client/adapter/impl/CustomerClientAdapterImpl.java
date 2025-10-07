package io.github.bsayli.openapi.client.adapter.impl;

import io.github.bsayli.openapi.client.adapter.CustomerClientAdapter;
import io.github.bsayli.openapi.client.common.Page;
import io.github.bsayli.openapi.client.common.ServiceClientResponse;
import io.github.bsayli.openapi.client.common.sort.SortDirection;
import io.github.bsayli.openapi.client.common.sort.SortField;
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
    return getCustomers(null, null, 0, 5, SortField.CUSTOMER_ID, SortDirection.ASC);
  }

  @Override
  public ServiceClientResponse<Page<CustomerDto>> getCustomers(
      String name,
      String email,
      Integer page,
      Integer size,
      SortField sortBy,
      SortDirection direction) {

    return api.getCustomers(
        name,
        email,
        page,
        size,
        sortBy != null ? sortBy.value() : SortField.CUSTOMER_ID.value(),
        direction != null ? direction.value() : SortDirection.ASC.value());
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

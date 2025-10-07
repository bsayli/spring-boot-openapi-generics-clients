package io.github.bsayli.openapi.client.adapter.impl;

import io.github.bsayli.openapi.client.adapter.CustomerClientAdapter;
import io.github.bsayli.openapi.client.common.ServiceClientResponse;
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
  public ServiceClientResponse<PageCustomerDto> getCustomers() {
    return getCustomers(null, null, 0, 5, "customerId", "asc");
  }

  @Override
  public ServiceClientResponse<PageCustomerDto> getCustomers(
      String name, String email, Integer page, Integer size, String sortBy, String direction) {

    return api.getCustomers(name, email, page, size, sortBy, direction);
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

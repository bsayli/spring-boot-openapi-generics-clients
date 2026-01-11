package io.github.bsayli.openapi.client.adapter.impl;

import io.github.bsayli.apicontract.envelope.ServiceResponse;
import io.github.bsayli.apicontract.paging.Page;
import io.github.bsayli.apicontract.paging.SortDirection;
import io.github.bsayli.openapi.client.adapter.CustomerClientAdapter;
import io.github.bsayli.openapi.client.customer.CustomerSortField;
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
  public ServiceResponse<CustomerDto> createCustomer(CustomerCreateRequest request) {
    return api.createCustomer(request);
  }

  @Override
  public ServiceResponse<CustomerDto> getCustomer(Integer customerId) {
    return api.getCustomer(customerId);
  }

  @Override
  public ServiceResponse<Page<CustomerDto>> getCustomers() {
    return getCustomers(null, null, 0, 5, CustomerSortField.CUSTOMER_ID, SortDirection.ASC);
  }

  @Override
  public ServiceResponse<Page<CustomerDto>> getCustomers(
      String name,
      String email,
      Integer page,
      Integer size,
      CustomerSortField sortBy,
      SortDirection direction) {

    return api.getCustomers(
        name,
        email,
        page,
        size,
        sortBy != null ? sortBy.value() : CustomerSortField.CUSTOMER_ID.value(),
        direction != null ? direction.value() : SortDirection.ASC.value());
  }

  @Override
  public ServiceResponse<CustomerDto> updateCustomer(
      Integer customerId, CustomerUpdateRequest request) {
    return api.updateCustomer(customerId, request);
  }

  @Override
  public ServiceResponse<CustomerDeleteResponse> deleteCustomer(Integer customerId) {
    return api.deleteCustomer(customerId);
  }
}

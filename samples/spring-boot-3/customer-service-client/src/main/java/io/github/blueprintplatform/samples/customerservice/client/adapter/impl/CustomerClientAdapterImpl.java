package io.github.blueprintplatform.samples.customerservice.client.adapter.impl;

import io.github.blueprintplatform.contracts.customer.CustomerDto;
import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.contract.paging.SortDirection;
import io.github.blueprintplatform.samples.customerservice.client.adapter.CustomerClientAdapter;
import io.github.blueprintplatform.samples.customerservice.client.customer.CustomerSortField;
import io.github.blueprintplatform.samples.customerservice.client.generated.api.CustomerControllerApi;
import io.github.blueprintplatform.samples.customerservice.client.generated.dto.CustomerCreateRequest;
import io.github.blueprintplatform.samples.customerservice.client.generated.dto.CustomerUpdateRequest;
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
  public ServiceResponse<Void> deleteCustomer(Integer customerId) {
    api.deleteCustomer(customerId);
    return ServiceResponse.of(null);
  }
}

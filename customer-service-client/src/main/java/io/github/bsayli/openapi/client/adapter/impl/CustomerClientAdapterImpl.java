package io.github.bsayli.openapi.client.adapter.impl;

import io.github.bsayli.openapi.client.adapter.CustomerClientAdapter;
import io.github.bsayli.openapi.client.common.ApiClientResponse;
import io.github.bsayli.openapi.client.generated.api.CustomerControllerApi;
import io.github.bsayli.openapi.client.generated.dto.CustomerCreateRequest;
import io.github.bsayli.openapi.client.generated.dto.CustomerCreateResponse;
import io.github.bsayli.openapi.client.generated.dto.CustomerDeleteResponse;
import io.github.bsayli.openapi.client.generated.dto.CustomerDto;
import io.github.bsayli.openapi.client.generated.dto.CustomerListResponse;
import io.github.bsayli.openapi.client.generated.dto.CustomerUpdateRequest;
import io.github.bsayli.openapi.client.generated.dto.CustomerUpdateResponse;
import org.springframework.stereotype.Service;

@Service
public class CustomerClientAdapterImpl implements CustomerClientAdapter {

  private final CustomerControllerApi customerControllerApi;

  public CustomerClientAdapterImpl(CustomerControllerApi customerControllerApi) {
    this.customerControllerApi = customerControllerApi;
  }

  @Override
  public ApiClientResponse<CustomerCreateResponse> createCustomer(CustomerCreateRequest request) {
    return customerControllerApi.createCustomer(request);
  }

  @Override
  public ApiClientResponse<CustomerDto> getCustomer(Integer customerId) {
    return customerControllerApi.getCustomer(customerId);
  }

  @Override
  public ApiClientResponse<CustomerListResponse> getCustomers() {
    return customerControllerApi.getCustomers();
  }

  @Override
  public ApiClientResponse<CustomerUpdateResponse> updateCustomer(
          Integer customerId, CustomerUpdateRequest request) {
    return customerControllerApi.updateCustomer(customerId, request);
  }

  @Override
  public ApiClientResponse<CustomerDeleteResponse> deleteCustomer(Integer customerId) {
    return customerControllerApi.deleteCustomer(customerId);
  }
}
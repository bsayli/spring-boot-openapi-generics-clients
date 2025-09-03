package io.github.bsayli.openapi.client.adapter;

import io.github.bsayli.openapi.client.common.ApiClientResponse;
import io.github.bsayli.openapi.client.generated.dto.*;

public interface CustomerClientAdapter {
  ApiClientResponse<CustomerCreateResponse> createCustomer(CustomerCreateRequest request);
  ApiClientResponse<CustomerDto> getCustomer(Integer customerId);
  ApiClientResponse<CustomerListResponse> getCustomers();
  ApiClientResponse<CustomerUpdateResponse> updateCustomer(Integer customerId, CustomerUpdateRequest request);
  ApiClientResponse<CustomerDeleteResponse> deleteCustomer(Integer customerId);
}
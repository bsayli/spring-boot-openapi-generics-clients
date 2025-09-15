package io.github.bsayli.openapi.client.adapter;

import io.github.bsayli.openapi.client.common.ServiceClientResponse;
import io.github.bsayli.openapi.client.generated.dto.*;

public interface CustomerClientAdapter {
  ServiceClientResponse<CustomerCreateResponse> createCustomer(CustomerCreateRequest request);

  ServiceClientResponse<CustomerDto> getCustomer(Integer customerId);

  ServiceClientResponse<CustomerListResponse> getCustomers();

  ServiceClientResponse<CustomerUpdateResponse> updateCustomer(
      Integer customerId, CustomerUpdateRequest request);

  ServiceClientResponse<CustomerDeleteResponse> deleteCustomer(Integer customerId);
}

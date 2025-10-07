package io.github.bsayli.openapi.client.adapter;

import io.github.bsayli.openapi.client.common.ServiceClientResponse;
import io.github.bsayli.openapi.client.generated.dto.*;

public interface CustomerClientAdapter {

  ServiceClientResponse<CustomerDto> createCustomer(CustomerCreateRequest request);

  ServiceClientResponse<CustomerDto> getCustomer(Integer customerId);

  ServiceClientResponse<PageCustomerDto> getCustomers();

  ServiceClientResponse<PageCustomerDto> getCustomers(
      String name, String email, Integer page, Integer size, String sortBy, String direction);

  ServiceClientResponse<CustomerDto> updateCustomer(
      Integer customerId, CustomerUpdateRequest request);

  ServiceClientResponse<CustomerDeleteResponse> deleteCustomer(Integer customerId);
}

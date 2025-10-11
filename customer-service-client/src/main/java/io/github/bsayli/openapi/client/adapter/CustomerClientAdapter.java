package io.github.bsayli.openapi.client.adapter;

import io.github.bsayli.openapi.client.common.Page;
import io.github.bsayli.openapi.client.common.ServiceClientResponse;
import io.github.bsayli.openapi.client.common.sort.ClientSortDirection;
import io.github.bsayli.openapi.client.common.sort.ClientSortField;
import io.github.bsayli.openapi.client.generated.dto.CustomerCreateRequest;
import io.github.bsayli.openapi.client.generated.dto.CustomerDeleteResponse;
import io.github.bsayli.openapi.client.generated.dto.CustomerDto;
import io.github.bsayli.openapi.client.generated.dto.CustomerUpdateRequest;

public interface CustomerClientAdapter {

  ServiceClientResponse<CustomerDto> createCustomer(CustomerCreateRequest request);

  ServiceClientResponse<CustomerDto> getCustomer(Integer customerId);

  ServiceClientResponse<Page<CustomerDto>> getCustomers();

  ServiceClientResponse<Page<CustomerDto>> getCustomers(
      String name,
      String email,
      Integer page,
      Integer size,
      ClientSortField sortBy,
      ClientSortDirection direction);

  ServiceClientResponse<CustomerDto> updateCustomer(
      Integer customerId, CustomerUpdateRequest request);

  ServiceClientResponse<CustomerDeleteResponse> deleteCustomer(Integer customerId);
}

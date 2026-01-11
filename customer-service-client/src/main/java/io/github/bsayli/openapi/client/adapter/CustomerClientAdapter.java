package io.github.bsayli.openapi.client.adapter;

import io.github.bsayli.apicontract.envelope.ServiceResponse;
import io.github.bsayli.apicontract.paging.Page;
import io.github.bsayli.apicontract.paging.SortDirection;
import io.github.bsayli.openapi.client.customer.CustomerSortField;
import io.github.bsayli.openapi.client.generated.dto.CustomerCreateRequest;
import io.github.bsayli.openapi.client.generated.dto.CustomerDeleteResponse;
import io.github.bsayli.openapi.client.generated.dto.CustomerDto;
import io.github.bsayli.openapi.client.generated.dto.CustomerUpdateRequest;

public interface CustomerClientAdapter {

  ServiceResponse<CustomerDto> createCustomer(CustomerCreateRequest request);

  ServiceResponse<CustomerDto> getCustomer(Integer customerId);

  ServiceResponse<Page<CustomerDto>> getCustomers();

  ServiceResponse<Page<CustomerDto>> getCustomers(
      String name,
      String email,
      Integer page,
      Integer size,
      CustomerSortField sortBy,
      SortDirection direction);

  ServiceResponse<CustomerDto> updateCustomer(Integer customerId, CustomerUpdateRequest request);

  ServiceResponse<CustomerDeleteResponse> deleteCustomer(Integer customerId);
}

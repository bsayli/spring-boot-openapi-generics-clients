package io.github.blueprintplatform.samples.customerservice.client.adapter;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.contract.paging.SortDirection;
import io.github.blueprintplatform.samples.customerservice.client.customer.CustomerSortField;
import io.github.blueprintplatform.samples.customerservice.client.generated.dto.CustomerCreateRequest;
import io.github.blueprintplatform.samples.customerservice.client.generated.dto.CustomerDto;
import io.github.blueprintplatform.samples.customerservice.client.generated.dto.CustomerUpdateRequest;

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

  ServiceResponse<Void> deleteCustomer(Integer customerId);
}

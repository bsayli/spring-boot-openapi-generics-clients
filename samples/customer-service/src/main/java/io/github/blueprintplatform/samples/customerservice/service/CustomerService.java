package io.github.blueprintplatform.samples.customerservice.service;

import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.contract.paging.SortDirection;
import io.github.blueprintplatform.samples.customerservice.api.dto.CustomerCreateRequest;
import io.github.blueprintplatform.samples.customerservice.api.dto.CustomerDto;
import io.github.blueprintplatform.samples.customerservice.api.dto.CustomerSearchCriteria;
import io.github.blueprintplatform.samples.customerservice.api.dto.CustomerUpdateRequest;
import io.github.blueprintplatform.samples.customerservice.common.api.sort.SortField;

public interface CustomerService {
  CustomerDto createCustomer(CustomerCreateRequest request);

  CustomerDto getCustomer(Integer customerId);

  Page<CustomerDto> getCustomers(
      CustomerSearchCriteria criteria,
      int page,
      int size,
      SortField sortBy,
      SortDirection direction);

  CustomerDto updateCustomer(Integer customerId, CustomerUpdateRequest request);

  void deleteCustomer(Integer customerId);
}

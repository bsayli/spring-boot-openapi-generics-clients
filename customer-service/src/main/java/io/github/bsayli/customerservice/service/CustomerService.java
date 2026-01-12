package io.github.bsayli.customerservice.service;

import io.github.bsayli.apicontract.paging.Page;
import io.github.bsayli.apicontract.paging.SortDirection;
import io.github.bsayli.customerservice.api.dto.CustomerCreateRequest;
import io.github.bsayli.customerservice.api.dto.CustomerDto;
import io.github.bsayli.customerservice.api.dto.CustomerSearchCriteria;
import io.github.bsayli.customerservice.api.dto.CustomerUpdateRequest;
import io.github.bsayli.customerservice.common.api.sort.SortField;

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

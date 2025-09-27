package io.github.bsayli.customerservice.service;

import io.github.bsayli.customerservice.api.dto.CustomerCreateRequest;
import io.github.bsayli.customerservice.api.dto.CustomerDto;
import io.github.bsayli.customerservice.api.dto.CustomerUpdateRequest;
import java.util.List;

public interface CustomerService {
  CustomerDto createCustomer(CustomerCreateRequest request);

  CustomerDto getCustomer(Integer customerId);

  List<CustomerDto> getCustomers();

  CustomerDto updateCustomer(Integer customerId, CustomerUpdateRequest request);

  void deleteCustomer(Integer customerId);
}

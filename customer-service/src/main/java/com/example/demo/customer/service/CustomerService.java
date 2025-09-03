package com.example.demo.customer.service;

import com.example.demo.customer.api.dto.CustomerCreateRequest;
import com.example.demo.customer.api.dto.CustomerDto;
import com.example.demo.customer.api.dto.CustomerUpdateRequest;
import java.util.List;

public interface CustomerService {
  CustomerDto createCustomer(CustomerCreateRequest request);

  CustomerDto getCustomer(Integer customerId);

  List<CustomerDto> getCustomers();

  CustomerDto updateCustomer(Integer customerId, CustomerUpdateRequest request);

  void deleteCustomer(Integer customerId);
}

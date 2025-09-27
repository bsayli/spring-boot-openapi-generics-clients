package io.github.bsayli.customerservice.service.impl;

import io.github.bsayli.customerservice.api.dto.CustomerCreateRequest;
import io.github.bsayli.customerservice.api.dto.CustomerDto;
import io.github.bsayli.customerservice.api.dto.CustomerUpdateRequest;
import io.github.bsayli.customerservice.service.CustomerService;
import java.util.List;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl implements CustomerService {

  private final AtomicInteger idSeq = new AtomicInteger(0);
  private final NavigableMap<Integer, CustomerDto> store = new ConcurrentSkipListMap<>();

  public CustomerServiceImpl() {
    createCustomer(new CustomerCreateRequest("Ahmet Yilmaz", "ahmet.yilmaz@example.com"));
    createCustomer(new CustomerCreateRequest("John Smith", "john.smith@example.com"));
    createCustomer(new CustomerCreateRequest("Carlos Hernandez", "carlos.hernandez@example.com"));
    createCustomer(new CustomerCreateRequest("Ananya Patel", "ananya.patel@example.com"));
  }

  @Override
  public CustomerDto createCustomer(CustomerCreateRequest request) {
    int id = idSeq.incrementAndGet();
    CustomerDto dto = new CustomerDto(id, request.name(), request.email());
    store.put(id, dto);
    return dto;
  }

  @Override
  public CustomerDto getCustomer(Integer customerId) {
    CustomerDto dto = store.get(customerId);
    if (dto == null) throw new NoSuchElementException("Customer not found: " + customerId);
    return dto;
  }

  @Override
  public List<CustomerDto> getCustomers() {
    return List.copyOf(store.values());
  }

  @Override
  public CustomerDto updateCustomer(Integer customerId, CustomerUpdateRequest request) {
    CustomerDto existing = store.get(customerId);
    if (existing == null) throw new NoSuchElementException("Customer not found: " + customerId);
    CustomerDto updated = new CustomerDto(existing.customerId(), request.name(), request.email());
    store.put(customerId, updated);
    return updated;
  }

  @Override
  public void deleteCustomer(Integer customerId) {
    store.remove(customerId);
  }
}

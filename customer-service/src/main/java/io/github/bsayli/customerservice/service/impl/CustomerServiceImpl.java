package io.github.bsayli.customerservice.service.impl;

import io.github.bsayli.customerservice.api.dto.CustomerCreateRequest;
import io.github.bsayli.customerservice.api.dto.CustomerDto;
import io.github.bsayli.customerservice.api.dto.CustomerSearchCriteria;
import io.github.bsayli.customerservice.api.dto.CustomerUpdateRequest;
import io.github.bsayli.customerservice.common.api.response.Page;
import io.github.bsayli.customerservice.common.api.sort.SortDirection;
import io.github.bsayli.customerservice.common.api.sort.SortField;
import io.github.bsayli.customerservice.service.CustomerService;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl implements CustomerService {

  private static final int MAX_PAGE_SIZE = 10;
  private final AtomicInteger idSeq = new AtomicInteger(0);
  private final NavigableMap<Integer, CustomerDto> store = new ConcurrentSkipListMap<>();

  public CustomerServiceImpl() {
    createCustomer(new CustomerCreateRequest("Ahmet Yilmaz", "ahmet.yilmaz@example.com"));
    createCustomer(new CustomerCreateRequest("John Smith", "john.smith@example.com"));
    createCustomer(new CustomerCreateRequest("Carlos Hernandez", "carlos.hernandez@example.com"));
    createCustomer(new CustomerCreateRequest("Ananya Patel", "ananya.patel@example.com"));
    createCustomer(new CustomerCreateRequest("Sofia Rossi", "sofia.rossi@example.com"));
    createCustomer(new CustomerCreateRequest("Hans Müller", "hans.muller@example.com"));
    createCustomer(new CustomerCreateRequest("Yuki Tanaka", "yuki.tanaka@example.com"));
    createCustomer(new CustomerCreateRequest("Amina El-Sayed", "amina.elsayed@example.com"));
    createCustomer(new CustomerCreateRequest("Lucas Silva", "lucas.silva@example.com"));
    createCustomer(new CustomerCreateRequest("Chloe Dubois", "chloe.dubois@example.com"));
    createCustomer(new CustomerCreateRequest("Andrei Popescu", "andrei.popescu@example.com"));
    createCustomer(new CustomerCreateRequest("Fatima Al-Harbi", "fatima.alharbi@example.com"));
    createCustomer(new CustomerCreateRequest("Emily Johnson", "emily.johnson@example.com"));
    createCustomer(new CustomerCreateRequest("Zanele Ndlovu", "zanele.ndlovu@example.com"));
    createCustomer(new CustomerCreateRequest("Mateo González", "mateo.gonzalez@example.com"));
    createCustomer(new CustomerCreateRequest("Olga Ivanova", "olga.ivanova@example.com"));
    createCustomer(new CustomerCreateRequest("Wei Chen", "wei.chen@example.com"));
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
  public Page<CustomerDto> getCustomers(
      CustomerSearchCriteria criteria,
      int page,
      int size,
      SortField sortBy,
      SortDirection direction) {
    var filtered = applyFilters(store.values().stream(), criteria);
    var sorted = filtered.sorted(buildComparator(sortBy, direction)).toList();
    return paginate(sorted, page, size);
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

  private Stream<CustomerDto> applyFilters(Stream<CustomerDto> stream, CustomerSearchCriteria c) {
    if (c == null) return stream;

    if (c.name() != null && !c.name().isBlank()) {
      String q = c.name().toLowerCase();
      stream = stream.filter(x -> x.name() != null && x.name().toLowerCase().contains(q));
    }
    if (c.email() != null && !c.email().isBlank()) {
      String q = c.email().toLowerCase();
      stream = stream.filter(x -> x.email() != null && x.email().toLowerCase().contains(q));
    }
    return stream;
  }

  private Comparator<CustomerDto> buildComparator(SortField sortBy, SortDirection dir) {
    Comparator<CustomerDto> cmp =
        switch (sortBy) {
          case CUSTOMER_ID ->
              Comparator.comparing(
                  CustomerDto::customerId, Comparator.nullsLast(Integer::compareTo));
          case NAME ->
              Comparator.comparing(
                  CustomerDto::name, Comparator.nullsLast(String::compareToIgnoreCase));
          case EMAIL ->
              Comparator.comparing(
                  CustomerDto::email, Comparator.nullsLast(String::compareToIgnoreCase));
        };
    return (dir == SortDirection.DESC) ? cmp.reversed() : cmp;
  }

  private Page<CustomerDto> paginate(List<CustomerDto> items, int page, int size) {
    int p = Math.clamp(page, 0, Integer.MAX_VALUE);
    int s = Math.clamp(size, 1, MAX_PAGE_SIZE);

    long total = items.size();
    long fromL = Math.min((long) p * s, total);
    long toL = Math.min(fromL + s, total);

    int from = (int) fromL;
    int to = (int) toL;

    var slice = items.subList(from, to);
    return Page.of(slice, p, s, total);
  }
}

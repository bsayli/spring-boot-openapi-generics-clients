package io.github.bsayli.customerservice.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.customerservice.api.dto.CustomerCreateRequest;
import io.github.bsayli.customerservice.api.dto.CustomerDto;
import io.github.bsayli.customerservice.api.dto.CustomerUpdateRequest;
import io.github.bsayli.customerservice.service.CustomerService;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Unit Test: CustomerServiceImpl")
class CustomerServiceImplTest {

  private CustomerService service;

  @BeforeEach
  void setUp() {
    service = new CustomerServiceImpl();
  }

  @Test
  @DisplayName("Initial seed should contain 4 customers")
  void initialSeed_shouldContainFour() {
    List<CustomerDto> all = service.getCustomers();
    assertEquals(4, all.size());
    assertTrue(all.stream().allMatch(c -> c.customerId() != null && c.customerId() > 0));
  }

  @Test
  @DisplayName("createCustomer should assign incremental ID and store the record")
  void createCustomer_shouldAssignIdAndStore() {
    var req = new CustomerCreateRequest("Jane Doe", "jane.doe@example.com");
    CustomerDto created = service.createCustomer(req);

    assertNotNull(created);
    assertNotNull(created.customerId());
    assertEquals("Jane Doe", created.name());
    assertEquals("jane.doe@example.com", created.email());

    List<CustomerDto> all = service.getCustomers();
    assertEquals(5, all.size());
    assertTrue(all.stream().anyMatch(c -> c.customerId().equals(created.customerId())));
  }

  @Test
  @DisplayName("getCustomer should return existing customer")
  void getCustomer_shouldReturn() {
    CustomerDto any = service.getCustomers().getFirst();
    CustomerDto found = service.getCustomer(any.customerId());
    assertEquals(any, found);
  }

  @Test
  @DisplayName("getCustomer should throw for missing id")
  void getCustomer_shouldThrowWhenMissing() {
    assertThrows(NoSuchElementException.class, () -> service.getCustomer(999_999));
  }

  @Test
  @DisplayName("updateCustomer should update name and email")
  void updateCustomer_shouldUpdate() {
    CustomerDto base =
        service.createCustomer(new CustomerCreateRequest("Temp", "temp@example.com"));
    var req = new CustomerUpdateRequest("Temp Updated", "temp.updated@example.com");

    CustomerDto updated = service.updateCustomer(base.customerId(), req);

    assertEquals(base.customerId(), updated.customerId());
    assertEquals("Temp Updated", updated.name());
    assertEquals("temp.updated@example.com", updated.email());
  }

  @Test
  @DisplayName("updateCustomer should throw when customer does not exist")
  void updateCustomer_shouldThrowWhenMissing() {
    var req = new CustomerUpdateRequest("X", "x@example.com");
    assertThrows(NoSuchElementException.class, () -> service.updateCustomer(123456, req));
  }

  @Test
  @DisplayName("deleteCustomer should remove the record")
  void deleteCustomer_shouldRemove() {
    CustomerDto base =
        service.createCustomer(new CustomerCreateRequest("Mark Lee", "mark.lee@example.com"));
    int sizeBefore = service.getCustomers().size();

    service.deleteCustomer(base.customerId());

    List<CustomerDto> after = service.getCustomers();
    assertEquals(sizeBefore - 1, after.size());
    assertTrue(after.stream().noneMatch(c -> c.customerId().equals(base.customerId())));
  }
}

package io.github.bsayli.customerservice.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.customerservice.api.dto.CustomerCreateRequest;
import io.github.bsayli.customerservice.api.dto.CustomerDto;
import io.github.bsayli.customerservice.api.dto.CustomerSearchCriteria;
import io.github.bsayli.customerservice.api.dto.CustomerUpdateRequest;
import io.github.bsayli.customerservice.common.api.response.Page;
import io.github.bsayli.customerservice.common.api.sort.SortDirection;
import io.github.bsayli.customerservice.common.api.sort.SortField;
import io.github.bsayli.customerservice.service.CustomerService;
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
  @DisplayName("Initial seed pagination: total=17, page0 size=10, page1 size=7")
  void initialSeed_paginationShouldBeCorrect() {
    Page<CustomerDto> p0 =
        service.getCustomers(
            new CustomerSearchCriteria(null, null),
            0,
            100,
            SortField.CUSTOMER_ID,
            SortDirection.ASC);

    assertEquals(17, p0.totalElements());
    assertEquals(2, p0.totalPages());
    assertEquals(10, p0.content().size());
    assertTrue(p0.hasNext());
    assertFalse(p0.hasPrev());

    Page<CustomerDto> p1 =
        service.getCustomers(
            new CustomerSearchCriteria(null, null),
            1,
            100,
            SortField.CUSTOMER_ID,
            SortDirection.ASC);

    assertEquals(17, p1.totalElements());
    assertEquals(2, p1.totalPages());
    assertEquals(7, p1.content().size());
    assertFalse(p1.hasNext());
    assertTrue(p1.hasPrev());
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

    Page<CustomerDto> anyPage =
        service.getCustomers(
            new CustomerSearchCriteria(null, null),
            0,
            10,
            SortField.CUSTOMER_ID,
            SortDirection.ASC);

    assertEquals(18, anyPage.totalElements());
    CustomerDto fetched = service.getCustomer(created.customerId());
    assertEquals(created, fetched);
  }

  @Test
  @DisplayName("getCustomer should return existing customer")
  void getCustomer_shouldReturn() {
    Page<CustomerDto> p0 =
        service.getCustomers(
            new CustomerSearchCriteria(null, null), 0, 1, SortField.CUSTOMER_ID, SortDirection.ASC);

    CustomerDto any = p0.content().getFirst();
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
  @DisplayName("deleteCustomer should remove the record and decrease total")
  void deleteCustomer_shouldRemove() {
    CustomerDto base =
        service.createCustomer(new CustomerCreateRequest("Mark Lee", "mark.lee@example.com"));

    Page<CustomerDto> before =
        service.getCustomers(
            new CustomerSearchCriteria(null, null),
            0,
            10,
            SortField.CUSTOMER_ID,
            SortDirection.ASC);
    long totalBefore = before.totalElements();

    service.deleteCustomer(base.customerId());

    Page<CustomerDto> after =
        service.getCustomers(
            new CustomerSearchCriteria(null, null),
            0,
            10,
            SortField.CUSTOMER_ID,
            SortDirection.ASC);
    assertEquals(totalBefore - 1, after.totalElements());
    assertThrows(NoSuchElementException.class, () -> service.getCustomer(base.customerId()));
  }
}

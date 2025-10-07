package io.github.bsayli.customerservice.api.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.customerservice.api.dto.*;
import io.github.bsayli.customerservice.common.api.response.Meta;
import io.github.bsayli.customerservice.common.api.response.Page;
import io.github.bsayli.customerservice.common.api.response.ServiceResponse;
import io.github.bsayli.customerservice.common.api.sort.Sort;
import io.github.bsayli.customerservice.common.api.sort.SortDirection;
import io.github.bsayli.customerservice.common.api.sort.SortField;
import io.github.bsayli.customerservice.service.CustomerService;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: CustomerController")
class CustomerControllerTest {

  @Mock private CustomerService customerService;

  @InjectMocks private CustomerController controller;

  private CustomerDto dto1;
  private CustomerDto dto2;

  @BeforeEach
  void setUp() {
    dto1 = new CustomerDto(1, "John Smith", "john.smith@example.com");
    dto2 = new CustomerDto(2, "Ahmet Yilmaz", "ahmet.yilmaz@example.com");

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setMethod("POST");
    request.setRequestURI("/v1/customers");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
  }

  @AfterEach
  void tearDown() {
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  @DisplayName("POST /v1/customers -> 201 Created + ServiceResponse(data, meta)")
  void createCustomer_shouldReturnCreated() {
    var request = new CustomerCreateRequest("John Smith", "john.smith@example.com");
    when(customerService.createCustomer(request)).thenReturn(dto1);

    ResponseEntity<ServiceResponse<CustomerDto>> resp = controller.createCustomer(request);

    assertEquals(HttpStatus.CREATED, resp.getStatusCode());
    assertNotNull(resp.getHeaders().getLocation(), "Location header should be set");

    var body = resp.getBody();
    assertNotNull(body);
    assertEquals(dto1, body.data());
    assertNotNull(body.meta());
    assertNotNull(body.meta().serverTime());

    verify(customerService).createCustomer(request);
  }

  @Test
  @DisplayName("GET /v1/customers/{id} -> 200 OK + ServiceResponse(data, meta)")
  void getCustomer_shouldReturnOk() {
    when(customerService.getCustomer(1)).thenReturn(dto1);

    ResponseEntity<ServiceResponse<CustomerDto>> resp = controller.getCustomer(1);

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    var body = resp.getBody();
    assertNotNull(body);
    assertEquals(dto1, body.data());
    assertNotNull(body.meta());
    verify(customerService).getCustomer(1);
  }

  @Test
  @DisplayName("GET /v1/customers -> 200 OK + Page<CustomerDto> + Meta.sort")
  void getCustomers_shouldReturnPaged() {
    var page = Page.of(List.of(dto1, dto2), 0, 5, 2);
    var criteria = new CustomerSearchCriteria(null, null);
    var sortBy = SortField.CUSTOMER_ID;
    var direction = SortDirection.ASC;

    when(customerService.getCustomers(criteria, 0, 5, sortBy, direction)).thenReturn(page);

    ResponseEntity<ServiceResponse<Page<CustomerDto>>> resp =
        controller.getCustomers(criteria, 0, 5, sortBy, direction);

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    var body = resp.getBody();
    assertNotNull(body);

    // Page assertions
    assertNotNull(body.data());
    assertEquals(2, body.data().content().size());
    assertEquals(0, body.data().page());
    assertEquals(5, body.data().size());
    assertEquals(2, body.data().totalElements());
    assertEquals(1, body.data().totalPages());

    Meta meta = body.meta();
    assertNotNull(meta);
    assertNotNull(meta.serverTime());
    assertNotNull(meta.sort());
    assertFalse(meta.sort().isEmpty());
    Sort s = meta.sort().getFirst();
    assertEquals(sortBy, s.field());
    assertEquals(direction, s.direction());

    verify(customerService).getCustomers(criteria, 0, 5, sortBy, direction);
  }

  @Test
  @DisplayName("PUT /v1/customers/{id} -> 200 OK + ServiceResponse(data)")
  void updateCustomer_shouldReturnOk() {
    var req = new CustomerUpdateRequest("John Smith", "john.smith@example.com");
    var updated = new CustomerDto(1, req.name(), req.email());
    when(customerService.updateCustomer(1, req)).thenReturn(updated);

    ResponseEntity<ServiceResponse<CustomerDto>> resp = controller.updateCustomer(1, req);

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    var body = resp.getBody();
    assertNotNull(body);
    assertEquals(updated, body.data());
    assertNotNull(body.meta());
    verify(customerService).updateCustomer(1, req);
  }

  @Test
  @DisplayName("DELETE /v1/customers/{id} -> 200 OK + ServiceResponse(CustomerDeleteResponse)")
  void deleteCustomer_shouldReturnOk() {
    doNothing().when(customerService).deleteCustomer(1);

    ResponseEntity<ServiceResponse<CustomerDeleteResponse>> resp = controller.deleteCustomer(1);

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    var body = resp.getBody();
    assertNotNull(body);
    assertNotNull(body.data());
    assertEquals(1, body.data().customerId());
    assertNotNull(body.meta());

    verify(customerService).deleteCustomer(1);
  }
}

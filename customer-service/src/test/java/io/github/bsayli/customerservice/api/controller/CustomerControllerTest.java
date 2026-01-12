package io.github.bsayli.customerservice.api.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.bsayli.apicontract.envelope.Meta;
import io.github.bsayli.apicontract.envelope.ServiceResponse;
import io.github.bsayli.apicontract.paging.Page;
import io.github.bsayli.apicontract.paging.Sort;
import io.github.bsayli.apicontract.paging.SortDirection;
import io.github.bsayli.customerservice.api.dto.CustomerCreateRequest;
import io.github.bsayli.customerservice.api.dto.CustomerDeleteResponse;
import io.github.bsayli.customerservice.api.dto.CustomerDto;
import io.github.bsayli.customerservice.api.dto.CustomerSearchCriteria;
import io.github.bsayli.customerservice.api.dto.CustomerUpdateRequest;
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
    var req = new CustomerCreateRequest("John Smith", "john.smith@example.com");
    when(customerService.createCustomer(req)).thenReturn(dto1);

    ResponseEntity<ServiceResponse<CustomerDto>> resp = controller.createCustomer(req);

    assertEquals(HttpStatus.CREATED, resp.getStatusCode());
    assertNotNull(resp.getHeaders().getLocation(), "Location header should be set");

    var body = resp.getBody();
    assertNotNull(body);

    assertEquals(dto1, body.getData());
    assertNotNull(body.getMeta());
    assertNotNull(body.getMeta().serverTime());

    verify(customerService).createCustomer(req);
  }

  @Test
  @DisplayName("GET /v1/customers/{id} -> 200 OK + ServiceResponse(data, meta)")
  void getCustomer_shouldReturnOk() {
    when(customerService.getCustomer(1)).thenReturn(dto1);

    ResponseEntity<ServiceResponse<CustomerDto>> resp = controller.getCustomer(1);

    assertEquals(HttpStatus.OK, resp.getStatusCode());

    var body = resp.getBody();
    assertNotNull(body);

    assertEquals(dto1, body.getData());
    assertNotNull(body.getMeta());
    assertNotNull(body.getMeta().serverTime());

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
    Page<CustomerDto> data = body.getData();
    assertNotNull(data);
    assertEquals(2, data.content().size());
    assertEquals(0, data.page());
    assertEquals(5, data.size());
    assertEquals(2, data.totalElements());
    assertEquals(1, data.totalPages());

    // Meta + sort assertions (api-contract: field is String)
    Meta meta = body.getMeta();
    assertNotNull(meta);
    assertNotNull(meta.serverTime());

    assertNotNull(meta.sort());
    assertFalse(meta.sort().isEmpty());

    Sort s = meta.sort().get(0);
    assertEquals(sortBy.value(), s.field());
    assertEquals(direction, s.direction());

    verify(customerService).getCustomers(criteria, 0, 5, sortBy, direction);
  }

  @Test
  @DisplayName("PUT /v1/customers/{id} -> 200 OK + ServiceResponse(data, meta)")
  void updateCustomer_shouldReturnOk() {
    var req = new CustomerUpdateRequest("John Smith", "john.smith@example.com");
    var updated = new CustomerDto(1, req.name(), req.email());
    when(customerService.updateCustomer(1, req)).thenReturn(updated);

    ResponseEntity<ServiceResponse<CustomerDto>> resp = controller.updateCustomer(1, req);

    assertEquals(HttpStatus.OK, resp.getStatusCode());

    var body = resp.getBody();
    assertNotNull(body);

    assertEquals(updated, body.getData());
    assertNotNull(body.getMeta());
    assertNotNull(body.getMeta().serverTime());

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

    assertNotNull(body.getData());
    assertEquals(1, body.getData().customerId());

    assertNotNull(body.getMeta());
    assertNotNull(body.getMeta().serverTime());

    verify(customerService).deleteCustomer(1);
  }
}

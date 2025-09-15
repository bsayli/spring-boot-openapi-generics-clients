package com.example.demo.customer.api.controller;

import static com.example.demo.common.api.ResponseMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.common.api.response.ServiceResponse;
import com.example.demo.customer.api.dto.*;
import com.example.demo.customer.service.CustomerService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
  }

  @Test
  @DisplayName("POST /v1/customers -> 201 CREATED + ApiResponse(CREATED)")
  void createCustomer_shouldReturnCreated() {
    var request = new CustomerCreateRequest("John Smith", "john.smith@example.com");
    when(customerService.createCustomer(request)).thenReturn(dto1);

    ResponseEntity<ServiceResponse<CustomerCreateResponse>> resp =
        controller.createCustomer(request);

    assertEquals(HttpStatus.CREATED, resp.getStatusCode());
    assertNotNull(resp.getBody());
    assertEquals(CREATED, resp.getBody().message());
    assertNotNull(resp.getBody().data());
    assertEquals(dto1, resp.getBody().data().customer());
    assertNotNull(resp.getBody().data().createdAt());
    verify(customerService).createCustomer(request);
  }

  @Test
  @DisplayName("GET /v1/customers/{id} -> 200 OK + ApiResponse.ok(dto)")
  void getCustomer_shouldReturnOk() {
    when(customerService.getCustomer(1)).thenReturn(dto1);

    ResponseEntity<ServiceResponse<CustomerDto>> resp = controller.getCustomer(1);

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertNotNull(resp.getBody());
    assertNotNull(resp.getBody().data());
    assertEquals(dto1, resp.getBody().data());
    verify(customerService).getCustomer(1);
  }

  @Test
  @DisplayName("GET /v1/customers -> 200 OK + ApiResponse(LISTED)")
  void getCustomers_shouldReturnListed() {
    when(customerService.getCustomers()).thenReturn(List.of(dto1, dto2));

    ResponseEntity<ServiceResponse<CustomerListResponse>> resp = controller.getCustomers();

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertNotNull(resp.getBody());
    assertEquals(LISTED, resp.getBody().message());
    assertNotNull(resp.getBody().data());
    assertEquals(2, resp.getBody().data().customers().size());
    assertEquals(dto1, resp.getBody().data().customers().get(0));
    verify(customerService).getCustomers();
  }

  @Test
  @DisplayName("PUT /v1/customers/{id} -> 200 OK + ApiResponse(UPDATED)")
  void updateCustomer_shouldReturnUpdated() {
    var req = new CustomerUpdateRequest("John Smith", "john.smith@example.com");
    var updated = new CustomerDto(1, req.name(), req.email());
    when(customerService.updateCustomer(1, req)).thenReturn(updated);

    ResponseEntity<ServiceResponse<CustomerUpdateResponse>> resp =
        controller.updateCustomer(1, req);

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertNotNull(resp.getBody());
    assertEquals(UPDATED, resp.getBody().message());
    assertNotNull(resp.getBody().data());
    assertEquals(updated, resp.getBody().data().customer());
    assertNotNull(resp.getBody().data().updatedAt());
    verify(customerService).updateCustomer(1, req);
  }

  @Test
  @DisplayName("DELETE /v1/customers/{id} -> 200 OK + ApiResponse(DELETED)")
  void deleteCustomer_shouldReturnDeleted() {
    doNothing().when(customerService).deleteCustomer(1);

    ResponseEntity<ServiceResponse<CustomerDeleteResponse>> resp = controller.deleteCustomer(1);

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertNotNull(resp.getBody());
    assertEquals(DELETED, resp.getBody().message());
    assertNotNull(resp.getBody().data());
    assertEquals(1, resp.getBody().data().customerId());
    assertNotNull(resp.getBody().data().deletedAt());
    assertFalse(resp.getBody().data().deletedAt().isAfter(Instant.now().plusSeconds(1)));

    verify(customerService).deleteCustomer(1);
  }
}

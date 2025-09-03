package com.example.demo.customer.api.controller;

import static com.example.demo.common.api.ApiMessages.CREATED;
import static com.example.demo.common.api.ApiMessages.DELETED;
import static com.example.demo.common.api.ApiMessages.LISTED;
import static com.example.demo.common.api.ApiMessages.UPDATED;

import com.example.demo.common.api.response.ApiResponse;
import com.example.demo.customer.api.dto.CustomerCreateRequest;
import com.example.demo.customer.api.dto.CustomerCreateResponse;
import com.example.demo.customer.api.dto.CustomerDeleteResponse;
import com.example.demo.customer.api.dto.CustomerDto;
import com.example.demo.customer.api.dto.CustomerListResponse;
import com.example.demo.customer.api.dto.CustomerUpdateRequest;
import com.example.demo.customer.api.dto.CustomerUpdateResponse;
import com.example.demo.customer.service.CustomerService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/customers")
@Validated
public class CustomerController {

  private final CustomerService customerService;

  public CustomerController(CustomerService customerService) {
    this.customerService = customerService;
  }

  @PostMapping
  public ResponseEntity<ApiResponse<CustomerCreateResponse>> createCustomer(
      @Valid @RequestBody CustomerCreateRequest request) {
    CustomerDto created = customerService.createCustomer(request);
    CustomerCreateResponse body = new CustomerCreateResponse(created, Instant.now());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.of(HttpStatus.CREATED, CREATED, body));
  }

  @GetMapping("/{customerId}")
  public ResponseEntity<ApiResponse<CustomerDto>> getCustomer(@PathVariable Integer customerId) {
    CustomerDto dto = customerService.getCustomer(customerId);
    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<CustomerListResponse>> getCustomers() {
    List<CustomerDto> all = customerService.getCustomers();
    CustomerListResponse body = new CustomerListResponse(all);
    return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, LISTED, body));
  }

  @PutMapping("/{customerId}")
  public ResponseEntity<ApiResponse<CustomerUpdateResponse>> updateCustomer(
      @PathVariable Integer customerId, @Valid @RequestBody CustomerUpdateRequest request) {
    CustomerDto updated = customerService.updateCustomer(customerId, request);
    CustomerUpdateResponse body = new CustomerUpdateResponse(updated, Instant.now());
    return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, UPDATED, body));
  }

  @DeleteMapping("/{customerId}")
  public ResponseEntity<ApiResponse<CustomerDeleteResponse>> deleteCustomer(
      @PathVariable Integer customerId) {
    customerService.deleteCustomer(customerId);
    CustomerDeleteResponse body = new CustomerDeleteResponse(customerId, Instant.now());
    return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, DELETED, body));
  }
}

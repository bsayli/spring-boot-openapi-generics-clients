package com.example.demo.customer.api.controller;

import static com.example.demo.common.api.ResponseMessages.CREATED;
import static com.example.demo.common.api.ResponseMessages.DELETED;
import static com.example.demo.common.api.ResponseMessages.LISTED;
import static com.example.demo.common.api.ResponseMessages.UPDATED;

import com.example.demo.common.api.response.ServiceResponse;
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
  public ResponseEntity<ServiceResponse<CustomerCreateResponse>> createCustomer(
      @Valid @RequestBody CustomerCreateRequest request) {
    CustomerDto created = customerService.createCustomer(request);
    CustomerCreateResponse body = new CustomerCreateResponse(created, Instant.now());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ServiceResponse.of(HttpStatus.CREATED, CREATED, body));
  }

  @GetMapping("/{customerId}")
  public ResponseEntity<ServiceResponse<CustomerDto>> getCustomer(
      @PathVariable Integer customerId) {
    CustomerDto dto = customerService.getCustomer(customerId);
    return ResponseEntity.ok(ServiceResponse.ok(dto));
  }

  @GetMapping
  public ResponseEntity<ServiceResponse<CustomerListResponse>> getCustomers() {
    List<CustomerDto> all = customerService.getCustomers();
    CustomerListResponse body = new CustomerListResponse(all);
    return ResponseEntity.ok(ServiceResponse.of(HttpStatus.OK, LISTED, body));
  }

  @PutMapping("/{customerId}")
  public ResponseEntity<ServiceResponse<CustomerUpdateResponse>> updateCustomer(
      @PathVariable Integer customerId, @Valid @RequestBody CustomerUpdateRequest request) {
    CustomerDto updated = customerService.updateCustomer(customerId, request);
    CustomerUpdateResponse body = new CustomerUpdateResponse(updated, Instant.now());
    return ResponseEntity.ok(ServiceResponse.of(HttpStatus.OK, UPDATED, body));
  }

  @DeleteMapping("/{customerId}")
  public ResponseEntity<ServiceResponse<CustomerDeleteResponse>> deleteCustomer(
      @PathVariable Integer customerId) {
    customerService.deleteCustomer(customerId);
    CustomerDeleteResponse body = new CustomerDeleteResponse(customerId, Instant.now());
    return ResponseEntity.ok(ServiceResponse.of(HttpStatus.OK, DELETED, body));
  }
}

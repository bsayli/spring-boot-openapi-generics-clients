package io.github.bsayli.customerservice.api.controller;

import static io.github.bsayli.customerservice.common.api.ApiConstants.Response.CREATED;
import static io.github.bsayli.customerservice.common.api.ApiConstants.Response.DELETED;
import static io.github.bsayli.customerservice.common.api.ApiConstants.Response.LISTED;
import static io.github.bsayli.customerservice.common.api.ApiConstants.Response.UPDATED;

import io.github.bsayli.customerservice.api.dto.*;
import io.github.bsayli.customerservice.common.api.response.ServiceResponse;
import io.github.bsayli.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(value = "/v1/customers", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class CustomerController {

  private final CustomerService customerService;

  public CustomerController(CustomerService customerService) {
    this.customerService = customerService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<ServiceResponse<CustomerCreateResponse>> createCustomer(
      @Valid @RequestBody CustomerCreateRequest request) {

    CustomerDto created = customerService.createCustomer(request);
    CustomerCreateResponse body = new CustomerCreateResponse(created, Instant.now());

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.customerId())
            .toUri();

    return ResponseEntity.created(location)
        .body(ServiceResponse.of(HttpStatus.CREATED, CREATED, body));
  }

  @GetMapping("/{customerId}")
  public ResponseEntity<ServiceResponse<CustomerDto>> getCustomer(
      @PathVariable @Min(1) Integer customerId) {
    CustomerDto dto = customerService.getCustomer(customerId);
    return ResponseEntity.ok(ServiceResponse.ok(dto));
  }

  @GetMapping
  public ResponseEntity<ServiceResponse<CustomerListResponse>> getCustomers() {
    List<CustomerDto> all = customerService.getCustomers();
    CustomerListResponse body = new CustomerListResponse(all);
    return ResponseEntity.ok(ServiceResponse.of(HttpStatus.OK, LISTED, body));
  }

  @PutMapping(path = "/{customerId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ServiceResponse<CustomerUpdateResponse>> updateCustomer(
      @PathVariable @Min(1) Integer customerId, @Valid @RequestBody CustomerUpdateRequest request) {

    CustomerDto updated = customerService.updateCustomer(customerId, request);
    CustomerUpdateResponse body = new CustomerUpdateResponse(updated, Instant.now());
    return ResponseEntity.ok(ServiceResponse.of(HttpStatus.OK, UPDATED, body));
  }

  @DeleteMapping("/{customerId}")
  public ResponseEntity<ServiceResponse<CustomerDeleteResponse>> deleteCustomer(
      @PathVariable @Min(1) Integer customerId) {

    customerService.deleteCustomer(customerId);
    CustomerDeleteResponse body = new CustomerDeleteResponse(customerId, Instant.now());
    return ResponseEntity.ok(ServiceResponse.of(HttpStatus.OK, DELETED, body));
  }
}

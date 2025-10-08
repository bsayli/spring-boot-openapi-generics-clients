package io.github.bsayli.customerservice.api.controller;

import io.github.bsayli.customerservice.api.dto.*;
import io.github.bsayli.customerservice.common.api.response.Meta;
import io.github.bsayli.customerservice.common.api.response.Page;
import io.github.bsayli.customerservice.common.api.response.ServiceResponse;
import io.github.bsayli.customerservice.common.api.sort.Sort;
import io.github.bsayli.customerservice.common.api.sort.SortDirection;
import io.github.bsayli.customerservice.common.api.sort.SortField;
import io.github.bsayli.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
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
  public ResponseEntity<ServiceResponse<CustomerDto>> createCustomer(
      @Valid @RequestBody CustomerCreateRequest request) {

    CustomerDto created = customerService.createCustomer(request);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.customerId())
            .toUri();

    return ResponseEntity.created(location).body(ServiceResponse.ok(created));
  }

  @GetMapping("/{customerId}")
  public ResponseEntity<ServiceResponse<CustomerDto>> getCustomer(
      @PathVariable @Min(1) Integer customerId) {
    CustomerDto dto = customerService.getCustomer(customerId);
    return ResponseEntity.ok(ServiceResponse.ok(dto));
  }

  @GetMapping
  public ResponseEntity<ServiceResponse<Page<CustomerDto>>> getCustomers(
      @ModelAttribute CustomerSearchCriteria criteria,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "5") @Min(1) @Max(10) int size,
      @RequestParam(defaultValue = "customerId") SortField sortBy,
      @RequestParam(defaultValue = "asc") SortDirection direction) {
    var paged = customerService.getCustomers(criteria, page, size, sortBy, direction);
    var meta = Meta.now(sortBy, direction);
    return ResponseEntity.ok(ServiceResponse.ok(paged, meta));
  }

  @PutMapping(path = "/{customerId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ServiceResponse<CustomerDto>> updateCustomer(
      @PathVariable @Min(1) Integer customerId, @Valid @RequestBody CustomerUpdateRequest request) {

    CustomerDto updated = customerService.updateCustomer(customerId, request);
    return ResponseEntity.ok(ServiceResponse.ok(updated));
  }

  @DeleteMapping("/{customerId}")
  public ResponseEntity<ServiceResponse<CustomerDeleteResponse>> deleteCustomer(
      @PathVariable @Min(1) Integer customerId) {

    customerService.deleteCustomer(customerId);
    var body = new CustomerDeleteResponse(customerId);
    return ResponseEntity.ok(ServiceResponse.ok(body));
  }
}

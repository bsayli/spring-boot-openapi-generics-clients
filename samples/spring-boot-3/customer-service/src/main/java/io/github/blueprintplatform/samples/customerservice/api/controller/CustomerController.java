package io.github.blueprintplatform.samples.customerservice.api.controller;

import io.github.blueprintplatform.contracts.customer.CustomerDto;
import io.github.blueprintplatform.openapi.generics.contract.envelope.Meta;
import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.contract.paging.SortDirection;
import io.github.blueprintplatform.samples.customerservice.api.dto.*;
import io.github.blueprintplatform.samples.customerservice.common.api.sort.SortField;
import io.github.blueprintplatform.samples.customerservice.service.CustomerService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(value = "/customers", produces = MediaType.APPLICATION_JSON_VALUE)
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

    return ResponseEntity.created(location).body(ServiceResponse.of(created));
  }

  @GetMapping("/{customerId}")
  public ResponseEntity<ServiceResponse<CustomerDto>> getCustomer(
      @PathVariable @Min(1) Integer customerId) {
    CustomerDto dto = customerService.getCustomer(customerId);
    return ResponseEntity.ok(ServiceResponse.of(dto));
  }

  @GetMapping
  public ResponseEntity<ServiceResponse<Page<CustomerDto>>> getCustomers(
      @ModelAttribute CustomerSearchCriteria criteria,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "5") @Min(1) @Max(10) int size,
      @RequestParam(defaultValue = "customerId") SortField sortBy,
      @RequestParam(defaultValue = "asc") SortDirection direction) {
    var paged = customerService.getCustomers(criteria, page, size, sortBy, direction);
    var meta = Meta.now(sortBy.value(), direction);
    return ResponseEntity.ok(ServiceResponse.of(paged, meta));
  }

  @PutMapping(path = "/{customerId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ServiceResponse<CustomerDto>> updateCustomer(
      @PathVariable @Min(1) Integer customerId, @Valid @RequestBody CustomerUpdateRequest request) {

    CustomerDto updated = customerService.updateCustomer(customerId, request);
    return ResponseEntity.ok(ServiceResponse.of(updated));
  }

  @DeleteMapping("/{customerId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponse(responseCode = "204", description = "Customer deleted")
  public ResponseEntity<Void> deleteCustomer(@PathVariable @Min(1) Integer customerId) {

    customerService.deleteCustomer(customerId);
    return ResponseEntity.noContent().build();
  }
}

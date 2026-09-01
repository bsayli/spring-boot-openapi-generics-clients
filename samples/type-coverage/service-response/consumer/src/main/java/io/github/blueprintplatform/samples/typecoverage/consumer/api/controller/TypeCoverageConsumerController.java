package io.github.blueprintplatform.samples.typecoverage.consumer.api.controller;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.AddressDto;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.CoverageStatus;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.TypeProfileDto;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.TypeSummaryDto;
import io.github.blueprintplatform.samples.typecoverage.consumer.service.TypeCoverageConsumerService;
import io.github.blueprintplatform.samples.typecoverage.contract.Batch;
import io.github.blueprintplatform.samples.typecoverage.contract.Window;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping(value = "/types", produces = MediaType.APPLICATION_JSON_VALUE)
public class TypeCoverageConsumerController {

  private final TypeCoverageConsumerService service;

  public TypeCoverageConsumerController(TypeCoverageConsumerService service) {
    this.service = service;
  }

  @GetMapping("/async/summary")
  public ResponseEntity<ServiceResponse<TypeSummaryDto>> asyncSummary() {
    return ResponseEntity.ok(service.asyncSummary());
  }

  @GetMapping("/async/paged-summaries")
  public ResponseEntity<ServiceResponse<Page<TypeSummaryDto>>> asyncPagedSummaries() {
    return ResponseEntity.ok(service.asyncPagedSummaries());
  }

  @GetMapping("/scalars/string")
  public ResponseEntity<ServiceResponse<String>> stringValue() {
    return ResponseEntity.ok(service.stringValue());
  }

  @GetMapping("/scalars/boolean")
  public ResponseEntity<ServiceResponse<Boolean>> booleanValue() {
    return ResponseEntity.ok(service.booleanValue());
  }

  @GetMapping("/scalars/integer")
  public ResponseEntity<ServiceResponse<Integer>> integerValue() {
    return ResponseEntity.ok(service.integerValue());
  }

  @GetMapping("/scalars/long")
  public ResponseEntity<ServiceResponse<Long>> longValue() {
    return ResponseEntity.ok(service.longValue());
  }

  @GetMapping("/scalars/decimal")
  public ResponseEntity<ServiceResponse<BigDecimal>> decimalValue() {
    return ResponseEntity.ok(service.decimalValue());
  }

  @GetMapping("/values/uuid")
  public ResponseEntity<ServiceResponse<UUID>> uuidValue() {
    return ResponseEntity.ok(service.uuidValue());
  }

  @GetMapping("/values/date")
  public ResponseEntity<ServiceResponse<LocalDate>> dateValue() {
    return ResponseEntity.ok(service.dateValue());
  }

  @GetMapping("/values/datetime")
  public ResponseEntity<ServiceResponse<OffsetDateTime>> dateTimeValue() {
    return ResponseEntity.ok(service.dateTimeValue());
  }

  @GetMapping("/values/enum")
  public ResponseEntity<ServiceResponse<CoverageStatus>> enumValue() {
    return ResponseEntity.ok(service.enumValue());
  }

  @GetMapping("/objects/address")
  public ResponseEntity<ServiceResponse<AddressDto>> address() {
    return ResponseEntity.ok(service.address());
  }

  @GetMapping("/objects/profile")
  public ResponseEntity<ServiceResponse<TypeProfileDto>> profile() {
    return ResponseEntity.ok(service.profile());
  }

  @GetMapping("/pages/summaries")
  public ResponseEntity<ServiceResponse<Page<TypeSummaryDto>>> pagedSummaries() {
    return ResponseEntity.ok(service.pagedSummaries());
  }

  @GetMapping("/pages/statuses")
  public ResponseEntity<ServiceResponse<Page<CoverageStatus>>> pagedStatuses() {
    return ResponseEntity.ok(service.pagedStatuses());
  }

  @GetMapping("/lists/summaries")
  public ResponseEntity<ServiceResponse<List<TypeSummaryDto>>> listSummaries() {
    return ResponseEntity.ok(service.listSummaries());
  }

  @GetMapping("/lists/statuses")
  public ResponseEntity<ServiceResponse<List<CoverageStatus>>> listStatuses() {
    return ResponseEntity.ok(service.listStatuses());
  }

  @GetMapping("/sets/summaries")
  public ResponseEntity<ServiceResponse<Set<TypeSummaryDto>>> setSummaries() {
    return ResponseEntity.ok(service.setSummaries());
  }

  @GetMapping("/sets/statuses")
  public ResponseEntity<ServiceResponse<Set<CoverageStatus>>> setStatuses() {
    return ResponseEntity.ok(service.setStatuses());
  }

  @GetMapping("/windows/summaries")
  public ResponseEntity<ServiceResponse<Window<TypeSummaryDto>>> windowSummaries() {
    return ResponseEntity.ok(service.windowSummaries());
  }

  @GetMapping("/windows/statuses")
  public ResponseEntity<ServiceResponse<Window<CoverageStatus>>> windowStatuses() {
    return ResponseEntity.ok(service.windowStatuses());
  }

  @GetMapping("/batches/summaries")
  public ResponseEntity<ServiceResponse<Batch<TypeSummaryDto>>> batchSummaries() {
    return ResponseEntity.ok(service.batchSummaries());
  }

  @GetMapping("/batches/statuses")
  public ResponseEntity<ServiceResponse<Batch<CoverageStatus>>> batchStatuses() {
    return ResponseEntity.ok(service.batchStatuses());
  }
}

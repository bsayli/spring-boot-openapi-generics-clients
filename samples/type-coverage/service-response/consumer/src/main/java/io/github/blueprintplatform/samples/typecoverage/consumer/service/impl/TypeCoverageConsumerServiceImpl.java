package io.github.blueprintplatform.samples.typecoverage.consumer.service.impl;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.typecoverage.client.adapter.TypeCoverageClientAdapter;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.AddressDto;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.CoverageStatus;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.TypeProfileDto;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.TypeSummaryDto;
import io.github.blueprintplatform.samples.typecoverage.consumer.service.TypeCoverageConsumerService;
import io.github.blueprintplatform.samples.typecoverage.contract.Batch;
import io.github.blueprintplatform.samples.typecoverage.contract.Window;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TypeCoverageConsumerServiceImpl implements TypeCoverageConsumerService {

  private final TypeCoverageClientAdapter adapter;

  public TypeCoverageConsumerServiceImpl(TypeCoverageClientAdapter adapter) {
    this.adapter = adapter;
  }

  @Override
  public ServiceResponse<String> stringValue() {
    return adapter.stringValue();
  }

  @Override
  public ServiceResponse<Boolean> booleanValue() {
    return adapter.booleanValue();
  }

  @Override
  public ServiceResponse<Integer> integerValue() {
    return adapter.integerValue();
  }

  @Override
  public ServiceResponse<Long> longValue() {
    return adapter.longValue();
  }

  @Override
  public ServiceResponse<BigDecimal> decimalValue() {
    return adapter.decimalValue();
  }

  @Override
  public ServiceResponse<UUID> uuidValue() {
    return adapter.uuidValue();
  }

  @Override
  public ServiceResponse<LocalDate> dateValue() {
    return adapter.dateValue();
  }

  @Override
  public ServiceResponse<OffsetDateTime> dateTimeValue() {
    return adapter.dateTimeValue();
  }

  @Override
  public ServiceResponse<CoverageStatus> enumValue() {
    return adapter.enumValue();
  }

  @Override
  public ServiceResponse<AddressDto> address() {
    return adapter.address();
  }

  @Override
  public ServiceResponse<TypeProfileDto> profile() {
    return adapter.profile();
  }

  @Override
  public ServiceResponse<Page<TypeSummaryDto>> pagedSummaries() {
    return adapter.pagedSummaries();
  }

  @Override
  public ServiceResponse<Page<CoverageStatus>> pagedStatuses() {
    return adapter.pagedStatuses();
  }

  @Override
  public ServiceResponse<List<TypeSummaryDto>> listSummaries() {
    return adapter.listSummaries();
  }

  @Override
  public ServiceResponse<List<CoverageStatus>> listStatuses() {
    return adapter.listStatuses();
  }

  @Override
  public ServiceResponse<Set<TypeSummaryDto>> setSummaries() {
    return adapter.setSummaries();
  }

  @Override
  public ServiceResponse<Set<CoverageStatus>> setStatuses() {
    return adapter.setStatuses();
  }

  @Override
  public ServiceResponse<Window<TypeSummaryDto>> windowSummaries() {
    return adapter.windowSummaries();
  }

  @Override
  public ServiceResponse<Window<CoverageStatus>> windowStatuses() {
    return adapter.windowStatuses();
  }

  @Override
  public ServiceResponse<Batch<TypeSummaryDto>> batchSummaries() {
    return adapter.batchSummaries();
  }

  @Override
  public ServiceResponse<Batch<CoverageStatus>> batchStatuses() {
    return adapter.batchStatuses();
  }
}
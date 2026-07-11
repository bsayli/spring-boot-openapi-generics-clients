package io.github.blueprintplatform.samples.typecoverage.client.adapter.impl;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.typecoverage.client.adapter.TypeCoverageClientAdapter;
import io.github.blueprintplatform.samples.typecoverage.client.generated.api.*;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.AddressDto;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.CoverageStatus;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.TypeProfileDto;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.TypeSummaryDto;
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
public class TypeCoverageClientAdapterImpl implements TypeCoverageClientAdapter {

  private final ScalarPayloadControllerApi scalarApi;
  private final ValuePayloadControllerApi valueApi;
  private final ObjectPayloadControllerApi objectApi;
  private final PagedPayloadControllerApi pagedApi;
  private final ListPayloadControllerApi listApi;
  private final SetPayloadControllerApi setApi;
  private final WindowPayloadControllerApi windowApi;
  private final BatchPayloadControllerApi batchApi;

  public TypeCoverageClientAdapterImpl(
          ScalarPayloadControllerApi scalarApi,
          ValuePayloadControllerApi valueApi,
          ObjectPayloadControllerApi objectApi,
          PagedPayloadControllerApi pagedApi,
          ListPayloadControllerApi listApi,
          SetPayloadControllerApi setApi,
          WindowPayloadControllerApi windowApi,
          BatchPayloadControllerApi batchApi) {
    this.scalarApi = scalarApi;
    this.valueApi = valueApi;
    this.objectApi = objectApi;
    this.pagedApi = pagedApi;
    this.listApi = listApi;
    this.setApi = setApi;
    this.windowApi = windowApi;
    this.batchApi = batchApi;
  }

  @Override
  public ServiceResponse<String> stringValue() {
    return scalarApi.stringValue();
  }

  @Override
  public ServiceResponse<Boolean> booleanValue() {
    return scalarApi.booleanValue();
  }

  @Override
  public ServiceResponse<Integer> integerValue() {
    return scalarApi.integerValue();
  }

  @Override
  public ServiceResponse<Long> longValue() {
    return scalarApi.longValue();
  }

  @Override
  public ServiceResponse<BigDecimal> decimalValue() {
    return scalarApi.decimalValue();
  }

  @Override
  public ServiceResponse<UUID> uuidValue() {
    return valueApi.uuidValue();
  }

  @Override
  public ServiceResponse<LocalDate> dateValue() {
    return valueApi.dateValue();
  }

  @Override
  public ServiceResponse<OffsetDateTime> dateTimeValue() {
    return valueApi.dateTimeValue();
  }

  @Override
  public ServiceResponse<CoverageStatus> enumValue() {
    return valueApi.enumValue();
  }

  @Override
  public ServiceResponse<AddressDto> address() {
    return objectApi.address();
  }

  @Override
  public ServiceResponse<TypeProfileDto> profile() {
    return objectApi.profile();
  }

  @Override
  public ServiceResponse<Page<TypeSummaryDto>> pagedSummaries() {
    return pagedApi.pagedSummaries();
  }

  @Override
  public ServiceResponse<Page<CoverageStatus>> pagedStatuses() {
    return pagedApi.pagedStatuses();
  }

  @Override
  public ServiceResponse<List<TypeSummaryDto>> listSummaries() {
    return listApi.listSummaries();
  }

  @Override
  public ServiceResponse<List<CoverageStatus>> listStatuses() {
    return listApi.listStatuses();
  }

  @Override
  public ServiceResponse<Set<TypeSummaryDto>> setSummaries() {
    return setApi.setSummaries();
  }

  @Override
  public ServiceResponse<Set<CoverageStatus>> setStatuses() {
    return setApi.setStatuses();
  }

  @Override
  public ServiceResponse<Window<TypeSummaryDto>> windowSummaries() {
    return windowApi.windowSummaries();
  }

  @Override
  public ServiceResponse<Window<CoverageStatus>> windowStatuses() {
    return windowApi.windowStatuses();
  }

  @Override
  public ServiceResponse<Batch<TypeSummaryDto>> batchSummaries() {
    return batchApi.batchSummaries();
  }

  @Override
  public ServiceResponse<Batch<CoverageStatus>> batchStatuses() {
    return batchApi.batchStatuses();
  }
}
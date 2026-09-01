package io.github.blueprintplatform.samples.typecoverage.consumer.service;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.AddressDto;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.CoverageStatus;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.TypeProfileDto;
import io.github.blueprintplatform.samples.typecoverage.client.generated.dto.TypeSummaryDto;
import io.github.blueprintplatform.samples.typecoverage.contract.Batch;
import io.github.blueprintplatform.samples.typecoverage.contract.Window;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface TypeCoverageConsumerService {

  ServiceResponse<TypeSummaryDto> asyncSummary();

  ServiceResponse<Page<TypeSummaryDto>> asyncPagedSummaries();

  ServiceResponse<String> stringValue();

  ServiceResponse<Boolean> booleanValue();

  ServiceResponse<Integer> integerValue();

  ServiceResponse<Long> longValue();

  ServiceResponse<BigDecimal> decimalValue();

  ServiceResponse<UUID> uuidValue();

  ServiceResponse<LocalDate> dateValue();

  ServiceResponse<OffsetDateTime> dateTimeValue();

  ServiceResponse<CoverageStatus> enumValue();

  ServiceResponse<AddressDto> address();

  ServiceResponse<TypeProfileDto> profile();

  ServiceResponse<Page<TypeSummaryDto>> pagedSummaries();

  ServiceResponse<Page<CoverageStatus>> pagedStatuses();

  ServiceResponse<List<TypeSummaryDto>> listSummaries();

  ServiceResponse<List<CoverageStatus>> listStatuses();

  ServiceResponse<Set<TypeSummaryDto>> setSummaries();

  ServiceResponse<Set<CoverageStatus>> setStatuses();

  ServiceResponse<Window<TypeSummaryDto>> windowSummaries();

  ServiceResponse<Window<CoverageStatus>> windowStatuses();

  ServiceResponse<Batch<TypeSummaryDto>> batchSummaries();

  ServiceResponse<Batch<CoverageStatus>> batchStatuses();
}

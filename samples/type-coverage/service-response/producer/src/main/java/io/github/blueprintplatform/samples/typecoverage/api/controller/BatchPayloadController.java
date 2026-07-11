package io.github.blueprintplatform.samples.typecoverage.api.controller;

import io.github.blueprintplatform.openapi.generics.contract.envelope.Meta;
import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.SortDirection;
import io.github.blueprintplatform.samples.typecoverage.api.dto.CoverageStatus;
import io.github.blueprintplatform.samples.typecoverage.api.dto.TypeSummaryDto;
import io.github.blueprintplatform.samples.typecoverage.contract.Batch;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/types/batches", produces = MediaType.APPLICATION_JSON_VALUE)
public class BatchPayloadController {

    @GetMapping("/summaries")
    public ResponseEntity<ServiceResponse<Batch<TypeSummaryDto>>> batchSummaries() {
        var elements =
                List.of(
                        new TypeSummaryDto(
                                UUID.fromString("77777777-7777-7777-7777-777777777777"),
                                "SERVICE-BATCH-001",
                                CoverageStatus.ACTIVE),
                        new TypeSummaryDto(
                                UUID.fromString("88888888-8888-8888-8888-888888888888"),
                                "SERVICE-BATCH-002",
                                CoverageStatus.PASSIVE));

        var batch = Batch.of(elements, 0L, false);
        var meta = Meta.now("code", SortDirection.ASC);

        return ResponseEntity.ok(ServiceResponse.of(batch, meta));
    }

    @GetMapping("/statuses")
    public ResponseEntity<ServiceResponse<Batch<CoverageStatus>>> batchStatuses() {
        var elements =
                List.of(CoverageStatus.ACTIVE, CoverageStatus.PASSIVE, CoverageStatus.EXPERIMENTAL);

        var batch = Batch.of(elements, 1L, true);
        var meta = Meta.now("status", SortDirection.ASC);

        return ResponseEntity.ok(ServiceResponse.of(batch, meta));
    }
}
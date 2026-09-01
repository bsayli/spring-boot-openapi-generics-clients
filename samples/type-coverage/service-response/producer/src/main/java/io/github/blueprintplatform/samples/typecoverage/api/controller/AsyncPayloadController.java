package io.github.blueprintplatform.samples.typecoverage.api.controller;

import io.github.blueprintplatform.openapi.generics.contract.envelope.Meta;
import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.Page;
import io.github.blueprintplatform.openapi.generics.contract.paging.SortDirection;
import io.github.blueprintplatform.samples.typecoverage.api.dto.CoverageStatus;
import io.github.blueprintplatform.samples.typecoverage.api.dto.TypeSummaryDto;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * Verifies that supported Spring MVC asynchronous response wrappers are removed before
 * OpenAPI Generics inspects and projects the underlying response contract.
 */
@RestController
@RequestMapping(value = "/types/async", produces = MediaType.APPLICATION_JSON_VALUE)
public class AsyncPayloadController {

  @GetMapping("/summary")
  public CompletionStage<ServiceResponse<TypeSummaryDto>> completionStageSummary() {
    return CompletableFuture.completedFuture(
        ServiceResponse.of(
            new TypeSummaryDto(
                UUID.fromString("77777777-7777-7777-7777-777777777777"),
                "ASYNC-SUMMARY-001",
                CoverageStatus.ACTIVE)));
  }

  @GetMapping("/paged-summaries")
  public DeferredResult<ServiceResponse<Page<TypeSummaryDto>>> deferredPagedSummaries() {
    var content =
        List.of(
            new TypeSummaryDto(
                UUID.fromString("88888888-8888-8888-8888-888888888888"),
                "ASYNC-PAGE-001",
                CoverageStatus.EXPERIMENTAL));

    var page = Page.of(content, 0, 1, 1);
    var meta = Meta.now("code", SortDirection.ASC);
    var result = new DeferredResult<ServiceResponse<Page<TypeSummaryDto>>>();

    result.setResult(ServiceResponse.of(page, meta));

    return result;
  }
}

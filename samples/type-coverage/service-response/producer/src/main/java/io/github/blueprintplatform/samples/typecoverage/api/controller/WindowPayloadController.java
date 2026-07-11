package io.github.blueprintplatform.samples.typecoverage.api.controller;

import io.github.blueprintplatform.openapi.generics.contract.envelope.Meta;
import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.openapi.generics.contract.paging.SortDirection;
import io.github.blueprintplatform.samples.typecoverage.api.dto.CoverageStatus;
import io.github.blueprintplatform.samples.typecoverage.api.dto.TypeSummaryDto;
import io.github.blueprintplatform.samples.typecoverage.contract.Window;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/types/windows", produces = MediaType.APPLICATION_JSON_VALUE)
public class WindowPayloadController {

    @GetMapping("/summaries")
    public ResponseEntity<ServiceResponse<Window<TypeSummaryDto>>> windowSummaries() {
        var items =
                List.of(
                        new TypeSummaryDto(
                                UUID.fromString("55555555-5555-5555-5555-555555555555"),
                                "SERVICE-WINDOW-001",
                                CoverageStatus.ACTIVE),
                        new TypeSummaryDto(
                                UUID.fromString("66666666-6666-6666-6666-666666666666"),
                                "SERVICE-WINDOW-002",
                                CoverageStatus.EXPERIMENTAL));

        var window = Window.of(items, "next-window-token", true);
        var meta = Meta.now("code", SortDirection.ASC);

        return ResponseEntity.ok(ServiceResponse.of(window, meta));
    }

    @GetMapping("/statuses")
    public ResponseEntity<ServiceResponse<Window<CoverageStatus>>> windowStatuses() {
        var items = List.of(CoverageStatus.ACTIVE, CoverageStatus.PASSIVE, CoverageStatus.EXPERIMENTAL);
        var window = Window.of(items, null, false);
        var meta = Meta.now("status", SortDirection.ASC);

        return ResponseEntity.ok(ServiceResponse.of(window, meta));
    }
}
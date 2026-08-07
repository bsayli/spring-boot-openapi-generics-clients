package io.github.blueprintplatform.samples.transportcoverage.consumer.api.controller;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.samples.transportcoverage.client.generated.dto.RegistrationResultDto;
import io.github.blueprintplatform.samples.transportcoverage.client.generated.dto.UploadResultDto;
import io.github.blueprintplatform.samples.transportcoverage.consumer.service.TransportCoverageConsumerService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transport")
public class TransportCoverageConsumerController {

    private final TransportCoverageConsumerService service;

    public TransportCoverageConsumerController(TransportCoverageConsumerService service) {
        this.service = service;
    }

    @PostMapping(value = "/multipart", produces = MediaType.APPLICATION_JSON_VALUE)
    public ServiceResponse<UploadResultDto> multipart(
            @RequestParam(defaultValue = "contract.txt") String fileName,
            @RequestParam(defaultValue = "multipart regression") String description,
            @RequestParam(defaultValue = "contract") String category) {

        return service.upload(
                "transport-coverage".getBytes(),
                fileName,
                description,
                category);
    }

    @GetMapping(
            value = "/binary/{documentId}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> binary(@PathVariable String documentId) {

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(service.download(documentId));
    }

    @PostMapping(value = "/form", produces = MediaType.APPLICATION_JSON_VALUE)
    public ServiceResponse<RegistrationResultDto> form(
            @RequestParam(defaultValue = "blueprint") String name,
            @RequestParam(defaultValue = "platform") String category) {

        return service.register(name, category);
    }
}
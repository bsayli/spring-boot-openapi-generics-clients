package io.github.blueprintplatform.samples.transportcoverage.api.controller;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.samples.transportcoverage.contract.UploadMetadataDto;
import io.github.blueprintplatform.samples.transportcoverage.contract.UploadResultDto;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/transport")
public class MultipartTransportController {

    @PostMapping(
            value = "/multipart",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServiceResponse<UploadResultDto>> upload(
            @RequestPart("file") MultipartFile file,
            @RequestBody(
                    content = @Content(encoding = @Encoding(name = "metadata", contentType = MediaType.APPLICATION_JSON_VALUE)))
            @RequestPart("metadata")
            UploadMetadataDto metadata) {

        var result =
                new UploadResultDto(
                        file.getOriginalFilename(),
                        file.getContentType(),
                        file.getSize(),
                        metadata.description(),
                        metadata.category());

        return ResponseEntity.ok(ServiceResponse.of(result));
    }
}

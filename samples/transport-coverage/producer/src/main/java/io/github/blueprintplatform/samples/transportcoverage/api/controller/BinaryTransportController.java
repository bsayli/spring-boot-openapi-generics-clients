package io.github.blueprintplatform.samples.transportcoverage.api.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/transport")
public class BinaryTransportController {

    @GetMapping(
            value = "/binary/{documentId}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> download(@PathVariable String documentId) {

        byte[] content =
                ("transport-coverage:" + documentId).getBytes(StandardCharsets.UTF_8);

        Resource resource =
                new ByteArrayResource(content) {
                    @Override
                    public String getFilename() {
                        return documentId + ".txt";
                    }
                };

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(content.length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(documentId + ".txt")
                                .build()
                                .toString())
                .body(resource);
    }
}
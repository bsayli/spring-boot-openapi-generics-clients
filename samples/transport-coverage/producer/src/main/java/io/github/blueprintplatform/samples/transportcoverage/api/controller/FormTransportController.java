package io.github.blueprintplatform.samples.transportcoverage.api.controller;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.samples.transportcoverage.contract.RegistrationResultDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/transport")
public class FormTransportController {

    @PostMapping(
            value = "/form",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServiceResponse<RegistrationResultDto>> register(
            @RequestParam String name, @RequestParam String category) {

        String id =
                UUID.nameUUIDFromBytes((name + ":" + category).getBytes(StandardCharsets.UTF_8)).toString();

        return ResponseEntity.ok(ServiceResponse.of(new RegistrationResultDto(id, name, category)));
    }
}

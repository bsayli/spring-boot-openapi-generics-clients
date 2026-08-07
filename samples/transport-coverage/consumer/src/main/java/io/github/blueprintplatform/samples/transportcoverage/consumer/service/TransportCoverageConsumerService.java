package io.github.blueprintplatform.samples.transportcoverage.consumer.service;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.samples.transportcoverage.client.generated.dto.RegistrationResultDto;
import io.github.blueprintplatform.samples.transportcoverage.client.generated.dto.UploadResultDto;
import org.springframework.core.io.Resource;

public interface TransportCoverageConsumerService {

    ServiceResponse<UploadResultDto> upload(
            byte[] content, String fileName, String description, String category);

    Resource download(String documentId);

    ServiceResponse<RegistrationResultDto> register(String name, String category);
}
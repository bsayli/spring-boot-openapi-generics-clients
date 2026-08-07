package io.github.blueprintplatform.samples.transportcoverage.consumer.service.impl;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.samples.transportcoverage.client.adapter.TransportCoverageClientAdapter;
import io.github.blueprintplatform.samples.transportcoverage.client.generated.dto.RegistrationResultDto;
import io.github.blueprintplatform.samples.transportcoverage.client.generated.dto.UploadMetadataDto;
import io.github.blueprintplatform.samples.transportcoverage.client.generated.dto.UploadResultDto;
import io.github.blueprintplatform.samples.transportcoverage.consumer.service.TransportCoverageConsumerService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class TransportCoverageConsumerServiceImpl implements TransportCoverageConsumerService {

    private final TransportCoverageClientAdapter client;

    public TransportCoverageConsumerServiceImpl(TransportCoverageClientAdapter client) {
        this.client = client;
    }

    @Override
    public ServiceResponse<UploadResultDto> upload(
            byte[] content, String fileName, String description, String category) {

        var resource =
                new ByteArrayResource(content) {
                    @Override
                    public String getFilename() {
                        return fileName;
                    }
                };

        var metadata =
                new UploadMetadataDto()
                        .description(description)
                        .category(category);

        return client.upload(resource, metadata);
    }

    @Override
    public Resource download(String documentId) {
        return client.download(documentId);
    }

    @Override
    public ServiceResponse<RegistrationResultDto> register(String name, String category) {
        return client.register(name, category);
    }
}
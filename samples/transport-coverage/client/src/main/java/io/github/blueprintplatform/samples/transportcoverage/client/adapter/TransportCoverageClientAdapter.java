package io.github.blueprintplatform.samples.transportcoverage.client.adapter;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.samples.transportcoverage.client.generated.dto.RegistrationResultDto;
import io.github.blueprintplatform.samples.transportcoverage.client.generated.dto.UploadMetadataDto;
import io.github.blueprintplatform.samples.transportcoverage.client.generated.dto.UploadResultDto;
import org.springframework.core.io.Resource;

public interface TransportCoverageClientAdapter {
    ServiceResponse<UploadResultDto> upload(Resource file, UploadMetadataDto metadata);

    Resource download(String documentId);

    ServiceResponse<RegistrationResultDto> register(String name, String category);
}

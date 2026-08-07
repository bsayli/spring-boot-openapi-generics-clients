package io.github.blueprintplatform.samples.transportcoverage.client.adapter.impl;

import io.github.blueprintplatform.openapi.generics.contract.envelope.ServiceResponse;
import io.github.blueprintplatform.samples.transportcoverage.client.adapter.TransportCoverageClientAdapter;
import io.github.blueprintplatform.samples.transportcoverage.client.generated.api.BinaryTransportControllerApi;
import io.github.blueprintplatform.samples.transportcoverage.client.generated.api.FormTransportControllerApi;
import io.github.blueprintplatform.samples.transportcoverage.client.generated.api.MultipartTransportControllerApi;
import io.github.blueprintplatform.samples.transportcoverage.client.generated.dto.RegistrationResultDto;
import io.github.blueprintplatform.samples.transportcoverage.client.generated.dto.UploadMetadataDto;
import io.github.blueprintplatform.samples.transportcoverage.client.generated.dto.UploadResultDto;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class TransportCoverageClientAdapterImpl implements TransportCoverageClientAdapter {

    private final MultipartTransportControllerApi multipartApi;
    private final BinaryTransportControllerApi binaryApi;
    private final FormTransportControllerApi formApi;

    public TransportCoverageClientAdapterImpl(
            MultipartTransportControllerApi multipartApi,
            BinaryTransportControllerApi binaryApi,
            FormTransportControllerApi formApi) {
        this.multipartApi = multipartApi;
        this.binaryApi = binaryApi;
        this.formApi = formApi;
    }

    @Override
    public ServiceResponse<UploadResultDto> upload(Resource file, UploadMetadataDto metadata) {
        return multipartApi.upload(file, metadata);
    }

    @Override
    public Resource download(String documentId) {
        return binaryApi.download(documentId);
    }

    @Override
    public ServiceResponse<RegistrationResultDto> register(String name, String category) {
        return formApi.register(name, category);
    }
}

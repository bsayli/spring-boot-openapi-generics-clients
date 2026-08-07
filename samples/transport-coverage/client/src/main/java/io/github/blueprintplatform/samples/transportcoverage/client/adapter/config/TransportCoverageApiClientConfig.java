package io.github.blueprintplatform.samples.transportcoverage.client.adapter.config;

import io.github.blueprintplatform.samples.transportcoverage.client.generated.api.BinaryTransportControllerApi;
import io.github.blueprintplatform.samples.transportcoverage.client.generated.api.FormTransportControllerApi;
import io.github.blueprintplatform.samples.transportcoverage.client.generated.api.MultipartTransportControllerApi;
import io.github.blueprintplatform.samples.transportcoverage.client.generated.invoker.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class TransportCoverageApiClientConfig {

    @Bean
    RestClient transportCoverageRestClient(RestClient.Builder builder) {
        return builder.build();
    }

    @Bean
    ApiClient transportCoverageApiClient(
            RestClient transportCoverageRestClient,
            @Value("${transport-coverage.api.base-url}") String baseUrl) {
        return new ApiClient(transportCoverageRestClient).setBasePath(baseUrl);
    }

    @Bean
    MultipartTransportControllerApi multipartTransportControllerApi(ApiClient apiClient) {
        return new MultipartTransportControllerApi(apiClient);
    }

    @Bean
    BinaryTransportControllerApi binaryTransportControllerApi(ApiClient apiClient) {
        return new BinaryTransportControllerApi(apiClient);
    }

    @Bean
    FormTransportControllerApi formTransportControllerApi(ApiClient apiClient) {
        return new FormTransportControllerApi(apiClient);
    }
}

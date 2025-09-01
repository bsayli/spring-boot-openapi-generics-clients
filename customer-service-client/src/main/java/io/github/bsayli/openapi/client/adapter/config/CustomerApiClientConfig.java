package io.github.bsayli.openapi.client.adapter.config;

import io.github.bsayli.openapi.client.generated.api.CustomerControllerApi;
import io.github.bsayli.openapi.client.generated.invoker.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CustomerApiClientConfig {

    @Bean
    public RestClient customerRestClient(RestClient.Builder builder,
                                         @Value("${customer.api.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean
    public ApiClient customerApiClient(RestClient customerRestClient,
                                       @Value("${customer.api.base-url}") String baseUrl) {
        return new ApiClient(customerRestClient).setBasePath(baseUrl);
    }

    @Bean
    public CustomerControllerApi customerControllerApi(ApiClient customerApiClient) {
        return new CustomerControllerApi(customerApiClient);
    }
}
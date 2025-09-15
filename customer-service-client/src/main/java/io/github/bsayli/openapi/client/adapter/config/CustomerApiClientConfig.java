package io.github.bsayli.openapi.client.adapter.config;

import io.github.bsayli.openapi.client.generated.api.CustomerControllerApi;
import io.github.bsayli.openapi.client.generated.invoker.ApiClient;
import java.time.Duration;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class CustomerApiClientConfig {

  @Bean(destroyMethod = "close")
  CloseableHttpClient customerHttpClient(
      @Value("${customer.api.max-connections-total:64}") int maxTotal,
      @Value("${customer.api.max-connections-per-route:16}") int maxPerRoute) {

    var cm =
        PoolingHttpClientConnectionManagerBuilder.create()
            .setMaxConnTotal(maxTotal)
            .setMaxConnPerRoute(maxPerRoute)
            .build();

    return HttpClients.custom()
        .setConnectionManager(cm)
        .evictExpiredConnections()
        .evictIdleConnections(org.apache.hc.core5.util.TimeValue.ofSeconds(30))
        .setUserAgent("customer-service-client")
        .disableAutomaticRetries()
        .build();
  }

  @Bean
  HttpComponentsClientHttpRequestFactory customerRequestFactory(
      CloseableHttpClient customerHttpClient,
      @Value("${customer.api.connect-timeout-seconds:10}") long connect,
      @Value("${customer.api.connection-request-timeout-seconds:10}") long connReq,
      @Value("${customer.api.read-timeout-seconds:15}") long read) {

    var f = new HttpComponentsClientHttpRequestFactory(customerHttpClient);
    f.setConnectTimeout(Duration.ofSeconds(connect));
    f.setConnectionRequestTimeout(Duration.ofSeconds(connReq));
    f.setReadTimeout(Duration.ofSeconds(read));
    return f;
  }

  @Bean
  RestClient customerRestClient(
      RestClient.Builder builder, HttpComponentsClientHttpRequestFactory customerRequestFactory) {
    return builder.requestFactory(customerRequestFactory).build();
  }

  @Bean
  ApiClient customerApiClient(
      RestClient customerRestClient, @Value("${customer.api.base-url}") String baseUrl) {
    return new ApiClient(customerRestClient).setBasePath(baseUrl);
  }

  @Bean
  CustomerControllerApi customerControllerApi(ApiClient customerApiClient) {
    return new CustomerControllerApi(customerApiClient);
  }
}

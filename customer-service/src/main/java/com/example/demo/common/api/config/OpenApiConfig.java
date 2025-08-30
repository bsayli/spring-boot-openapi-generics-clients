package com.example.demo.common.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${app.openapi.base-url:}")
    private String baseUrl;

    @Bean
    public OpenAPI customerServiceOpenAPI() {
        var openapi = new OpenAPI()
                .info(new Info()
                        .title("Customer Service API")
                        .version("0.1.0")
                        .description("Demo: type-safe generic API responses with OpenAPI"));

        if (baseUrl != null && !baseUrl.isBlank()) {
            openapi.addServersItem(new Server()
                    .url(baseUrl)
                    .description("Local service URL"));
        }
        return openapi;
    }
}
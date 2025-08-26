// src/main/java/com/example/demo/common/api/config/OpenApiConfig.java
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

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String normalize(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) return "";
        return path.startsWith("/") ? path : "/" + path;
    }

    @Bean
    public OpenAPI customerServiceOpenAPI() {
        var openapi = new OpenAPI()
                .info(new Info()
                        .title("Customer Service API")
                        .version("0.1.0")
                        .description("Demo: type-safe generic API responses with OpenAPI"));

        if (!isBlank(baseUrl)) {
            openapi.addServersItem(new Server()
                    .url(baseUrl + normalize(contextPath))
                    .description("Local service URL"));
        }
        return openapi;
    }
}
package com.example.demo.common.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Value("${app.openapi.version:${project.version:unknown}}")
  private String version;

  @Value("${app.openapi.base-url:}")
  private String baseUrl;

  @Bean
  public OpenAPI customerServiceOpenAPI() {
    var openapi =
        new OpenAPI()
            .info(
                new Info()
                    .title(OpenApiConstants.TITLE)
                    .version(version)
                    .description(OpenApiConstants.DESCRIPTION));

    if (baseUrl != null && !baseUrl.isBlank()) {
      openapi.addServersItem(
          new Server().url(baseUrl).description(OpenApiConstants.SERVER_DESCRIPTION));
    }
    return openapi;
  }
}

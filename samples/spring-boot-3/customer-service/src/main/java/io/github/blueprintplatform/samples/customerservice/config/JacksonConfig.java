package io.github.blueprintplatform.samples.customerservice.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.blueprintplatform.openapi.generics.contract.paging.SortDirection;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

  @Bean
  public SimpleModule sortDirectionModule() {
    SimpleModule module = new SimpleModule();

    module.addSerializer(
        SortDirection.class,
        new JsonSerializer<SortDirection>() {
          @Override
          public void serialize(
              SortDirection value, JsonGenerator gen, SerializerProvider serializers)
              throws IOException {

            gen.writeString(value.value());
          }
        });

    return module;
  }
}

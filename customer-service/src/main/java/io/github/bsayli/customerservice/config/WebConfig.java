package io.github.bsayli.customerservice.config;

import io.github.bsayli.apicontract.paging.SortDirection;
import io.github.bsayli.customerservice.common.api.sort.SortField;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(String.class, SortField.class, SortField::from);
    registry.addConverter(String.class, SortDirection.class, SortDirection::from);
  }
}

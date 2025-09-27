package io.github.bsayli.customerservice.testconfig;

import io.github.bsayli.customerservice.service.CustomerService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestControllerMocksConfig {

  @Bean
  public CustomerService customerService() {
    return Mockito.mock(CustomerService.class);
  }
}

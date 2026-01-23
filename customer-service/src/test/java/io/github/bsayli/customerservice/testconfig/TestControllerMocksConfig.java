package io.github.bsayli.customerservice.testconfig;

import io.github.bsayli.customerservice.common.i18n.LocalizedMessageResolver;
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

  @Bean
  public LocalizedMessageResolver messageResolver() {
    var mr = Mockito.mock(LocalizedMessageResolver.class);

    Mockito.when(mr.getMessage("problem.title.not_found")).thenReturn("Resource not found");
    Mockito.when(mr.getMessage("problem.title.internal_error")).thenReturn("Internal server error");
    Mockito.when(mr.getMessage("problem.title.bad_request")).thenReturn("Bad request");
    Mockito.when(mr.getMessage("problem.title.validation_failed")).thenReturn("Validation failed");
    Mockito.when(mr.getMessage("problem.title.method_not_allowed"))
        .thenReturn("Method not allowed");

    Mockito.when(mr.getMessage("problem.detail.not_found"))
        .thenReturn("Requested resource was not found.");
    Mockito.when(mr.getMessage("problem.detail.internal_error"))
        .thenReturn("Unexpected error occurred.");
    Mockito.when(mr.getMessage("problem.detail.bad_request")).thenReturn("Malformed request body.");
    Mockito.when(mr.getMessage("problem.detail.validation_failed"))
        .thenReturn("One or more fields are invalid.");
    Mockito.when(mr.getMessage("problem.detail.method_not_allowed"))
        .thenReturn("The request method is not supported for this resource.");

    Mockito.when(mr.getMessage("request.body.invalid")).thenReturn("Invalid JSON payload.");
    Mockito.when(mr.getMessage("request.endpoint.not_found")).thenReturn("Endpoint not found.");
    Mockito.when(mr.getMessage("request.resource.not_found")).thenReturn("Resource not found.");
    Mockito.when(mr.getMessage("request.param.invalid"))
        .thenReturn("One or more parameters are invalid.");

    Mockito.when(mr.getMessage("server.internal.error"))
        .thenReturn("Internal server error. Please try again later.");

    Mockito.when(mr.getMessage("request.method.not_supported", "GET"))
        .thenReturn("HTTP method not supported: GET");
    Mockito.when(mr.getMessage("request.method.not_supported", "POST"))
        .thenReturn("HTTP method not supported: POST");
    Mockito.when(mr.getMessage("request.method.not_supported", "PUT"))
        .thenReturn("HTTP method not supported: PUT");
    Mockito.when(mr.getMessage("request.method.not_supported", "DELETE"))
        .thenReturn("HTTP method not supported: DELETE");

    Mockito.when(mr.getMessage("request.param.required_missing", "page"))
        .thenReturn("Missing required parameter: page");
    Mockito.when(mr.getMessage("request.param.required_missing", "size"))
        .thenReturn("Missing required parameter: size");
    Mockito.when(mr.getMessage("request.param.required_missing", "sortBy"))
        .thenReturn("Missing required parameter: sortBy");
    Mockito.when(mr.getMessage("request.param.required_missing", "direction"))
        .thenReturn("Missing required parameter: direction");

    Mockito.when(mr.getMessage("request.header.missing", "Authorization"))
        .thenReturn("Required request header 'Authorization' is missing");

    Mockito.when(mr.getMessage("request.param.type_mismatch", "Integer"))
        .thenReturn("Invalid value (expected Integer).");
    Mockito.when(mr.getMessage("request.param.type_mismatch", "SortDirection"))
        .thenReturn("Invalid value (expected SortDirection).");
    Mockito.when(mr.getMessage("request.param.type_mismatch", "SortField"))
        .thenReturn("Invalid value (expected SortField).");

    Mockito.when(mr.getMessage("request.body.field.unrecognized", "foo"))
        .thenReturn("Unrecognized field: 'foo'");
    Mockito.when(mr.getMessage("request.body.invalid_format", "String", "123"))
        .thenReturn("Invalid format: expected String, value 123");

    return mr;
  }
}

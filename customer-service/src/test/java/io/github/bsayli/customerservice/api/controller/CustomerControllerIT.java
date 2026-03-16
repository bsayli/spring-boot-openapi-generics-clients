package io.github.bsayli.customerservice.api.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bsayli.apicontract.paging.Page;
import io.github.bsayli.apicontract.paging.SortDirection;
import io.github.bsayli.customerservice.api.dto.CustomerCreateRequest;
import io.github.bsayli.customerservice.api.dto.CustomerDto;
import io.github.bsayli.customerservice.api.dto.CustomerSearchCriteria;
import io.github.bsayli.customerservice.api.dto.CustomerUpdateRequest;
import io.github.bsayli.customerservice.api.error.ApiRequestExceptionHandler;
import io.github.bsayli.customerservice.api.error.ApplicationExceptionHandler;
import io.github.bsayli.customerservice.common.api.sort.SortField;
import io.github.bsayli.customerservice.service.CustomerService;
import io.github.bsayli.customerservice.testconfig.TestControllerMocksConfig;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CustomerController.class)
@Import({
  ApiRequestExceptionHandler.class,
  ApplicationExceptionHandler.class,
  TestControllerMocksConfig.class
})
@Tag("integration")
class CustomerControllerIT {

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper om;
  @Autowired private CustomerService customerService;

  @Test
  @DisplayName("POST /v1/customers -> 201 Created, Location header ve ServiceResponse(data, meta)")
  void createCustomer_created201_withLocation() throws Exception {
    var req = new CustomerCreateRequest("John Smith", "john.smith@example.com");
    var dto = new CustomerDto(1, req.name(), req.email());
    when(customerService.createCustomer(any(CustomerCreateRequest.class))).thenReturn(dto);

    mvc.perform(
            post("/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(req)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", endsWith("/v1/customers/1")))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.customerId").value(1))
        .andExpect(jsonPath("$.data.name").value("John Smith"))
        .andExpect(jsonPath("$.data.email").value("john.smith@example.com"))
        .andExpect(jsonPath("$.meta.serverTime").exists())
        .andExpect(jsonPath("$.meta.sort").isArray());
  }

  @Test
  @DisplayName("POST /v1/customers -> 400 validation error (MethodArgumentNotValid)")
  void createCustomer_validationError_methodArgumentNotValid() throws Exception {
    var badJson =
        """
            {"name":"","email":"not-an-email"}
            """;

    mvc.perform(post("/v1/customers").contentType(MediaType.APPLICATION_JSON).content(badJson))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("urn:customer-service:problem:validation-failed"))
        .andExpect(jsonPath("$.title").value("Validation failed"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.detail").value("One or more fields are invalid."))
        .andExpect(jsonPath("$.instance").value("/v1/customers"))
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.extensions.errors").isArray())
        .andExpect(jsonPath("$.extensions.errors.length()").value(3))
        .andExpect(jsonPath("$.extensions.errors[*].code", hasItems("VALIDATION_FAILED")))
        .andExpect(jsonPath("$.extensions.errors[*].field", hasItems("name", "email")))
        .andExpect(
            jsonPath(
                "$.extensions.errors[*].message",
                hasItems(
                    "must not be blank",
                    "must be a well-formed email address",
                    "size must be between 2 and 80")));
  }

  @Test
  @DisplayName("POST /v1/customers -> 400 invalid JSON (HttpMessageNotReadable)")
  void createCustomer_badJson_notReadable() throws Exception {
    var malformed = "{ \"name\": \"John\", \"email\": }";

    mvc.perform(post("/v1/customers").contentType(MediaType.APPLICATION_JSON).content(malformed))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("urn:customer-service:problem:bad-request"))
        .andExpect(jsonPath("$.title").value("Bad request"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.detail").value("Malformed request body."))
        .andExpect(jsonPath("$.instance").value("/v1/customers"))
        .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.extensions.errors[0].code").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.extensions.errors[0].message").value("Invalid JSON payload."));
  }

  @Test
  @DisplayName("GET /v1/customers/{id} -> 200 OK (one customer)")
  void getCustomer_ok200() throws Exception {
    var dto = new CustomerDto(1, "John Smith", "john.smith@example.com");
    when(customerService.getCustomer(1)).thenReturn(dto);

    mvc.perform(get("/v1/customers/{id}", 1))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.customerId").value(1))
        .andExpect(jsonPath("$.data.name").value("John Smith"))
        .andExpect(jsonPath("$.data.email").value("john.smith@example.com"))
        .andExpect(jsonPath("$.meta.serverTime").exists())
        .andExpect(jsonPath("$.meta.sort").isArray());
  }

  @Test
  @DisplayName("GET /v1/customers/{id} -> 404 NOT_FOUND (NoSuchElementException)")
  void getCustomer_notFound404() throws Exception {
    when(customerService.getCustomer(99))
        .thenThrow(new NoSuchElementException("Customer not found: 99"));

    mvc.perform(get("/v1/customers/{id}", 99))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("urn:customer-service:problem:not-found"))
        .andExpect(jsonPath("$.title").value("Resource not found"))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value("Requested resource was not found."))
        .andExpect(jsonPath("$.instance").value("/v1/customers/99"))
        .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
        .andExpect(jsonPath("$.extensions.errors[0].code").value("NOT_FOUND"))
        .andExpect(jsonPath("$.extensions.errors[0].message").value("Customer not found: 99"))
        .andExpect(jsonPath("$.extensions.errors[0].resource").value("Customer"));
  }

  @Test
  @DisplayName("GET /v1/customers/{id} -> 404 NOT_FOUND fallback message")
  void getCustomer_notFound404_fallbackMessage() throws Exception {
    when(customerService.getCustomer(77)).thenThrow(new NoSuchElementException(""));

    mvc.perform(get("/v1/customers/{id}", 77))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("urn:customer-service:problem:not-found"))
        .andExpect(jsonPath("$.title").value("Resource not found"))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value("Requested resource was not found."))
        .andExpect(jsonPath("$.instance").value("/v1/customers/77"))
        .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
        .andExpect(jsonPath("$.extensions.errors[0].code").value("NOT_FOUND"))
        .andExpect(jsonPath("$.extensions.errors[0].message").value("Resource not found."))
        .andExpect(jsonPath("$.extensions.errors[0].resource").value("Customer"));
  }

  @Test
  @DisplayName("GET /v1/customers/{id} -> 400 BAD_REQUEST (type mismatch)")
  void getCustomer_typeMismatch400() throws Exception {
    mvc.perform(get("/v1/customers/{id}", "abc"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("urn:customer-service:problem:bad-request"))
        .andExpect(jsonPath("$.title").value("Bad request"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.detail").value("One or more parameters are invalid."))
        .andExpect(jsonPath("$.instance").value("/v1/customers/abc"))
        .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.extensions.errors[0].code").value("BAD_REQUEST"))
        .andExpect(
            jsonPath("$.extensions.errors[0].message").value("Invalid value (expected Integer)."))
        .andExpect(jsonPath("$.extensions.errors[0].field").value("customerId"));
  }

  @Test
  @DisplayName("GET /v1/customers/{id} -> 400 validation error (@Min violation)")
  void getCustomer_constraintViolation_min() throws Exception {
    mvc.perform(get("/v1/customers/{id}", 0))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("urn:customer-service:problem:validation-failed"))
        .andExpect(jsonPath("$.title").value("Validation failed"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.detail").value("One or more fields are invalid."))
        .andExpect(jsonPath("$.instance").value("/v1/customers/0"))
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.extensions.errors[0].code").value("VALIDATION_FAILED"));
  }

  @Test
  @DisplayName("GET /v1/customers/{id} -> 500 Internal Server Error (RFC 9457 ProblemDetail)")
  void getCustomer_internalServerError_generic() throws Exception {
    when(customerService.getCustomer(1)).thenThrow(new RuntimeException("Unexpected failure"));

    mvc.perform(get("/v1/customers/{id}", 1))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("urn:customer-service:problem:internal-error"))
        .andExpect(jsonPath("$.title").value("Internal server error"))
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.detail").value("Internal server error. Please try again later."))
        .andExpect(jsonPath("$.instance").value("/v1/customers/1"))
        .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
        .andExpect(jsonPath("$.extensions.errors[0].code").value("INTERNAL_ERROR"))
        .andExpect(
            jsonPath("$.extensions.errors[0].message")
                .value("Internal server error. Please try again later."));
  }

  @Test
  @DisplayName("GET /v1/customers -> 200 OK, Page<CustomerDto> + default meta.sort")
  void getCustomers_list200_defaultSort() throws Exception {
    var d1 = new CustomerDto(1, "John Smith", "john.smith@example.com");
    var d2 = new CustomerDto(2, "Ahmet Yilmaz", "ahmet.yilmaz@example.com");
    var page = Page.of(List.of(d1, d2), 0, 5, 2);

    when(customerService.getCustomers(
            any(CustomerSearchCriteria.class),
            anyInt(),
            anyInt(),
            any(SortField.class),
            any(SortDirection.class)))
        .thenReturn(page);

    mvc.perform(get("/v1/customers"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.content.length()").value(2))
        .andExpect(jsonPath("$.data.content[0].customerId").value(1))
        .andExpect(jsonPath("$.data.page").value(0))
        .andExpect(jsonPath("$.data.size").value(5))
        .andExpect(jsonPath("$.data.totalElements").value(2))
        .andExpect(jsonPath("$.data.totalPages").value(1))
        .andExpect(jsonPath("$.meta.serverTime").exists())
        .andExpect(jsonPath("$.meta.sort[0].field").value("customerId"))
        .andExpect(jsonPath("$.meta.sort[0].direction").value("asc"));
  }

  @Test
  @DisplayName("GET /v1/customers -> 200 OK, custom sort metadata")
  void getCustomers_list200_customSort() throws Exception {
    var d1 = new CustomerDto(2, "Jane Doe", "jane.doe@example.com");
    var d2 = new CustomerDto(1, "Ahmet Yilmaz", "ahmet.yilmaz@example.com");
    var page = Page.of(List.of(d1, d2), 0, 5, 2);

    when(customerService.getCustomers(
            any(CustomerSearchCriteria.class),
            anyInt(),
            anyInt(),
            any(SortField.class),
            any(SortDirection.class)))
        .thenReturn(page);

    mvc.perform(get("/v1/customers").param("sortBy", "name").param("direction", "desc"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.content.length()").value(2))
        .andExpect(jsonPath("$.meta.serverTime").exists())
        .andExpect(jsonPath("$.meta.sort[0].field").value("name"))
        .andExpect(jsonPath("$.meta.sort[0].direction").value("desc"));
  }

  @Test
  @DisplayName("GET /v1/customers -> 400 BAD_REQUEST (sortBy type mismatch)")
  void getCustomers_sortBy_typeMismatch400() throws Exception {
    mvc.perform(get("/v1/customers").param("sortBy", "foo"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("urn:customer-service:problem:bad-request"))
        .andExpect(jsonPath("$.title").value("Bad request"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.detail").value("One or more parameters are invalid."))
        .andExpect(jsonPath("$.instance").value("/v1/customers"))
        .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.extensions.errors[0].code").value("BAD_REQUEST"))
        .andExpect(
            jsonPath("$.extensions.errors[0].message").value("Invalid value (expected SortField)."))
        .andExpect(jsonPath("$.extensions.errors[0].field").value("sortBy"));
  }

  @Test
  @DisplayName("GET /v1/customers -> 400 validation error (page @Min)")
  void getCustomers_page_validation_min() throws Exception {
    mvc.perform(get("/v1/customers").param("page", "-1"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("urn:customer-service:problem:validation-failed"))
        .andExpect(jsonPath("$.title").value("Validation failed"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.detail").value("One or more fields are invalid."))
        .andExpect(jsonPath("$.instance").value("/v1/customers"))
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.extensions.errors[0].code").value("VALIDATION_FAILED"));
  }

  @Test
  @DisplayName("GET /v1/customers -> 400 validation error (size @Min)")
  void getCustomers_size_validation_min() throws Exception {
    mvc.perform(get("/v1/customers").param("size", "0"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("urn:customer-service:problem:validation-failed"))
        .andExpect(jsonPath("$.title").value("Validation failed"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.detail").value("One or more fields are invalid."))
        .andExpect(jsonPath("$.instance").value("/v1/customers"))
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.extensions.errors[0].code").value("VALIDATION_FAILED"));
  }

  @Test
  @DisplayName("GET /v1/customers -> 400 validation error (size @Max)")
  void getCustomers_size_validation_max() throws Exception {
    mvc.perform(get("/v1/customers").param("size", "11"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("urn:customer-service:problem:validation-failed"))
        .andExpect(jsonPath("$.title").value("Validation failed"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.detail").value("One or more fields are invalid."))
        .andExpect(jsonPath("$.instance").value("/v1/customers"))
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.extensions.errors[0].code").value("VALIDATION_FAILED"));
  }

  @Test
  @DisplayName("PUT /v1/customers/{id} -> 200 OK (update)")
  void updateCustomer_ok200() throws Exception {
    var req = new CustomerUpdateRequest("Jane Doe", "jane.doe@example.com");
    var updated = new CustomerDto(1, req.name(), req.email());
    when(customerService.updateCustomer(1, req)).thenReturn(updated);

    mvc.perform(
            put("/v1/customers/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(req)))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.customerId").value(1))
        .andExpect(jsonPath("$.data.name").value("Jane Doe"))
        .andExpect(jsonPath("$.data.email").value("jane.doe@example.com"))
        .andExpect(jsonPath("$.meta.serverTime").exists())
        .andExpect(jsonPath("$.meta.sort").isArray());
  }

  @Test
  @DisplayName("PUT /v1/customers/{id} -> 400 validation error (MethodArgumentNotValid)")
  void updateCustomer_validationError_methodArgumentNotValid() throws Exception {
    var badJson =
        """
            {"name":"","email":"bad-email"}
            """;

    mvc.perform(
            put("/v1/customers/{id}", 1).contentType(MediaType.APPLICATION_JSON).content(badJson))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("urn:customer-service:problem:validation-failed"))
        .andExpect(jsonPath("$.title").value("Validation failed"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.detail").value("One or more fields are invalid."))
        .andExpect(jsonPath("$.instance").value("/v1/customers/1"))
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.extensions.errors").isArray())
        .andExpect(jsonPath("$.extensions.errors.length()").value(3))
        .andExpect(jsonPath("$.extensions.errors[*].field", hasItems("name", "email")));
  }

  @Test
  @DisplayName("DELETE /v1/customers/{id} -> 200 OK (delete)")
  void deleteCustomer_ok200() throws Exception {
    doNothing().when(customerService).deleteCustomer(1);

    mvc.perform(delete("/v1/customers/{id}", 1))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.customerId").value(1))
        .andExpect(jsonPath("$.meta.serverTime").exists())
        .andExpect(jsonPath("$.meta.sort").isArray());
  }

  @Test
  @DisplayName("DELETE /v1/customers -> 405 method not allowed")
  void collectionDelete_methodNotAllowed405() throws Exception {
    mvc.perform(delete("/v1/customers"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("urn:customer-service:problem:method-not-allowed"))
        .andExpect(jsonPath("$.title").value("Method not allowed"))
        .andExpect(jsonPath("$.status").value(405))
        .andExpect(
            jsonPath("$.detail").value("The request method is not supported for this resource."))
        .andExpect(jsonPath("$.instance").value("/v1/customers"))
        .andExpect(jsonPath("$.errorCode").value("METHOD_NOT_ALLOWED"))
        .andExpect(jsonPath("$.extensions.errors[0].code").value("METHOD_NOT_ALLOWED"))
        .andExpect(
            jsonPath("$.extensions.errors[0].message").value("HTTP method not supported: DELETE"));
  }

  @AfterEach
  void resetMocks() {
    Mockito.reset(customerService);
  }
}
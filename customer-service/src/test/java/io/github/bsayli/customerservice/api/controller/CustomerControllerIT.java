package io.github.bsayli.customerservice.api.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bsayli.customerservice.api.dto.CustomerCreateRequest;
import io.github.bsayli.customerservice.api.dto.CustomerDto;
import io.github.bsayli.customerservice.api.dto.CustomerSearchCriteria;
import io.github.bsayli.customerservice.api.dto.CustomerUpdateRequest;
import io.github.bsayli.customerservice.api.error.CustomerControllerAdvice;
import io.github.bsayli.customerservice.common.api.response.Page;
import io.github.bsayli.customerservice.common.api.sort.SortDirection;
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
@Import({CustomerControllerAdvice.class, TestControllerMocksConfig.class})
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
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.customerId").value(1))
        .andExpect(jsonPath("$.data.name").value("John Smith"))
        .andExpect(jsonPath("$.data.email").value("john.smith@example.com"))
        .andExpect(jsonPath("$.meta.serverTime").exists());
  }

  @Test
  @DisplayName("POST /v1/customers -> 400 Bad Request (validation)")
  void createCustomer_validation400() throws Exception {
    var badJson =
        """
      {"name":"","email":"not-an-email"}
      """;

    mvc.perform(post("/v1/customers").contentType(MediaType.APPLICATION_JSON).content(badJson))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Validation failed"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.violations").isArray());
  }

  @Test
  @DisplayName("POST /v1/customers -> 400 Bad Request when JSON is malformed")
  void createCustomer_badJson_notReadable() throws Exception {
    var malformed = "{ \"name\": \"John\", \"email\": }"; // invalid JSON

    mvc.perform(post("/v1/customers").contentType(MediaType.APPLICATION_JSON).content(malformed))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Bad request"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"));
  }

  @Test
  @DisplayName("GET /v1/customers/{id} -> 200 OK ve tek müşteri")
  void getCustomer_ok200() throws Exception {
    var dto = new CustomerDto(1, "John Smith", "john.smith@example.com");
    when(customerService.getCustomer(1)).thenReturn(dto);

    mvc.perform(get("/v1/customers/{id}", 1))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.customerId").value(1))
        .andExpect(jsonPath("$.data.name").value("John Smith"))
        .andExpect(jsonPath("$.meta.serverTime").exists());
  }

  @Test
  @DisplayName("GET /v1/customers/{id} -> 404 NOT_FOUND (NoSuchElementException)")
  void getCustomer_notFound404() throws Exception {
    when(customerService.getCustomer(99))
        .thenThrow(new NoSuchElementException("Customer not found: 99"));

    mvc.perform(get("/v1/customers/{id}", 99))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Resource not found"))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
        .andExpect(jsonPath("$.detail").value("Customer not found: 99"));
  }

  @Test
  @DisplayName("GET /v1/customers/{id} -> 400 BAD_REQUEST (type mismatch)")
  void getCustomer_typeMismatch400() throws Exception {
    mvc.perform(get("/v1/customers/{id}", "abc"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Bad request"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.violations[0].field").value("customerId"))
        .andExpect(jsonPath("$.violations[0].message").exists());
  }

  @Test
  @DisplayName("GET /v1/customers/{id} -> 400 Bad Request on @Min violation (id=0)")
  void getCustomer_constraintViolation_min() throws Exception {
    mvc.perform(get("/v1/customers/{id}", 0)) // @Min(1) violated
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Validation failed"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.violations").isArray());
  }

  @Test
  @DisplayName("GET /v1/customers/{id} -> 500 Internal Server Error handled by advice")
  void getCustomer_internalServerError_generic() throws Exception {
    when(customerService.getCustomer(1)).thenThrow(new RuntimeException("Boom"));

    mvc.perform(get("/v1/customers/{id}", 1))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value("Internal server error"))
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
        .andExpect(jsonPath("$.detail").value("Boom"));
  }

  @Test
  @DisplayName("GET /v1/customers -> 200 OK, Page<CustomerDto> ve meta.sort")
  void getCustomers_list200_paged() throws Exception {
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
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
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
  @DisplayName("PUT /v1/customers/{id} -> 200 OK, data=CustomerDto")
  void updateCustomer_ok200() throws Exception {
    var req = new CustomerUpdateRequest("Jane Doe", "jane.doe@example.com");
    var updated = new CustomerDto(1, req.name(), req.email());
    when(customerService.updateCustomer(1, req)).thenReturn(updated);

    mvc.perform(
            put("/v1/customers/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(req)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.customerId").value(1))
        .andExpect(jsonPath("$.data.name").value("Jane Doe"))
        .andExpect(jsonPath("$.data.email").value("jane.doe@example.com"))
        .andExpect(jsonPath("$.meta.serverTime").exists());
  }

  @Test
  @DisplayName("DELETE /v1/customers/{id} -> 200 OK ve data.customerId")
  void deleteCustomer_ok200() throws Exception {
    doNothing().when(customerService).deleteCustomer(1);

    mvc.perform(delete("/v1/customers/{id}", 1))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.data.customerId").value(1))
        .andExpect(jsonPath("$.meta.serverTime").exists());
  }

  @AfterEach
  void resetMocks() {
    Mockito.reset(customerService);
  }
}

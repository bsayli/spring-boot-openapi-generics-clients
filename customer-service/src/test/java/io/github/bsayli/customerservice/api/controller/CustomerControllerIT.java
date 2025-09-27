package io.github.bsayli.customerservice.api.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bsayli.customerservice.api.dto.CustomerCreateRequest;
import io.github.bsayli.customerservice.api.dto.CustomerDto;
import io.github.bsayli.customerservice.api.dto.CustomerUpdateRequest;
import io.github.bsayli.customerservice.api.error.CustomerControllerAdvice;
import io.github.bsayli.customerservice.service.CustomerService;
import io.github.bsayli.customerservice.testconfig.TestControllerMocksConfig;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
  @DisplayName("POST /v1/customers -> 201 Created, Location header ve ServiceResponse(CREATED)")
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
        .andExpect(jsonPath("$.status").value(201))
        .andExpect(jsonPath("$.message").value("CREATED"))
        .andExpect(jsonPath("$.data.customer.customerId").value(1))
        .andExpect(jsonPath("$.data.customer.name").value("John Smith"))
        .andExpect(jsonPath("$.data.customer.email").value("john.smith@example.com"))
        .andExpect(jsonPath("$.data.createdAt").exists());
  }

  @Test
  @DisplayName("POST /v1/customers -> 400 Bad Request (validation hatası)")
  void createCustomer_validation400() throws Exception {
    var badJson =
        """
      {"name":"","email":"not-an-email"}
      """;
    mvc.perform(post("/v1/customers").contentType(MediaType.APPLICATION_JSON).content(badJson))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.data.code").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.data.timestamp").exists())
        .andExpect(jsonPath("$.data.violations").isArray());
  }

  @Test
  @DisplayName("GET /v1/customers/{id} -> 200 OK ve tek müşteri")
  void getCustomer_ok200() throws Exception {
    var dto = new CustomerDto(1, "John Smith", "john.smith@example.com");
    when(customerService.getCustomer(1)).thenReturn(dto);

    mvc.perform(get("/v1/customers/{id}", 1))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("OK"))
        .andExpect(jsonPath("$.data.customerId").value(1))
        .andExpect(jsonPath("$.data.name").value("John Smith"));
  }

  @Test
  @DisplayName("GET /v1/customers/{id} -> 404 NOT_FOUND (NoSuchElementException)")
  void getCustomer_notFound404() throws Exception {
    when(customerService.getCustomer(99))
        .thenThrow(new NoSuchElementException("Customer not found: 99"));

    mvc.perform(get("/v1/customers/{id}", 99))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value("NOT_FOUND"))
        .andExpect(jsonPath("$.data.code").value("NOT_FOUND"))
        .andExpect(jsonPath("$.data.message").value("Customer not found: 99"));
  }

  @Test
  @DisplayName("GET /v1/customers/{id} -> 400 BAD_REQUEST (type mismatch)")
  void getCustomer_typeMismatch400() throws Exception {
    mvc.perform(get("/v1/customers/{id}", "abc"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.data.code").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.data.violations[0].field").value("customerId"))
        .andExpect(jsonPath("$.data.violations[0].message").exists());
  }

  @Test
  @DisplayName("GET /v1/customers -> 200 OK ve LISTED mesajı")
  void getCustomers_list200() throws Exception {
    var d1 = new CustomerDto(1, "John Smith", "john.smith@example.com");
    var d2 = new CustomerDto(2, "Ahmet Yilmaz", "ahmet.yilmaz@example.com");
    when(customerService.getCustomers()).thenReturn(List.of(d1, d2));

    mvc.perform(get("/v1/customers"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(200))
        .andExpect(jsonPath("$.message").value("LISTED"))
        .andExpect(jsonPath("$.data.customers.length()").value(2))
        .andExpect(jsonPath("$.data.customers[0].customerId").value(1))
        .andExpect(jsonPath("$.data.customers[1].customerId").value(2));
  }

  @Test
  @DisplayName("PUT /v1/customers/{id} -> 200 OK ve UPDATED")
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
        .andExpect(jsonPath("$.message").value("UPDATED"))
        .andExpect(jsonPath("$.data.customer.customerId").value(1))
        .andExpect(jsonPath("$.data.updatedAt").exists());
  }

  @Test
  @DisplayName("DELETE /v1/customers/{id} -> 200 OK ve DELETED")
  void deleteCustomer_ok200() throws Exception {
    doNothing().when(customerService).deleteCustomer(1);

    mvc.perform(delete("/v1/customers/{id}", 1))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value("DELETED"))
        .andExpect(jsonPath("$.data.customerId").value(1))
        .andExpect(jsonPath("$.data.deletedAt").exists());
  }
}

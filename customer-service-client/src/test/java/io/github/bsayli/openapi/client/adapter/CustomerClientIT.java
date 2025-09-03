package io.github.bsayli.openapi.client.adapter;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bsayli.openapi.client.adapter.config.CustomerApiClientConfig;
import io.github.bsayli.openapi.client.generated.api.CustomerControllerApi;
import io.github.bsayli.openapi.client.generated.dto.CustomerCreateRequest;
import io.github.bsayli.openapi.client.generated.dto.CustomerUpdateRequest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestClient;

@SpringJUnitConfig(classes = {CustomerApiClientConfig.class, CustomerClientIT.TestBeans.class})
class CustomerClientIT {

  static MockWebServer server;

  @Autowired
  private CustomerControllerApi api;

  @BeforeAll
  static void startServer() throws Exception {
    server = new MockWebServer();
    server.start();
    System.setProperty("customer.api.base-url", server.url("/customer-service").toString());
  }

  @AfterAll
  static void stopServer() throws Exception {
    server.shutdown();
    System.clearProperty("customer.api.base-url");
  }

  @Test
  @DisplayName("POST /v1/customers -> 201 CREATED + CustomerCreateResponse")
  void createCustomer_shouldReturn201_andMappedBody() {
    var body =
            """
            {
              "status": 201,
              "message": "CREATED",
              "data": {
                "customer": { "customerId": 1, "name": "Jane Doe", "email": "jane@example.com" },
                "createdAt": "2025-01-01T12:34:56Z"
              },
              "errors": []
            }
            """;

    server.enqueue(new MockResponse()
            .setResponseCode(201)
            .addHeader("Content-Type", "application/json")
            .setBody(body));

    var req = new CustomerCreateRequest().name("Jane Doe").email("jane@example.com");
    var resp = api.createCustomer(req);

    assertNotNull(resp);
    assertEquals(201, resp.getStatus());
    assertEquals("CREATED", resp.getMessage());
    assertNotNull(resp.getData().getCustomer());
    assertEquals("Jane Doe", resp.getData().getCustomer().getName());
  }

  @Test
  @DisplayName("GET /v1/customers/{id} -> 200 OK + CustomerDto")
  void getCustomer_shouldReturn200_andMappedBody() {
    var body =
            """
            {
              "status": 200,
              "message": "OK",
              "data": { "customerId": 1, "name": "Jane Doe", "email": "jane@example.com" },
              "errors": []
            }
            """;

    server.enqueue(new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody(body));

    var resp = api.getCustomer(1);

    assertNotNull(resp);
    assertEquals(200, resp.getStatus());
    assertEquals("OK", resp.getMessage());
    assertNotNull(resp.getData());
    assertEquals(1, resp.getData().getCustomerId());
  }

  @Test
  @DisplayName("GET /v1/customers -> 200 OK + CustomerListResponse")
  void getCustomers_shouldReturn200_andMappedBody() {
    var body =
            """
            {
              "status": 200,
              "message": "LISTED",
              "data": {
                "customers": [
                  { "customerId": 1, "name": "Jane Doe", "email": "jane@example.com" },
                  { "customerId": 2, "name": "John Smith", "email": "john.smith@example.com" }
                ]
              },
              "errors": []
            }
            """;

    server.enqueue(new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody(body));

    var resp = api.getCustomers();

    assertNotNull(resp);
    assertEquals(200, resp.getStatus());
    assertEquals("LISTED", resp.getMessage());
    assertNotNull(resp.getData());
    assertEquals(2, resp.getData().getCustomers().size());
  }

  @Test
  @DisplayName("PUT /v1/customers/{id} -> 200 OK + CustomerUpdateResponse")
  void updateCustomer_shouldReturn200_andMappedBody() {
    var body =
            """
            {
              "status": 200,
              "message": "UPDATED",
              "data": {
                "customer": { "customerId": 1, "name": "Jane Updated", "email": "jane.updated@example.com" },
                "updatedAt": "2025-01-02T12:00:00Z"
              },
              "errors": []
            }
            """;

    server.enqueue(new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody(body));

    var req = new CustomerUpdateRequest().name("Jane Updated").email("jane.updated@example.com");
    var resp = api.updateCustomer(1, req);

    assertNotNull(resp);
    assertEquals(200, resp.getStatus());
    assertEquals("UPDATED", resp.getMessage());
    assertNotNull(resp.getData().getCustomer());
    assertEquals("Jane Updated", resp.getData().getCustomer().getName());
  }

  @Test
  @DisplayName("DELETE /v1/customers/{id} -> 200 OK + CustomerDeleteResponse")
  void deleteCustomer_shouldReturn200_andMappedBody() {
    var body =
            """
            {
              "status": 200,
              "message": "DELETED",
              "data": {
                "customerId": 1,
                "deletedAt": "2025-01-02T12:00:00Z"
              },
              "errors": []
            }
            """;

    server.enqueue(new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody(body));

    var resp = api.deleteCustomer(1);

    assertNotNull(resp);
    assertEquals(200, resp.getStatus());
    assertEquals("DELETED", resp.getMessage());
    assertNotNull(resp.getData());
    assertEquals(1, resp.getData().getCustomerId());
  }

  @Configuration
  static class TestBeans {
    @Bean
    RestClient.Builder restClientBuilder() {
      return RestClient.builder();
    }
  }
}
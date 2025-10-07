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

  @Autowired private CustomerControllerApi api;

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
  @DisplayName("POST /v1/customers -> 201 Created + maps {data, meta}")
  void createCustomer_shouldReturn201_andMapBody() {
    var body =
        """
            {
              "data": { "customerId": 1, "name": "Jane Doe", "email": "jane@example.com" },
              "meta": { "requestId": "req-1", "serverTime": "2025-01-01T12:34:56Z", "sort": [] }
            }
            """;

    server.enqueue(
        new MockResponse()
            .setResponseCode(201)
            .addHeader("Content-Type", "application/json")
            .setBody(body));

    var req = new CustomerCreateRequest().name("Jane Doe").email("jane@example.com");
    var resp = api.createCustomer(req);

    assertNotNull(resp);
    assertNotNull(resp.getData());
    assertEquals(1, resp.getData().getCustomerId());
    assertEquals("Jane Doe", resp.getData().getName());
    assertEquals("jane@example.com", resp.getData().getEmail());

    assertNotNull(resp.getMeta());
    assertEquals("req-1", resp.getMeta().getRequestId());
    assertNotNull(resp.getMeta().getServerTime());
  }

  @Test
  @DisplayName("GET /v1/customers/{id} -> 200 OK + maps {data, meta}")
  void getCustomer_shouldReturn200_andMapBody() {
    var body =
        """
            {
              "data": { "customerId": 1, "name": "Jane Doe", "email": "jane@example.com" },
              "meta": { "requestId": "req-2", "serverTime": "2025-01-02T09:00:00Z", "sort": [] }
            }
            """;

    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody(body));

    var resp = api.getCustomer(1);

    assertNotNull(resp);
    assertNotNull(resp.getData());
    assertEquals(1, resp.getData().getCustomerId());
    assertEquals("Jane Doe", resp.getData().getName());

    assertNotNull(resp.getMeta());
    assertEquals("req-2", resp.getMeta().getRequestId());
    assertNotNull(resp.getMeta().getServerTime());
  }

  @Test
  @DisplayName("GET /v1/customers -> 200 OK + maps Page<CustomerDto> in data and meta")
  void getCustomers_shouldReturn200_andMapPage() {
    var body =
        """
            {
              "data": {
                "content": [
                  { "customerId": 1, "name": "Jane Doe", "email": "jane@example.com" },
                  { "customerId": 2, "name": "John Smith", "email": "john.smith@example.com" }
                ],
                "page": 0,
                "size": 5,
                "totalElements": 2,
                "totalPages": 1,
                "hasNext": false,
                "hasPrev": false
              },
              "meta": { "requestId": "req-3", "serverTime": "2025-01-03T10:00:00Z", "sort": [] }
            }
            """;

    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody(body));

    // generated signature accepts query params
    var resp = api.getCustomers(null, null, 0, 5, "customerId", "asc");

    assertNotNull(resp);
    assertNotNull(resp.getData());
    assertEquals(0, resp.getData().getPage());
    assertEquals(5, resp.getData().getSize());
    assertEquals(2L, resp.getData().getTotalElements());
    assertEquals(2, resp.getData().getContent().size());
    assertEquals(1, resp.getData().getContent().get(0).getCustomerId());

    assertNotNull(resp.getMeta());
    assertEquals("req-3", resp.getMeta().getRequestId());
    assertNotNull(resp.getMeta().getServerTime());
  }

  @Test
  @DisplayName("PUT /v1/customers/{id} -> 200 OK + maps {data, meta}")
  void updateCustomer_shouldReturn200_andMapBody() {
    var body =
        """
            {
              "data": { "customerId": 1, "name": "Jane Updated", "email": "jane.updated@example.com" },
              "meta": { "requestId": "req-4", "serverTime": "2025-01-04T12:00:00Z", "sort": [] }
            }
            """;

    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody(body));

    var req = new CustomerUpdateRequest().name("Jane Updated").email("jane.updated@example.com");
    var resp = api.updateCustomer(1, req);

    assertNotNull(resp);
    assertNotNull(resp.getData());
    assertEquals(1, resp.getData().getCustomerId());
    assertEquals("Jane Updated", resp.getData().getName());
    assertEquals("jane.updated@example.com", resp.getData().getEmail());

    assertNotNull(resp.getMeta());
    assertEquals("req-4", resp.getMeta().getRequestId());
    assertNotNull(resp.getMeta().getServerTime());
  }

  @Test
  @DisplayName("DELETE /v1/customers/{id} -> 200 OK + maps {data, meta}")
  void deleteCustomer_shouldReturn200_andMapBody() {
    // NOTE: According to the latest schema, CustomerDeleteResponse has only customerId.
    var body =
        """
            {
              "data": { "customerId": 1 },
              "meta": { "requestId": "req-5", "serverTime": "2025-01-05T08:00:00Z", "sort": [] }
            }
            """;

    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody(body));

    var resp = api.deleteCustomer(1);

    assertNotNull(resp);
    assertNotNull(resp.getData());
    assertEquals(1, resp.getData().getCustomerId());

    assertNotNull(resp.getMeta());
    assertEquals("req-5", resp.getMeta().getRequestId());
    assertNotNull(resp.getMeta().getServerTime());
  }

  @Configuration
  static class TestBeans {
    @Bean
    RestClient.Builder restClientBuilder() {
      return RestClient.builder();
    }
  }
}

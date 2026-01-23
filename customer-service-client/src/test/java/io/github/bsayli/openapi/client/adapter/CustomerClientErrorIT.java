package io.github.bsayli.openapi.client.adapter;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bsayli.openapi.client.adapter.config.CustomerApiClientConfig;
import io.github.bsayli.openapi.client.common.problem.ApiProblemException;
import io.github.bsayli.openapi.client.generated.api.CustomerControllerApi;
import io.github.bsayli.openapi.client.generated.dto.CustomerCreateRequest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestClient;

@SpringJUnitConfig(classes = {CustomerApiClientConfig.class, CustomerClientErrorIT.TestBeans.class})
class CustomerClientErrorIT {

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
  @DisplayName(
      "GET /v1/customers/{id} -> 404 Problem => throws ApiProblemException with parsed body")
  void getCustomer_404_problem() {
    var problem =
        """
                    {
                      "type":"https://example.org/problem/not-found",
                      "title":"Not Found",
                      "status":404,
                      "detail":"Customer 999 not found",
                      "errorCode":"CUS_404"
                    }
                    """;

    server.enqueue(
        new MockResponse()
            .setResponseCode(404)
            .addHeader("Content-Type", "application/problem+json")
            .setBody(problem));

    var ex = assertThrows(ApiProblemException.class, () -> api.getCustomer(999));

    assertEquals(404, ex.getStatus());
    assertNotNull(ex.getProblem());
    assertEquals("Not Found", ex.getProblem().getTitle());
    assertEquals("Customer 999 not found", ex.getProblem().getDetail());
    assertEquals("CUS_404", ex.getProblem().getErrorCode());
  }

  @Test
  @DisplayName("POST /v1/customers -> 400 Problem (validation) => throws ApiProblemException")
  void createCustomer_400_problem() {
    var problem =
        """
                    {
                      "title":"Bad Request",
                      "status":400,
                      "detail":"email must be a well-formed email address",
                      "errorCode":"VAL_001",
                      "extensions": {
                        "errors":[{"code":"invalid_email","message":"email format"}]
                      }
                    }
                    """;

    server.enqueue(
        new MockResponse()
            .setResponseCode(400)
            .addHeader("Content-Type", "application/problem+json")
            .setBody(problem));

    var req = new CustomerCreateRequest().name("Bad Email").email("not-an-email");

    var ex = assertThrows(ApiProblemException.class, () -> api.createCustomer(req));

    assertEquals(400, ex.getStatus());
    assertNotNull(ex.getProblem());
    assertEquals("Bad Request", ex.getProblem().getTitle());
    assertEquals("VAL_001", ex.getProblem().getErrorCode());

    assertTrue(ex.hasErrors());
    assertEquals(1, ex.getErrors().size());
    assertEquals("invalid_email", ex.firstErrorOrNull().getCode());
  }

  @Test
  @DisplayName(
      "DELETE /v1/customers/{id} -> 500 (no body) => throws ApiProblemException with fallback ProblemDetail")
  void deleteCustomer_500_no_body() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(500)
            .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE));

    var ex = assertThrows(ApiProblemException.class, () -> api.deleteCustomer(1));

    assertEquals(500, ex.getStatus());

    var pd = ex.getProblem();
    assertNotNull(pd);

    assertEquals(500, pd.getStatus());
    assertEquals("Empty problem response body", pd.getTitle());
    assertEquals("Upstream returned an empty error response body.", pd.getDetail());
    assertEquals("UPSTREAM_EMPTY_PROBLEM", pd.getErrorCode());
    assertNotNull(pd.getType());
  }

  @Configuration
  static class TestBeans {
    @Bean
    RestClient.Builder restClientBuilder() {
      return RestClient.builder();
    }

    @Bean
    ObjectMapper objectMapper() {
      return new ObjectMapper();
    }
  }
}

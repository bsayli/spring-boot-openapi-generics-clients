package io.github.blueprintplatform.samples.customerservice.client.adapter;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.blueprintplatform.samples.customerservice.client.adapter.config.CustomerApiClientConfig;
import io.github.blueprintplatform.samples.customerservice.client.common.problem.ApiProblemException;
import io.github.blueprintplatform.samples.customerservice.client.generated.api.CustomerControllerApi;
import io.github.blueprintplatform.samples.customerservice.client.generated.dto.CustomerCreateRequest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
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
      "GET /customers/{id} -> 404 Problem => throws ApiProblemException with parsed body")
  void getCustomer_404_problem() {
    var problem =
        """
            {
              "type":"https://example.org/problem/not-found",
              "title":"Not Found",
              "status":404,
              "detail":"Customer 999 not found",
              "instance":"https://example.org/trace/customer-999",
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

    ProblemDetail pd = ex.getProblem();
    assertNotNull(pd);
    assertEquals("Not Found", pd.getTitle());
    assertEquals("Customer 999 not found", pd.getDetail());
    assertNotNull(pd.getType());
    assertEquals("https://example.org/problem/not-found", pd.getType().toString());
    assertNotNull(pd.getInstance());
    assertEquals("https://example.org/trace/customer-999", pd.getInstance().toString());

    assertEquals("CUS_404", ex.getErrorCode());
    assertFalse(ex.hasErrors());
    assertNull(ex.firstErrorOrNull());
  }

  @Test
  @DisplayName("POST /customers -> 400 Problem (validation) => throws ApiProblemException")
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

    ProblemDetail pd = ex.getProblem();
    assertNotNull(pd);
    assertEquals("Bad Request", pd.getTitle());
    assertEquals("email must be a well-formed email address", pd.getDetail());

    assertEquals("VAL_001", ex.getErrorCode());
    assertTrue(ex.hasErrors());
    assertEquals(1, ex.getErrors().size());

    var firstError = ex.firstErrorOrNull();
    assertNotNull(firstError);
    assertEquals("invalid_email", firstError.code());
    assertEquals("email format", firstError.message());
  }

  @Test
  @DisplayName(
      "DELETE /customers/{id} -> 500 (no body) => throws ApiProblemException with fallback ProblemDetail")
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
    assertNotNull(pd.getType());
    assertTrue(pd.getType().toString().contains("upstream-empty"));

    assertEquals("UPSTREAM_EMPTY_PROBLEM", ex.getErrorCode());
    assertTrue(ex.hasErrors());
    assertNotNull(ex.firstErrorOrNull());
  }

  @Test
  @DisplayName("GET /customers/{id} -> 502 text/plain => fallback non-json problem")
  void getCustomer_502_nonJsonFallback() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(502)
            .addHeader("Content-Type", "text/plain")
            .setBody("bad gateway"));

    var ex = assertThrows(ApiProblemException.class, () -> api.getCustomer(10));

    assertEquals(502, ex.getStatus());

    var pd = ex.getProblem();
    assertNotNull(pd);
    assertNotNull(pd.getType());
    assertTrue(pd.getType().toString().contains("upstream-non-json"));

    assertEquals("UPSTREAM_NON_JSON_ERROR", ex.getErrorCode());
    assertTrue(ex.hasErrors());
  }

  @Test
  @DisplayName("GET /customers/{id} -> 500 invalid problem json => fallback unparsable")
  void getCustomer_500_unparsableProblemFallback() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(500)
            .addHeader("Content-Type", "application/problem+json")
            .setBody("{ invalid-json"));

    var ex = assertThrows(ApiProblemException.class, () -> api.getCustomer(20));

    assertEquals(500, ex.getStatus());

    var pd = ex.getProblem();
    assertNotNull(pd);
    assertNotNull(pd.getType());
    assertTrue(pd.getType().toString().contains("upstream-unparsable"));

    assertEquals("UPSTREAM_UNPARSABLE_PROBLEM", ex.getErrorCode());
    assertTrue(ex.hasErrors());
  }

  @Configuration
  static class TestBeans {
    @Bean
    RestClient.Builder restClientBuilder() {
      return RestClient.builder();
    }

    @Bean
    ObjectMapper objectMapper() {
      return Jackson2ObjectMapperBuilder.json().build();
    }
  }
}

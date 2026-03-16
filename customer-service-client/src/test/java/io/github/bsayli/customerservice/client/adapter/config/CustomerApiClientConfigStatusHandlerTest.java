package io.github.bsayli.customerservice.client.adapter.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bsayli.customerservice.client.common.problem.ApiProblemException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@DisplayName("Unit: CustomerApiClientConfig.problemDetailStatusHandler")
class CustomerApiClientConfigStatusHandlerTest {

  @Test
  @DisplayName(
      "400 with application/problem+json -> throws ApiProblemException with parsed ProblemDetail")
  void handler_parses_problem_detail_on_4xx() {
    var om = org.springframework.http.converter.json.Jackson2ObjectMapperBuilder.json().build();
    RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");

    RestClientCustomizer customizer = new CustomerApiClientConfig().problemDetailStatusHandler(om);
    customizer.customize(builder);

    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

    String body =
        """
            {
              "type":"https://example.org/problem/bad-request",
              "title":"Bad Request",
              "status":400,
              "detail":"Validation failed",
              "instance":"https://example.org/trace/abc",
              "errorCode":"VAL_001",
              "extensions": {
                "errors": [
                  { "code":"too_short", "message":"name too short" }
                ]
              }
            }
            """;

    server
        .expect(once(), requestTo("http://localhost/err400"))
        .andRespond(
            withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body));

    RestClient client = builder.build();

    ApiProblemException ex =
        assertThrows(
            ApiProblemException.class,
            () -> client.get().uri("/err400").retrieve().body(String.class));

    assertEquals(400, ex.getStatus());

    ProblemDetail pd = ex.getProblem();
    assertNotNull(pd);
    assertEquals(400, pd.getStatus());
    assertEquals("Bad Request", pd.getTitle());
    assertEquals("Validation failed", pd.getDetail());
    assertNotNull(pd.getType());
    assertEquals("https://example.org/problem/bad-request", pd.getType().toString());
    assertNotNull(pd.getInstance());
    assertEquals("https://example.org/trace/abc", pd.getInstance().toString());

    assertEquals("VAL_001", ex.getErrorCode());
    assertTrue(ex.hasErrors());
    assertEquals(1, ex.getErrors().size());
    assertNotNull(ex.firstErrorOrNull());
    assertEquals("too_short", ex.firstErrorOrNull().code());
    assertEquals("name too short", ex.firstErrorOrNull().message());

    server.verify();
  }

  @Test
  @DisplayName("500 with empty body -> throws ApiProblemException with fallback ProblemDetail")
  void handler_handles_empty_body_on_5xx() {
    var om = new ObjectMapper();
    RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");

    RestClientCustomizer customizer = new CustomerApiClientConfig().problemDetailStatusHandler(om);
    customizer.customize(builder);

    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

    server
        .expect(once(), requestTo("http://localhost/err500"))
        .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

    RestClient client = builder.build();

    ApiProblemException ex =
        assertThrows(
            ApiProblemException.class,
            () -> client.get().uri("/err500").retrieve().body(String.class));

    assertEquals(500, ex.getStatus());

    ProblemDetail pd = ex.getProblem();
    assertNotNull(pd);
    assertEquals(500, pd.getStatus());
    assertEquals("Empty problem response body", pd.getTitle());
    assertEquals("Upstream returned an empty error response body.", pd.getDetail());
    assertNotNull(pd.getType());
    assertTrue(pd.getType().toString().contains("upstream-empty"));

    assertEquals("UPSTREAM_EMPTY_PROBLEM", ex.getErrorCode());
    assertFalse(ex.hasErrors());

    server.verify();
  }
}
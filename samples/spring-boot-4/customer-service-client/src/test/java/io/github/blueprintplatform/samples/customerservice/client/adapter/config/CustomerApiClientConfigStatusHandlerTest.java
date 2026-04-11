package io.github.blueprintplatform.samples.customerservice.client.adapter.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import io.github.blueprintplatform.samples.customerservice.client.adapter.support.ProblemDetailSupport;
import io.github.blueprintplatform.samples.customerservice.client.common.problem.ApiProblemException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@DisplayName("Unit: CustomerApiClientConfig.defaultStatusHandler")
class CustomerApiClientConfigStatusHandlerTest {

  private static final String BASE_URL = "http://localhost";
  private static final String URI_400 = "/err400";
  private static final String URI_500 = "/err500";

  private static final String ERROR_CODE_VALIDATION = "VAL_001";
  private static final String ERROR_CODE_EMPTY = "UPSTREAM_EMPTY_PROBLEM";

  private static final String TITLE_BAD_REQUEST = "Bad Request";
  private static final String DETAIL_VALIDATION = "Validation failed";

  private static final String TITLE_EMPTY = "Empty problem response body";
  private static final String DETAIL_EMPTY =
          "Upstream returned an empty error response body.";

  private ObjectMapper objectMapper() {
    return JsonMapper.builder()
            .findAndAddModules()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();
  }

  private static class TestContext {
    final RestClient client;
    final MockRestServiceServer server;

    private TestContext(RestClient client, MockRestServiceServer server) {
      this.client = client;
      this.server = server;
    }
  }

  private TestContext buildClient(ObjectMapper om) {
    var config = new CustomerApiClientConfig();

    var httpClient = config.customerHttpClient(64, 16, 10, 10, 15);
    var requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

    RestClient.Builder builder =
            RestClient.builder()
                    .baseUrl(BASE_URL)
                    .requestFactory(requestFactory)
                    .defaultStatusHandler(
                            org.springframework.http.HttpStatusCode::isError,
                            (request, response) -> {
                              var pd = ProblemDetailSupport.extract(om, response);
                              throw new ApiProblemException(pd, response.getStatusCode().value());
                            });

    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

    return new TestContext(builder.build(), server);
  }

  @Test
  @DisplayName("400 → parses ProblemDetail and throws ApiProblemException")
  void handler_parses_problem_detail_on_4xx() {
    var ctx = buildClient(objectMapper());

    ctx.server.expect(once(), requestTo(BASE_URL + URI_400))
            .andRespond(
                    withStatus(HttpStatus.BAD_REQUEST)
                            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                            .body(problemJson()));

    ApiProblemException ex = performGetExpectingException(ctx.client, URI_400);

    assertBasicProblem(ex, 400, TITLE_BAD_REQUEST, DETAIL_VALIDATION);
    assertValidationDetails(ex);

    ctx.server.verify();
  }

  @Test
  @DisplayName("500 → empty body fallback ProblemDetail")
  void handler_handles_empty_body_on_5xx() {
    var ctx = buildClient(objectMapper());

    ctx.server.expect(once(), requestTo(BASE_URL + URI_500))
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

    ApiProblemException ex = performGetExpectingException(ctx.client, URI_500);

    assertBasicProblem(ex, 500, TITLE_EMPTY, DETAIL_EMPTY);
    assertEquals(ERROR_CODE_EMPTY, ex.getErrorCode());
    assertFalse(ex.hasErrors());

    ctx.server.verify();
  }

  private ApiProblemException performGetExpectingException(RestClient client, String uri) {
    return assertThrows(
            ApiProblemException.class,
            () -> client.get().uri(uri).retrieve().body(String.class));
  }

  private void assertBasicProblem(
          ApiProblemException ex, int status, String expectedTitle, String expectedDetail) {

    assertEquals(status, ex.getStatus());

    ProblemDetail pd = ex.getProblem();
    assertNotNull(pd);
    assertEquals(expectedTitle, pd.getTitle());
    assertEquals(expectedDetail, pd.getDetail());
  }

  private void assertValidationDetails(ApiProblemException ex) {
    assertEquals(ERROR_CODE_VALIDATION, ex.getErrorCode());

    assertTrue(ex.hasErrors());
    assertEquals(1, ex.getErrors().size());

    var error = ex.firstErrorOrNull();
    assertNotNull(error);
    assertEquals("too_short", error.code());
    assertEquals("name too short", error.message());
  }

  private String problemJson() {
    return """
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
  }
}
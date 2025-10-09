package io.github.bsayli.openapi.client.adapter.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bsayli.openapi.client.generated.dto.ProblemDetail;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;

public final class ProblemDetailSupport {

  private static final Logger log = LoggerFactory.getLogger(ProblemDetailSupport.class);

  private ProblemDetailSupport() {}

  public static ProblemDetail extract(ObjectMapper om, ClientHttpResponse response) {
    ProblemDetail pd;
    MediaType contentType =
            Optional.ofNullable(response.getHeaders().getContentType()).orElse(MediaType.ALL);
    HttpStatusCode status;

    try {
      status = response.getStatusCode();
    } catch (IOException e) {
      log.warn("Unable to read status code from response", e);
      status = HttpStatusCode.valueOf(500);
    }

    try (InputStream is = response.getBody()) {
      byte[] bytes = is.readNBytes(200_000);
      if (bytes.length > 0) {
        pd = om.readValue(bytes, ProblemDetail.class);
      } else {
        pd = fallback(status, "Empty problem response body");
      }
    } catch (IOException e) {
      log.warn(
              "Unable to deserialize ProblemDetail (status={}, contentType={}); using generic fallback",
              status,
              contentType,
              e);
      pd = fallback(status, "Unparseable problem response");
    } catch (Exception e) {
      log.warn("Unexpected error while parsing ProblemDetail", e);
      pd = fallback(status, "Unparseable problem response");
    }

    return pd;
  }

  private static ProblemDetail fallback(HttpStatusCode status, String detail) {
    ProblemDetail pd = new ProblemDetail();
    pd.setStatus(status.value());
    pd.setTitle("HTTP error");
    pd.setDetail(detail);
    return pd;
  }
}
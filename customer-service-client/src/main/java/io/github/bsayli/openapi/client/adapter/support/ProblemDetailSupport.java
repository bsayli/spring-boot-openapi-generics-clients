package io.github.bsayli.openapi.client.adapter.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bsayli.openapi.client.generated.dto.ProblemDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;

public final class ProblemDetailSupport {

  private static final Logger log = LoggerFactory.getLogger(ProblemDetailSupport.class);

  private ProblemDetailSupport() {}

  public static ProblemDetail extract(ObjectMapper om, ClientHttpResponse response) {
    ResponseSnapshot snap = ResponseSnapshot.read(response);

    if (snap.statusReadError() != null) {
      log.warn("Unable to read upstream status code", snap.statusReadError());
    }
    if (snap.bodyReadError() != null) {
      log.warn("Unable to read upstream response body", snap.bodyReadError());
    }

    if (snap.body().length == 0) {
      return snap.statusUnavailable()
          ? ProblemDetailFallbacks.statusUnavailable(snap.contentType(), snap.statusReadError())
          : ProblemDetailFallbacks.emptyBody(
              snap.status(), snap.contentType(), snap.bodyReadError());
    }

    if (!isJson(snap.contentType())) {
      return ProblemDetailFallbacks.nonJson(
          snap.status(), snap.contentType(), snap.statusUnavailable());
    }

    try {
      return om.readValue(snap.body(), ProblemDetail.class);
    } catch (Exception e) {
      log.warn(
          "Unable to deserialize ProblemDetail (status={}, contentType={}, bodyBytes={})",
          snap.status(),
          snap.contentType(),
          snap.body().length,
          e);
      return ProblemDetailFallbacks.unparsable(
          snap.status(), snap.contentType(), snap.statusUnavailable(), e);
    }
  }

  private static boolean isJson(MediaType contentType) {
    if (contentType == null) return false;
    return MediaType.APPLICATION_JSON.isCompatibleWith(contentType)
        || MediaType.APPLICATION_PROBLEM_JSON.isCompatibleWith(contentType);
  }
}

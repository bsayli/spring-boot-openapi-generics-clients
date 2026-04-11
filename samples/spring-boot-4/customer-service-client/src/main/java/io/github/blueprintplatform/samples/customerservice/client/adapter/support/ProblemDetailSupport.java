package io.github.blueprintplatform.samples.customerservice.client.adapter.support;

import io.github.blueprintplatform.openapi.generics.contract.error.ProblemExtensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.client.ClientHttpResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public final class ProblemDetailSupport {

  private static final Logger log = LoggerFactory.getLogger(ProblemDetailSupport.class);

  private static final String KEY_ERROR_CODE = "errorCode";
  private static final String KEY_EXTENSIONS = "extensions";

  private ProblemDetailSupport() {}

  public static ProblemDetail extract(ObjectMapper om, ClientHttpResponse response) {
    ResponseSnapshot snap = ResponseSnapshot.read(response);

    logIfErrors(snap);

    if (snap.body().length == 0) {
      return handleEmptyBody(snap);
    }

    if (!isJson(snap.contentType())) {
      return ProblemDetailFallbacks.nonJson(
              snap.status(), snap.contentType(), snap.statusUnavailable());
    }

    try {
      return deserializeAndEnrich(om, snap);
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

  private static void logIfErrors(ResponseSnapshot snap) {
    if (snap.statusReadError() != null) {
      log.warn("Unable to read upstream status code", snap.statusReadError());
    }
    if (snap.bodyReadError() != null) {
      log.warn("Unable to read upstream response body", snap.bodyReadError());
    }
  }

  private static ProblemDetail handleEmptyBody(ResponseSnapshot snap) {
    return snap.statusUnavailable()
            ? ProblemDetailFallbacks.statusUnavailable(snap.contentType(), snap.statusReadError())
            : ProblemDetailFallbacks.emptyBody(
            snap.status(), snap.contentType(), snap.bodyReadError());
  }

  private static ProblemDetail deserializeAndEnrich(ObjectMapper om, ResponseSnapshot snap) {

    ProblemDetail pd = om.readValue(snap.body(), ProblemDetail.class);
    JsonNode tree = om.readTree(snap.body());

    enrichErrorCode(pd, tree);
    enrichExtensions(om, pd, tree);

    return pd;
  }

  private static void enrichErrorCode(ProblemDetail pd, JsonNode tree) {
    JsonNode node = tree.get(KEY_ERROR_CODE);
    if (node != null && !node.isNull()) {
      pd.setProperty(KEY_ERROR_CODE, node.asText());
    }
  }

  private static void enrichExtensions(ObjectMapper om, ProblemDetail pd, JsonNode tree) {
    JsonNode node = tree.get(KEY_EXTENSIONS);
    if (node == null || node.isNull()) {
      return;
    }

    try {
      ProblemExtensions ext = om.treeToValue(node, ProblemExtensions.class);
      pd.setProperty(KEY_EXTENSIONS, ext);
    } catch (Exception e) {
      log.warn("Failed to map extensions to ProblemExtensions", e);
    }
  }

  private static boolean isJson(MediaType contentType) {
    if (contentType == null) {
      return false;
    }
    return MediaType.APPLICATION_JSON.isCompatibleWith(contentType)
            || MediaType.APPLICATION_PROBLEM_JSON.isCompatibleWith(contentType);
  }
}
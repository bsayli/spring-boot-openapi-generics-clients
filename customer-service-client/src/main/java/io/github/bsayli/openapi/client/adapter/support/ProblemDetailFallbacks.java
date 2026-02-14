package io.github.bsayli.openapi.client.adapter.support;

import io.github.bsayli.openapi.client.generated.dto.ErrorItem;
import io.github.bsayli.openapi.client.generated.dto.ProblemDetail;
import io.github.bsayli.openapi.client.generated.dto.ProblemDetailExtensions;
import java.net.URI;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;

final class ProblemDetailFallbacks {

  private static final HttpStatusCode STATUS_INTERNAL_SERVER_ERROR = HttpStatusCode.valueOf(500);

  private static final String TITLE_HTTP_ERROR = "HTTP error";
  private static final String TITLE_NON_JSON = "Non-JSON error response";
  private static final String TITLE_UNPARSABLE = "Unparseable problem response";
  private static final String TITLE_EMPTY = "Empty problem response body";

  private static final String DETAIL_NON_JSON = "Upstream returned non-JSON error response.";
  private static final String DETAIL_UNPARSABLE =
      "Upstream returned a problem response, but it could not be parsed.";
  private static final String DETAIL_EMPTY = "Upstream returned an empty error response body.";
  private static final String DETAIL_STATUS_UNAVAILABLE =
      "Unable to read HTTP status from upstream.";

  private static final String ERROR_CODE_UPSTREAM_NON_JSON = "UPSTREAM_NON_JSON_ERROR";
  private static final String ERROR_CODE_UPSTREAM_UNPARSABLE = "UPSTREAM_UNPARSABLE_PROBLEM";
  private static final String ERROR_CODE_UPSTREAM_EMPTY = "UPSTREAM_EMPTY_PROBLEM";
  private static final String ERROR_CODE_UPSTREAM_STATUS_UNAVAILABLE =
      "UPSTREAM_STATUS_UNAVAILABLE";

  private static final URI TYPE_NON_JSON =
          URI.create("urn:customer-service:problem:client-fallback-upstream-non-json");

  private static final URI TYPE_UNPARSABLE =
          URI.create("urn:customer-service:problem:client-fallback-upstream-unparsable");

  private static final URI TYPE_EMPTY =
          URI.create("urn:customer-service:problem:client-fallback-upstream-empty");

  private static final URI TYPE_STATUS_UNAVAILABLE =
          URI.create("urn:customer-service:problem:client-fallback-upstream-status-unavailable");

  private static final String ERROR_ITEM_RESOURCE_UPSTREAM = "upstream";
  private static final String ERROR_ITEM_FIELD_CONTENT_TYPE = "contentType";
  private static final String ERROR_ITEM_FIELD_STATUS = "status";
  private static final String ERROR_ITEM_FIELD_CAUSE = "cause";

  private static final String MSG_CONTENT_TYPE_PREFIX = "Upstream Content-Type: ";
  private static final String MSG_STATUS_UNAVAILABLE = "unavailable";

  private ProblemDetailFallbacks() {}

  static ProblemDetail emptyBody(
      HttpStatusCode status, MediaType contentType, Throwable bodyReadError) {
    ProblemDetail pd =
        baseProblem(status, TYPE_EMPTY, TITLE_EMPTY, DETAIL_EMPTY, ERROR_CODE_UPSTREAM_EMPTY);
    addContextErrors(pd, ERROR_CODE_UPSTREAM_EMPTY, false, contentType, bodyReadError);
    return pd;
  }

  static ProblemDetail statusUnavailable(MediaType contentType, Throwable statusReadError) {
    ProblemDetail pd =
        baseProblem(
            STATUS_INTERNAL_SERVER_ERROR,
            TYPE_STATUS_UNAVAILABLE,
            TITLE_HTTP_ERROR,
            DETAIL_STATUS_UNAVAILABLE,
            ERROR_CODE_UPSTREAM_STATUS_UNAVAILABLE);
    addContextErrors(
        pd, ERROR_CODE_UPSTREAM_STATUS_UNAVAILABLE, true, contentType, statusReadError);
    return pd;
  }

  static ProblemDetail nonJson(
      HttpStatusCode status, MediaType contentType, boolean statusUnavailable) {
    ProblemDetail pd =
        baseProblem(
            status, TYPE_NON_JSON, TITLE_NON_JSON, DETAIL_NON_JSON, ERROR_CODE_UPSTREAM_NON_JSON);
    addContextErrors(pd, ERROR_CODE_UPSTREAM_NON_JSON, statusUnavailable, contentType, null);
    return pd;
  }

  static ProblemDetail unparsable(
      HttpStatusCode status,
      MediaType contentType,
      boolean statusUnavailable,
      Throwable parseError) {
    ProblemDetail pd =
        baseProblem(
            status,
            TYPE_UNPARSABLE,
            TITLE_UNPARSABLE,
            DETAIL_UNPARSABLE,
            ERROR_CODE_UPSTREAM_UNPARSABLE);
    addContextErrors(
        pd, ERROR_CODE_UPSTREAM_UNPARSABLE, statusUnavailable, contentType, parseError);
    return pd;
  }

  private static ProblemDetail baseProblem(
      HttpStatusCode status, URI type, String title, String detail, String errorCode) {

    ProblemDetail pd = new ProblemDetail();
    pd.setStatus(status.value());
    pd.setType(type);
    pd.setTitle((title != null && !title.isBlank()) ? title : TITLE_HTTP_ERROR);
    pd.setDetail(detail);
    pd.setErrorCode(errorCode);
    return pd;
  }

  private static void addContextErrors(
      ProblemDetail pd,
      String problemCode,
      boolean statusUnavailable,
      MediaType contentType,
      Throwable cause) {

    ProblemDetailExtensions ext = new ProblemDetailExtensions();

    String ct = contentType != null ? contentType.toString() : "";
    if (!ct.isBlank()) {
      ext.addErrorsItem(
          errorItem(problemCode, MSG_CONTENT_TYPE_PREFIX + ct, ERROR_ITEM_FIELD_CONTENT_TYPE));
    }

    if (statusUnavailable) {
      ext.addErrorsItem(
          errorItem(
              ERROR_CODE_UPSTREAM_STATUS_UNAVAILABLE,
              MSG_STATUS_UNAVAILABLE,
              ERROR_ITEM_FIELD_STATUS));
    }

    if (cause != null) {
      ext.addErrorsItem(
          errorItem(problemCode, cause.getClass().getSimpleName(), ERROR_ITEM_FIELD_CAUSE));
    }

    if (ext.getErrors() != null && !ext.getErrors().isEmpty()) {
      pd.setExtensions(ext);
    }
  }

  private static ErrorItem errorItem(String code, String message, String field) {
    ErrorItem item = new ErrorItem();
    item.setCode(code);
    item.setMessage(message);
    item.setField(field);
    item.setResource(ERROR_ITEM_RESOURCE_UPSTREAM);
    return item;
  }
}

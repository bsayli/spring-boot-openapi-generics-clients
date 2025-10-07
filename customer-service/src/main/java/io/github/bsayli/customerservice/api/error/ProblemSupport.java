package io.github.bsayli.customerservice.api.error;

import io.github.bsayli.customerservice.common.api.response.error.ErrorItem;
import io.github.bsayli.customerservice.common.api.response.error.ProblemExtensions;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.util.UriComponentsBuilder;

final class ProblemSupport {

  static final String KEY_ERROR_CODE = "errorCode";
  static final String KEY_EXTENSIONS = "extensions";

  static final String TYPE_NOT_FOUND = "not-found";
  static final String TYPE_VALIDATION_FAILED = "validation-failed";
  static final String TYPE_BAD_REQUEST = "bad-request";
  static final String TYPE_INTERNAL_ERROR = "internal-error";
  static final String TYPE_METHOD_NOT_ALLOWED = "method-not-allowed";

  static final String TITLE_BAD_REQUEST = "Bad request";
  static final String TITLE_VALIDATION_FAILED = "Validation failed";
  static final String TITLE_NOT_FOUND = "Resource not found";
  static final String TITLE_INTERNAL_ERROR = "Internal server error";
  static final String TITLE_METHOD_NOT_ALLOWED = "Method not allowed";

  static final String DETAIL_NOT_FOUND = "Requested resource was not found.";
  static final String DETAIL_VALIDATION_FAILED = "One or more fields are invalid.";
  static final String DETAIL_NOT_READABLE = "Malformed request body.";
  static final String DETAIL_PARAM_INVALID = "One or more parameters are invalid.";
  static final String DETAIL_GENERIC_ERROR = "Unexpected error occurred.";

  private static final String PROBLEM_BASE = "https://example.com/problems/";

  private ProblemSupport() {}

  static URI type(String slug) {
    return URI.create(PROBLEM_BASE + slug);
  }

  static ProblemDetail baseProblem(
      URI type, HttpStatus status, String title, String detail, HttpServletRequest req) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
    pd.setType(type);
    pd.setTitle(title);
    String path = Optional.ofNullable(req.getRequestURI()).orElse("/");
    pd.setInstance(UriComponentsBuilder.fromPath(path).build().toUri());
    return pd;
  }

  static ErrorItem error(String code, String message, String field, String resource, String id) {
    return new ErrorItem(code, message, field, resource, id);
  }

  static void attachErrors(ProblemDetail pd, String errorCode, List<ErrorItem> errors) {
    pd.setProperty(KEY_ERROR_CODE, errorCode);
    pd.setProperty(KEY_EXTENSIONS, ProblemExtensions.ofErrors(errors));
  }
}

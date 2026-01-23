package io.github.bsayli.customerservice.api.error;

import static io.github.bsayli.customerservice.api.error.ProblemSupport.*;
import static io.github.bsayli.customerservice.common.api.ApiConstants.ErrorCode.BAD_REQUEST;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import io.github.bsayli.apicontract.error.ErrorItem;
import io.github.bsayli.customerservice.common.i18n.LocalizedMessageResolver;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "io.github.bsayli.customerservice.api.controller")
@Order(2)
public class JsonExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(JsonExceptionHandler.class);

  private static final String KEY_PROBLEM_TITLE_BAD_REQUEST = "problem.title.bad_request";
  private static final String KEY_PROBLEM_DETAIL_BAD_REQUEST = "problem.detail.bad_request";

  private static final String KEY_REQUEST_BODY_INVALID = "request.body.invalid";
  private static final String KEY_REQUEST_BODY_FIELD_UNRECOGNIZED =
      "request.body.field.unrecognized";
  private static final String KEY_REQUEST_BODY_INVALID_FORMAT = "request.body.invalid_format";

  private static final String FALLBACK_UNKNOWN = "unknown";

  private final LocalizedMessageResolver messageResolver;

  public JsonExceptionHandler(LocalizedMessageResolver messageResolver) {
    this.messageResolver = messageResolver;
  }

  @ExceptionHandler(InvalidFormatException.class)
  public ProblemDetail handleInvalidFormat(InvalidFormatException ex, HttpServletRequest req) {
    String expectedType = expectedTypeName(ex);
    String actualValue = safeValue(ex.getValue());

    List<ErrorItem> errors =
        ex.getPath().stream()
            .map(
                ref ->
                    error(
                        BAD_REQUEST,
                        messageResolver.getMessage(
                            KEY_REQUEST_BODY_INVALID_FORMAT, expectedType, actualValue),
                        ref.getFieldName(),
                        null,
                        null))
            .toList();

    ProblemDetail pd = buildBadRequestProblem(req);

    attachErrors(
        pd,
        BAD_REQUEST,
        errors.isEmpty()
            ? List.of(
                error(
                    BAD_REQUEST,
                    messageResolver.getMessage(KEY_REQUEST_BODY_INVALID),
                    null,
                    null,
                    null))
            : errors);

    return pd;
  }

  @ExceptionHandler(UnrecognizedPropertyException.class)
  public ProblemDetail handleUnrecognized(
      UnrecognizedPropertyException ex, HttpServletRequest req) {
    String field = ex.getPropertyName();
    log.warn("Unrecognized field: '{}' (known: {})", field, ex.getKnownPropertyIds());

    ProblemDetail pd = buildBadRequestProblem(req);

    attachErrors(
        pd,
        BAD_REQUEST,
        List.of(
            error(
                BAD_REQUEST,
                messageResolver.getMessage(KEY_REQUEST_BODY_FIELD_UNRECOGNIZED, field),
                field,
                null,
                null)));

    return pd;
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ProblemDetail handleNotReadable(
      HttpMessageNotReadableException ex, HttpServletRequest req) {
    String raw =
        Optional.ofNullable(ex.getCause()).map(Throwable::getMessage).orElseGet(ex::getMessage);
    log.warn("Bad request (not readable): {}", raw);

    ProblemDetail pd = buildBadRequestProblem(req);

    attachErrors(
        pd,
        BAD_REQUEST,
        List.of(
            error(
                BAD_REQUEST,
                messageResolver.getMessage(KEY_REQUEST_BODY_INVALID),
                null,
                null,
                null)));

    return pd;
  }

  private ProblemDetail buildBadRequestProblem(HttpServletRequest req) {
    return baseProblem(
        type(TYPE_BAD_REQUEST),
        HttpStatus.BAD_REQUEST,
        messageResolver.getMessage(KEY_PROBLEM_TITLE_BAD_REQUEST),
        messageResolver.getMessage(KEY_PROBLEM_DETAIL_BAD_REQUEST),
        req);
  }

  private String expectedTypeName(InvalidFormatException ex) {
    return Optional.ofNullable(ex.getTargetType())
        .map(Class::getSimpleName)
        .orElse(FALLBACK_UNKNOWN);
  }

  private String safeValue(Object v) {
    if (v == null) return "null";
    String s = v.toString();
    return s.isBlank() ? "null" : s;
  }
}

package io.github.bsayli.customerservice.api.error;

import static io.github.bsayli.customerservice.api.error.ProblemSupport.*;
import static io.github.bsayli.customerservice.common.api.ApiConstants.ErrorCode.*;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import io.github.bsayli.customerservice.common.api.response.error.ErrorItem;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import org.slf4j.*;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(basePackages = "io.github.bsayli.customerservice.api.controller")
@Order(2)
public class JsonExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(JsonExceptionHandler.class);

  @ExceptionHandler(InvalidFormatException.class)
  public ProblemDetail handleInvalidFormat(InvalidFormatException ex, HttpServletRequest req) {
    List<ErrorItem> errors =
        ex.getPath().stream()
            .map(
                ref ->
                    error(
                        BAD_REQUEST,
                        "Invalid format: " + safe(ex.getValue()),
                        ref.getFieldName(),
                        null,
                        null))
            .toList();

    ProblemDetail pd =
        baseProblem(
            type(TYPE_BAD_REQUEST),
            HttpStatus.BAD_REQUEST,
            TITLE_BAD_REQUEST,
            DETAIL_NOT_READABLE,
            req);

    attachErrors(
        pd,
        BAD_REQUEST,
        errors.isEmpty()
            ? List.of(error(BAD_REQUEST, "Invalid JSON payload.", null, null, null))
            : errors);
    return pd;
  }

  @ExceptionHandler(UnrecognizedPropertyException.class)
  public ProblemDetail handleUnrecognized(
      UnrecognizedPropertyException ex, HttpServletRequest req) {
    String field = ex.getPropertyName();
    log.warn("Unrecognized field: '{}' (known: {})", field, ex.getKnownPropertyIds());

    ProblemDetail pd =
        baseProblem(
            type(TYPE_BAD_REQUEST),
            HttpStatus.BAD_REQUEST,
            TITLE_BAD_REQUEST,
            DETAIL_NOT_READABLE,
            req);

    attachErrors(
        pd,
        BAD_REQUEST,
        List.of(error(BAD_REQUEST, "Unrecognized field: '" + field + "'", field, null, null)));
    return pd;
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ProblemDetail handleNotReadable(
      HttpMessageNotReadableException ex, HttpServletRequest req) {
    String raw =
        Optional.ofNullable(ex.getCause()).map(Throwable::getMessage).orElseGet(ex::getMessage);
    log.warn("Bad request (not readable): {}", raw);

    ProblemDetail pd =
        baseProblem(
            type(TYPE_BAD_REQUEST),
            HttpStatus.BAD_REQUEST,
            TITLE_BAD_REQUEST,
            DETAIL_NOT_READABLE,
            req);

    attachErrors(
        pd, BAD_REQUEST, List.of(error(BAD_REQUEST, "Invalid JSON payload.", null, null, null)));
    return pd;
  }

  private String safe(Object v) {
    return v != null ? v.toString() : "null";
  }
}

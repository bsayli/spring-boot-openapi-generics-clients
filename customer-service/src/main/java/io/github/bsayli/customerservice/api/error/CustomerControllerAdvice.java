package io.github.bsayli.customerservice.api.error;

import static io.github.bsayli.customerservice.common.api.ApiConstants.ErrorCode.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.util.UriComponentsBuilder;

@RestControllerAdvice(basePackages = "io.github.bsayli.customerservice.api.controller")
public class CustomerControllerAdvice {

  private static final Logger log = LoggerFactory.getLogger(CustomerControllerAdvice.class);

  // Common JSON keys
  private static final String KEY_ERROR_CODE = "errorCode";
  private static final String KEY_VIOLATIONS = "violations";
  private static final String KEY_FIELD = "field";
  private static final String KEY_MESSAGE = "message";
  private static final String KEY_REQUEST_ID = "requestId";

  // Common problem types
  private static final String TYPE_NOT_FOUND = "not-found";
  private static final String TYPE_VALIDATION_FAILED = "validation-failed";
  private static final String TYPE_BAD_REQUEST = "bad-request";
  private static final String TYPE_INTERNAL_ERROR = "internal-error";

  // Common titles
  private static final String TITLE_BAD_REQUEST = "Bad request";
  private static final String TITLE_VALIDATION_FAILED = "Validation failed";
  private static final String TITLE_NOT_FOUND = "Resource not found";
  private static final String TITLE_INTERNAL_ERROR = "Internal server error";

  // Common details
  private static final String DETAIL_VALIDATION_FAILED = "One or more fields are invalid.";
  private static final String DETAIL_NOT_READABLE = "Request body is not readable or malformed.";
  private static final String DETAIL_GENERIC_ERROR = "Unexpected error occurred.";
  private static final String DETAIL_INVALID_PARAM = "Invalid request parameter";

  private static URI type(String slug) {
    return URI.create("https://example.com/problems/" + slug);
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ProblemDetail> handleNotFound(
      NoSuchElementException ex, HttpServletRequest req) {
    ProblemDetail pd =
        baseProblem(
            type(TYPE_NOT_FOUND), HttpStatus.NOT_FOUND, TITLE_NOT_FOUND, ex.getMessage(), req);
    pd.setProperty(KEY_ERROR_CODE, NOT_FOUND);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleMethodArgInvalid(
      MethodArgumentNotValidException ex, HttpServletRequest req) {
    List<Map<String, String>> violations =
        ex.getBindingResult().getFieldErrors().stream().map(this::toViolation).toList();
    ProblemDetail pd =
        baseProblem(
            type(TYPE_VALIDATION_FAILED),
            HttpStatus.BAD_REQUEST,
            TITLE_VALIDATION_FAILED,
            DETAIL_VALIDATION_FAILED,
            req);
    pd.setProperty(KEY_ERROR_CODE, VALIDATION_FAILED);
    pd.setProperty(KEY_VIOLATIONS, violations);
    return ResponseEntity.badRequest().body(pd);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ProblemDetail> handleConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest req) {
    List<Map<String, String>> violations =
        ex.getConstraintViolations().stream().map(this::toViolation).toList();
    ProblemDetail pd =
        baseProblem(
            type(TYPE_VALIDATION_FAILED),
            HttpStatus.BAD_REQUEST,
            TITLE_VALIDATION_FAILED,
            DETAIL_VALIDATION_FAILED,
            req);
    pd.setProperty(KEY_ERROR_CODE, VALIDATION_FAILED);
    pd.setProperty(KEY_VIOLATIONS, violations);
    return ResponseEntity.badRequest().body(pd);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ProblemDetail> handleNotReadable(
      HttpMessageNotReadableException ex, HttpServletRequest req) {
    String detail =
        Optional.ofNullable(ex.getCause()).map(Throwable::getMessage).orElseGet(ex::getMessage);
    log.warn("Bad request (not readable): {}", detail);
    ProblemDetail pd =
        baseProblem(
            type(TYPE_BAD_REQUEST),
            HttpStatus.BAD_REQUEST,
            TITLE_BAD_REQUEST,
            DETAIL_NOT_READABLE,
            req);
    pd.setProperty(KEY_ERROR_CODE, BAD_REQUEST);
    return ResponseEntity.badRequest().body(pd);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ProblemDetail> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
    String required =
        Optional.ofNullable(ex.getRequiredType()).map(Class::getSimpleName).orElse("unknown");
    String msg = "Type mismatch for parameter '%s' (expected %s)".formatted(ex.getName(), required);
    ProblemDetail pd =
        baseProblem(type(TYPE_BAD_REQUEST), HttpStatus.BAD_REQUEST, TITLE_BAD_REQUEST, msg, req);
    pd.setProperty(KEY_ERROR_CODE, BAD_REQUEST);
    pd.setProperty(
        KEY_VIOLATIONS,
        List.of(
            Map.of(
                KEY_FIELD,
                ex.getName(),
                KEY_MESSAGE,
                "Type mismatch (expected %s)".formatted(required))));
    return ResponseEntity.badRequest().body(pd);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ProblemDetail> handleIllegalArgument(
      IllegalArgumentException ex, HttpServletRequest req) {
    ProblemDetail pd =
        baseProblem(
            type(TYPE_BAD_REQUEST),
            HttpStatus.BAD_REQUEST,
            TITLE_BAD_REQUEST,
            DETAIL_INVALID_PARAM + ": " + ex.getMessage(),
            req);
    pd.setProperty(KEY_ERROR_CODE, BAD_REQUEST);
    return ResponseEntity.badRequest().body(pd);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleGeneric(Exception ex, HttpServletRequest req) {
    log.error("Unhandled exception", ex);
    ProblemDetail pd =
        baseProblem(
            type(TYPE_INTERNAL_ERROR),
            HttpStatus.INTERNAL_SERVER_ERROR,
            TITLE_INTERNAL_ERROR,
            Optional.ofNullable(ex.getMessage()).orElse(DETAIL_GENERIC_ERROR),
            req);
    pd.setProperty(KEY_ERROR_CODE, INTERNAL_ERROR);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
  }

  private ProblemDetail baseProblem(
      URI type, HttpStatus status, String title, String detail, HttpServletRequest req) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
    pd.setType(type);
    pd.setTitle(title);
    String path = Optional.ofNullable(req.getRequestURI()).orElse("/");
    pd.setInstance(UriComponentsBuilder.fromPath(path).build().toUri());
    String requestId = req.getHeader("Request-Id");
    if (requestId != null && !requestId.isBlank()) {
      pd.setProperty(KEY_REQUEST_ID, requestId);
    }
    return pd;
  }

  private Map<String, String> toViolation(FieldError fe) {
    return Map.of(
        KEY_FIELD, Optional.of(fe.getField()).orElse(""),
        KEY_MESSAGE, Optional.ofNullable(fe.getDefaultMessage()).orElse("invalid"));
  }

  private Map<String, String> toViolation(ConstraintViolation<?> v) {
    String path = v.getPropertyPath() == null ? "" : v.getPropertyPath().toString();
    return Map.of(
        KEY_FIELD, path, KEY_MESSAGE, Optional.ofNullable(v.getMessage()).orElse("invalid"));
  }
}

package io.github.bsayli.customerservice.api.error;

import static io.github.bsayli.customerservice.api.error.ProblemSupport.*;
import static io.github.bsayli.customerservice.common.api.ApiConstants.ErrorCode.BAD_REQUEST;
import static io.github.bsayli.customerservice.common.api.ApiConstants.ErrorCode.NOT_FOUND;
import static io.github.bsayli.customerservice.common.api.ApiConstants.ErrorCode.VALIDATION_FAILED;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import io.github.bsayli.apicontract.error.ErrorItem;
import io.github.bsayli.customerservice.common.i18n.LocalizedMessageResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Order(1)
public class ApiRequestExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ApiRequestExceptionHandler.class);

  private static final String KEY_PROBLEM_TITLE_NOT_FOUND = "problem.title.not_found";
  private static final String KEY_PROBLEM_DETAIL_NOT_FOUND = "problem.detail.not_found";

  private static final String KEY_PROBLEM_TITLE_BAD_REQUEST = "problem.title.bad_request";
  private static final String KEY_PROBLEM_DETAIL_BAD_REQUEST = "problem.detail.bad_request";
  private static final String KEY_PROBLEM_DETAIL_PARAM_INVALID = "request.param.invalid";

  private static final String KEY_PROBLEM_TITLE_VALIDATION_FAILED =
      "problem.title.validation_failed";
  private static final String KEY_PROBLEM_DETAIL_VALIDATION_FAILED =
      "problem.detail.validation_failed";

  private static final String KEY_PROBLEM_TITLE_METHOD_NOT_ALLOWED =
      "problem.title.method_not_allowed";
  private static final String KEY_PROBLEM_DETAIL_METHOD_NOT_ALLOWED =
      "problem.detail.method_not_allowed";

  private static final String KEY_ENDPOINT_NOT_FOUND = "request.endpoint.not_found";
  private static final String KEY_METHOD_NOT_SUPPORTED = "request.method.not_supported";
  private static final String KEY_PARAM_REQUIRED_MISSING = "request.param.required_missing";
  private static final String KEY_HEADER_REQUIRED_MISSING = "request.header.missing";
  private static final String KEY_PARAM_TYPE_MISMATCH = "request.param.type_mismatch";

  private static final String KEY_REQUEST_BODY_INVALID = "request.body.invalid";
  private static final String KEY_REQUEST_BODY_FIELD_UNRECOGNIZED =
      "request.body.field.unrecognized";
  private static final String KEY_REQUEST_BODY_INVALID_FORMAT = "request.body.invalid_format";

  private static final String ERROR_CODE_METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";
  private static final String FALLBACK_INVALID = "invalid";
  private static final String FALLBACK_UNKNOWN = "unknown";

  private final LocalizedMessageResolver messageResolver;

  public ApiRequestExceptionHandler(LocalizedMessageResolver messageResolver) {
    this.messageResolver = messageResolver;
  }

  @Override
  @Nullable
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      @NonNull WebRequest request) {

    HttpServletRequest req = ((ServletWebRequest) request).getRequest();

    List<ErrorItem> errors =
        ex.getBindingResult().getFieldErrors().stream().map(this::toErrorItem).toList();

    ProblemDetail pd = buildValidationProblem(req);
    attachErrors(pd, VALIDATION_FAILED, errors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ProblemDetail handleConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest req) {

    List<ErrorItem> errors = ex.getConstraintViolations().stream().map(this::toErrorItem).toList();

    ProblemDetail pd = buildValidationProblem(req);
    attachErrors(pd, VALIDATION_FAILED, errors);
    return pd;
  }

  @ExceptionHandler(BindException.class)
  public ProblemDetail handleBindException(BindException ex, HttpServletRequest req) {
    List<ErrorItem> errors = ex.getFieldErrors().stream().map(this::toErrorItem).toList();

    ProblemDetail pd = buildValidationProblem(req);
    attachErrors(pd, VALIDATION_FAILED, errors);
    return pd;
  }

  @Override
  @Nullable
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      @NonNull WebRequest request) {

    HttpServletRequest req = ((ServletWebRequest) request).getRequest();
    Throwable cause = ex.getCause();

    if (cause instanceof InvalidFormatException invalidFormatException) {
      return handleInvalidFormat(invalidFormatException, req);
    }

    if (cause instanceof UnrecognizedPropertyException unrecognizedPropertyException) {
      return handleUnrecognized(unrecognizedPropertyException, req);
    }

    String raw = Optional.ofNullable(cause).map(Throwable::getMessage).orElseGet(ex::getMessage);
    log.warn("Bad request (not readable): {}", raw);

    ProblemDetail pd = buildBadRequestBodyProblem(req);
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

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
  }

  @Override
  @Nullable
  protected ResponseEntity<Object> handleNoResourceFoundException(
      NoResourceFoundException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      @NonNull WebRequest request) {

    HttpServletRequest req = ((ServletWebRequest) request).getRequest();

    log.warn("Endpoint not found: {}", ex.getResourcePath());

    ProblemDetail pd =
        baseProblem(
            type(TYPE_NOT_FOUND),
            HttpStatus.NOT_FOUND,
            messageResolver.getMessage(KEY_PROBLEM_TITLE_NOT_FOUND),
            messageResolver.getMessage(KEY_PROBLEM_DETAIL_NOT_FOUND),
            req);

    attachErrors(
        pd,
        NOT_FOUND,
        List.of(
            error(
                NOT_FOUND, messageResolver.getMessage(KEY_ENDPOINT_NOT_FOUND), null, null, null)));

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
  }

  @Override
  @Nullable
  protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
      HttpRequestMethodNotSupportedException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      @NonNull WebRequest request) {

    HttpServletRequest req = ((ServletWebRequest) request).getRequest();
    String method = ex.getMethod();

    ProblemDetail pd =
        baseProblem(
            type(TYPE_METHOD_NOT_ALLOWED),
            HttpStatus.METHOD_NOT_ALLOWED,
            messageResolver.getMessage(KEY_PROBLEM_TITLE_METHOD_NOT_ALLOWED),
            messageResolver.getMessage(KEY_PROBLEM_DETAIL_METHOD_NOT_ALLOWED),
            req);

    attachErrors(
        pd,
        ERROR_CODE_METHOD_NOT_ALLOWED,
        List.of(
            error(
                ERROR_CODE_METHOD_NOT_ALLOWED,
                messageResolver.getMessage(KEY_METHOD_NOT_SUPPORTED, method),
                null,
                null,
                null)));

    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(pd);
  }

  @Override
  @Nullable
  protected ResponseEntity<Object> handleMissingServletRequestParameter(
      MissingServletRequestParameterException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      @NonNull WebRequest request) {

    HttpServletRequest req = ((ServletWebRequest) request).getRequest();
    String param = ex.getParameterName();

    ProblemDetail pd = buildBadRequestParamProblem(req);

    attachErrors(
        pd,
        BAD_REQUEST,
        List.of(
            error(
                BAD_REQUEST,
                messageResolver.getMessage(KEY_PARAM_REQUIRED_MISSING, param),
                param,
                null,
                null)));

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
  }

  @Override
  @Nullable
  protected ResponseEntity<Object> handleServletRequestBindingException(
      @NonNull ServletRequestBindingException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      @NonNull WebRequest request) {

    HttpServletRequest req = ((ServletWebRequest) request).getRequest();
    ProblemDetail pd = buildBadRequestParamProblem(req);

    if (ex instanceof MissingRequestHeaderException missingHeaderEx) {
      String header = missingHeaderEx.getHeaderName();

      attachErrors(
          pd,
          BAD_REQUEST,
          List.of(
              error(
                  BAD_REQUEST,
                  messageResolver.getMessage(KEY_HEADER_REQUIRED_MISSING, header),
                  header,
                  null,
                  null)));

      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    attachErrors(
        pd,
        BAD_REQUEST,
        List.of(
            error(
                BAD_REQUEST,
                messageResolver.getMessage(KEY_PROBLEM_DETAIL_PARAM_INVALID),
                null,
                null,
                null)));

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
  }

  @Override
  @Nullable
  protected ResponseEntity<Object> handleTypeMismatch(
      TypeMismatchException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      @NonNull WebRequest request) {

    HttpServletRequest req = ((ServletWebRequest) request).getRequest();

    String paramName =
        ex instanceof MethodArgumentTypeMismatchException matme ? matme.getName() : null;

    String expected =
        Optional.ofNullable(ex.getRequiredType())
            .map(Class::getSimpleName)
            .orElse(FALLBACK_UNKNOWN);

    ProblemDetail pd = buildBadRequestParamProblem(req);

    attachErrors(
        pd,
        BAD_REQUEST,
        List.of(
            error(
                BAD_REQUEST,
                messageResolver.getMessage(KEY_PARAM_TYPE_MISMATCH, expected),
                paramName,
                null,
                null)));

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
  }

  private ResponseEntity<Object> handleInvalidFormat(
      InvalidFormatException ex, HttpServletRequest req) {
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

    ProblemDetail pd = buildBadRequestBodyProblem(req);

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

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
  }

  private ResponseEntity<Object> handleUnrecognized(
      UnrecognizedPropertyException ex, HttpServletRequest req) {
    String field = ex.getPropertyName();
    log.warn("Unrecognized field: '{}' (known: {})", field, ex.getKnownPropertyIds());

    ProblemDetail pd = buildBadRequestBodyProblem(req);

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

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
  }

  private ProblemDetail buildValidationProblem(HttpServletRequest req) {
    return baseProblem(
        type(TYPE_VALIDATION_FAILED),
        HttpStatus.BAD_REQUEST,
        messageResolver.getMessage(KEY_PROBLEM_TITLE_VALIDATION_FAILED),
        messageResolver.getMessage(KEY_PROBLEM_DETAIL_VALIDATION_FAILED),
        req);
  }

  private ProblemDetail buildBadRequestParamProblem(HttpServletRequest req) {
    return baseProblem(
        type(TYPE_BAD_REQUEST),
        HttpStatus.BAD_REQUEST,
        messageResolver.getMessage(KEY_PROBLEM_TITLE_BAD_REQUEST),
        messageResolver.getMessage(KEY_PROBLEM_DETAIL_PARAM_INVALID),
        req);
  }

  private ProblemDetail buildBadRequestBodyProblem(HttpServletRequest req) {
    return baseProblem(
        type(TYPE_BAD_REQUEST),
        HttpStatus.BAD_REQUEST,
        messageResolver.getMessage(KEY_PROBLEM_TITLE_BAD_REQUEST),
        messageResolver.getMessage(KEY_PROBLEM_DETAIL_BAD_REQUEST),
        req);
  }

  private ErrorItem toErrorItem(FieldError fe) {
    String field = fe.getField();
    String message = resolveMessageOrKey(fe.getDefaultMessage(), FALLBACK_INVALID);
    return error(VALIDATION_FAILED, message, field, null, null);
  }

  private ErrorItem toErrorItem(ConstraintViolation<?> v) {
    String field = v.getPropertyPath() == null ? "" : v.getPropertyPath().toString();

    String resolvedTemplate = resolveMessageOrKey(v.getMessageTemplate(), null);
    String message =
        looksLikeMessageKeyTemplate(v.getMessageTemplate())
            ? resolvedTemplate
            : resolveMessageOrKey(v.getMessage(), FALLBACK_INVALID);

    return error(VALIDATION_FAILED, message, field, null, null);
  }

  private String resolveMessageOrKey(String keyOrText, String fallback) {
    if (keyOrText == null) {
      return fallback;
    }

    String s = keyOrText.trim();
    if (s.isEmpty()) {
      return fallback;
    }

    if (looksLikeMessageKeyTemplate(s)) {
      String key = extractKey(s);
      return messageResolver.getMessage(key);
    }

    return s;
  }

  private boolean looksLikeMessageKeyTemplate(String s) {
    return s != null && s.length() > 2 && s.startsWith("{") && s.endsWith("}");
  }

  private String extractKey(String template) {
    return template.substring(1, template.length() - 1).trim();
  }

  private String expectedTypeName(InvalidFormatException ex) {
    return Optional.ofNullable(ex.getTargetType())
        .map(Class::getSimpleName)
        .orElse(FALLBACK_UNKNOWN);
  }

  private String safeValue(Object v) {
    if (v == null) {
      return "null";
    }

    String s = v.toString();
    return s.isBlank() ? "null" : s;
  }
}

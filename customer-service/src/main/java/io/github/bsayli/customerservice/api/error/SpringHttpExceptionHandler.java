package io.github.bsayli.customerservice.api.error;

import static io.github.bsayli.customerservice.api.error.ProblemSupport.*;
import static io.github.bsayli.customerservice.common.api.ApiConstants.ErrorCode.BAD_REQUEST;
import static io.github.bsayli.customerservice.common.api.ApiConstants.ErrorCode.NOT_FOUND;

import io.github.bsayli.customerservice.common.i18n.LocalizedMessageResolver;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice(basePackages = "io.github.bsayli.customerservice.api.controller")
@Order(3)
public class SpringHttpExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(SpringHttpExceptionHandler.class);

  private static final String KEY_PROBLEM_TITLE_NOT_FOUND = "problem.title.not_found";
  private static final String KEY_PROBLEM_DETAIL_NOT_FOUND = "problem.detail.not_found";

  private static final String KEY_PROBLEM_TITLE_BAD_REQUEST = "problem.title.bad_request";
  private static final String KEY_PROBLEM_DETAIL_PARAM_INVALID = "request.param.invalid";

  private static final String KEY_PROBLEM_TITLE_METHOD_NOT_ALLOWED =
      "problem.title.method_not_allowed";
  private static final String KEY_PROBLEM_DETAIL_METHOD_NOT_ALLOWED =
      "problem.detail.method_not_allowed";

  private static final String KEY_ENDPOINT_NOT_FOUND = "request.endpoint.not_found";
  private static final String KEY_METHOD_NOT_SUPPORTED = "request.method.not_supported";
  private static final String KEY_PARAM_REQUIRED_MISSING = "request.param.required_missing";
  private static final String KEY_HEADER_REQUIRED_MISSING = "request.header.missing";
  private static final String KEY_PARAM_TYPE_MISMATCH = "request.param.type_mismatch";

  private static final String ERROR_CODE_METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";
  private static final String FALLBACK_UNKNOWN = "unknown";

  private final LocalizedMessageResolver messageResolver;

  public SpringHttpExceptionHandler(LocalizedMessageResolver messageResolver) {
    this.messageResolver = messageResolver;
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ProblemDetail handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest req) {
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

    return pd;
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ProblemDetail handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
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

    return pd;
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ProblemDetail handleMissingParam(
      MissingServletRequestParameterException ex, HttpServletRequest req) {
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

    return pd;
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ProblemDetail handleMissingHeader(
      MissingRequestHeaderException ex, HttpServletRequest req) {
    String header = ex.getHeaderName();

    ProblemDetail pd = buildBadRequestParamProblem(req);

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

    return pd;
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ProblemDetail handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
    String paramName = ex.getName();
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

    return pd;
  }

  private ProblemDetail buildBadRequestParamProblem(HttpServletRequest req) {
    return baseProblem(
        type(TYPE_BAD_REQUEST),
        HttpStatus.BAD_REQUEST,
        messageResolver.getMessage(KEY_PROBLEM_TITLE_BAD_REQUEST),
        messageResolver.getMessage(KEY_PROBLEM_DETAIL_PARAM_INVALID),
        req);
  }
}

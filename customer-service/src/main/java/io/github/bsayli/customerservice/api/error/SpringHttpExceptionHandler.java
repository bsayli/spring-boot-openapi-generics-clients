package io.github.bsayli.customerservice.api.error;

import static io.github.bsayli.customerservice.api.error.ProblemSupport.*;
import static io.github.bsayli.customerservice.common.api.ApiConstants.ErrorCode.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import org.slf4j.*;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.web.*;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice(basePackages = "io.github.bsayli.customerservice.api.controller")
@Order(3)
public class SpringHttpExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(SpringHttpExceptionHandler.class);

  @ExceptionHandler(NoResourceFoundException.class)
  public ProblemDetail handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest req) {
    log.warn("Endpoint not found: {}", ex.getResourcePath());

    ProblemDetail pd =
        baseProblem(
            type(TYPE_NOT_FOUND), HttpStatus.NOT_FOUND, TITLE_NOT_FOUND, DETAIL_NOT_FOUND, req);

    attachErrors(pd, NOT_FOUND, List.of(error(NOT_FOUND, "Endpoint not found.", null, null, null)));
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
            TITLE_METHOD_NOT_ALLOWED,
            "The request method is not supported for this resource.",
            req);

    attachErrors(
        pd,
        "METHOD_NOT_ALLOWED",
        List.of(
            error("METHOD_NOT_ALLOWED", "HTTP method not supported: " + method, null, null, null)));
    return pd;
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ProblemDetail handleMissingParam(
      MissingServletRequestParameterException ex, HttpServletRequest req) {
    String param = ex.getParameterName();

    ProblemDetail pd =
        baseProblem(
            type(TYPE_BAD_REQUEST),
            HttpStatus.BAD_REQUEST,
            TITLE_BAD_REQUEST,
            DETAIL_PARAM_INVALID,
            req);

    attachErrors(
        pd,
        BAD_REQUEST,
        List.of(error(BAD_REQUEST, "Missing required parameter: " + param, param, null, null)));
    return pd;
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ProblemDetail handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
    String expected =
        Optional.ofNullable(ex.getRequiredType()).map(Class::getSimpleName).orElse("unknown");

    ProblemDetail pd =
        baseProblem(
            type(TYPE_BAD_REQUEST),
            HttpStatus.BAD_REQUEST,
            TITLE_BAD_REQUEST,
            DETAIL_PARAM_INVALID,
            req);

    attachErrors(
        pd,
        BAD_REQUEST,
        List.of(
            error(
                BAD_REQUEST,
                "Invalid value (expected " + expected + ").",
                ex.getName(),
                null,
                null)));
    return pd;
  }
}

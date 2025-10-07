package io.github.bsayli.customerservice.api.error;

import static io.github.bsayli.customerservice.api.error.ProblemSupport.*;
import static io.github.bsayli.customerservice.common.api.ApiConstants.ErrorCode.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.slf4j.*;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(basePackages = "io.github.bsayli.customerservice.api.controller")
@Order(4)
public class ApplicationExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ApplicationExceptionHandler.class);

  @ExceptionHandler(NoSuchElementException.class)
  public ProblemDetail handleNotFound(NoSuchElementException ex, HttpServletRequest req) {
    ProblemDetail pd =
        baseProblem(
            type(TYPE_NOT_FOUND), HttpStatus.NOT_FOUND, TITLE_NOT_FOUND, DETAIL_NOT_FOUND, req);

    String msg = Optional.ofNullable(ex.getMessage()).orElse("Resource not found.");
    attachErrors(pd, NOT_FOUND, List.of(error(NOT_FOUND, msg, null, "Customer", null)));
    return pd;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGeneric(Exception ex, HttpServletRequest req) {
    log.error("Unhandled exception", ex);

    ProblemDetail pd =
        baseProblem(
            type(TYPE_INTERNAL_ERROR),
            HttpStatus.INTERNAL_SERVER_ERROR,
            TITLE_INTERNAL_ERROR,
            DETAIL_GENERIC_ERROR,
            req);

    attachErrors(
        pd, INTERNAL_ERROR, List.of(error(INTERNAL_ERROR, "Internal error.", null, null, null)));
    return pd;
  }
}

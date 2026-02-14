package io.github.bsayli.customerservice.api.error;

import static io.github.bsayli.customerservice.api.error.ProblemSupport.*;
import static io.github.bsayli.customerservice.common.api.ApiConstants.ErrorCode.INTERNAL_ERROR;
import static io.github.bsayli.customerservice.common.api.ApiConstants.ErrorCode.NOT_FOUND;

import io.github.bsayli.customerservice.common.i18n.LocalizedMessageResolver;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "io.github.bsayli.customerservice.api.controller")
@Order(4)
public class ApplicationExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ApplicationExceptionHandler.class);

  private static final String KEY_PROBLEM_TITLE_NOT_FOUND = "problem.title.not_found";
  private static final String KEY_PROBLEM_DETAIL_NOT_FOUND = "problem.detail.not_found";

  private static final String KEY_PROBLEM_TITLE_INTERNAL_ERROR = "problem.title.internal_error";
  private static final String KEY_PROBLEM_DETAIL_INTERNAL_ERROR = "problem.detail.internal_error";

  private static final String KEY_SERVER_INTERNAL_ERROR = "server.internal.error";
  private static final String KEY_ENDPOINT_RESOURCE_NOT_FOUND = "request.resource.not_found";

  private static final String FALLBACK_RESOURCE_NOT_FOUND = "Resource not found.";

  private final LocalizedMessageResolver messageResolver;

  public ApplicationExceptionHandler(LocalizedMessageResolver messageResolver) {
    this.messageResolver = messageResolver;
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ProblemDetail handleNotFound(NoSuchElementException ex, HttpServletRequest req) {
    ProblemDetail pd =
        baseProblem(
            type(TYPE_NOT_FOUND),
            HttpStatus.NOT_FOUND,
            messageResolver.getMessage(KEY_PROBLEM_TITLE_NOT_FOUND),
            messageResolver.getMessage(KEY_PROBLEM_DETAIL_NOT_FOUND),
            req);

    String msg =
        Optional.ofNullable(ex.getMessage())
            .filter(s -> !s.isBlank())
            .orElseGet(() -> messageResolver.getMessage(KEY_ENDPOINT_RESOURCE_NOT_FOUND));

    if (msg == null || msg.isBlank()) {
      msg = FALLBACK_RESOURCE_NOT_FOUND;
    }

    attachErrors(pd, NOT_FOUND, List.of(error(NOT_FOUND, msg, null, "Customer", null)));
    return pd;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGeneric(Exception ex, HttpServletRequest req) {
    log.error("Unhandled exception", ex);

    String detail = messageResolver.getMessage(KEY_SERVER_INTERNAL_ERROR);
    if (detail == null || detail.isBlank()) {
      detail = messageResolver.getMessage(KEY_PROBLEM_DETAIL_INTERNAL_ERROR);
    }

    ProblemDetail pd =
        baseProblem(
            type(TYPE_INTERNAL_ERROR),
            HttpStatus.INTERNAL_SERVER_ERROR,
            messageResolver.getMessage(KEY_PROBLEM_TITLE_INTERNAL_ERROR),
            detail,
            req);

    attachErrors(pd, INTERNAL_ERROR, List.of(error(INTERNAL_ERROR, detail, null, null, null)));
    return pd;
  }
}

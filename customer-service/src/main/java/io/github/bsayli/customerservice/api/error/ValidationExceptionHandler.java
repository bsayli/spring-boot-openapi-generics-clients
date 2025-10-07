package io.github.bsayli.customerservice.api.error;

import static io.github.bsayli.customerservice.api.error.ProblemSupport.*;
import static io.github.bsayli.customerservice.common.api.ApiConstants.ErrorCode.*;

import io.github.bsayli.customerservice.common.api.response.error.ErrorItem;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(basePackages = "io.github.bsayli.customerservice.api.controller")
@Order(1)
public class ValidationExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleMethodArgInvalid(
      MethodArgumentNotValidException ex, HttpServletRequest req) {
    List<ErrorItem> errors =
        ex.getBindingResult().getFieldErrors().stream().map(this::toErrorItem).toList();

    ProblemDetail pd =
        baseProblem(
            type(TYPE_VALIDATION_FAILED),
            HttpStatus.BAD_REQUEST,
            TITLE_VALIDATION_FAILED,
            DETAIL_VALIDATION_FAILED,
            req);

    attachErrors(pd, VALIDATION_FAILED, errors);
    return pd;
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ProblemDetail handleConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest req) {
    List<ErrorItem> errors = ex.getConstraintViolations().stream().map(this::toErrorItem).toList();

    ProblemDetail pd =
        baseProblem(
            type(TYPE_VALIDATION_FAILED),
            HttpStatus.BAD_REQUEST,
            TITLE_VALIDATION_FAILED,
            DETAIL_VALIDATION_FAILED,
            req);

    attachErrors(pd, VALIDATION_FAILED, errors);
    return pd;
  }

  @ExceptionHandler(BindException.class)
  public ProblemDetail handleBindException(BindException ex, HttpServletRequest req) {
    List<ErrorItem> errors = ex.getFieldErrors().stream().map(this::toErrorItem).toList();

    ProblemDetail pd =
        baseProblem(
            type(TYPE_VALIDATION_FAILED),
            HttpStatus.BAD_REQUEST,
            TITLE_VALIDATION_FAILED,
            DETAIL_VALIDATION_FAILED,
            req);

    attachErrors(pd, VALIDATION_FAILED, errors);
    return pd;
  }

  private ErrorItem toErrorItem(FieldError fe) {
    String field = Optional.of(fe.getField()).orElse("");
    String message = Optional.ofNullable(fe.getDefaultMessage()).orElse("invalid");
    return error(VALIDATION_FAILED, message, field, null, null);
  }

  private ErrorItem toErrorItem(ConstraintViolation<?> v) {
    String field = v.getPropertyPath() == null ? "" : v.getPropertyPath().toString();
    String message = Optional.ofNullable(v.getMessage()).orElse("invalid");
    return error(VALIDATION_FAILED, message, field, null, null);
  }
}

package com.example.demo.customer.api.error;

import static com.example.demo.common.api.ServiceErrorCodes.BAD_REQUEST;
import static com.example.demo.common.api.ServiceErrorCodes.INTERNAL_ERROR;
import static com.example.demo.common.api.ServiceErrorCodes.NOT_FOUND;
import static com.example.demo.common.api.ServiceErrorCodes.VALIDATION_FAILED;

import com.example.demo.common.api.response.ServiceResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.example.demo.customer.api.controller")
public class CustomerControllerAdvice {

  private static final Logger log = LoggerFactory.getLogger(CustomerControllerAdvice.class);

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ServiceResponse<ErrorPayload>> handleNotFound(NoSuchElementException ex) {
    var payload = new ErrorPayload(NOT_FOUND, ex.getMessage(), Instant.now(), List.of());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ServiceResponse.of(HttpStatus.NOT_FOUND, NOT_FOUND, payload));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ServiceResponse<ErrorPayload>> handleMethodArgInvalid(
      MethodArgumentNotValidException ex) {
    List<Violation> violations =
        ex.getBindingResult().getFieldErrors().stream().map(this::toViolation).toList();
    var payload = new ErrorPayload(VALIDATION_FAILED, BAD_REQUEST, Instant.now(), violations);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ServiceResponse.of(HttpStatus.BAD_REQUEST, BAD_REQUEST, payload));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ServiceResponse<ErrorPayload>> handleConstraintViolation(
      ConstraintViolationException ex) {
    List<Violation> violations =
        ex.getConstraintViolations().stream().map(this::toViolation).toList();
    var payload = new ErrorPayload(VALIDATION_FAILED, BAD_REQUEST, Instant.now(), violations);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ServiceResponse.of(HttpStatus.BAD_REQUEST, BAD_REQUEST, payload));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ServiceResponse<ErrorPayload>> handleGeneric(Exception ex) {
    log.error("Unhandled exception", ex);
    var payload = new ErrorPayload(INTERNAL_ERROR, ex.getMessage(), Instant.now(), List.of());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ServiceResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR, payload));
  }

  private Violation toViolation(FieldError fe) {
    return new Violation(fe.getField(), fe.getDefaultMessage());
  }

  private Violation toViolation(ConstraintViolation<?> v) {
    String path = v.getPropertyPath() == null ? null : v.getPropertyPath().toString();
    return new Violation(path, v.getMessage());
  }

  public record Violation(String field, String message) {}

  public record ErrorPayload(
      String code, String message, Instant timestamp, List<Violation> violations) {}
}

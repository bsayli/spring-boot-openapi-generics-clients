package io.github.bsayli.customerservice.api.error;

import static io.github.bsayli.customerservice.common.api.ApiConstants.ErrorCode.*;

import io.github.bsayli.customerservice.common.api.response.ServiceResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ServiceResponse<ErrorPayload>> handleNotReadable(
      HttpMessageNotReadableException ex) {
    Throwable cause = ex.getCause();
    String causeMsg =
        (cause != null && cause.getMessage() != null) ? cause.getMessage() : ex.getMessage();
    log.warn("Bad request (not readable): {}", causeMsg);
    var payload = new ErrorPayload(VALIDATION_FAILED, BAD_REQUEST, Instant.now(), List.of());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ServiceResponse.of(HttpStatus.BAD_REQUEST, BAD_REQUEST, payload));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ServiceResponse<ErrorPayload>> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex) {
    String requiredType =
        java.util.Optional.ofNullable(ex.getRequiredType())
            .map(Class::getSimpleName)
            .orElse("unknown");
    log.warn(
        "Bad request (type mismatch): param={}, value={}, requiredType={}",
        ex.getName(),
        ex.getValue(),
        requiredType);
    var v = new Violation(ex.getName(), "Type mismatch (expected " + requiredType + ")");
    var payload = new ErrorPayload(VALIDATION_FAILED, BAD_REQUEST, Instant.now(), List.of(v));
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

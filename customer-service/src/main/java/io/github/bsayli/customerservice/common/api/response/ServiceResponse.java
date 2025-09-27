package io.github.bsayli.customerservice.common.api.response;

import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;

public record ServiceResponse<T>(int status, String message, T data, List<ErrorDetail> errors) {
  public static <T> ServiceResponse<T> ok(T data) {
    return new ServiceResponse<>(HttpStatus.OK.value(), "OK", data, Collections.emptyList());
  }

  public static <T> ServiceResponse<T> of(HttpStatus status, String message, T data) {
    return new ServiceResponse<>(status.value(), message, data, Collections.emptyList());
  }

  public static <T> ServiceResponse<T> error(HttpStatus status, String message) {
    return new ServiceResponse<>(status.value(), message, null, Collections.emptyList());
  }

  public static <T> ServiceResponse<T> error(
      HttpStatus status, String message, List<ErrorDetail> errors) {
    return new ServiceResponse<>(
        status.value(), message, null, errors != null ? errors : Collections.emptyList());
  }
}

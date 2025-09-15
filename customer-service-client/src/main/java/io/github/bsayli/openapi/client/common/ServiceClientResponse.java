package io.github.bsayli.openapi.client.common;

import java.util.List;
import java.util.Objects;

public class ServiceClientResponse<T> {

  private Integer status;
  private String message;
  private List<ClientErrorDetail> errors;
  private T data;

  public ServiceClientResponse() {}

  public ServiceClientResponse(
      Integer status, String message, List<ClientErrorDetail> errors, T data) {
    this.status = status;
    this.message = message;
    this.errors = errors;
    this.data = data;
  }

  public static <T> ServiceClientResponse<T> from(
      Integer status, String message, List<ClientErrorDetail> errors, T data) {
    return new ServiceClientResponse<>(status, message, errors, data);
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public List<ClientErrorDetail> getErrors() {
    return errors;
  }

  public void setErrors(List<ClientErrorDetail> errors) {
    this.errors = errors;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ServiceClientResponse<?> that)) return false;
    return Objects.equals(status, that.status)
        && Objects.equals(message, that.message)
        && Objects.equals(errors, that.errors)
        && Objects.equals(data, that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, message, errors, data);
  }

  @Override
  public String toString() {
    return "ServiceClientResponse{"
        + "status="
        + status
        + ", message='"
        + message
        + '\''
        + ", errors="
        + errors
        + ", data="
        + data
        + '}';
  }
}

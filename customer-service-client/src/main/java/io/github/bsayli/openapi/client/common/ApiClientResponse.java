package io.github.bsayli.openapi.client.common;

import java.util.List;
import java.util.Objects;

public class ApiClientResponse<T> {

  private Integer status;
  private String statusText;
  private String message;
  private List<ApiClientError> errors;
  private T data;

  public ApiClientResponse() {}

  public ApiClientResponse(
      Integer status, String statusText, String message, List<ApiClientError> errors, T data) {
    this.status = status;
    this.statusText = statusText;
    this.message = message;
    this.errors = errors;
    this.data = data;
  }

  public static <T> ApiClientResponse<T> from(
      Integer status, String statusText, String message, List<ApiClientError> errors, T data) {
    return new ApiClientResponse<>(status, statusText, message, errors, data);
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public String getStatusText() {
    return statusText;
  }

  public void setStatusText(String statusText) {
    this.statusText = statusText;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public List<ApiClientError> getErrors() {
    return errors;
  }

  public void setErrors(List<ApiClientError> errors) {
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
    if (!(o instanceof ApiClientResponse<?> that)) return false;
    return Objects.equals(status, that.status)
        && Objects.equals(statusText, that.statusText)
        && Objects.equals(message, that.message)
        && Objects.equals(errors, that.errors)
        && Objects.equals(data, that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, statusText, message, errors, data);
  }

  @Override
  public String toString() {
    return "ApiClientResponse{"
        + "status="
        + status
        + ", statusText='"
        + statusText
        + '\''
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
